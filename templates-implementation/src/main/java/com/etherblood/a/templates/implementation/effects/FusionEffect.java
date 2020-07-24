package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.DeathOptions;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.deserializers.filedtypes.CardId;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.function.IntUnaryOperator;

public class FusionEffect implements Effect {

    @CardId
    public final int[] materialIds;
    @CardId
    public final int resultId;

    public FusionEffect(int[] materialIds, int resultId) {
        this.materialIds = materialIds;
        this.resultId = resultId;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int self, int triggerTarget) {
        if (self != triggerTarget) {
            return;
        }
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(self, core.OWNER);
        if (!data.hasValue(triggerTarget, core.OWNER, owner)) {
            return;
        }
        IntList required = new IntList(materialIds);
        IntList material = new IntList();
        for (int minion : data.listInValueOrder(core.IN_BATTLE_ZONE)) {
            if (!data.hasValue(minion, core.OWNER, owner)) {
                continue;
            }
            if (data.hasValue(minion, core.DEATH_REQUEST, DeathOptions.SACRIFICE)) {
                continue;
            }
            int templateId = data.get(minion, core.CARD_TEMPLATE);
            int index = required.indexOf(templateId);
            if (index >= 0) {
                material.add(minion);
                required.swapRemoveAt(index);
            }
        }
        if (required.nonEmpty()) {
            return;
        }
        for (int minion : material) {
            data.set(minion, core.DEATH_REQUEST, DeathOptions.SACRIFICE);
        }
        SystemsUtil.summonMinion(data, templates, random, events, resultId, owner);
    }
}
