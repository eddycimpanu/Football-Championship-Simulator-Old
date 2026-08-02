package org.example.domain;

import java.time.LocalDate;

public class Referee extends Person {

    public Referee(String firstName, String lastName, String nationality, LocalDate dateOfBirth) {
        super(firstName, lastName, nationality, dateOfBirth);
    }
}