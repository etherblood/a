package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.entities.ComponentMeta;
import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.entities.collections.IntList;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.templates.api.deserializers.filedtypes.CardId;
import java.util.function.IntUnaryOperator;

public class TransformTemplateEffect implements Effect {

    @CardId
    public final int template;

    public TransformTemplateEffect(int template) {
        this.template = template;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        if (!data.has(target, core.ORIGINAL_CARD_TEMPLATE)) {
            data.set(target, core.ORIGINAL_CARD_TEMPLATE, data.get(target, core.CARD_TEMPLATE));
        }
        data.set(target, core.CARD_TEMPLATE, template);

        if (data.has(target, core.IN_BATTLE_ZONE)) {
            CardTemplate cardTemplate = templates.getCard(template);
            IntList blacklist = new IntList(
                    core.OWNER,
                    core.TEAM,
                    core.IN_BATTLE_ZONE,
                    core.IN_GRAVEYARD_ZONE,
                    core.IN_HAND_ZONE,
                    core.IN_LIBRARY_ZONE,
                    core.TIRED,
                    core.ORIGINAL_CARD_TEMPLATE,
                    core.CARD_TEMPLATE,
                    core.ATTACK_TARGET,
                    core.BLOCK_TARGET);
            for (ComponentMeta meta : data.getComponents().getMetas()) {
                if (blacklist.contains(meta.id)) {
                    continue;
                }

                if (cardTemplate.has(meta.id)) {
                    data.set(target, meta.id, cardTemplate.get(meta.id));
                } else {
                    data.remove(target, meta.id);
                }
            }
            data.set(target, core.SUMMONING_SICKNESS, 1);
        }
    }
}
