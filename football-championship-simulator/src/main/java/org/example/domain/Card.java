package org.example.domain;

public class Card extends MatchEvent {

    private final Person offender;
    private final CardType type;

    public Card(int minute, Person offender, CardType type) {
        super(minute);
        this.offender = offender;
        this.type = type;
    }

    public Person getOffender() { return offender; }
    public CardType getType() { return type; }

    @Override
    public String describe() {
        return getMinute() + "' " + type + " card: " + offender.getFullName();
    }
}