package org.example.domain;

import java.util.UUID;

public class PlayerSeasonStats {

    private final UUID id;
    private final Player player;
    private final Team team;
    private final int season;

    // General
    private int appearances;
    private int minutesPlayed;
    private int starts;
    private int substituteAppearances;

    // Attack
    private int goals;
    private int assists;
    private int shotsTotal;
    private int shotsOnTarget;
    private int penaltiesScored;
    private int penaltiesMissed;
    private int dribblesCompleted;

    // Passing
    private int passesAttempted;
    private int passesCompleted;
    private int keyPasses;

    // Defense
    private int tacklesWon;
    private int interceptions;
    private int clearances;
    private int aerialDuelsWon;
    private int ownGoals;

    // Discipline
    private int yellowCards;
    private int redCards;
    private int foulsCommitted;
    private int foulsSuffered;
    private int offsides;

    // Goalkeeper-specific
    private int saves;
    private int cleanSheets;
    private int goalsConceded;

    public PlayerSeasonStats(Player player, Team team, int season) {
        this.id = UUID.randomUUID();
        this.player = player;
        this.team = team;
        this.season = season;
    }

    // --- General ---
    public void recordAppearance(int minutes, boolean wasStarter) {
        this.appearances++;
        this.minutesPlayed += minutes;
        if (wasStarter) {
            this.starts++;
        } else {
            this.substituteAppearances++;
        }
    }

    // --- Attack ---
    public void addGoal() { this.goals++; }
    public void addAssist() { this.assists++; }
    public void addShot(boolean onTarget) {
        this.shotsTotal++;
        if (onTarget) this.shotsOnTarget++;
    }
    public void addPenaltyScored() { this.penaltiesScored++; }
    public void addPenaltyMissed() { this.penaltiesMissed++; }
    public void addDribbleCompleted() { this.dribblesCompleted++; }

    // --- Passing ---
    public void addPass(boolean completed) {
        this.passesAttempted++;
        if (completed) this.passesCompleted++;
    }
    public void addKeyPass() { this.keyPasses++; }

    // --- Defense ---
    public void addTackleWon() { this.tacklesWon++; }
    public void addInterception() { this.interceptions++; }
    public void addClearance() { this.clearances++; }
    public void addAerialDuelWon() { this.aerialDuelsWon++; }
    public void addOwnGoal() { this.ownGoals++; }

    // --- Discipline ---
    public void addYellowCard() { this.yellowCards++; }
    public void addRedCard() { this.redCards++; }
    public void addFoulCommitted() { this.foulsCommitted++; }
    public void addFoulSuffered() { this.foulsSuffered++; }
    public void addOffside() { this.offsides++; }

    // --- Goalkeeper ---
    public void addSave() { this.saves++; }
    public void addCleanSheet() { this.cleanSheets++; }
    public void addGoalsConceded(int goals) { this.goalsConceded += goals; }

    // --- Derived stats ---
    public double getPassAccuracy() {
        return passesAttempted == 0 ? 0.0 : (double) passesCompleted / passesAttempted * 100;
    }

    public double getShotAccuracy() {
        return shotsTotal == 0 ? 0.0 : (double) shotsOnTarget / shotsTotal * 100;
    }

    public double getGoalsPerMatch() {
        return appearances == 0 ? 0.0 : (double) goals / appearances;
    }

    // --- Getters ---
    public UUID getId() { return id; }
    public Player getPlayer() { return player; }
    public Team getTeam() { return team; }
    public int getSeason() { return season; }
    public int getAppearances() { return appearances; }
    public int getMinutesPlayed() { return minutesPlayed; }
    public int getStarts() { return starts; }
    public int getSubstituteAppearances() { return substituteAppearances; }
    public int getGoals() { return goals; }
    public int getAssists() { return assists; }
    public int getShotsTotal() { return shotsTotal; }
    public int getShotsOnTarget() { return shotsOnTarget; }
    public int getPenaltiesScored() { return penaltiesScored; }
    public int getPenaltiesMissed() { return penaltiesMissed; }
    public int getDribblesCompleted() { return dribblesCompleted; }
    public int getPassesAttempted() { return passesAttempted; }
    public int getPassesCompleted() { return passesCompleted; }
    public int getKeyPasses() { return keyPasses; }
    public int getTacklesWon() { return tacklesWon; }
    public int getInterceptions() { return interceptions; }
    public int getClearances() { return clearances; }
    public int getAerialDuelsWon() { return aerialDuelsWon; }
    public int getOwnGoals() { return ownGoals; }
    public int getYellowCards() { return yellowCards; }
    public int getRedCards() { return redCards; }
    public int getFoulsCommitted() { return foulsCommitted; }
    public int getFoulsSuffered() { return foulsSuffered; }
    public int getOffsides() { return offsides; }
    public int getSaves() { return saves; }
    public int getCleanSheets() { return cleanSheets; }
    public int getGoalsConceded() { return goalsConceded; }
}