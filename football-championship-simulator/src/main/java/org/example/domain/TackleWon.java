package org.example.domain;

// primaryPlayer = defender who won the tackle, secondaryPlayer = dispossessed attacker
public record TackleWon(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer)
        implements MatchEvent {

    @Override
    public String describe() {
        return minute + "' Tackle won: " + primaryPlayer.getFullName();
    }
}
