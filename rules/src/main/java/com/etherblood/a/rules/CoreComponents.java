package com.etherblood.a.rules;

import com.etherblood.a.entities.ComponentsModule;
import java.util.function.ToIntFunction;

public class CoreComponents implements ComponentsModule {

    public final int OWNER;
    public final int TEAM;
    public final int PLAYER_INDEX;
    public final int TEAM_INDEX;
    public final int ACTIVE_PLAYER_PHASE;
    public final int ACTIVE_TEAM_PHASE;
    public final int ATTACK_TARGET;
    public final int BLOCK_TARGET;
    public final int CAST_TARGET;
    public final int USE_ABILITY_TARGET;
    public final int DAMAGE_REQUEST;
    public final int DAMAGE_ACTION;
    public final int DEATH_REQUEST;
    public final int DEATH_ACTION;
    public final int DRAW_CARDS_REQUEST;
    public final int DRAW_CARDS_ACTION;
    public final int PLAYER_RESULT_REQUEST;
    public final int PLAYER_RESULT;
    public final int TEAM_RESULT;
    public final int HEALTH;
    public final int ATTACK;
    public final int TEMPORARY_HEALTH;
    public final int TEMPORARY_ATTACK;
    public final int ORIGINAL_OWNER;
    public final int ORIGINAL_TEAM;
    public final int BOUND_TO;
    public final int IN_BATTLE_ZONE;
    public final int IN_HAND_ZONE;
    public final int IN_LIBRARY_ZONE;
    public final int IN_GRAVEYARD_ZONE;
    public final int CANNOT_ATTACK;
    public final int CANNOT_BLOCK;
    public final int CANNOT_BE_ATTACKED;
    public final int CANNOT_BE_BLOCKED;
    public final int CANNOT_BE_MULLIGANED;
    public final int HEXPROOF;
    public final int TEMPORARY_HEXPROOF;
    public final int MANA;
    public final int DELAYED_MANA;
    public final int MANA_POOL;
    public final int MANA_POOL_AURA;
    public final int MANA_POOL_AURA_GROWTH;
    public final int DRAWS_PER_TURN;
    public final int DRAWS_ON_ATTACK;
    public final int GIVE_DRAWS_ON_ATTACK;
    public final int DRAWS_ON_BLOCK;
    public final int DRAWS_ON_ATTACKED;
    public final int FATIGUE;
    public final int TIRED;
    public final int SUMMONING_SICKNESS;
    public final int CARD_TEMPLATE;
    public final int ORIGINAL_CARD_TEMPLATE;
    public final int HERO;
    public final int MULLIGAN;
    public final int POISONED;
    public final int VENOM;
    public final int TRAMPLE;
    public final int LIFELINK;
    public final int VIGILANCE;
    public final int FLYING;
    public final int REACH;
    public final int RAGE;
    public final int FAST_TO_ATTACK;
    public final int FAST_TO_DEFEND;
    public final int BUSHIDO;
    public final int INDESTRUCTIBLE;
    public final int REGENERATION;
    public final int TEMPORARY_PREVENT_COMBAT_DAMAGE;

    public final int TRIGGER_SELF_CAST;
    public final int TRIGGER_OTHER_CAST;
    public final int TRIGGER_SELF_SUMMON;
    public final int TRIGGER_OTHER_SUMMON;

    public final int TRIGGER_SELF_DEATH;
    public final int TRIGGER_SELF_ENTER_BATTLE;
    public final int TRIGGER_SELF_ENTER_GRAVEYARD;
    public final int TRIGGER_SELF_FIGHT;
    public final int TRIGGER_OWNER_UPKEEP;
    public final int TRIGGER_OWNER_DRAW;

    public final int ACTIVATED_ABILITY;
    public final int NINJUTSU_TARGET;
    public final int NINJUTSU_ORDER;
    public final int NINJUTSU;
    public final int BLOCKED;

    public final int HASTE_AURA;
    public final int ATTACK_AURA;
    public final int HEALTH_AURA;
    public final int VENOM_AURA;
    public final int LIFELINK_AURA;
    public final int PREVENT_COMBAT_DAMAGE_AURA;

    public CoreComponents(ToIntFunction<String> register) {
        OWNER = register.applyAsInt("OWNER");
        TEAM = register.applyAsInt("TEAM");
        PLAYER_INDEX = register.applyAsInt("PLAYER_INDEX");
        TEAM_INDEX = register.applyAsInt("TEAM_INDEX");
        ACTIVE_PLAYER_PHASE = register.applyAsInt("ACTIVE_PLAYER_PHASE");
        ACTIVE_TEAM_PHASE = register.applyAsInt("ACTIVE_TEAM_PHASE");
        ATTACK_TARGET = register.applyAsInt("ATTACK_TARGET");
        BLOCK_TARGET = register.applyAsInt("BLOCK_TARGET");
        CAST_TARGET = register.applyAsInt("CAST_TARGET");
        USE_ABILITY_TARGET = register.applyAsInt("USE_ABILITY_TARGET");
        DAMAGE_REQUEST = register.applyAsInt("DAMAGE_REQUEST");
        DAMAGE_ACTION = register.applyAsInt("DAMAGE_ACTION");
        DEATH_REQUEST = register.applyAsInt("DEATH_REQUEST");
        DEATH_ACTION = register.applyAsInt("DEATH_ACTION");
        PLAYER_RESULT_REQUEST = register.applyAsInt("PLAYER_RESULT_REQUEST");
        PLAYER_RESULT = register.applyAsInt("PLAYER_RESULT");
        TEAM_RESULT = register.applyAsInt("TEAM_RESULT");
        HEALTH = register.applyAsInt("HEALTH");
        ATTACK = register.applyAsInt("ATTACK");
        TEMPORARY_HEALTH = register.applyAsInt("TEMPORARY_HEALTH");
        TEMPORARY_ATTACK = register.applyAsInt("TEMPORARY_ATTACK");
        ORIGINAL_OWNER = register.applyAsInt("ORIGINAL_OWNER");
        ORIGINAL_TEAM = register.applyAsInt("ORIGINAL_TEAM");
        BOUND_TO = register.applyAsInt("BOUND_TO");
        IN_BATTLE_ZONE = register.applyAsInt("IN_BATTLE_ZONE");
        IN_HAND_ZONE = register.applyAsInt("IN_HAND_ZONE");
        IN_LIBRARY_ZONE = register.applyAsInt("IN_LIBRARY_ZONE");
        IN_GRAVEYARD_ZONE = register.applyAsInt("IN_GRAVEYARD_ZONE");
        CANNOT_ATTACK = register.applyAsInt("CANNOT_ATTACK");
        CANNOT_BLOCK = register.applyAsInt("CANNOT_BLOCK");
        CANNOT_BE_ATTACKED = register.applyAsInt("CANNOT_BE_ATTACKED");
        CANNOT_BE_BLOCKED = register.applyAsInt("CANNOT_BE_BLOCKED");
        CANNOT_BE_MULLIGANED = register.applyAsInt("CANNOT_BE_MULLIGANED");
        HEXPROOF = register.applyAsInt("HEXPROOF");
        TEMPORARY_HEXPROOF = register.applyAsInt("TEMPORARY_HEXPROOF");
        MANA = register.applyAsInt("MANA");
        DELAYED_MANA = register.applyAsInt("DELAYED_MANA");
        MANA_POOL = register.applyAsInt("MANA_POOL");
        MANA_POOL_AURA = register.applyAsInt("MANA_POOL_AURA");
        MANA_POOL_AURA_GROWTH = register.applyAsInt("MANA_POOL_AURA_GROWTH");
        DRAW_CARDS_REQUEST = register.applyAsInt("DRAW_CARDS_REQUEST");
        DRAW_CARDS_ACTION = register.applyAsInt("DRAW_CARDS_ACTION");
        DRAWS_PER_TURN = register.applyAsInt("DRAWS_PER_TURN");
        DRAWS_ON_ATTACK = register.applyAsInt("DRAWS_ON_ATTACK");
        GIVE_DRAWS_ON_ATTACK = register.applyAsInt("GIVE_DRAWS_ON_ATTACK");
        DRAWS_ON_BLOCK = register.applyAsInt("DRAWS_ON_BLOCK");
        DRAWS_ON_ATTACKED = register.applyAsInt("DRAWS_ON_ATTACKED");
        FATIGUE = register.applyAsInt("FATIGUE");
        TIRED = register.applyAsInt("TIRED");
        SUMMONING_SICKNESS = register.applyAsInt("SUMMONING_SICKNESS");
        CARD_TEMPLATE = register.applyAsInt("CARD_TEMPLATE");
        ORIGINAL_CARD_TEMPLATE = register.applyAsInt("ORIGINAL_CARD_TEMPLATE");
        HERO = register.applyAsInt("HERO");
        MULLIGAN = register.applyAsInt("MULLIGAN");
        POISONED = register.applyAsInt("POISONED");
        VENOM = register.applyAsInt("VENOM");
        TRAMPLE = register.applyAsInt("TRAMPLE");
        LIFELINK = register.applyAsInt("LIFELINK");
        VIGILANCE = register.applyAsInt("VIGILANCE");
        FLYING = register.applyAsInt("FLYING");
        REACH = register.applyAsInt("REACH");
        RAGE = register.applyAsInt("RAGE");
        FAST_TO_ATTACK = register.applyAsInt("FAST_TO_ATTACK");
        FAST_TO_DEFEND = register.applyAsInt("FAST_TO_DEFEND");
        BUSHIDO = register.applyAsInt("BUSHIDO");
        INDESTRUCTIBLE = register.applyAsInt("INDESTRUCTIBLE");
        REGENERATION = register.applyAsInt("REGENERATION");
        TEMPORARY_PREVENT_COMBAT_DAMAGE = register.applyAsInt("TEMPORARY_PREVENT_COMBAT_DAMAGE");

        TRIGGER_SELF_CAST = register.applyAsInt("TRIGGER_SELF_CAST");
        TRIGGER_OTHER_CAST = register.applyAsInt("TRIGGER_OTHER_CAST");
        TRIGGER_SELF_SUMMON = register.applyAsInt("TRIGGER_SELF_SUMMON");
        TRIGGER_OTHER_SUMMON = register.applyAsInt("TRIGGER_OTHER_SUMMON");

        TRIGGER_SELF_DEATH = register.applyAsInt("TRIGGER_SELF_DEATH");
        TRIGGER_SELF_ENTER_BATTLE = register.applyAsInt("TRIGGER_SELF_ENTER_BATTLE");
        TRIGGER_SELF_ENTER_GRAVEYARD = register.applyAsInt("TRIGGER_SELF_ENTER_GRAVEYARD");
        TRIGGER_OWNER_UPKEEP = register.applyAsInt("TRIGGER_OWNER_UPKEEP");
        TRIGGER_OWNER_DRAW = register.applyAsInt("TRIGGER_OWNER_DRAW");
        TRIGGER_SELF_FIGHT = register.applyAsInt("TRIGGER_SELF_FIGHT");

        ACTIVATED_ABILITY = register.applyAsInt("ACTIVATED_ABILITY");

        NINJUTSU_TARGET = register.applyAsInt("NINJUTSU_TARGET");
        NINJUTSU_ORDER = register.applyAsInt("NINJUTSU_ORDER");
        NINJUTSU = register.applyAsInt("NINJUTSU");
        BLOCKED = register.applyAsInt("BLOCKED");

        HASTE_AURA = register.applyAsInt("HASTE_AURA");
        ATTACK_AURA = register.applyAsInt("ATTACK_AURA");
        HEALTH_AURA = register.applyAsInt("HEALTH_AURA");
        VENOM_AURA = register.applyAsInt("VENOM_AURA");
        LIFELINK_AURA = register.applyAsInt("LIFELINK_AURA");
        PREVENT_COMBAT_DAMAGE_AURA = register.applyAsInt("PREVENT_COMBAT_DAMAGE_AURA");
    }
}
