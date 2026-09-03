package org.example.domain;

// primaryPlayer = scorer, secondaryPlayer = assistant (nullable)
public record GoalScored(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer)
        implements MatchEvent {

    @Override
    public String describe() {
        if (secondaryPlayer == null) {
            return minute + "' Goal: " + primaryPlayer.getFullName();
        }
        return minute + "' Goal: " + primaryPlayer.getFullName() + " (assist: " + secondaryPlayer.getFullName() + ")";
    }
}
