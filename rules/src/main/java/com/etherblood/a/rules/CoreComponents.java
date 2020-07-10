package com.etherblood.a.rules;

import com.etherblood.a.entities.ComponentsModule;
import java.util.function.ToIntFunction;

public class CoreComponents implements ComponentsModule {

    public final int END_PHASE_REQUEST;
    public final int END_PHASE_ACTION;
    public final int START_PHASE_REQUEST;
    public final int START_PHASE_ACTION;
    public final int DAMAGE_REQUEST;
    public final int DAMAGE_ACTION;
    public final int DEATH_REQUEST;
    public final int DEATH_ACTION;
    public final int DAMAGE_SURVIVAL_REQUEST;
    public final int DAMAGE_SURVIVAL_ACTION;
    public final int DRAW_CARDS_REQUEST;
    public final int DRAW_CARDS_ACTION;
    public final int DISCARD_CARDS_REQUEST;
    public final int DISCARD_CARDS_ACTION;
    public final int PLAYER_RESULT_REQUEST;
    public final int PLAYER_RESULT_ACTION;
    public final int PLAYER_RESULT;
    public final int PLAYER_INDEX;
    public final int ACTIVE_PLAYER_PHASE;
    public final int HEALTH;
    public final int ATTACK;
    public final int TEMPORARY_HEALTH;
    public final int TEMPORARY_ATTACK;
    public final int OWNED_BY;
    public final int IN_BATTLE_ZONE;
    public final int IN_HAND_ZONE;
    public final int IN_LIBRARY_ZONE;
    public final int IN_GRAVEYARD_ZONE;
    public final int BLOCKS_ATTACKER;
    public final int CANNOT_ATTACK;
    public final int CANNOT_BLOCK;
    public final int CANNOT_BE_BLOCKED;
    public final int ATTACKS_TARGET;
    public final int CAST_TARGET;
    public final int MANA;
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
    public final int OWN_MINIONS_HASTE_AURA;
    public final int OWN_MINIONS_HEALTH_AURA;
    public final int OWN_MINIONS_VENOM_AURA;
    public final int BUSHIDO;
    public final int BUSHIDO_TRIGGERED;
    public final int INDESTRUCTIBLE;

    public CoreComponents(ToIntFunction<String> register) {
        END_PHASE_REQUEST = register.applyAsInt("END_PHASE_REQUEST");
        END_PHASE_ACTION = register.applyAsInt("END_PHASE_ACTION");
        START_PHASE_REQUEST = register.applyAsInt("START_PHASE_REQUEST");
        START_PHASE_ACTION = register.applyAsInt("START_PHASE_ACTION");
        DAMAGE_REQUEST = register.applyAsInt("DAMAGE_REQUEST");
        DAMAGE_ACTION = register.applyAsInt("DAMAGE_ACTION");
        DEATH_REQUEST = register.applyAsInt("DEATH_REQUEST");
        DEATH_ACTION = register.applyAsInt("DEATH_ACTION");
        PLAYER_RESULT_REQUEST = register.applyAsInt("PLAYER_RESULT_REQUEST");
        PLAYER_RESULT_ACTION = register.applyAsInt("PLAYER_RESULT_ACTION");
        PLAYER_RESULT = register.applyAsInt("PLAYER_RESULT");
        PLAYER_INDEX = register.applyAsInt("PLAYER_INDEX");
        ACTIVE_PLAYER_PHASE = register.applyAsInt("ACTIVE_PLAYER_PHASE");
        HEALTH = register.applyAsInt("HEALTH");
        ATTACK = register.applyAsInt("ATTACK");
        TEMPORARY_HEALTH = register.applyAsInt("TEMPORARY_HEALTH");
        TEMPORARY_ATTACK = register.applyAsInt("TEMPORARY_ATTACK");
        OWNED_BY = register.applyAsInt("OWNED_BY");
        IN_BATTLE_ZONE = register.applyAsInt("IN_BATTLE_ZONE");
        IN_HAND_ZONE = register.applyAsInt("IN_HAND_ZONE");
        IN_LIBRARY_ZONE = register.applyAsInt("IN_LIBRARY_ZONE");
        IN_GRAVEYARD_ZONE = register.applyAsInt("IN_GRAVEYARD_ZONE");
        CANNOT_ATTACK = register.applyAsInt("CANNOT_ATTACK");
        CANNOT_BLOCK = register.applyAsInt("CANNOT_BLOCK");
        CANNOT_BE_BLOCKED = register.applyAsInt("CANNOT_BE_BLOCKED");
        BLOCKS_ATTACKER = register.applyAsInt("BLOCKS_ATTACKER");
        ATTACKS_TARGET = register.applyAsInt("ATTACKS_PLAYER");
        CAST_TARGET = register.applyAsInt("CAST_TARGET");
        MANA = register.applyAsInt("MANA");
        MANA_POOL = register.applyAsInt("MANA_POOL");
        MANA_POOL_AURA = register.applyAsInt("MANA_POOL_AURA");
        MANA_POOL_AURA_GROWTH = register.applyAsInt("MANA_POOL_AURA_GROWTH");
        DRAW_CARDS_REQUEST = register.applyAsInt("DRAW_CARDS_REQUEST");
        DRAW_CARDS_ACTION = register.applyAsInt("DRAW_CARDS_ACTION");
        DISCARD_CARDS_REQUEST = register.applyAsInt("DISCARD_CARDS_REQUEST");
        DISCARD_CARDS_ACTION = register.applyAsInt("DISCARD_CARDS_ACTION");
        DRAWS_PER_TURN = register.applyAsInt("DRAWS_PER_TURN");
        DRAWS_ON_ATTACK = register.applyAsInt("DRAWS_ON_ATTACK");
        GIVE_DRAWS_ON_ATTACK = register.applyAsInt("GIVE_DRAWS_ON_ATTACK");
        DRAWS_ON_BLOCK = register.applyAsInt("DRAWS_ON_BLOCK");
        DRAWS_ON_ATTACKED = register.applyAsInt("DRAWS_ON_ATTACKED");
        FATIGUE = register.applyAsInt("FATIGUE");
        TIRED = register.applyAsInt("TIRED");
        SUMMONING_SICKNESS = register.applyAsInt("SUMMONING_SICKNESS");
        CARD_TEMPLATE = register.applyAsInt("CARD_TEMPLATE");
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
        OWN_MINIONS_HASTE_AURA = register.applyAsInt("OWN_MINIONS_HASTE_AURA");
        OWN_MINIONS_HEALTH_AURA = register.applyAsInt("OWN_MINIONS_HEALTH_AURA");
        OWN_MINIONS_VENOM_AURA = register.applyAsInt("OWN_MINIONS_VENOM_AURA");
        DAMAGE_SURVIVAL_REQUEST = register.applyAsInt("DAMAGE_SURVIVAL_REQUEST");
        DAMAGE_SURVIVAL_ACTION = register.applyAsInt("DAMAGE_SURVIVAL_ACTION");
        BUSHIDO = register.applyAsInt("BUSHIDO");
        BUSHIDO_TRIGGERED = register.applyAsInt("BUSHIDO_TRIGGERED");
        INDESTRUCTIBLE = register.applyAsInt("INDESTRUCTIBLE");
    }
}
