package com.etherblood.a.rules.updates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Effect;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

public class TriggerService {

    private final EntityData data;
    private final CoreComponents core;
    private final GameTemplates templates;
    private final IntUnaryOperator random;
    private final GameEventListener events;

    public TriggerService(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events) {
        this.data = data;
        this.core = data.getComponents().getModule(CoreComponents.class);
        this.templates = templates;
        this.random = random;
        this.events = events;
    }

    public void onSummoned(int minion) {
        triggerSelf(minion, core.TRIGGER_SELF_SUMMON);
        triggerOthers(minion, core.TRIGGER_OTHER_SUMMON);
    }

    public void onCast(int minion) {
        triggerSelf(minion, core.TRIGGER_SELF_CAST);
        triggerOthers(minion, core.TRIGGER_OTHER_CAST);
    }

    public void onDeath(int minion) {
        triggerSelf(minion, core.TRIGGER_SELF_DEATH);
    }

    public void onEnterBattle(int card) {
        triggerSelf(card, core.TRIGGER_SELF_ENTER_BATTLE);
    }

    public void onEnterGraveyard(int card) {
        triggerSelf(card, core.TRIGGER_SELF_ENTER_GRAVEYARD);
    }

    public void onSurvive(int minion) {
        triggerSelf(minion, core.TRIGGER_SELF_SURVIVE);
    }

    public void onFight(int minion) {
        triggerSelf(minion, core.TRIGGER_SELF_FIGHT);
    }

    public void onUpkeep(int owner) {
        triggerOwner(owner, core.TRIGGER_OWNER_UPKEEP);
    }

    public void onDraw(int owner) {
        triggerOwner(owner, core.TRIGGER_OWNER_DRAW);
    }

    private void triggerOwner(int owner, int ownerComponent) {
        for (int other : data.listInValueOrder(ownerComponent)) {
            if (data.hasValue(other, core.OWNER, owner)) {
                trigger(ownerComponent, other, owner);
            }
        }
    }

    private void triggerSelf(int triggerTarget, int selfComponent) {
        if (data.has(triggerTarget, selfComponent)) {
            trigger(selfComponent, triggerTarget, triggerTarget);
        }
    }

    private void triggerOthers(int triggerTarget, int otherComponent) {
        for (int other : data.listInValueOrder(otherComponent)) {
            if (other == triggerTarget) {
                continue;
            }
            trigger(otherComponent, other, triggerTarget);
        }
    }

    public void trigger(int triggerComponent, int self, int triggerTarget) {
        for (Effect effect : triggers(self).get(triggerComponent)) {
            effect.apply(data, templates, random, events, self, triggerTarget);
        }
    }

    public void cleanupEffects(int entity) {
        for (int trigger : triggers(entity).keySet()) {
            assert !triggers(entity).get(trigger).isEmpty();
            data.remove(entity, trigger);
        }
    }

    public void initEffects(int entity) {
        for (int trigger : triggers(entity).keySet()) {
            assert !triggers(entity).get(trigger).isEmpty();
            data.set(entity, trigger, data.createEntity());
        }
    }

    private Map<Integer, List<Effect>> triggers(int entity) {
        int templateId = data.get(entity, core.CARD_TEMPLATE);
        CardTemplate template = templates.getCard(templateId);
        if (data.has(entity, core.IN_BATTLE_ZONE)) {
            return template.getBattleTriggers();
        } else if (data.has(entity, core.IN_HAND_ZONE)) {
            return template.getHandTriggers();
        } else if (data.has(entity, core.IN_GRAVEYARD_ZONE)) {
            return template.getGraveyardTriggers();
        } else if (data.has(entity, core.IN_LIBRARY_ZONE)) {
            return template.getLibraryTriggers();
        } else {
            throw new AssertionError();
        }
    }
}
