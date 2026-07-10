package org.example.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Player extends Person {

    private Position position;
    private int kitNumber;
    private Team currentTeam;
    private final List<PlayerSeasonStats> historyStats;

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
        this.kitNumber = kitNumber;
    }

    public Team getCurrentTeam() {
        return currentTeam;
    }

    public List<PlayerSeasonStats> getHistoryStats() {
        return historyStats;
    }
}