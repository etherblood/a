package com.etherblood.a.game.tests.templates;

import com.etherblood.a.game.tests.*;
import com.etherblood.a.rules.EntityUtil;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.EndAttackPhase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LathlissDragonQueenTest extends AbstractGameTest {

    @Test
    public void lathliss_summon_dragon_token() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        data.set(player(1), core.MANA, Integer.MAX_VALUE);
        
        int lathliss = createMinion(player(0), "cards/lathliss_dragon_queen.json");
        int babyDragon0 = createHandCard(player(0), "cards/baby_dragon.json");
        int babyDragon1 = createHandCard(player(1), "cards/baby_dragon.json");

        Assertions.assertEquals(3, data.list(core.IN_BATTLE_ZONE).size());
        
        System.out.println(EntityUtil.toMap(data));
        System.out.println("");
        System.out.println("");
        game.getMoves().apply(new Cast(player(0), babyDragon0, ~0));
        System.out.println(EntityUtil.toMap(data));
        Assertions.assertEquals(5, data.list(core.IN_BATTLE_ZONE).size());

        game.getMoves().apply(new EndAttackPhase(player(0)));
        game.getMoves().apply(new Cast(player(1), babyDragon1, ~0));

        Assertions.assertEquals(6, data.list(core.IN_BATTLE_ZONE).size());
    }
}