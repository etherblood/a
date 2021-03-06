package com.etherblood.a.templates.implementation.effects;

import com.etherblood.a.rules.templates.Effect;
import com.etherblood.a.entities.EntityData;
import com.etherblood.a.game.events.api.GameEventListener;
import com.etherblood.a.rules.CoreComponents;
import com.etherblood.a.rules.GameTemplates;
import com.etherblood.a.rules.updates.ZoneService;
import com.etherblood.a.templates.api.deserializers.filedtypes.CardId;
import java.util.function.IntUnaryOperator;

public class DrawCardTemplateEffect implements Effect {

    @CardId
    public final int cardId;

    public DrawCardTemplateEffect(int cardId) {
        this.cardId = cardId;
    }

    @Override
    public void apply(EntityData data, GameTemplates templates, IntUnaryOperator random, GameEventListener events, int source, int target) {
        CoreComponents core = data.getComponents().getModule(CoreComponents.class);
        ZoneService zoneService = new ZoneService(data, templates, random, events);
        int owner = data.get(source, core.OWNER);
        for (int card : data.list(core.IN_LIBRARY_ZONE)) {
            if (data.hasValue(card, core.CARD_TEMPLATE, cardId) && data.hasValue(card, core.OWNER, owner)) {
                zoneService.removeFromLibrary(card);
                zoneService.addToHand(card);
                break;
            }
        }
    }
}
