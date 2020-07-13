package com.etherblood.a.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.Bot;
import com.etherblood.a.ai.bots.RandomMover;
import com.etherblood.a.ai.bots.mcts.MctsBot;
import com.etherblood.a.ai.bots.mcts.MctsBotSettings;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.templates.api.setup.RawGameSetup;
import com.etherblood.a.templates.api.setup.RawPlayerSetup;
import com.etherblood.a.network.api.jwt.JwtAuthentication;
import com.etherblood.a.network.api.jwt.JwtParser;
import com.etherblood.a.network.api.matchmaking.GameRequest;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.GameDataPrinter;
import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.game.events.api.NoopGameEventListener;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.moves.Surrender;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.templates.api.setup.RawLibraryTemplate;
import com.google.gson.JsonElement;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameService {

    private static final Logger LOG = LoggerFactory.getLogger(GameService.class);

    private final Random random = new SecureRandom();
    private final Server server;
    private final JwtParser jwtParser;
    private final Function<String, JsonElement> assetLoader;
    private final long botId;
    private final RawLibraryTemplate botLibrary;

    private final Map<UUID, GameReplayService> games = new HashMap<>();
    private final List<GamePlayerMapping> players = new ArrayList<>();
    private final List<GameBotMapping> bots = new ArrayList<>();

    private final Map<UUID, Future<Move>> botMoves = new HashMap<>();
    private final ExecutorService executor;

    private final Map<Integer, GameRequest> connectionGameRequests = new LinkedHashMap<>();

    public GameService(Server server, JwtParser jwtParser, Function<String, JsonElement> assetLoader, long botId, RawLibraryTemplate botLibrary, ExecutorService executor) {
        this.server = server;
        this.jwtParser = jwtParser;
        this.assetLoader = assetLoader;
        this.botId = botId;
        this.botLibrary = botLibrary;
        this.executor = executor;
    }

    public synchronized void onDisconnect(Connection connection) {
        GameRequest gameRequest = connectionGameRequests.remove(connection.getID());
        if (gameRequest != null) {
            LOG.info("Removed game request of connection {} due to disconnect.", connection.getID());
        }
        for (GamePlayerMapping player : players) {
            if (player.connectionId == connection.getID()) {
                LOG.info("Game_{} surrendering for player {} due to disconnect.", player.gameId, player.playerId);
                GameReplayService game = games.get(player.gameId);
                makeMove(player.gameId, new Surrender(game.getPlayerEntity(player.playerIndex)));
                assert game.isGameOver();
                assert !games.containsKey(player.gameId);
                return;
            }
        }
    }

    public synchronized void onGameRequest(Connection connection, GameRequest request) {
        connectionGameRequests.put(connection.getID(), request);
        matchmake();
    }

    public synchronized void onMoveRequest(Connection connection, Move move) {
        if (move instanceof Update) {
            return;
        }
        GamePlayerMapping mapping = getPlayerByConnectionId(connection.getID());
        UUID gameId = mapping.gameId;
        makeMove(gameId, move);
    }

    private void makeMove(UUID gameId, Move move) {
        GameReplayService game = games.get(gameId);
        Game gameInstance = game.createInstance();
        GameDataPrinter printer = new GameDataPrinter(gameInstance);
        LOG.info("Game_{} make move '{}'.", gameId, printer.toMoveString(move));
        MoveReplay moveReplay = game.apply(move);
        for (GamePlayerMapping player : getPlayersByGameId(gameId)) {
            Connection other = getConnection(player.connectionId);
            if (other != null) {
                other.sendTCP(moveReplay);
            }
        }
        Future<Move> botMove = botMoves.remove(gameId);
        if (botMove != null) {
            botMove.cancel(true);
        }
        if (game.isGameOver()) {
            LOG.info("Game_{} ended.", gameId);
            games.remove(gameId);
            for (GamePlayerMapping player : getPlayersByGameId(gameId)) {
                int playerEntity = game.getPlayerEntity(player.playerIndex);
                LOG.info("{} {}.", game.getPlayerName(player.playerIndex), game.hasPlayerWon(playerEntity) ? "won" : (game.hasPlayerLost(playerEntity) ? "lost" : " has no result"));
                players.remove(player);
            }
            for (GameBotMapping bot : getBotsByGameId(gameId)) {
                int playerEntity = game.getPlayerEntity(bot.playerIndex);
                LOG.info("{} {}.", game.getPlayerName(bot.playerIndex), game.hasPlayerWon(playerEntity) ? "won" : (game.hasPlayerLost(playerEntity) ? "lost" : " has no result"));
                bots.remove(bot);
            }
        }
    }

    public synchronized void botMoves() throws Exception {
        Iterator<Map.Entry<UUID, Future<Move>>> iterator = botMoves.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Future<Move>> entry = iterator.next();
            if (entry.getValue().isDone()) {
                iterator.remove();
                Move move = entry.getValue().get();
                UUID gameId = entry.getKey();
                GameDataPrinter printer = new GameDataPrinter(games.get(gameId).createInstance());
                LOG.debug("Game_{} computed move '{}'.", gameId, printer.toMoveString(move));
                makeMove(gameId, move);
            }
        }
        if (bots.isEmpty()) {
            return;
        }
        LOG.debug("Processing {} bots.", bots.size());
        for (GameBotMapping bot : bots) {
            if (botMoves.containsKey(bot.gameId)) {
                LOG.debug("Game_{} Still computing move.", bot.gameId);
                continue;
            }

            GameReplayService game = games.get(bot.gameId);
            MoveBotGame botGame = bot.game;
            game.updateInstance(botGame.getGame());
            if (botGame.isGameOver()) {
                throw new AssertionError();
            }
            if (botGame.isPlayerIndexActive(bot.playerIndex)) {
                LOG.debug("Game_{} start computing move.", bot.gameId);
                botMoves.put(bot.gameId, executor.submit(() -> bot.bot.findMove(bot.playerIndex)));
            } else {
                LOG.debug("Game_{} skip, it is not the bots turn.", bot.gameId);
            }
        }
    }

    private synchronized void matchmake() {
        List<Map.Entry<Integer, GameRequest>> humanRequests = new ArrayList<>();
        Iterator<Map.Entry<Integer, GameRequest>> iterator = connectionGameRequests.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, GameRequest> entry = iterator.next();
            GameRequest request = entry.getValue();
            switch (request.opponent) {
                case BOT:
                    Connection connection = Objects.requireNonNull(getConnection(entry.getKey()));
                    RawPlayerSetup human = new RawPlayerSetup();
                    JwtAuthentication jwt = jwtParser.verify(request.jwt);
                    human.id = jwt.user.id;
                    human.name = jwt.user.login;
                    human.library = request.library;

                    RawPlayerSetup bot = new RawPlayerSetup();
                    bot.id = botId;
                    bot.name = "Bot";
                    bot.library = botLibrary;

                    applyPlayerEasterEggs(human, bot);

                    RawGameSetup setup = new RawGameSetup();
                    setup.teamCount = 2;
                    setup.players = shuffle(new RawPlayerSetup[]{human, bot});
                    for (int teamIndex = 0; teamIndex < setup.players.length; teamIndex++) {
                        setup.players[teamIndex].teamIndex = teamIndex;
                    }
                    setup.theCoinAlias = "the_coin";
                    UUID gameId = UUID.randomUUID();
                    GameReplayService game = new GameReplayService(setup, assetLoader);
                    MoveReplay moveReplay = game.apply(new Start());

                    players.add(new GamePlayerMapping(gameId, human.id, Arrays.asList(setup.players).indexOf(human), connection.getID()));

                    Game gameInstance = game.createInstance();
                    MoveBotGame moveBotGame = new MoveBotGame(gameInstance);
                    Bot botInstance;
                    if (request.strength <= 0) {
                        botInstance = new RandomMover(moveBotGame, new Random());
                    } else {
                        MctsBotSettings<Move, MoveBotGame> settings = new MctsBotSettings<>();
                        settings.maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
                        settings.strength = request.strength;
                        botInstance = new MctsBot(moveBotGame, () -> new MoveBotGame(simulationGame(gameInstance)), settings);
                    }
                    bots.add(new GameBotMapping(gameId, Arrays.asList(setup.players).indexOf(bot), moveBotGame, botInstance));

                    games.put(gameId, game);
                    connection.sendTCP(setup);
                    connection.sendTCP(moveReplay);

                    iterator.remove();
                    break;

                case HUMAN:
                    humanRequests.add(entry);
                    break;
            }
        }

        for (int i = 0; i + 2 <= humanRequests.size(); i += 2) {
            Map.Entry<Integer, GameRequest> entry0 = humanRequests.get(i);
            Connection connection0 = Objects.requireNonNull(getConnection(entry0.getKey()));
            Map.Entry<Integer, GameRequest> entry1 = humanRequests.get(i + 1);
            Connection connection1 = Objects.requireNonNull(getConnection(entry1.getKey()));

            GameRequest request0 = entry0.getValue();
            RawPlayerSetup player0 = new RawPlayerSetup();
            JwtAuthentication jwt0 = jwtParser.verify(request0.jwt);
            player0.id = jwt0.user.id;
            player0.name = jwt0.user.login;
            player0.library = request0.library;

            GameRequest request1 = entry1.getValue();
            RawPlayerSetup player1 = new RawPlayerSetup();
            JwtAuthentication jwt1 = jwtParser.verify(request1.jwt);
            player1.id = jwt1.user.id;
            player1.name = jwt1.user.login;
            player1.library = request1.library;

            applyPlayerEasterEggs(player0, player1);
            applyPlayerEasterEggs(player1, player0);

            RawGameSetup setup = new RawGameSetup();
            setup.teamCount = 2;
            setup.players = shuffle(new RawPlayerSetup[]{player0, player1});
            for (int teamIndex = 0; teamIndex < setup.players.length; teamIndex++) {
                setup.players[teamIndex].teamIndex = teamIndex;
            }
            setup.theCoinAlias = "the_coin";
            GameReplayService game = new GameReplayService(setup, assetLoader);
            MoveReplay moveReplay = game.apply(new Start());
            UUID gameId = UUID.randomUUID();
            games.put(gameId, game);

            players.add(new GamePlayerMapping(gameId, player0.id, Arrays.asList(setup.players).indexOf(player0), connection0.getID()));
            players.add(new GamePlayerMapping(gameId, player1.id, Arrays.asList(setup.players).indexOf(player1), connection1.getID()));

            connection0.sendTCP(setup);
            connection1.sendTCP(setup);
            connection0.sendTCP(moveReplay);
            connection1.sendTCP(moveReplay);

            connectionGameRequests.remove(connection0.getID());
            connectionGameRequests.remove(connection1.getID());
        }
    }

    private void applyPlayerEasterEggs(RawPlayerSetup player, RawPlayerSetup opponent) {
        if (player.name.equalsIgnoreCase("yalee")) {
            replaceCard(player.library, "raigeki", "fabi_raigeki");
        }
        if (opponent.name.equalsIgnoreCase("destroflyer")) {
            replaceCard(player.library, "the_coin", "the_other_coin");
        }
    }

    private void replaceCard(RawLibraryTemplate library, String toRemove, String replacement) {
        if (library.cards.containsKey(toRemove)) {
            library.cards.put(replacement, library.cards.remove(toRemove));
        }
    }

    private <T> T[] shuffle(T[] array) {
        for (int i = 0; i < array.length; i++) {
            int j = random.nextInt(array.length - i);
            T tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
        return array;
    }

    private List<GamePlayerMapping> getPlayersByGameId(UUID gameId) {
        return players.stream().filter(x -> x.gameId.equals(gameId)).collect(Collectors.toList());
    }

    private List<GameBotMapping> getBotsByGameId(UUID gameId) {
        return bots.stream().filter(x -> x.gameId.equals(gameId)).collect(Collectors.toList());
    }

    private GamePlayerMapping getPlayerByConnectionId(int connectionId) {
        return players.stream().filter(x -> x.connectionId == connectionId).findAny().get();
    }

    private Connection getConnection(int connectionId) {
        return Arrays.stream(server.getConnections()).filter(x -> x.getID() == connectionId).findAny().orElse(null);
    }

    private Game simulationGame(Game game) {
        EntityData data = new SimpleEntityData(game.getSettings().components);
        MoveService moves = new MoveService(game.getSettings(), data, HistoryRandom.producer(), null, false, false, new NoopGameEventListener());
        return new Game(game.getSettings(), data, moves);
    }
}
