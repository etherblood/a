package com.etherblood.a.game.tests.templates;

import com.etherblood.a.game.tests.AbstractGameTest;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.EndAttackPhase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RoyalJellyTest extends AbstractGameTest {

    @Test
    public void royalJelly_draw_drone() {
        int royalJelly = createHandCard(player(0), "royal_jelly");
        int queen = createLibraryCard(player(0), "bee_queen");
        int drone = createLibraryCard(player(0), "bee_drone");

        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        game.getMoves().apply(new Cast(player(0), royalJelly, ~0));

        Assertions.assertTrue(data.has(queen, core.IN_LIBRARY_ZONE));
        Assertions.assertTrue(data.has(drone, core.IN_HAND_ZONE));
    }

    @Test
    public void royalJelly_draw_queen() {
        int royalJelly = createHandCard(player(1), "royal_jelly");
        int queen = createLibraryCard(player(1), "bee_queen");
        int drone = createLibraryCard(player(1), "bee_drone");

        game.getMoves().apply(new EndAttackPhase(player(0)));
        data.set(player(1), core.MANA, Integer.MAX_VALUE);
        game.getMoves().apply(new Cast(player(1), royalJelly, ~0));

        Assertions.assertTrue(data.has(queen, core.IN_HAND_ZONE));
        Assertions.assertTrue(data.has(drone, core.IN_LIBRARY_ZONE));
    }
}
