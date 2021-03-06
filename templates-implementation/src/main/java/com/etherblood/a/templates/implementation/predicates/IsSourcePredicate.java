package com.etherblood.a.templates.implementation.predicates;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.templates.api.TargetPredicate;

public class IsSourcePredicate implements TargetPredicate {

    private final boolean isSource;

    public IsSourcePredicate(boolean isSource) {
        this.isSource = isSource;
    }

    @Override
    public boolean test(EntityData data, GameTemplates templates, int source, int target) {
        return (source == target) == isSource;
    }
}
