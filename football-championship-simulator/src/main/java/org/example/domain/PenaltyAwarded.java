package org.example.domain;

// team = team awarded the penalty; primaryPlayer = defender whose foul conceded it (nullable if unresolved)
public record PenaltyAwarded(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer)
        implements MatchEvent {

    @Override
    public String describe() {
        return minute + "' Penalty awarded: " + team.getName();
    }
}
