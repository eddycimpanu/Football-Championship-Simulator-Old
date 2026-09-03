package org.example.domain;

// primaryPlayer = passer, secondaryPlayer = intended receiver
public record PassAttempt(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer,
                           boolean completed) implements MatchEvent {

    @Override
    public String describe() {
        String outcome = completed ? "completed" : "failed";
        return minute + "' Pass by " + primaryPlayer.getFullName() + " (" + outcome + ")";
    }
}
