package com.etherblood.a.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.ai.bots.evaluation.RolloutToEvaluation;
import com.etherblood.a.ai.bots.evaluation.SimpleEvaluation;
import com.etherblood.a.ai.bots.mcts.MctsBot;
import com.etherblood.a.ai.bots.mcts.MctsBotSettings;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.game.GameSetup;
import com.etherblood.a.network.api.game.PlayerSetup;
import com.etherblood.a.network.api.jwt.Token;
import com.etherblood.a.network.api.jwt.JwtUtils;
import com.etherblood.a.network.api.matchmaking.GameRequest;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.moves.Surrender;
import com.etherblood.a.templates.RawLibraryTemplate;
import com.google.gson.JsonObject;
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

    private final Server server;
    private final Function<String, JsonObject> assetLoader;
    private final long botId;
    private final RawLibraryTemplate botLibrary;

    private final Map<UUID, GameReplayService> games = new HashMap<>();
    private final List<GamePlayerMapping> players = new ArrayList<>();
    private final List<GameBotMapping> bots = new ArrayList<>();

    private final Map<UUID, Future<Move>> botMoves = new HashMap<>();
    private final ExecutorService executor;

    private final Map<Integer, GameRequest> connectionGameRequests = new LinkedHashMap<>();

    public GameService(Server server, Function<String, JsonObject> assetLoader, long botId, RawLibraryTemplate botLibrary, ExecutorService executor) {
        this.server = server;
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
        for (int i = 0; i < players.size(); i++) {
            GamePlayerMapping player = players.get(i);
            if (player.connectionId == connection.getID()) {
                LOG.info("Surrendering game {} for player {} due to disconnect.", player.gameId, player.playerId);
                GameReplayService game = games.get(player.gameId);
                makeMove(player.gameId, new Surrender(game.getPlayerEntity(player.playerIndex)));
                return;
            }
        }
    }

    public synchronized void onGameRequest(Connection connection, GameRequest request) {
        connectionGameRequests.put(connection.getID(), request);
        matchmake();
    }

    public synchronized void onMoveRequest(Connection connection, Move move) {
        GamePlayerMapping mapping = getPlayerByConnectionId(connection.getID());
        UUID gameId = mapping.gameId;
        makeMove(gameId, move);
    }

    private void makeMove(UUID gameId, Move move) {
        LOG.info("Game {} make move {}.", gameId, move);
        GameReplayService gameReplayService = games.get(gameId);
        MoveReplay moveReplay = gameReplayService.apply(move);
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
        if (gameReplayService.isGameOver()) {
            LOG.info("Game {} ended.", gameId);
            games.remove(gameId);
            for (GamePlayerMapping player : getPlayersByGameId(gameId)) {
                players.remove(player);
            }
            for (GameBotMapping bot : getBotsByGameId(gameId)) {
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
                LOG.info("Apply computed move {} to game {}.", move, entry.getKey());
                makeMove(entry.getKey(), move);
            }
        }
        if (bots.isEmpty()) {
            return;
        }
        LOG.debug("Processing {} bots.", bots.size());
        for (GameBotMapping bot : bots) {
            if (botMoves.containsKey(bot.gameId)) {
                LOG.debug("Still computing move for game {}.", bot.gameId);
                continue;
            }

            GameReplayService game = games.get(bot.gameId);
            MoveBotGame botGame = bot.bot.getSourceGame();
            game.updateInstance(botGame.getGame());
            if (botGame.isGameOver()) {
                throw new AssertionError();
            }
            if (botGame.isPlayerIndexActive(bot.playerIndex)) {
                LOG.info("Start computing move for game {}.", bot.gameId);
                botMoves.put(bot.gameId, executor.submit(() -> bot.bot.findBestMove(bot.playerIndex)));
            } else {
                LOG.debug("Skip game {}, it is not the bots turn.", bot.gameId);
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
                    PlayerSetup human = new PlayerSetup();
                    Token jwt = JwtUtils.verify(request.jwt);
                    human.id = jwt.user.id;
                    human.name = jwt.user.login;
                    human.library = request.library;

                    PlayerSetup bot = new PlayerSetup();
                    bot.id = botId;
                    bot.name = "Bot";
                    bot.library = botLibrary;

                    GameSetup setup = new GameSetup();
                    setup.players = new PlayerSetup[]{human, bot};
                    UUID gameId = UUID.randomUUID();
                    GameReplayService game = new GameReplayService(setup, assetLoader);
                    MoveReplay moveReplay = game.apply(new Start());
                    games.put(gameId, game);

                    players.add(new GamePlayerMapping(gameId, human.id, 0, connection.getID()));

                    Function<MoveBotGame, float[]> simple = new SimpleEvaluation<Move, MoveBotGame>()::evaluate;
                    Function<MoveBotGame, float[]> rolloutEvaluation = new RolloutToEvaluation<>(new Random(), 10, simple)::evaluate;

                    MctsBotSettings<Move, MoveBotGame> settings = new MctsBotSettings<>();
                    settings.strength = request.strength;
                    settings.evaluation = rolloutEvaluation;
                    Game gameInstance = game.createInstance();
                    MctsBot<Move, MoveBotGame> mcts = new MctsBot<>(new MoveBotGame(gameInstance), new MoveBotGame(simulationGame(gameInstance)), settings);
                    bots.add(new GameBotMapping(gameId, 1, mcts));

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
            PlayerSetup player0 = new PlayerSetup();
            Token jwt0 = JwtUtils.verify(request0.jwt);
            player0.id = jwt0.user.id;
            player0.name = jwt0.user.login;
            player0.library = request0.library;

            GameRequest request1 = entry1.getValue();
            PlayerSetup player1 = new PlayerSetup();
            Token jwt1 = JwtUtils.verify(request1.jwt);
            player1.id = jwt1.user.id;
            player1.name = jwt1.user.login;
            player1.library = request1.library;

            GameSetup setup = new GameSetup();
            setup.players = new PlayerSetup[]{player0, player1};
            GameReplayService game = new GameReplayService(setup, assetLoader);
            MoveReplay moveReplay = game.apply(new Start());
            UUID gameId = UUID.randomUUID();
            games.put(gameId, game);

            players.add(new GamePlayerMapping(gameId, player0.id, 0, connection0.getID()));
            players.add(new GamePlayerMapping(gameId, player1.id, 1, connection1.getID()));

            connection0.sendTCP(setup);
            connection1.sendTCP(setup);
            connection0.sendTCP(moveReplay);
            connection1.sendTCP(moveReplay);

            connectionGameRequests.remove(connection0.getID());
            connectionGameRequests.remove(connection1.getID());
        }
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
        MoveService moves = new MoveService(game.getSettings(), data, HistoryRandom.producer(), null, false, false);
        return new Game(game.getSettings(), data, moves);
    }
}
