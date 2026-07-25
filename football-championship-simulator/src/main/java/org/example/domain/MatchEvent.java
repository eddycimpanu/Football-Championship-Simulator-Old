package org.example.domain;

public abstract class MatchEvent {

    private final int minute;
    private final Team team;

    protected MatchEvent(int minute, Team team) {
        this.minute = minute;
        this.team = team;
    }

    public int getMinute() { return minute; }
    public Team getTeam() { return team; }

    public abstract String describe();
}