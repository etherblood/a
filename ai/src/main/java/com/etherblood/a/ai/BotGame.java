package com.etherblood.a.ai;

import com.etherblood.a.entities.EntityData;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Philipp
 */
public interface BotGame<T, V extends BotGame<T, V>> {

    void applyMove(T move);

    List<T> generateMoves();

    boolean isGameOver();

    int playerCount();

    int activePlayerIndex();

    boolean isPlayerIndexActive(int playerIndex);

    float[] resultPlayerScores();

    void copyStateFrom(V source);

    void randomizeHiddenInformation(Random random, int selfIndex);

    EntityData getData();

    String toMoveString(T move);

}
