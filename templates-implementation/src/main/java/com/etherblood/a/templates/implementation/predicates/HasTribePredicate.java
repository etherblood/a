package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.templates.api.TargetPredicate;

public class HasTribePredicate implements TargetPredicate {

    public final Tribe tribe;

    public HasTribePredicate(Tribe tribe) {
        this.tribe = tribe;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int templateId = data.get(target, core.CARD_TEMPLATE);
        CardTemplate template = templates.getCard(templateId);
        return template.getTribes().contains(tribe);
    }
}
