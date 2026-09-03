package org.example.domain;

// primaryPlayer = offender
public record CardShown(int minute, int second, Team team, Player primaryPlayer, Player secondaryPlayer,
                         CardType cardType) implements MatchEvent {

    @Override
    public String describe() {
        return minute + "' " + cardType + " card: " + primaryPlayer.getFullName();
    }
}
