package org.example.domain;

// team = attacking team taking the corner; primaryPlayer = corner taker
public record Corner(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer)
        implements MatchEvent {

    @Override
    public String describe() {
        return minute + "' Corner: " + team.getName();
    }
}
