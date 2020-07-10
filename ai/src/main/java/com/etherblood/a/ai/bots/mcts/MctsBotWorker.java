package com.etherblood.a.ai.bots.mcts;

import com.etherblood.a.ai.MoveBotGame;
import com.etherblood.a.rules.moves.Move;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MctsBotWorker implements Runnable, Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(MctsBotWorker.class);

    private static final float EPSILON = 1e-6f;
    private final float uctConstant;
    private final float firstPlayUrgency;
    private final Function<MoveBotGame, float[]> evaluation;
    private final Random random;
    private final int strength;
    private final float raveMultiplier;
    private final MoveBotGame sourceGame, simulationGame;
    private final int playerCount;
    private final int botPlayerIndex;
    private final MctsNode rootNode;
    private final MctsRaveScores raveScores;

    public MctsBotWorker(MoveBotGame sourceGame, MoveBotGame simulationGame, MctsBotSettings<Move, MoveBotGame> settings, int playerIndex, MctsNode rootNode, MctsRaveScores raveScores) {
        this.sourceGame = sourceGame;
        this.simulationGame = simulationGame;
        this.random = settings.random;
        this.strength = settings.strength;
        this.uctConstant = settings.uctConstant;
        this.firstPlayUrgency = settings.firstPlayUrgency;
        this.evaluation = settings.evaluation;
        this.raveMultiplier = settings.raveMultiplier;
        this.playerCount = sourceGame.playerCount();
        this.botPlayerIndex = playerIndex;
        this.rootNode = rootNode;
        this.raveScores = raveScores;
    }

    @Override
    public Void call() throws Exception {
        run();
        return null;
    }

    @Override
    public void run() {
        LOG.debug("worker started.");
        int iterations = 0;
        while (rootNode.visits() < strength) {
            simulationGame.copyStateFrom(sourceGame);
            // randomize opponents hand cards
            // TODO: use a priori knowledge for better approximation of real hand cards
            // TODO: the simulated opponent also only 'knows' our hand...
            simulationGame.randomizeHiddenInformation(random, botPlayerIndex);
            iteration(rootNode, raveScores);
            iterations++;
            if (Thread.interrupted()) {
                throw new RuntimeException(new InterruptedException());
            }
        }
        LOG.debug("worker finished after {} iterations.", iterations);
    }

    private void iteration(MctsNode rootNode, MctsRaveScores raveScores) {
        Deque<MctsNode> nodePath = new LinkedList<>();
        Deque<Move> movePath = new LinkedList<>();
        nodePath.add(rootNode);
        Move selectedMove = select(nodePath, movePath, raveScores);

        if (selectedMove != null) {
            MctsNode child = new MctsNode(playerCount);
            nodePath.getLast().addChild(selectedMove, child);
            nodePath.add(child);
            movePath.add(selectedMove);
            simulationGame.applyMove(selectedMove);
        }

        float[] result = evaluation.apply(simulationGame);
        assert Math.abs(IntStream.range(0, result.length).mapToDouble(i -> result[i]).sum() - 1) < 0.0000001;
        for (MctsNode node : nodePath) {
            node.updateScores(result);
        }
        for (Move move : movePath) {
            raveScores.updateScores(move, result);
        }
        float[] avgWeights = Arrays.copyOf(result, result.length);
        for (int i = 0; i < avgWeights.length; i++) {
            avgWeights[i] *= movePath.size();
        }
        raveScores.getDefaultScore().updateScores(avgWeights);
    }

    private Move select(Deque<MctsNode> nodePath, Deque<Move> movePath, MctsRaveScores raveScores) {
        MctsNode node = nodePath.getLast();
        Move selectedMove = uctSelect(node, raveScores);
        while ((node = getChild(node, selectedMove)) != null) {
            nodePath.add(node);
            movePath.add(selectedMove);
            simulationGame.applyMove(selectedMove);
            if (simulationGame.isGameOver()) {
                return null;
            }
            selectedMove = uctSelect(nodePath.getLast(), raveScores);
        }
        return selectedMove;
    }

    private Move uctSelect(MctsNode node, MctsRaveScores raveScores) {
        int activePlayerIndex = simulationGame.activePlayerIndex();
        List<Move> moves = simulationGame.generateMoves(activePlayerIndex);
        if (moves.size() == 1) {
            return moves.get(0);
        }
        List<Move> bestMoves = new ArrayList<>();
        float bestValue = Float.NEGATIVE_INFINITY;
        for (Move move : moves) {
            float score;
            MctsNode child = getChild(node, move);
            if (child == null) {
                score = firstPlayUrgency;
            } else {
                score = calcUtc(node.visits(), child.visits(), child.score(activePlayerIndex));
            }

            MctsRaveScore raveScore = raveScores.get(move);
            if (raveScore == null) {
                raveScore = raveScores.get(null);
            }
            float raveValue = raveScore.getScore(activePlayerIndex) / (node.visits() + 1);
            score += raveMultiplier * raveValue;

            if (score > bestValue) {
                bestMoves.clear();
                bestValue = score;
            }
            if (score == bestValue) {
                bestMoves.add(move);
            }
        }
        if (bestMoves.size() == 1) {
            return bestMoves.get(0);
        }
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    private float calcUtc(float parentTotal, float childTotal, float childScore) {
        float exploitation = childScore / (childTotal + EPSILON);
        float exploration = (float) (Math.sqrt(uctConstant * Math.log(parentTotal + 1) / (childTotal + EPSILON)));
        return exploitation + exploration;
    }

    private MctsNode getChild(MctsNode node, Move move) {
        return node.getChildOrDefault(move, null);
    }
}
