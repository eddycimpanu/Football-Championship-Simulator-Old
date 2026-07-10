package org.example.domain;

import java.util.UUID;

public class Stadium {

    private final UUID id;
    private String name;
    private String city;
    private int capacity;
    private int yearBuilt;

    public Stadium(String name, String city, int capacity, int yearBuilt) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.city = city;
        this.capacity = capacity;
        this.yearBuilt = yearBuilt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public int getCapacity() { return capacity; }
    public int getYearBuilt() { return yearBuilt; }
}