package org.example.domain;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team {

    private final UUID id;
    private String name;
    private String country;
    private int foundedYear;
    private Stadium stadium;
    private BigDecimal transferBudget;
    private Coach coach;
    private final Set<Player> currentSquad;

    public Team(String name, String country, int foundedYear, Stadium stadium, BigDecimal transferBudget, Coach coach) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.country = country;
        this.foundedYear = foundedYear;
        this.stadium = stadium;
        this.transferBudget = transferBudget;
        this.coach = coach;
        this.currentSquad = new HashSet<>();
    }

    void addPlayer(Player player) {
        currentSquad.add(player);
    }

    void removePlayer(Player player) {
        currentSquad.remove(player);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public int getFoundedYear() { return foundedYear; }
    public Stadium getStadium() { return stadium; }
    public BigDecimal getTransferBudget() { return transferBudget; }
    public Coach getCoach() { return coach; }
    public void setCoach(Coach coach) { this.coach = coach; }
    public Set<Player> getCurrentSquad() { return currentSquad; }
}