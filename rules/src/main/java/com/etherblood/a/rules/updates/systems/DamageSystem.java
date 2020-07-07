package com.etherblood.a.rules.updates.systems;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.game.events.api.events.DamageEvent;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.updates.ActionSystem;
import com.etherblood.a.rules.updates.Modifier;
import com.etherblood.a.rules.updates.Trigger;
import java.util.function.IntUnaryOperator;

public class DamageSystem implements ActionSystem {

    private final EntityData data;
    private final CoreComponents core;
    private final Modifier[] modifiers;
    private final Trigger[] triggers;

    public DamageSystem(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.modifiers = new Modifier[]{
            (entity, value) -> data.has(entity, core.IN_BATTLE_ZONE) ? value : 0
        };
        this.triggers = new Trigger[]{
            (entity, value) -> {
                int templateId = data.get(entity, core.CARD_TEMPLATE);
                CardTemplate template = templates.getCard(templateId);
                if (!template.getOnSelfSurviveEffects().isEmpty()) {
                    data.set(entity, core.DAMAGE_SURVIVAL_REQUEST, value);
                }
                events.fire(new DamageEvent(entity, value));
            }
        };
    }

    @Override
    public boolean isActive() {
        return data.list(core.DAMAGE_REQUEST).nonEmpty();
    }

    @Override
    public void before() {
        for (int entity : data.list(core.DAMAGE_REQUEST)) {
            int damage = data.get(entity, core.DAMAGE_REQUEST);
            for (int i = 0; damage > 0 && i < modifiers.length; i++) {
                damage = modifiers[i].modify(entity, damage);
            }
            if (damage > 0) {
                data.set(entity, core.DAMAGE_ACTION, damage);
            }
            data.remove(entity, core.DAMAGE_REQUEST);
        }
    }

    @Override
    public void run() {
        for (int entity : data.list(core.DAMAGE_ACTION)) {
            int damage = data.get(entity, core.DAMAGE_ACTION);
            for (Trigger trigger : triggers) {
                trigger.trigger(entity, damage);
            }
        }
    }

    @Override
    public void after() {
        for (int entity : data.list(core.DAMAGE_ACTION)) {
            int damage = data.get(entity, core.DAMAGE_ACTION);
            assert data.has(entity, core.IN_BATTLE_ZONE);

            int previous = data.getOptional(entity, core.HEALTH).orElse(0);
            data.set(entity, core.HEALTH, previous - damage);
            data.remove(entity, core.DAMAGE_ACTION);
        }
    }
}
