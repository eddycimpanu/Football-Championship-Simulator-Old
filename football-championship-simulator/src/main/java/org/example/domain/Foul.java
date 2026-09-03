package org.example.domain;

// team = team charged with the foul; primaryPlayer is null when the offender isn't resolved at this point
public record Foul(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer)
        implements MatchEvent {

    @Override
    public String describe() {
        return minute + "' Foul: " + team.getName();
    }
}
