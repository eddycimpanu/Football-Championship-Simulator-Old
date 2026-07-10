package org.example.domain;

import java.time.LocalDate;

public class Coach extends Person {

    private Team currentTeam;

    public Coach(String firstName, String lastName, String nationality, LocalDate dateOfBirth) {
        super(firstName, lastName, nationality, dateOfBirth);
    }

    public Team getCurrentTeam() { return currentTeam; }
    public void setCurrentTeam(Team team) { this.currentTeam = team; }
}