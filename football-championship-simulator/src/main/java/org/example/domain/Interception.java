package org.example.domain;

// primaryPlayer = defender who intercepted, secondaryPlayer = intended receiver
public record Interception(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer)
        implements MatchEvent {

    @Override
    public String describe() {
        return minute + "' Interception: " + primaryPlayer.getFullName();
    }
}
