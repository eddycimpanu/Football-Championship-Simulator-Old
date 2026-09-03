package org.example.domain;

// team = goalkeeper's team; primaryPlayer = goalkeeper, secondaryPlayer = shooter
public record Save(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer)
        implements MatchEvent {

    @Override
    public String describe() {
        return minute + "' Save: " + primaryPlayer.getFullName();
    }
}
