package com.etherblood.a.gui;

import com.destrostudios.cardgui.Board;
import com.destrostudios.cardgui.BoardAppState;
import com.destrostudios.cardgui.BoardObject;
import com.destrostudios.cardgui.BoardSettings;
import com.destrostudios.cardgui.Card;
import com.destrostudios.cardgui.CardZone;
import com.destrostudios.cardgui.Interactivity;
import com.destrostudios.cardgui.TargetSnapMode;
import com.destrostudios.cardgui.boardobjects.TargetArrow;
import com.destrostudios.cardgui.events.MoveCardEvent;
import com.destrostudios.cardgui.interactivities.AimToTargetInteractivity;
import com.destrostudios.cardgui.interactivities.DragToPlayInteractivity;
import com.destrostudios.cardgui.samples.boardobjects.connectionmarker.ConnectionMarker;
import com.destrostudios.cardgui.samples.boardobjects.connectionmarker.ConnectionMarkerVisualizer;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowSettings;
import com.destrostudios.cardgui.samples.boardobjects.targetarrow.SimpleTargetArrowVisualizer;
import com.destrostudios.cardgui.samples.visualization.DebugZoneVisualizer;
import com.destrostudios.cardgui.transformations.SimpleTargetRotationTransformation;
import com.destrostudios.cardgui.zones.CenteredIntervalZone;
import com.destrostudios.cardgui.zones.SimpleIntervalZone;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.gui.prettycards.CardImages;
import com.etherblood.a.gui.prettycards.CardModel;
import com.etherblood.a.gui.prettycards.CardPainterAWT;
import com.etherblood.a.gui.prettycards.CardPainterJME;
import com.etherblood.a.gui.prettycards.CardVisualizer_Card;
import com.etherblood.a.gui.prettycards.CardVisualizer_Minion;
import com.etherblood.a.gui.prettycards.MinionModel;
import com.etherblood.a.gui.soprettyboard.CameraAppState;
import com.etherblood.a.network.api.GameReplayService;
import com.etherblood.a.network.api.jwt.JwtAuthentication;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.PlayerPhase;
import com.etherblood.a.rules.moves.Block;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.templates.CardCast;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.templates.DisplayCardTemplate;
import com.etherblood.a.templates.DisplayMinionTemplate;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.IntPredicate;

public class GameBoardAppstate extends AbstractAppState implements ActionListener {

    private static final float ZONE_HEIGHT = 1.3f;
    private final Consumer<Move> moveRequester;
    private final GameReplayService gameReplayService;
    private Game game;
    private Board board;
    private final Map<Integer, PlayerZones> playerZones = new HashMap<>();
    private final Map<Integer, Card<CardModel>> visualCards = new HashMap<>();
    private final Map<Integer, Card<MinionModel>> visualMinions = new HashMap<>();
    private final Map<BoardObject<?>, Integer> objectEntities = new HashMap<>();
    private final Map<Integer, ConnectionMarker> attacks = new HashMap<>();
    private BitmapText hudText;
    private final int userControlledPlayer;
    private final CardImages cardImages;

    private final Node rootNode, guiNode;
    private final BitmapFont guiFont;

    private CameraAppState cameraAppstate;

    public GameBoardAppstate(Consumer<Move> moveRequester, GameReplayService gameReplayService, JwtAuthentication authentication, int strength, CardImages cardImages, Node rootNode, Node guiNode, BitmapFont guiFont) {
        this.moveRequester = moveRequester;
        this.gameReplayService = gameReplayService;
        this.game = gameReplayService.createInstance();
        this.userControlledPlayer = game.findPlayerByIndex(gameReplayService.getPlayerIndex(authentication.user.id));
        this.cardImages = cardImages;
        this.rootNode = rootNode;
        this.guiNode = guiNode;
        this.guiFont = guiFont;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        app.getInputManager().addMapping("space", new KeyTrigger(KeyInput.KEY_SPACE));
        app.getInputManager().addListener(this, "space");

    }

    @Override
    public void update(float tpf) {
        gameReplayService.updateInstance(game);
        updateBoard();
        updateCamera();
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        cameraAppstate = stateManager.getState(CameraAppState.class);
        board = new Board();
        stateManager.attach(initBoardGui());

        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.setColor(ColorRGBA.White);
        hudText.setLocalTranslation(0, cameraAppstate.getCamera().getHeight(), 0);
        guiNode.attachChild(hudText);
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        stateManager.detach(stateManager.getState(BoardAppState.class));
        guiNode.detachChild(hudText);

        playerZones.clear();
        visualCards.clear();
        visualMinions.clear();
        objectEntities.clear();
        attacks.clear();
        board = null;
        game = null;
    }

    private void updateBoard() {
        EntityData data = game.getData();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        StringBuilder builder = new StringBuilder();
        IntList players = data.list(core.PLAYER_INDEX);
        for (int player : players) {
            int playerIndex = data.get(player, core.PLAYER_INDEX);
            builder.append(gameReplayService.getPlayerName(playerIndex));
            builder.append(System.lineSeparator());
            builder.append(" mana: ");
            builder.append(data.getOptional(player, core.MANA).orElse(0));
            builder.append(System.lineSeparator());
            if (game.hasPlayerLost(player)) {
                builder.append(" LOST");
            } else if (game.hasPlayerWon(player)) {
                builder.append(" WON");
            } else if (data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.ATTACK_PHASE)) {
                builder.append(" ATTACK_PHASE");
            } else if (data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.BLOCK_PHASE)) {
                builder.append(" BLOCK_PHASE");
            } else if (data.hasValue(player, core.ACTIVE_PLAYER_PHASE, PlayerPhase.MULLIGAN_PHASE)) {
                builder.append(" MULLIGAN_PHASE");
            }
            builder.append(System.lineSeparator());
            builder.append(System.lineSeparator());
        }
        hudText.setText(builder.toString());

        IntList handCards = data.list(core.IN_HAND_ZONE);
        IntList battleCards = data.list(core.IN_BATTLE_ZONE);
        IntList libraryCards = data.list(core.IN_LIBRARY_ZONE);
        for (int player : players) {
            PlayerZones zones = playerZones.get(player);
            IntPredicate playerFilter = x -> data.hasValue(x, core.OWNED_BY, player);
            updateZone(libraryCards.stream().filter(playerFilter).toArray(), zones.getDeckZone(), Vector3f.UNIT_Y);
            updateZone(handCards.stream().filter(playerFilter).toArray(), zones.getHandZone(), Vector3f.UNIT_X);
            updateZone(battleCards.stream().filter(playerFilter).toArray(), zones.getBoardZone(), Vector3f.UNIT_X);
        }

        for (int attacker : data.list(core.ATTACKS_TARGET)) {
            if (!attacks.containsKey(attacker)) {
                int target = data.get(attacker, core.ATTACKS_TARGET);
                if (visualMinions.containsKey(target)) {
                    ConnectionMarker arrow = new ConnectionMarker() {
                        @Override
                        public void update(float lastTimePerFrame) {
                            getModel().updateIfNotEquals(true, false, () -> {
                            });
                        }

                    };
                    arrow.getModel().setSourceBoardObject(visualMinions.get(attacker));
                    attacks.put(attacker, arrow);
                    board.register(arrow);
                }
            }
        }

        for (Map.Entry<Integer, ConnectionMarker> entry : new ArrayList<>(attacks.entrySet())) {
            ConnectionMarker arrow = entry.getValue();
            int attacker = entry.getKey();
            OptionalInt target = data.getOptional(attacker, core.ATTACKS_TARGET);
            if (target.isPresent()) {
                Card<MinionModel> targetObject = visualMinions.get(target.getAsInt());
                if (targetObject != null) {
                    arrow.getModel().setTargetBoardObject(targetObject);
                    continue;
                }
            }
            attacks.remove(attacker);
            board.unregister(arrow);
        }
    }

    private void updateZone(int[] cards, CardZone cardZone, Vector3f interval) {
        EntityData data = game.getData();
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        for (Card card : new ArrayList<>(cardZone.getCards())) {
            int entity = objectEntities.get(card);
            if (!data.has(entity, core.IN_LIBRARY_ZONE) && !data.has(entity, core.IN_HAND_ZONE) && !data.has(entity, core.IN_BATTLE_ZONE)) {
                cardZone.removeCard(card);
                board.unregister(card);
                objectEntities.remove(card);
                visualCards.remove(entity);
                visualMinions.remove(entity);
            }
        }
        if (game.isGameOver()) {
            return;
        }
        int index = 0;
        for (int cardEntity : cards) {
            if (data.has(cardEntity, core.CARD_TEMPLATE)) {
                Card<CardModel> card = getOrCreateCard(cardEntity);
                CardModel cardModel = card.getModel();
                cardModel.setEntityId(cardEntity);
                cardModel.setFaceUp(!data.has(cardEntity, core.IN_LIBRARY_ZONE));
                cardModel.setTemplate((DisplayCardTemplate) game.getTemplates().getCard(data.get(cardEntity, core.CARD_TEMPLATE)));

                if (game.getMoves().canCast(userControlledPlayer, cardEntity)) {
                    card.setInteractivity(castInteractivity(userControlledPlayer, cardEntity));
                    cardModel.setGlow(ColorRGBA.Yellow);
                } else if (game.getMoves().canDeclareMulligan(userControlledPlayer, cardEntity)) {
                    card.setInteractivity(mulliganInteractivity(userControlledPlayer, cardEntity));
                    cardModel.setGlow(ColorRGBA.Red);
                } else {
                    card.clearInteractivity();
                    cardModel.setGlow(null);
                }
                board.triggerEvent(new MoveCardEvent(card, cardZone, interval.mult(index)));
            } else if (data.has(cardEntity, core.MINION_TEMPLATE)) {
                Card<MinionModel> card = getOrCreateMinion(cardEntity);
                MinionModel minionModel = card.getModel();
                minionModel.setEntityId(cardEntity);
                minionModel.setFaceUp(true);
                minionModel.setAttack(data.getOptional(cardEntity, core.ATTACK).orElse(0));
                minionModel.setHealth(data.getOptional(cardEntity, core.HEALTH).orElse(0));
                DisplayMinionTemplate template = (DisplayMinionTemplate) game.getTemplates().getMinion(data.get(cardEntity, core.MINION_TEMPLATE));
                minionModel.setTemplate(template);
                minionModel.setDamaged(minionModel.getHealth() < template.get(core.HEALTH));

                if (game.getMoves().canDeclareAttack(userControlledPlayer, cardEntity)) {
                    card.setInteractivity(attackInteractivity(userControlledPlayer, cardEntity));
                    minionModel.setGlow(ColorRGBA.Red);
                } else if (game.getMoves().canBlock(userControlledPlayer, cardEntity)) {
                    card.setInteractivity(blockInteractivity(userControlledPlayer, cardEntity));
                    minionModel.setGlow(ColorRGBA.Blue);
                } else {
                    card.clearInteractivity();
                    minionModel.setGlow(null);
                }
                board.triggerEvent(new MoveCardEvent(card, cardZone, interval.mult(index)));
            }
            index++;
        }
    }

    private Interactivity attackInteractivity(int player, int attacker) {
        return new AimToTargetInteractivity(TargetSnapMode.VALID) {
            @Override
            public boolean isValid(BoardObject target) {
                if (target instanceof Card) {
                    int targetId = objectEntities.get(target);
                    return game.getMoves().canDeclareAttack(player, attacker, targetId);
                }
                return false;
            }

            @Override
            public void trigger(BoardObject source, BoardObject target) {
                int actor = objectEntities.get(source);
                int dest = objectEntities.get(target);
                requestMove(new DeclareAttack(player, actor, dest));
            }
        };
    }

    private Interactivity blockInteractivity(int player, int blocker) {
        return new AimToTargetInteractivity(TargetSnapMode.VALID) {
            @Override
            public boolean isValid(BoardObject target) {
                if (target instanceof Card) {
                    int targetId = objectEntities.get(target);
                    return game.getMoves().canBlock(player, blocker, targetId);
                }
                return false;
            }

            @Override
            public void trigger(BoardObject source, BoardObject target) {
                int actor = objectEntities.get(source);
                int dest = objectEntities.get(target);
                requestMove(new Block(player, actor, dest));
            }
        };
    }

    private Interactivity castInteractivity(int player, int castable) {
        CoreComponents core = game.getData().getComponents().getModule(CoreComponents.class);
        int cardTemplate = game.getData().get(castable, core.CARD_TEMPLATE);
        CardTemplate template = game.getTemplates().getCard(cardTemplate);
        CardCast cast = template.getAttackPhaseCast() != null ? template.getAttackPhaseCast() : template.getBlockPhaseCast();
        if (cast.isTargeted()) {
            return new AimToTargetInteractivity(TargetSnapMode.VALID) {
                @Override
                public boolean isValid(BoardObject target) {
                    if (target instanceof Card) {
                        int targetId = objectEntities.get(target);
                        return game.getMoves().canCast(player, castable, targetId);
                    }
                    return false;
                }

                @Override
                public void trigger(BoardObject source, BoardObject target) {
                    int targetId = objectEntities.get(target);
                    requestMove(new Cast(player, castable, targetId));
                }
            };
        }
        return new DragToPlayInteractivity() {

            @Override
            public void trigger(BoardObject boardObject, BoardObject target) {
                requestMove(new Cast(player, castable, ~0));
            }
        };
    }

    private Interactivity mulliganInteractivity(int player, int card) {
        return new DragToPlayInteractivity() {

            @Override
            public void trigger(BoardObject boardObject, BoardObject target) {
                requestMove(new DeclareMulligan(player, card));
            }
        };
    }

    private Card<CardModel> getOrCreateCard(int myCard) {
        Card<CardModel> card = visualCards.get(myCard);
        if (card == null) {
            Card<CardModel> inner = new Card<>(new CardModel());
            card = inner;
            visualCards.put(myCard, card);
            objectEntities.put(card, myCard);

            card.rotation().addRelativeTransformation(new SimpleTargetRotationTransformation(new Quaternion().fromAngles(0, 0, -FastMath.PI)), () -> !inner.getModel().isFaceUp());
        }
        return card;
    }

    private Card<MinionModel> getOrCreateMinion(int myCard) {
        Card<MinionModel> card = visualMinions.get(myCard);
        if (card == null) {
            Card<MinionModel> inner = new Card<>(new MinionModel());
            card = inner;
            visualMinions.put(myCard, card);
            objectEntities.put(card, myCard);

            EntityData data = game.getData();
            CoreComponents core = data.getComponents().getModule(CoreComponents.class);
            card.rotation().addRelativeTransformation(new SimpleTargetRotationTransformation(new Quaternion().fromAngles(0, -FastMath.PI / 6, 0)), () -> data.has(myCard, core.TIRED));
            card.rotation().addRelativeTransformation(new SimpleTargetRotationTransformation(new Quaternion().fromAngles(0, 0, -FastMath.PI)), () -> !inner.getModel().isFaceUp());
        }
        return card;
    }

    private void updateCamera() {
        if (game.isGameOver()) {
            return;
        }

        Vector3f position = new Vector3f();
        Quaternion rotation = new Quaternion();
        boolean isPlayer1 = userControlledPlayer == game.findPlayerByIndex(1);
        position.set(0, 3.8661501f, 6.470482f);
        if (isPlayer1) {
            position.addLocal(0, 0, -10.339f);
        }
        rotation.lookAt(new Vector3f(0, -0.7237764f, -0.6900346f), Vector3f.UNIT_Y);
        if (isPlayer1) {
            rotation = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y).multLocal(rotation);
        }
        cameraAppstate.moveTo(position, rotation, 0.3f);
    }

    private BoardAppState initBoardGui() {
        CoreComponents core = game.getData().getComponents().getModule(CoreComponents.class);
        board.registerVisualizer_Class(CardZone.class, new DebugZoneVisualizer() {

            @Override
            protected Geometry createVisualizationObject(AssetManager assetManager) {
                Geometry geometry = super.createVisualizationObject(assetManager);
                geometry.setCullHint(Spatial.CullHint.Always);
                return geometry;
            }

            @Override
            protected Vector2f getSize(CardZone zone) {
                for (PlayerZones playerZones : playerZones.values()) {
                    if (zone == playerZones.getDeckZone()) {
                        return new Vector2f(1, ZONE_HEIGHT);
                    } else if (zone == playerZones.getHandZone()) {
                        return new Vector2f(5, ZONE_HEIGHT - 0.1f);
                    } else if (zone == playerZones.getBoardZone()) {
                        return new Vector2f(5, ZONE_HEIGHT);
                    }
                }
                return super.getSize(zone);
            }
        });
        CardPainterJME cardPainterJME = new CardPainterJME(new CardPainterAWT(cardImages));
        board.registerVisualizer(card -> card.getModel() instanceof CardModel, new CardVisualizer_Card(cardPainterJME));
        board.registerVisualizer(card -> card.getModel() instanceof MinionModel, new CardVisualizer_Minion(cardPainterJME));
        board.registerVisualizer_Class(TargetArrow.class, new SimpleTargetArrowVisualizer(SimpleTargetArrowSettings.builder()
                .color(ColorRGBA.White)
                .width(0.5f)
                .build()));
        board.registerVisualizer_Class(ConnectionMarker.class, new ConnectionMarkerVisualizer(SimpleTargetArrowSettings.builder()
                .arcHeight(0.1f)
                .width(0.25f)
                .build()));
        IntList players = game.getData().list(core.PLAYER_INDEX);

        Vector3f offset = new Vector3f(0, 0, ZONE_HEIGHT);
        float directionX = 1;
        float directionZ = 1;
        Quaternion zoneRotation = Quaternion.IDENTITY;

        for (int player : players) {
            if (game.getData().hasValue(player, core.PLAYER_INDEX, 1)) {
                directionX *= -1;
                directionZ *= -1;
                zoneRotation = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Y);
            }

            float x = -1.25f;
            float z = 2 * (ZONE_HEIGHT / 2);
            x += 3.25f;
            SimpleIntervalZone boardZone = new SimpleIntervalZone(offset.add(directionX * x, 0, directionZ * z), zoneRotation, new Vector3f(-directionX, 1, 1));
            x += 1.25f;

            x = -0.5f;
            x += 3.75f;
            SimpleIntervalZone deckZone = new SimpleIntervalZone(offset.add(directionX * x, 0, directionZ * z), zoneRotation, new Vector3f(0, 0.02f, 0));
            z += ZONE_HEIGHT / 2;

            x = 0;
            z += (ZONE_HEIGHT - 0.25f);
            Quaternion handRotation = zoneRotation.mult(new Quaternion().fromAngleAxis(FastMath.QUARTER_PI, Vector3f.UNIT_X));
            CenteredIntervalZone handZone = new CenteredIntervalZone(offset.add(directionX * x, 0, directionZ * z), handRotation, new Vector3f(0.85f, 1, 1));

            board.addZone(deckZone);
            board.addZone(handZone);
            board.addZone(boardZone);
            playerZones.put(player, new PlayerZones(deckZone, handZone, boardZone));
        }
        BoardAppState boardAppState = new BoardAppState(board, rootNode, BoardSettings.builder()
                .draggedCardProjectionZ(0.9975f)
                .build());
        return boardAppState;
    }

    @Override
    public void onAction(String name, boolean isPressed, float lastTimePerFrame) {
        if (!isEnabled()) {
            return;
        }
        if ("space".equals(name) && isPressed) {
            EntityData data = game.getData();
            CoreComponents core = data.getComponents().getModule(CoreComponents.class);
            data.getOptional(userControlledPlayer, core.ACTIVE_PLAYER_PHASE).ifPresent(phase -> {
                switch (phase) {
                    case PlayerPhase.BLOCK_PHASE:
                        requestMove(new EndBlockPhase(userControlledPlayer));
                        break;
                    case PlayerPhase.ATTACK_PHASE:
                        requestMove(new EndAttackPhase(userControlledPlayer));
                        break;
                    case PlayerPhase.MULLIGAN_PHASE:
                        requestMove(new EndMulliganPhase(userControlledPlayer));
                        break;
                }
            });
        }
    }

    private void requestMove(Move move) {
        moveRequester.accept(move);
    }

}
