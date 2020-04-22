package com.etherblood.a.ai.moves;

import com.etherblood.a.rules.Game;

public class Block implements Move {

    public final int player, source, target;

    public Block(int player, int source, int target) {
        this.player = player;
        this.source = source;
        this.target = target;
    }

    @Override
    public int hashCode() {
        return 127 * player + 31 * source + target;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Block)) {
            return false;
        }
        Block other = (Block) obj;
        return player == other.player && source == other.source && target == other.target;
    }

    @Override
    public void apply(Game game) {
        game.getMoves().block(player, source, target);
    }

}
