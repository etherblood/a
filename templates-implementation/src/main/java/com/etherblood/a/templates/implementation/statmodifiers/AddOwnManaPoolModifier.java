package com.etherblood.a.templates.implementation.statmodifiers;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.StatModifier;
import com.etherblood.a.rules.updates.EffectiveStatsService;

public class AddOwnManaPoolModifier implements StatModifier {

    @Override
    public int modify(EntityData data, GameTemplates templates, int self, int stat) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(self, core.OWNED_BY);
        int manaPool = new EffectiveStatsService(data, templates).manaPool(owner);
        return stat + manaPool;
    }

}
