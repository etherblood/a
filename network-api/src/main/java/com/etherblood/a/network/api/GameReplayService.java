package com.etherblood.a.network.api;

import com.etherblood.a.rules.HistoryRandom;
import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.entities.Components;
import com.etherblood.a.entities.ComponentsBuilder;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.SimpleEntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.network.api.game.GameSetup;
import com.etherblood.a.network.api.game.PlayerSetup;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.GameSettings;
import com.etherblood.a.rules.GameSettingsBuilder;
import com.etherblood.a.rules.MoveService;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.setup.SimpleSetup;
import com.etherblood.a.templates.LibraryTemplate;
import com.etherblood.a.templates.TemplatesLoader;
import com.etherblood.a.templates.TemplatesParser;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;

public class GameReplayService {

    private final GameReplay replay;
    private final Function<String, JsonObject> assetLoader;

    private Game cachedGame = null;

    public GameReplayService(GameSetup setup, Function<String, JsonObject> assetLoader) {
        this.assetLoader = assetLoader;
        replay = new GameReplay();
        replay.setup = setup;
        replay.moves = new ArrayList<>();
    }

    public synchronized Game createInstance() {
        ComponentsBuilder componentsBuilder = new ComponentsBuilder();
        componentsBuilder.registerModule(CoreComponents::new);
        Components components = componentsBuilder.build();

        TemplatesLoader loader = new TemplatesLoader(assetLoader, new TemplatesParser(components));

        GameSetup setup = replay.setup;
        SimpleSetup simpleSetup = new SimpleSetup(setup.players.length);
        for (int i = 0; i < setup.players.length; i++) {
            PlayerSetup player = setup.players[i];
            LibraryTemplate library = loader.parseLibrary(player.library);
            simpleSetup.setLibrary(i, new IntList(library.cards));
            simpleSetup.setHero(i, library.hero);
        }
        GameSettingsBuilder builder = new GameSettingsBuilder();
        builder.components = components;
        builder.templates = loader.buildGameTemplates();
        GameSettings settings = builder.build();
        EntityData data = new SimpleEntityData(settings.components);
        MoveService moves = new MoveService(settings, data, HistoryRandom.producer(), Collections.emptyList(), true, true);
        Game game = new Game(settings, data, moves);
        simpleSetup.apply(game);
        updateInstance(game);
        return game;
    }

    public synchronized void updateInstance(Game game) {
        for (int i = game.getMoves().getHistory().size(); i < replay.moves.size(); i++) {
            MoveReplay moveReplay = replay.moves.get(i);
            for (int randomResult : moveReplay.randomResults) {
                game.getMoves().getRandom().getHistory().add(randomResult);
            }
            game.getMoves().move(moveReplay.move);
        }
    }

    public synchronized void apply(Move move) {
        if (cachedGame == null) {
            cachedGame = createInstance();
        }
        MoveService moves = cachedGame.getMoves();
        moves.move(move);
        MoveReplay moveReplay = moves.getHistory().get(moves.getHistory().size() - 1);
        replay.moves.add(moveReplay);
    }

    public synchronized void apply(MoveReplay moveReplay) {
        replay.moves.add(moveReplay);
        if (cachedGame != null) {
            updateInstance(cachedGame);
        }
    }
}
