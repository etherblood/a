package com.etherblood.a.game.tests;

import com.etherblood.a.rules.moves.Block;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MinionTemplatesTest extends AbstractGameTest {

    @Test
    public void boombot_deathrattle() {
        int boombot = summon(player(0), "minions/boombot.json");
        int boomBotDamage = 2;
        int previousHealth = data.get(hero(1), core.HEALTH);

        data.set(boombot, core.DEATH_REQUEST, 1);
        game.getMoves().apply(new Update());

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth - boomBotDamage, actualHealth);
    }

    @Test
    public void dragonlord_atarka_trample_through_ornithopter() {
        int atarka = summon(player(0), "minions/dragonlord_atarka.json");
        data.remove(atarka, core.SUMMONING_SICKNESS);
        int ornithopter = summon(player(1), "minions/ornithopter.json");
        data.remove(ornithopter, core.SUMMONING_SICKNESS);

        int previousHealth = data.get(hero(1), core.HEALTH);
        int atarkaAttack = data.get(atarka, core.ATTACK);

        game.getMoves().apply(new DeclareAttack(player(0), atarka, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        game.getMoves().apply(new Block(player(1), ornithopter, atarka));
        game.getMoves().apply(new EndBlockPhase(player(1)));

        Assertions.assertFalse(data.has(ornithopter, core.IN_BATTLE_ZONE));

        int actualHealth = data.get(hero(1), core.HEALTH);
        Assertions.assertEquals(previousHealth - atarkaAttack, actualHealth);
    }

    @Test
    public void dragonlord_dromoka_lifelink() {
        int atarka = summon(player(0), "minions/dragonlord_dromoka.json");
        data.remove(atarka, core.SUMMONING_SICKNESS);

        int previousHealth = data.get(hero(0), core.HEALTH);
        int dromokaAttack = data.get(atarka, core.ATTACK);

        game.getMoves().apply(new DeclareAttack(player(0), atarka, hero(1)));
        game.getMoves().apply(new EndAttackPhase(player(0)));

        game.getMoves().apply(new EndBlockPhase(player(1)));

        int actualHealth = data.get(hero(0), core.HEALTH);
        Assertions.assertEquals(previousHealth + dromokaAttack, actualHealth);
    }

    @Test
    public void armadillo_cloak() {
        int armadilloCloakHealth = 2;
        int armadilloCloakAttack = 2;
        
        int ornithopter = summon(player(0), "minions/ornithopter.json");
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
        Assertions.assertEquals(hero(0), data.get(ornithopter, core.LIFELINK));
    }
}