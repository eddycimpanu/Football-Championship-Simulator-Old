package org.example.domain;

// team = team credited with the goal; primaryPlayer = scorer, who plays for the conceding team
public record OwnGoal(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer)
        implements MatchEvent {

    @Override
    public String describe() {
        return minute + "' Goal: " + primaryPlayer.getFullName() + " (own goal)";
    }
}
