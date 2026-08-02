package org.example.domain;

public class Goal extends MatchEvent {

    private final Player scorer;
    private final Player assistant;
    private final boolean isOwnGoal;

    public Goal(int minute, Team team, Player scorer, Player assistant, boolean isOwnGoal) {
        super(minute, team);
        this.scorer = scorer;
        this.assistant = assistant;
        this.isOwnGoal = isOwnGoal;
    }

    public Goal(int minute, Team team, Player scorer, Player assistant) {
        this(minute, team, scorer, assistant, false);
    }

    public Goal(int minute, Team team, Player scorer) {
        this(minute, team, scorer, null, false);
    }

    public Player getScorer() { return scorer; }
    public Player getAssistant() { return assistant; }
    public boolean isOwnGoal() { return isOwnGoal; }

    @Override
    public String describe() {
        String tag = isOwnGoal ? " (own goal)" : "";
        if (assistant == null) {
            return getMinute() + "' Goal: " + scorer.getFullName() + tag;
        }
        return getMinute() + "' Goal: " + scorer.getFullName() + " (assist: " + assistant.getFullName() + ")" + tag;
    }
}