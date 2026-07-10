package org.example.domain;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.UUID;

public abstract class Person {
    private final UUID id;
    private String firstName;
    private String lastName;
    private String nationality;
    private LocalDate dateOfBirth;

    protected Person(String firstName, String lastName, String nationality, LocalDate dateOfBirth) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public UUID getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getNationality() { return nationality; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return id.equals(person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
