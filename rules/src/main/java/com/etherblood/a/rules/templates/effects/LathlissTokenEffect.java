package com.etherblood.a.rules.templates.effects;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.templates.CardTemplate;
import com.etherblood.a.rules.templates.Tribe;
import com.etherblood.a.rules.templates.effects.filedtypes.CardId;
import com.etherblood.a.rules.updates.SystemsUtil;
import java.util.Set;
import java.util.function.IntUnaryOperator;

public class LathlissTokenEffect implements Effect {

    @CardId
    public final int tokenId;

    public LathlissTokenEffect(int tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int self, int triggerTarget) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        int owner = data.get(self, core.OWNED_BY);
        if (!data.hasValue(triggerTarget, core.OWNED_BY, owner)) {
            return;
        }
        int targetTemplateId = data.get(triggerTarget, core.CARD_TEMPLATE);
        CardTemplate targetTemplate = templates.getCard(targetTemplateId);
        Set<Tribe> tribes = targetTemplate.getTribes();
        if (tribes.contains(Tribe.DRAGON) && !tribes.contains(Tribe.TOKEN)) {
            SystemsUtil.summonMinion(data, templates, random, events, tokenId, owner);
        }
    }
}
