package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.GameSettings;

public abstract class Effect {

    public abstract void apply(GameSettings settings, EntityData data, int source, int target);
}
