package com.etherblood.a.network.api;

import com.etherblood.a.rules.MoveReplay;
import com.etherblood.a.templates.api.setup.RawGameSetup;

import java.util.ArrayList;
import java.util.List;

public class GameReplay {

    public RawGameSetup setup;
    public List<MoveReplay> moves;

    public GameReplay(GameReplay other) {
        this(new RawGameSetup(other.setup), new ArrayList<>(other.moves));
    }

    public GameReplay(RawGameSetup setup, List<MoveReplay> moves) {
        this.setup = setup;
        this.moves = moves;
    }

    public GameReplay() {
    }
}
