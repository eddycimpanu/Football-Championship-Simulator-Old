package org.example.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Match {

    private static final int REGULATION_MINUTES = 90;
    private static final int SECONDS_PER_MINUTE = 60;

    private int elapsedSeconds;
    private int regulationElapsedSeconds;

    private final Team homeTeam;
    private final Team awayTeam;
    private final Stadium stadium;
    private final Referee referee;
    private final Random random;

    private final List<Player> homeLineup;
    private final List<Player> awayLineup;
    private final List<Player> homeOnField;
    private final List<Player> awayOnField;

    private final List<MatchEvent> events;

    private boolean isFinished;

    private Team attackingTeam;
    private Team defendingTeam;
    private PhaseState currentState;

    public Match(Team homeTeam, Team awayTeam, Referee referee, Random random) {
        if (!homeTeam.isValidLineup()) {
            throw new IllegalStateException("Home team " + homeTeam.getName() + " does not have a valid starting lineup!");
        }
        if (!awayTeam.isValidLineup()) {
            throw new IllegalStateException("Away team " + awayTeam.getName() + " does not have a valid starting lineup!");
        }

        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.referee = referee;
        this.random = random;
        this.stadium = homeTeam.getStadium();

        this.homeLineup = new ArrayList<>(homeTeam.getStartingLineUp());
        this.awayLineup = new ArrayList<>(awayTeam.getStartingLineUp());
        this.homeOnField = new ArrayList<>(homeLineup);
        this.awayOnField = new ArrayList<>(awayLineup);

        this.events = new ArrayList<>();
        this.isFinished = false;
    }

    public void simulateMatch() {
        currentState = PhaseState.KICKOFF;
        attackingTeam = homeTeam;
        defendingTeam = awayTeam;
        this.elapsedSeconds = 0;
        PhaseSimulator phaseSimulator = new PhaseSimulator(random);

        int stoppageSeconds = simulatePeriod(phaseSimulator, REGULATION_MINUTES * SECONDS_PER_MINUTE, true);
        this.regulationElapsedSeconds = elapsedSeconds;

        int stoppageEndSeconds = REGULATION_MINUTES * SECONDS_PER_MINUTE + stoppageSeconds;
        simulatePeriod(phaseSimulator, stoppageEndSeconds, false);

        finishMatch();
    }

    private int simulatePeriod(PhaseSimulator phaseSimulator, int toSeconds, boolean accumulateStoppage) {
        int stoppageSeconds = 0;

        while (elapsedSeconds < toSeconds) {
            int eventsBefore = events.size();
            PhaseState finalState = phaseSimulator.simulatePhase(currentState, attackingTeam, defendingTeam, this);
            List<MatchEvent> phaseEvents = events.subList(eventsBefore, events.size());

            elapsedSeconds += phaseSimulator.decidePhaseDuration(phaseEvents);
            if (accumulateStoppage) {
                stoppageSeconds += phaseSimulator.decideDeadTimeSeconds(phaseEvents);
            }

            if (isPossessionChange(finalState)) {
                Team temp = attackingTeam;
                attackingTeam = defendingTeam;
                defendingTeam = temp;
            }

            currentState = (finalState == PhaseState.GOAL || finalState == PhaseState.OWN_GOAL)
                    ? PhaseState.KICKOFF
                    : finalState;
        }

        return stoppageSeconds;
    }

    private boolean isPossessionChange(PhaseState state) {
        return switch (state) {
            case DISPOSSESSION, GOAL_KICK, GOAL, OWN_GOAL -> true;
            default -> false;
        };
    }

    public void recordEvent(MatchEvent event) {
        validateNotFinished();
        events.add(event);

        if (event instanceof CardShown card && card.cardType() == CardType.RED) {
            homeOnField.remove(card.primaryPlayer());
            awayOnField.remove(card.primaryPlayer());
        }
    }

    private void validateNotFinished() {
        if (isFinished) {
            throw new IllegalStateException("Cannot modify a match that has already finished!");
        }
    }

    public void finishMatch() {
        if (isFinished) return;
        this.isFinished = true;
        // TODO: actualizăm clasamentul (puncte, gol-averaj) — probabil prin clasa Standings/Championship
    }

    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public Stadium getStadium() { return stadium; }
    public Referee getReferee() { return referee; }
    public boolean isFinished() { return isFinished; }
    public List<Player> getHomeLineup() { return homeLineup; }
    public List<Player> getAwayLineup() { return awayLineup; }
    public List<MatchEvent> getEvents() { return events; }
    public int getCurrentMinute() { return elapsedSeconds / SECONDS_PER_MINUTE; }
    public int getCurrentSecond() { return elapsedSeconds % SECONDS_PER_MINUTE; }
    public int getRegulationElapsedSeconds() { return regulationElapsedSeconds; }

    public int getHomeScore() { return countGoals(homeTeam); }
    public int getAwayScore() { return countGoals(awayTeam); }

    public int getHomeShotsTotal() { return countShots(homeTeam, false); }
    public int getHomeShotsOnTarget() { return countShots(homeTeam, true); }
    public int getHomeCorners() { return countEventsForTeam(Corner.class, homeTeam); }
    public int getHomeFouls() { return countEventsForTeam(Foul.class, homeTeam); }
    public int getHomeOffsides() { return countEventsForTeam(Offside.class, homeTeam); }
    public int getHomeYellowCards() { return countCards(homeTeam, CardType.YELLOW); }
    public int getHomeRedCards() { return countCards(homeTeam, CardType.RED); }
    public int getHomePenalties() { return countEventsForTeam(PenaltyAwarded.class, homeTeam); }

    public int getAwayShotsTotal() { return countShots(awayTeam, false); }
    public int getAwayShotsOnTarget() { return countShots(awayTeam, true); }
    public int getAwayCorners() { return countEventsForTeam(Corner.class, awayTeam); }
    public int getAwayFouls() { return countEventsForTeam(Foul.class, awayTeam); }
    public int getAwayOffsides() { return countEventsForTeam(Offside.class, awayTeam); }
    public int getAwayYellowCards() { return countCards(awayTeam, CardType.YELLOW); }
    public int getAwayRedCards() { return countCards(awayTeam, CardType.RED); }
    public int getAwayPenalties() { return countEventsForTeam(PenaltyAwarded.class, awayTeam); }

    private int countGoals(Team team) {
        int count = 0;
        for (MatchEvent event : events) {
            if ((event instanceof GoalScored goal && goal.team() == team)
                    || (event instanceof OwnGoal ownGoal && ownGoal.team() == team)) {
                count++;
            }
        }
        return count;
    }

    private int countShots(Team team, boolean onTargetOnly) {
        int count = 0;
        for (MatchEvent event : events) {
            if (event instanceof ShotAttempt shot && shot.team() == team && (!onTargetOnly || shot.isOnTarget())) {
                count++;
            }
        }
        return count;
    }

    private int countCards(Team team, CardType type) {
        int count = 0;
        for (MatchEvent event : events) {
            if (event instanceof CardShown card && card.team() == team && card.cardType() == type) {
                count++;
            }
        }
        return count;
    }

    private int countEventsForTeam(Class<? extends MatchEvent> type, Team team) {
        int count = 0;
        for (MatchEvent event : events) {
            if (type.isInstance(event) && event.team() == team) {
                count++;
            }
        }
        return count;
    }

    public List<Player> getOnFieldPlayers(Team team) {
        return (team == homeTeam) ? homeOnField : awayOnField;
    }
}
