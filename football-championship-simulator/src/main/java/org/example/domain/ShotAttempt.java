package org.example.domain;

// primaryPlayer = shooter, secondaryPlayer = goalkeeper facing the shot
public record ShotAttempt(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer,
                           ShotResult result) implements MatchEvent {

    public boolean isOnTarget() {
        return result != ShotResult.MISS;
    }

    @Override
    public String describe() {
        return minute + "' Shot: " + primaryPlayer.getFullName() + " (" + result + ")";
    }
}
