package com.etherblood.a.templates.implementation.cards;

import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.templates.implementation.AbstractGameTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DittoDragonTest extends AbstractGameTest {

    @Test
    public void fusionAfterClone() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int dragon1 = createMinion(player(0), "blue_eyes_white_dragon");
        int dragon2 = createMinion(player(0), "blue_eyes_white_dragon");
        int ditto = createCard(player(0), "ditto", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), ditto, dragon1));

        Assertions.assertTrue(data.has(dragon1, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(dragon2, core.IN_GRAVEYARD_ZONE));
        Assertions.assertTrue(data.has(ditto, core.IN_GRAVEYARD_ZONE));

        IntList minions = data.listInValueOrder(core.IN_BATTLE_ZONE);
        int ultimateDragon = minions.get(minions.size() - 1);

        Assertions.assertEquals(3, minions.size());
        Assertions.assertEquals(getCardId("blue_eyes_ultimate_dragon"), data.get(ultimateDragon, core.CARD_TEMPLATE));
        Assertions.assertEquals(player(0), data.get(ultimateDragon, core.OWNER));
        Assertions.assertTrue( data.has(ultimateDragon, core.SUMMONING_SICKNESS));
    }
    
    @Test
    public void cloneDamage() {
        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        int dragon = createMinion(player(0), "blue_eyes_white_dragon");
        
        data.set(dragon, core.DAMAGE_REQUEST, 3);
        moves.apply(new Update());
        
        int ditto = createCard(player(0), "ditto", core.IN_HAND_ZONE);

        moves.apply(new Cast(player(0), ditto, dragon));
        
        Assertions.assertEquals(effectiveStats.health(dragon), effectiveStats.health(ditto));
    }
}
