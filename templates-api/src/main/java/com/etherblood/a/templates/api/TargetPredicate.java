package com.etherblood.a.templates.api;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameTemplates;

public interface TargetPredicate {

    boolean test(EntityData data, GameTemplates templates, int source, int target);
}
