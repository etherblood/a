package com.etherblood.a.game.server.matchmaking;

public class BotRequest {

    public final int playerIndex;
    public final int strength;

    public BotRequest(int playerIndex, int strength) {
        this.playerIndex = playerIndex;
        this.strength = strength;
    }
}
