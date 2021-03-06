package com.etherblood.a.rules;

import com.etherblood.a.entities.EntityData;
import com.etherblood.a.rules.moves.Cast;
import com.etherblood.a.rules.moves.DeclareAttack;
import com.etherblood.a.rules.moves.DeclareBlock;
import com.etherblood.a.rules.moves.DeclareMulligan;
import com.etherblood.a.rules.moves.EndAttackPhase;
import com.etherblood.a.rules.moves.EndBlockPhase;
import com.etherblood.a.rules.moves.EndMulliganPhase;
import com.etherblood.a.rules.moves.Move;
import com.etherblood.a.rules.moves.Start;
import com.etherblood.a.rules.moves.Surrender;
import com.etherblood.a.rules.moves.Update;
import com.etherblood.a.rules.moves.UseAbility;
import com.etherblood.a.rules.templates.CardTemplate;

public class GameDataPrinter {

    private final Game game;
    private final CoreComponents core;

    public GameDataPrinter(Game game) {
        this.game = game;
        this.core = game.getData().getComponents().getModule(CoreComponents.class);
    }

    public String toMoveString(Move move) {
        if (move instanceof EndBlockPhase) {
            return "End BlockPhase";
        }
        if (move instanceof EndAttackPhase) {
            return "End AttackPhase";
        }
        if (move instanceof EndMulliganPhase) {
            return "End MulliganPhase";
        }
        if (move instanceof DeclareMulligan) {
            DeclareMulligan mulligan = (DeclareMulligan) move;
            return "DeclareMulligan " + toCardString(mulligan.card);
        }
        if (move instanceof Cast) {
            Cast cast = (Cast) move;
            if (cast.target != null) {
                return "Cast " + toCardString(cast.source) + " -> " + toMinionString(cast.target);
            } else {
                return "Cast " + toCardString(cast.source);
            }
        }
        if (move instanceof DeclareBlock) {
            DeclareBlock block = (DeclareBlock) move;
            return "DeclareBlock " + toMinionString(block.source) + " -> " + toMinionString(block.target);
        }
        if (move instanceof DeclareAttack) {
            DeclareAttack attack = (DeclareAttack) move;
            return "DeclareAttack " + toMinionString(attack.source) + " -> " + toMinionString(attack.target);
        }
        if (move instanceof UseAbility) {
            UseAbility useAbility = (UseAbility) move;
            if (useAbility.target != null) {
                return "UseAbility " + toMinionString(useAbility.source) + " -> " + toMinionString(useAbility.target);
            } else {
                return "UseAbility " + toMinionString(useAbility.source);
            }
        }
        if (move instanceof Start) {
            return "Start";
        }
        if (move instanceof Update) {
            return "Update";
        }
        if (move instanceof Surrender) {
            return "Surrender";
        }
        return String.valueOf(move);
    }

    public String toMinionString(int minion) {
        EntityData data = game.getData();
        if (!data.has(minion, core.CARD_TEMPLATE)) {
            return "#" + minion + " NO_TEMPLATE " + EntityUtil.extractEntityComponents(data, minion);
        }
        int templateId = data.get(minion, core.CARD_TEMPLATE);
        CardTemplate template = game.getTemplates().getCard(templateId);
        return "#" + minion + " " + template.getTemplateName() + " (" + data.getOptional(minion, core.ATTACK).orElse(0) + ", " + data.getOptional(minion, core.HEALTH).orElse(0) + ")";
    }

    public String toCardString(int card) {
        EntityData data = game.getData();
        if (!data.has(card, core.CARD_TEMPLATE)) {
            return "#" + card + " NO_TEMPLATE " + EntityUtil.extractEntityComponents(data, card);
        }
        int templateId = data.get(card, core.CARD_TEMPLATE);
        CardTemplate template = game.getTemplates().getCard(templateId);
        return "#" + card + " " + template.getTemplateName();
    }
}
