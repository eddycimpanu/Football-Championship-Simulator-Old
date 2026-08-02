package org.example;

import org.example.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Team teamA = createFakeTeam("Team A", 90);
        Team teamB = createFakeTeam("Team B", 5);
        Referee referee = new Referee("Ref", "Erat", "Romania", LocalDate.of(1980, 1, 1));

        Match match = new Match(teamA, teamB, referee);
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
            player.setShooting(baseSkill);
            player.setPassing(baseSkill);
            player.setDefending(baseSkill);
            player.setGoalkeeping(baseSkill);
            player.transferTo(team);
        }

        for (int i = 1; i <= 11; i++) {
            team.addStarter(i);
        }

        return team;
    }
}