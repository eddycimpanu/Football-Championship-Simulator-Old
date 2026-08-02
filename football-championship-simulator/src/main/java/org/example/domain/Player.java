package org.example.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Player extends Person {

    private Position position;
    private int kitNumber;
    private Team currentTeam;
    private final List<PlayerSeasonStats> historyStats;

    private int shooting;
    private int passing;
    private int defending;
    private int goalkeeping;

    public Player(String firstName, String lastName, String nationality, LocalDate dateOfBirth, Position position, int kitNumber) {
        super(firstName, lastName, nationality, dateOfBirth);
        this.position = position;
        this.kitNumber = kitNumber;
        this.historyStats = new ArrayList<>();
    }

    public void transferTo(Team newTeam) {
        if (this.currentTeam != null) {
            this.currentTeam.removePlayer(this);
        }
        if (newTeam.isKitNumberTaken(this.kitNumber)) {
            int freeNumber = newTeam.findFreeKitNumber();
            this.setKitNumber(freeNumber);
        }
        newTeam.addPlayer(this);
        this.currentTeam = newTeam;
    }

    public void addSeasonStats(PlayerSeasonStats stats) {
        historyStats.add(stats);
    }

    public Position getPosition() {
        return position;
    }

    public int getKitNumber() {
        return kitNumber;
    }

    public void setKitNumber(int kitNumber) {
        if(this.currentTeam.isKitNumberTaken(kitNumber) == false) {
            this.kitNumber = kitNumber;
        }
        else throw new IllegalStateException("The kit number "+ kitNumber +" is already used");
    }

    public Team getCurrentTeam() {
        return currentTeam;
    }

    public List<PlayerSeasonStats> getHistoryStats() {
        return historyStats;
    }

    public int getShooting() { return shooting; }
    public void setShooting(int shooting) { this.shooting = shooting; }

    public int getPassing() { return passing; }
    public void setPassing(int passing) { this.passing = passing; }

    public int getDefending() { return defending; }
    public void setDefending(int defending) { this.defending = defending; }

    public int getGoalkeeping() { return goalkeeping; }
    public void setGoalkeeping(int goalkeeping) { this.goalkeeping = goalkeeping; }
}