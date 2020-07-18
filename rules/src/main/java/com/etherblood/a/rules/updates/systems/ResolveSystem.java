package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.game.events.api.events.DamageEvent;
import com.etherblood.a.game.events.api.events.DeathEvent;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.PlayerResult;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.rules.updates.EffectiveStatsService;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.rules.updates.ZoneService;
import java.util.OptionalInt;
import java.util.function.IntUnaryOperator;

public class ResolveSystem {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;

    public ResolveSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.random = random;
        this.events = events;
    }

    public void run() {
        StateDrivenUpdatesService state = new StateDrivenUpdatesService(data, templates, random, new EffectiveStatsService(data, templates));
        do {
            do {
                discard();
                draw();
                damage();
                death();
                playerResults();

                stateUpdates(state);
            } while (data.list(core.DAMAGE_REQUEST).nonEmpty()
                    || data.list(core.DEATH_REQUEST).nonEmpty()
                    || data.list(core.DRAW_CARDS_REQUEST).nonEmpty()
                    || data.list(core.DISCARD_CARDS).nonEmpty()
                    || data.list(core.PLAYER_RESULT_REQUEST).nonEmpty());
        } while (survive());
    }

    private void stateUpdates(StateDrivenUpdatesService state) {
        state.killHealthless();
        state.unbindFreedMinions();
        state.removeInvalidAttacks();
        state.removeInvalidBlocks();
        state.attackWithRagers();
    }

    private void discard() {
        ZoneService zoneService = new ZoneService(data, templates);
        for (int player : data.list(core.DISCARD_CARDS)) {
            int cards = data.get(player, core.DISCARD_CARDS);
            IntList handCards = data.list(core.IN_HAND_ZONE);
            for (int i = handCards.size() - 1; i >= 0; i--) {
                if (!data.hasValue(handCards.get(i), core.OWNER, player)) {
                    handCards.swapRemoveAt(i);
                }
            }
            cards = Math.min(cards, handCards.size());
            for (int i = 0; i < cards; i++) {
                int card = handCards.swapRemoveAt(random.applyAsInt(handCards.size()));
                data.remove(card, core.IN_HAND_ZONE);
                zoneService.addToGraveyard(card);
            }
            data.remove(player, core.DISCARD_CARDS);
        }
    }

    private void draw() {
        for (int player : data.list(core.DRAW_CARDS_REQUEST)) {
            assert data.has(player, core.PLAYER_INDEX);
            int cards = data.get(player, core.DRAW_CARDS_REQUEST);
            if (cards > 0) {
                data.set(player, core.DRAW_CARDS_ACTION, cards);
            }
            data.remove(player, core.DRAW_CARDS_REQUEST);
        }
        for (int player : data.list(core.DRAW_CARDS_ACTION)) {
            int cards = data.get(player, core.DRAW_CARDS_ACTION);

            //triggers
            for (int minion : data.list(core.IN_BATTLE_ZONE)) {
                int templateId = data.get(minion, core.CARD_TEMPLATE);
                CardTemplate template = templates.getCard(templateId);
                for (Effect onDrawEffect : template.getOnDrawEffects()) {
                    for (int i = 0; i < cards; i++) {
                        onDrawEffect.apply(data, templates, random, events, minion, player);
                    }
                }
            }
        }
        for (int player : data.list(core.DRAW_CARDS_ACTION)) {
            int cards = data.get(player, core.DRAW_CARDS_ACTION);
            SystemsUtil.drawCards(data, cards, random, player);
            data.remove(player, core.DRAW_CARDS_ACTION);
        }
    }

    private void damage() {
        for (int entity : data.list(core.DAMAGE_REQUEST)) {
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                int damage = data.get(entity, core.DAMAGE_REQUEST);
                if (damage > 0) {
                    data.set(entity, core.DAMAGE_ACTION, damage);
                }
            }
            data.remove(entity, core.DAMAGE_REQUEST);
        }

        for (int entity : data.list(core.DAMAGE_ACTION)) {
            int damage = data.get(entity, core.DAMAGE_ACTION);

            int templateId = data.get(entity, core.CARD_TEMPLATE);
            CardTemplate template = templates.getCard(templateId);
            if (!template.getOnSelfSurviveEffects().isEmpty()) {
                data.set(entity, core.DAMAGE_SURVIVAL_REQUEST, damage);
            }
            events.fire(new DamageEvent(entity, damage));
        }
        for (int entity : data.list(core.DAMAGE_ACTION)) {
            int damage = data.get(entity, core.DAMAGE_ACTION);
            assert data.has(entity, core.IN_BATTLE_ZONE);

            if (data.has(entity, core.TEMPORARY_HEALTH)) {
                int temporaryHealth = data.get(entity, core.TEMPORARY_HEALTH);
                if (damage > temporaryHealth) {
                    damage -= temporaryHealth;
                    data.remove(entity, core.TEMPORARY_HEALTH);
                } else {
                    data.set(entity, core.TEMPORARY_HEALTH, temporaryHealth - damage);
                    damage = 0;
                }
            }

            int health = data.getOptional(entity, core.HEALTH).orElse(0);
            data.set(entity, core.HEALTH, health - damage);
            data.remove(entity, core.DAMAGE_ACTION);
        }
    }

    private void death() {
        for (int entity : data.list(core.DEATH_REQUEST)) {
            int death = data.get(entity, core.DEATH_REQUEST);
            if (data.has(entity, core.IN_BATTLE_ZONE) && !data.has(entity, core.INDESTRUCTIBLE)) {
                data.set(entity, core.DEATH_ACTION, death);
            }
            data.remove(entity, core.DEATH_REQUEST);
        }
        for (int entity : data.list(core.DEATH_ACTION)) {

            if (data.has(entity, core.HERO)) {
                int owner = data.get(entity, core.OWNER);
                data.set(owner, core.PLAYER_RESULT_REQUEST, PlayerResult.LOSS);
            }

            int templateId = data.get(entity, core.CARD_TEMPLATE);
            CardTemplate template = templates.getCard(templateId);
            for (Effect onDeathEffect : template.getOnSelfDeathEffects()) {
                onDeathEffect.apply(data, templates, random, events, entity, entity);
            }

            events.fire(new DeathEvent(entity));
        }

        ZoneService zoneService = new ZoneService(data, templates);
        for (int entity : data.list(core.DEATH_ACTION)) {
            data.remove(entity, core.DEATH_ACTION);
            zoneService.removeFromBattle(entity);
            zoneService.addToGraveyard(entity);
        }
    }

    private void playerResults() {
        IntList playerResultRequests = data.list(core.PLAYER_RESULT_REQUEST);
        for (int player : playerResultRequests) {
            int result = data.get(player, core.PLAYER_RESULT_REQUEST);
            data.set(player, core.PLAYER_RESULT, result);

            data.remove(player, core.PLAYER_RESULT_REQUEST);
        }
        if (playerResultRequests.nonEmpty()) {
            IntList teams = data.list(core.TEAM_INDEX);
            for (int team : teams) {
                updateTeamResult(team);
            }
            int total = teams.size();
            int losses = 0;
            int wins = 0;
            for (int team : teams) {
                if (data.hasValue(team, core.TEAM_RESULT, PlayerResult.LOSS)) {
                    losses++;
                }
                if (data.hasValue(team, core.TEAM_RESULT, PlayerResult.WIN)) {
                    wins++;
                }
            }
            if (wins != 0) {
                for (int team : teams) {
                    if (!data.has(team, core.TEAM_RESULT)) {
                        setTeamResult(team, PlayerResult.LOSS);
                        break;
                    }
                }
            } else if (losses + 1 == total) {
                for (int team : teams) {
                    if (!data.has(team, core.TEAM_RESULT)) {
                        setTeamResult(team, PlayerResult.WIN);
                        break;
                    }
                }
            }

            for (int player : data.list(core.PLAYER_RESULT)) {
                data.remove(player, core.ACTIVE_PLAYER_PHASE);
            }
        }
    }

    private void setTeamResult(int team, int result) {
        data.set(team, core.TEAM_RESULT, result);
        for (int player : data.list(core.PLAYER_INDEX)) {
            if (data.hasValue(player, core.TEAM, team)) {
                data.set(player, core.PLAYER_RESULT, result);
            }
        }
    }

    private void updateTeamResult(int team) {
        for (int player : data.list(core.PLAYER_INDEX)) {
            if (!data.hasValue(player, core.TEAM, team)) {
                continue;
            }
            OptionalInt playerResult = data.getOptional(player, core.PLAYER_RESULT);
            if (!playerResult.isPresent()) {
                return;
            } else if (playerResult.getAsInt() == PlayerResult.WIN) {
                setTeamResult(team, PlayerResult.WIN);
                return;
            }
        }
        setTeamResult(team, PlayerResult.LOSS);
    }

    private boolean survive() {
        boolean result = false;
        for (int entity : data.list(core.DAMAGE_SURVIVAL_REQUEST)) {
            int survival = data.get(entity, core.DAMAGE_SURVIVAL_REQUEST);
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                data.set(entity, core.DAMAGE_SURVIVAL_ACTION, survival);
                result = true;
            }
            data.remove(entity, core.DAMAGE_SURVIVAL_REQUEST);
        }
        for (int entity : data.list(core.DAMAGE_SURVIVAL_ACTION)) {
            int templateId = data.get(entity, core.CARD_TEMPLATE);
            CardTemplate template = templates.getCard(templateId);
            for (Effect onSurviveEffect : template.getOnSelfSurviveEffects()) {
                onSurviveEffect.apply(data, templates, random, events, entity, entity);
            }
            data.remove(entity, core.DAMAGE_SURVIVAL_ACTION);
        }
        return result;
    }
}
