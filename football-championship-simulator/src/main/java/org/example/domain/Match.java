package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public class Match {

    private int currentMinute;

    private final Team homeTeam;
    private final Team awayTeam;
    private final Stadium stadium;
    private final Referee referee;

    private final List<Player> homeLineup;
    private final List<Player> awayLineup;
    private final List<Player> homeOnField;
    private final List<Player> awayOnField;

    private final List<MatchEvent> events;

    private int homeScore;
    private int awayScore;
    private boolean isFinished;

    // statistici home
    private int homeShotsTotal;
    private int homeShotsOnTarget;
    private int homeCorners;
    private int homeFouls;
    private int homeOffsides;
    private int homeYellowCards;
    private int homeRedCards;

    // statistici away
    private int awayShotsTotal;
    private int awayShotsOnTarget;
    private int awayCorners;
    private int awayFouls;
    private int awayOffsides;
    private int awayYellowCards;
    private int awayRedCards;

    public Match(Team homeTeam, Team awayTeam, Referee referee) {
        if (!homeTeam.isValidLineup()) {
            throw new IllegalStateException("Home team " + homeTeam.getName() + " does not have a valid starting lineup!");
        }
        if (!awayTeam.isValidLineup()) {
            throw new IllegalStateException("Away team " + awayTeam.getName() + " does not have a valid starting lineup!");
        }

        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.referee = referee;
        this.stadium = homeTeam.getStadium();

        this.homeLineup = new ArrayList<>(homeTeam.getStartingLineUp());
        this.awayLineup = new ArrayList<>(awayTeam.getStartingLineUp());
        this.homeOnField = new ArrayList<>(homeLineup);
        this.awayOnField = new ArrayList<>(awayLineup);

        this.events = new ArrayList<>();
        this.homeScore = 0;
        this.awayScore = 0;
        this.isFinished = false;
    }

    public void simulateMatch() {
        PhaseState currentState = PhaseState.KICKOFF;
        Team attackingTeam = homeTeam;
        Team defendingTeam = awayTeam;
        this.currentMinute = 0;
        double stoppageTime = 0;
        PhaseSimulator phaseSimulator = new PhaseSimulator();

        while (currentMinute < 90) {
            PhaseState finalState = phaseSimulator.simulatePhase(currentState, attackingTeam, defendingTeam, this);
            int duration = phaseSimulator.decidePhaseDuration(finalState);

            currentMinute += duration;
            stoppageTime += calculateStoppageContribution(finalState, duration);

            if (isPossessionChange(finalState)) {
                Team temp = attackingTeam;
                attackingTeam = defendingTeam;
                defendingTeam = temp;
            }

            currentState = (finalState == PhaseState.GOAL || finalState == PhaseState.OWN_GOAL)
                    ? PhaseState.KICKOFF
                    : finalState;
        }

        int allowedStoppage = (int) Math.round(stoppageTime);
        int stoppageEndMinute = 90 + allowedStoppage;

        while (currentMinute < stoppageEndMinute) {
            PhaseState finalState = phaseSimulator.simulatePhase(currentState, attackingTeam, defendingTeam, this);
            int duration = phaseSimulator.decidePhaseDuration(finalState);

            currentMinute += duration;

            if (isPossessionChange(finalState)) {
                Team temp = attackingTeam;
                attackingTeam = defendingTeam;
                defendingTeam = temp;
            }

            currentState = (finalState == PhaseState.GOAL || finalState == PhaseState.OWN_GOAL)
                    ? PhaseState.KICKOFF
                    : finalState;
        }

        finishMatch();
    }

    private double calculateStoppageContribution(PhaseState finalState, int duration) {
        double contribution = 0;

        if (duration > 1) {
            contribution += (duration - 1);
        }

        switch (finalState) {
            case GOAL:
            case OWN_GOAL:
                contribution += 0.5;
                break;
            default:
                break;
        }

        return contribution;
    }

    private boolean isPossessionChange(PhaseState state) {
        return switch (state) {
            case DISPOSSESSION, GOAL_KICK, GOAL, OWN_GOAL -> true;
            default -> false;
        };
    }

    public void addOwnGoal(Player scorer) {
        validateNotFinished();

        Team scoringTeam;
        if (homeOnField.contains(scorer)) {
            awayScore++;
            scoringTeam = awayTeam;
        } else if (awayOnField.contains(scorer)) {
            homeScore++;
            scoringTeam = homeTeam;
        } else {
            throw new IllegalArgumentException(scorer.getFullName() + " is not currently on the field!");
        }

        events.add(new Goal(currentMinute, scoringTeam, scorer, null, true));
    }

    public void addGoal(Player scorer, Player assistant) {
        validateNotFinished();

        Team scoringTeam;
        if (homeOnField.contains(scorer)) {
            homeScore++;
            scoringTeam = homeTeam;
        } else if (awayOnField.contains(scorer)) {
            awayScore++;
            scoringTeam = awayTeam;
        } else {
            throw new IllegalArgumentException(scorer.getFullName() + " is not currently on the field!");
        }

        events.add(new Goal(currentMinute, scoringTeam, scorer, assistant));
    }

    public void addGoal(Player scorer) {
        addGoal(scorer, null);
    }

    public void addCard(Person offender, CardType type) {
        validateNotFinished();

        boolean isHomeOffender = offender instanceof Player && homeOnField.contains(offender);
        boolean isAwayOffender = offender instanceof Player && awayOnField.contains(offender);
        Team offenderTeam = isHomeOffender ? homeTeam : (isAwayOffender ? awayTeam : null);

        events.add(new Card(currentMinute, offenderTeam, (Player) offender, type));

        if (type == CardType.YELLOW) {
            if (isHomeOffender) homeYellowCards++;
            else if (isAwayOffender) awayYellowCards++;
        } else if (type == CardType.RED) {
            if (isHomeOffender) homeRedCards++;
            else if (isAwayOffender) awayRedCards++;

            homeOnField.remove(offender);
            awayOnField.remove(offender);
        }
    }

    public void recordFoul(Team team) {
        if (team == homeTeam) homeFouls++;
        else awayFouls++;
    }

    public void recordCorner(Team team) {
        if (team == homeTeam) homeCorners++;
        else awayCorners++;
    }

    public void recordOffside(Team team) {
        if (team == homeTeam) homeOffsides++;
        else awayOffsides++;
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
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    public boolean isFinished() { return isFinished; }
    public List<Player> getHomeLineup() { return homeLineup; }
    public List<Player> getAwayLineup() { return awayLineup; }
    public List<MatchEvent> getEvents() { return events; }
    public int getCurrentMinute() { return currentMinute; }

    public int getHomeShotsTotal() { return homeShotsTotal; }
    public int getHomeShotsOnTarget() { return homeShotsOnTarget; }
    public int getHomeCorners() { return homeCorners; }
    public int getHomeFouls() { return homeFouls; }
    public int getHomeOffsides() { return homeOffsides; }
    public int getHomeYellowCards() { return homeYellowCards; }
    public int getHomeRedCards() { return homeRedCards; }

    public int getAwayShotsTotal() { return awayShotsTotal; }
    public int getAwayShotsOnTarget() { return awayShotsOnTarget; }
    public int getAwayCorners() { return awayCorners; }
    public int getAwayFouls() { return awayFouls; }
    public int getAwayOffsides() { return awayOffsides; }
    public int getAwayYellowCards() { return awayYellowCards; }
    public int getAwayRedCards() { return awayRedCards; }

    public List<Player> getOnFieldPlayers(Team team) {
        return (team == homeTeam) ? homeOnField : awayOnField;
    }
}