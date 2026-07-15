package org.example.domain;

public abstract class MatchEvent {

    private final int minute;

    protected MatchEvent(int minute) {
        this.minute = minute;
    }

    public int getMinute() {
        return minute;
    }

    public abstract String describe();
}