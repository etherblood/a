package com.etherblood.a.rules.classic;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.game.events.api.events.DeathEvent;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.DeathOptions;
import com.etherblood.a.rules.EffectiveStatsService;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.PlayerResult;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.updates.SystemsUtil;
import com.etherblood.a.rules.updates.TriggerService;
import com.etherblood.a.rules.updates.ZoneService;
import java.util.function.IntUnaryOperator;

public class ResolveSystem implements Runnable {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;
    private final TriggerService triggerService;
    private final ZoneService zoneService;

    public ResolveSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.random = random;
        this.events = events;
        this.triggerService = new TriggerService(data, templates, random, events);
        this.zoneService = new ZoneService(data, templates, random, events);
    }

    @Override
    public void run() {
        StateDrivenUpdatesService state = new StateDrivenUpdatesService(data, templates, random, new EffectiveStatsService(data, templates));
        do {
            death();

            stateUpdates(state);
        } while (data.list(core.DEATH_REQUEST).nonEmpty());
    }

    private void stateUpdates(StateDrivenUpdatesService state) {
        state.killHealthless();
        state.unbindFreedMinions();
        state.removeInvalidAttacks();
        state.removeInvalidBlocks();
        state.attackWithRagers();
    }

    private void death() {
        IntList deathRequests = data.listOrdered(core.DEATH_REQUEST, core.IN_BATTLE_ZONE);
        if (deathRequests.isEmpty()) {
            return;
        }
        IntList deaths = new IntList();
        for (int entity : deathRequests) {
            int deathOptions = data.get(entity, core.DEATH_REQUEST);
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                if (deathOptions != DeathOptions.SACRIFICE) {
                    if (data.has(entity, core.INDESTRUCTIBLE)) {
                        continue;
                    }
                    if (data.has(entity, core.REGENERATION)) {
                        int regeneration = data.get(entity, core.REGENERATION);
                        int owner = data.get(entity, core.OWNER);
                        int mana = data.getOptional(owner, core.MANA).orElse(0);
                        if (regeneration <= mana) {
                            data.set(owner, core.MANA, mana - regeneration);
                            regenerate(entity);
                            continue;
                        }
                    }
                }
                deaths.add(entity);
            }
        }
        data.clear(core.DEATH_REQUEST);
        for (int entity : deaths) {
            if (data.has(entity, core.HERO)) {
                int owner = data.get(entity, core.OWNER);
                SystemsUtil.setPlayerResult(data, owner, PlayerResult.LOSS);
            }

            triggerService.onDeath(entity);
            events.fire(new DeathEvent(entity));
        }

        for (int entity : deaths) {
            if (data.has(entity, core.IN_BATTLE_ZONE)) {
                zoneService.removeFromBattle(entity);
                zoneService.addToGraveyard(entity);
            }
        }
    }

    private void regenerate(int entity) {
        CardTemplate template = templates.getCard(data.get(entity, core.CARD_TEMPLATE));
        data.set(entity, core.HEALTH, template.getBattle().getComponents().get(core.HEALTH));
        data.set(entity, core.TIRED, 1);
        data.remove(entity, core.ATTACK_TARGET);
        data.remove(entity, core.BLOCK_TARGET);
        for (int other : data.list(core.ATTACK_TARGET)) {
            if (data.hasValue(other, core.ATTACK_TARGET, entity)) {
                data.remove(other, core.ATTACK_TARGET);
            }
        }
        for (int other : data.list(core.BLOCK_TARGET)) {
            if (data.hasValue(other, core.BLOCK_TARGET, entity)) {
                data.remove(other, core.BLOCK_TARGET);
            }
        }
    }
}
