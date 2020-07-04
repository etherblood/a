package com.etherblood.a.game.tests.templates;

import com.etherblood.a.game.tests.AbstractGameTest;
import com.etherblood.a.rules.moves.Cast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArmadilloCloakTest extends AbstractGameTest {


    @Test
    public void armadilloCloak() {
        int armadilloCloakHealth = 2;
        int armadilloCloakAttack = 2;

        int ornithopter = createMinion(player(0), "cards/ornithopter.json");
        int armadillo_cloak = createHandCard(player(0), "cards/armadillo_cloak.json");

        int previousHealth = data.get(ornithopter, core.HEALTH);
        int previousAttack = data.get(ornithopter, core.ATTACK);

        data.set(player(0), core.MANA, Integer.MAX_VALUE);
        game.getMoves().apply(new Cast(player(0), armadillo_cloak, ornithopter));

        int actualHealth = data.get(ornithopter, core.HEALTH);
        int actualAttack = data.get(ornithopter, core.ATTACK);

        Assertions.assertEquals(previousHealth + armadilloCloakHealth, actualHealth);
        Assertions.assertEquals(previousAttack + armadilloCloakAttack, actualAttack);
        Assertions.assertTrue(data.has(ornithopter, core.TRAMPLE));
        Assertions.assertTrue(data.has(ornithopter, core.LIFELINK));
    }

}