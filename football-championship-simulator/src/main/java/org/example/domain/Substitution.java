package org.example.domain;

public class Substitution extends MatchEvent {

    private final Player playerOut;
    private final Player playerIn;

    public Substitution(int minute, Team team, Player playerOut, Player playerIn) {
        super(minute, team);
        this.playerOut = playerOut;
        this.playerIn = playerIn;
    }

    public Player getPlayerOut() { return playerOut; }
    public Player getPlayerIn() { return playerIn; }

    @Override
    public String describe() {
        return getMinute() + "' SUB: " + playerOut.getFullName() + " -> " + playerIn.getFullName();
    }
}