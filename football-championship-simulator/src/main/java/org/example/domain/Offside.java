package org.example.domain;

// team = team caught offside; primaryPlayer is null when the offender isn't resolved at this point
public record Offside(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer)
        implements MatchEvent {

    @Override
    public String describe() {
        return minute + "' Offside: " + team.getName();
    }
}
