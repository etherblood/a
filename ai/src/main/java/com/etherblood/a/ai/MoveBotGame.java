package com.etherblood.a.ai;

import com.etherblood.a.rules.EntityUtil;
import com.etherblood.a.rules.Game;
import com.etherblood.a.rules.GameDataPrinter;
import com.etherblood.a.rules.moves.Move;
import java.util.List;
import java.util.stream.Collectors;

public class MoveBotGame extends BotGameAdapter<Move, MoveBotGame> {

    public MoveBotGame(Game game) {
        super(game);
    }

    @Override
    public List<Move> generateMoves() {
        return game.getMoves().generate(true);
    }

    @Override
    public List<Move> generateMoves(int playerIndex) {
        return game.getMoves().generate(true, getGame().findPlayerByIndex(playerIndex));
    }

    @Override
    public String toMoveString(Move move) {
        return new GameDataPrinter(game).toMoveString(move);
    }

    @Override
    public void applyMove(Move move) {
        game.getMoves().apply(move);
    }

    @Override
    public void copyStateFrom(MoveBotGame source) {
        if (game.getTemplates() != source.game.getTemplates()) {
            // equals() would be slow, require reference identity for performance
            throw new IllegalArgumentException();
        }
        EntityUtil.copy(source.game.getData(), game.getData());
    }

    @Override
    public List<Move> getMoveHistory() {
        return game.getMoves().getHistory().stream().map(x -> x.move).collect(Collectors.toList());
    }

}
