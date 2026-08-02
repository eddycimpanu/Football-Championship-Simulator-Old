package org.example.domain;

import java.math.BigDecimal;
import java.util.*;

public class Team {

    private static final int STARTING_LINEUP_SIZE = 11;

    private final UUID id;
    private String name;
    private String country;
    private int foundedYear;
    private Stadium stadium;
    private BigDecimal transferBudget;
    private Coach coach;
    private final Set<Player> currentSquad;
    private List<Player> startingLineUp;

    private Player penaltyTaker;
    private Player cornerTaker;
    private Player freeKickTaker;

    public Team(String name, String country, int foundedYear, Stadium stadium, BigDecimal transferBudget, Coach coach) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.country = country;
        this.foundedYear = foundedYear;
        this.stadium = stadium;
        this.transferBudget = transferBudget;
        this.coach = coach;
        this.currentSquad = new HashSet<>();
        this.startingLineUp = new ArrayList<>();
    }

    void addPlayer(Player player) {
        if (findPlayerByKitNumber(player.getKitNumber()) != null) {
            throw new IllegalArgumentException(
                    "Kit number " + player.getKitNumber() + " is already taken in " + name);
        }
        currentSquad.add(player);
    }

    void removePlayer(Player player) {
        currentSquad.remove(player);
        startingLineUp.remove(player);
    }

    public void addStarter(int kitNumber) {
        Player player = getPlayerByKitNumberOrThrow(kitNumber);
        if (startingLineUp.contains(player)) {
            throw new IllegalArgumentException(player.getFullName() + " is already in the starting lineup");
        }
        if (startingLineUp.size() >= STARTING_LINEUP_SIZE) {
            throw new IllegalStateException("Starting lineup is already full");
        }
        startingLineUp.add(player);
    }

    public void removeStarter(int kitNumber) {
        Player player = getPlayerByKitNumberOrThrow(kitNumber);
        if (!startingLineUp.remove(player)) {
            throw new IllegalArgumentException(player.getFullName() + " is not in the starting lineup");
        }
    }

    public void replaceStarter(int outKitNumber, int inKitNumber) {
        Player playerOut = getPlayerByKitNumberOrThrow(outKitNumber);
        Player playerIn = getPlayerByKitNumberOrThrow(inKitNumber);

        if (!startingLineUp.contains(playerOut)) {
            throw new IllegalArgumentException(playerOut.getFullName() + " is not currently a starter");
        }
        if (startingLineUp.contains(playerIn)) {
            throw new IllegalArgumentException(playerIn.getFullName() + " is already a starter");
        }

        startingLineUp.remove(playerOut);
        startingLineUp.add(playerIn);
    }

    public boolean isValidLineup() {
        if (startingLineUp.size() != STARTING_LINEUP_SIZE) {
            return false;
        }
        Set<Player> uniqueCheck = new HashSet<>(startingLineUp);
        return uniqueCheck.size() == STARTING_LINEUP_SIZE;
    }

    private Player getPlayerByKitNumberOrThrow(int kitNumber) {
        Player player = findPlayerByKitNumber(kitNumber);
        if (player == null) {
            throw new IllegalArgumentException("No player with kit number " + kitNumber + " in " + name);
        }
        return player;
    }

    private Player findPlayerByKitNumber(int kitNumber) {
        for (Player player : currentSquad) {
            if (player.getKitNumber() == kitNumber) {
                return player;
            }
        }
        return null;
    }

    public boolean isKitNumberTaken(int kitNumber) {
        return findPlayerByKitNumber(kitNumber) != null;
    }

    public int findFreeKitNumber() {
        for (int number = 1; number <= 99; number++) {
            if (!isKitNumberTaken(number)) {
                return number;
            }
        }
        throw new IllegalStateException("No free kit numbers available in " + name);
    }

    public Player getPenaltyTaker() {
        if (penaltyTaker == null || !currentSquad.contains(penaltyTaker)) {
            penaltyTaker = findDefaultTaker("shooting");
        }
        return penaltyTaker;
    }

    public void setPenaltyTaker(Player player) {
        validateTakerInSquad(player);
        this.penaltyTaker = player;
    }

    public Player getCornerTaker() {
        if (cornerTaker == null || !currentSquad.contains(cornerTaker)) {
            cornerTaker = findDefaultTaker("passing");
        }
        return cornerTaker;
    }

    public void setCornerTaker(Player player) {
        validateTakerInSquad(player);
        this.cornerTaker = player;
    }

    public Player getFreeKickTaker() {
        if (freeKickTaker == null || !currentSquad.contains(freeKickTaker)) {
            freeKickTaker = findDefaultTaker("passing");
        }
        return freeKickTaker;
    }

    public void setFreeKickTaker(Player player) {
        validateTakerInSquad(player);
        this.freeKickTaker = player;
    }

    private void validateTakerInSquad(Player player) {
        if (!currentSquad.contains(player)) {
            throw new IllegalArgumentException(player.getFullName() + " is not part of " + name + "'s squad");
        }
    }

    private Player findDefaultTaker(String attribute) {
        Player best = null;
        int bestValue = -1;

        for (Player player : startingLineUp) {
            if (player.getPosition() == Position.GOALKEEPER) continue;

            int value = switch (attribute) {
                case "shooting" -> player.getShooting();
                case "passing" -> player.getPassing();
                default -> 0;
            };

            if (value > bestValue) {
                best = player;
                bestValue = value;
            }
        }

        if (best == null && !startingLineUp.isEmpty()) {
            best = startingLineUp.get(0);
        }

        return best;
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
    public List<Player> getStartingLineUp() { return startingLineUp; }
}