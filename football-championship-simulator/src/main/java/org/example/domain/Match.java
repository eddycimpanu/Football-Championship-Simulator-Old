package org.example.domain;

import java.util.ArrayList;
import java.util.List;

public class Match {

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

    }

    p
    public void addGoal(int minute, Player scorer, Player assistant) {
        validateNotFinished();

        if (homeOnField.contains(scorer)) {
            homeScore++;
        } else if (awayOnField.contains(scorer)) {
            awayScore++;
        } else {
            throw new IllegalArgumentException(scorer.getFullName() + " is not currently on the field!");
        }

        events.add(new Goal(minute, scorer, assistant));

        // TODO: actualizăm PlayerSeasonStats pentru scorer (addGoal) și assistant (addAssist),
        // odată ce avem legătura Match -> Season
    }

    public void addGoal(int minute, Player scorer) {
        addGoal(minute, scorer, null);
    }

    public void addCard(int minute, Person offender, CardType type) {
        validateNotFinished();

        events.add(new Card(minute, offender, type));

        if (type == CardType.RED) {
            homeOnField.remove(offender);
            awayOnField.remove(offender);
        }

        // TODO: al doilea galben -> roșu automat
        // TODO: actualizăm PlayerSeasonStats (addYellowCard / addRedCard)
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
}