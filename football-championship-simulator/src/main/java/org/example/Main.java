package org.example;

import org.example.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Team teamA = createFakeTeam("Team A", 90);
        Team teamB = createFakeTeam("Team B", 88);
        Referee referee = new Referee("Ref", "Erat", "Romania", LocalDate.of(1980, 1, 1));

        Match match = new Match(teamA, teamB, referee, new Random());
        match.simulateMatch();

        System.out.println("Final score: " + teamA.getName() + " " + match.getHomeScore()
                + " - " + match.getAwayScore() + " " + teamB.getName());
        System.out.println("Match ended at minute: " + match.getCurrentMinute());
        System.out.println("Total events: " + match.getEvents().size());

        for (MatchEvent event : match.getEvents()) {
            System.out.println(event.describe());
        }
    }

    private static Team createFakeTeam(String name, int baseSkill) {
        Stadium stadium = new Stadium(name + " Arena", "City", 40000, 2000);
        Coach coach = new Coach("Coach", name, "Romania", LocalDate.of(1975, 1, 1));
        Team team = new Team(name, "Romania", 1950, stadium, BigDecimal.valueOf(10_000_000), coach);

        Position[] positions = {
                Position.GOALKEEPER,
                Position.DEFENDER, Position.DEFENDER, Position.DEFENDER, Position.DEFENDER,
                Position.MIDFIELDER, Position.MIDFIELDER, Position.MIDFIELDER,
                Position.ATTACKER, Position.ATTACKER, Position.ATTACKER
        };

        for (int i = 0; i < 11; i++) {
            Player player = new Player("Player", "" + (i + 1), "Romania",
                    LocalDate.of(2000, 1, 1), positions[i], i + 1);
            applyPositionSkills(player, positions[i], baseSkill);
            player.transferTo(team);
        }

        for (int i = 1; i <= 11; i++) {
            team.addStarter(i);
        }

        return team;
    }

    private static void applyPositionSkills(Player player, Position position, int baseSkill) {
        switch (position) {
            case GOALKEEPER -> {
                player.setShooting(clampSkill(baseSkill - 40));
                player.setPassing(clampSkill(baseSkill - 20));
                player.setDefending(clampSkill(baseSkill - 10));
                player.setGoalkeeping(clampSkill(baseSkill + 5));
            }
            case DEFENDER -> {
                player.setShooting(clampSkill(baseSkill - 30));
                player.setPassing(clampSkill(baseSkill - 5));
                player.setDefending(clampSkill(baseSkill + 8));
                player.setGoalkeeping(clampSkill(baseSkill - 60));
            }
            case MIDFIELDER -> {
                player.setShooting(clampSkill(baseSkill - 10));
                player.setPassing(clampSkill(baseSkill + 8));
                player.setDefending(clampSkill(baseSkill - 5));
                player.setGoalkeeping(clampSkill(baseSkill - 60));
            }
            case ATTACKER -> {
                player.setShooting(clampSkill(baseSkill + 8));
                player.setPassing(clampSkill(baseSkill - 5));
                player.setDefending(clampSkill(baseSkill - 30));
                player.setGoalkeeping(clampSkill(baseSkill - 60));
            }
        }
    }

    private static int clampSkill(int value) {
        return Math.max(1, Math.min(99, value));
    }
}