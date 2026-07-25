package org.example.domain;

public class Goal extends MatchEvent {

    private final Player scorer;
    private final Player assistant;

    public Goal(int minute, Team team, Player scorer, Player assistant) {
        super(minute, team);
        this.scorer = scorer;
        this.assistant = assistant;
    }

    public Goal(int minute, Player scorer) {
        this(minute, scorer, null);
    }

    public Player getScorer() { return scorer; }
    public Player getAssistant() { return assistant; }

    @Override
    public String describe() {
        if (assistant == null) {
            return getMinute() + "' Goal: " + scorer.getFullName();
        }
        return getMinute() + "' Goal: " + scorer.getFullName() + " (assist: " + assistant.getFullName() + ")";
    }
}