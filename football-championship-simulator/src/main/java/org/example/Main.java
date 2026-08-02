package org.example;

import org.example.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Team teamA = createFakeTeam("Team A", 85);
        Team teamB = createFakeTeam("Team B", 80);

        PhaseSimulator simulator = new PhaseSimulator();

        int shootCount = 0;
        int foulCount = 0;
        int offsideCount = 0;
        int dispossessionCount = 0;
        int throwInCount = 0;
        int cornerCount = 0;
        int goalKickCount = 0;

        Match dummyMatch = createDummyMatch(teamA, teamB);

        for (int i = 0; i < 1000; i++) {
            PhaseState result = simulator.simulatePhase(PhaseState.ATTACK, teamA, teamB, dummyMatch, 0);
            switch (result) {
                case SHOOT -> shootCount++;
                case FOUL -> foulCount++;
                case OFFSIDE -> offsideCount++;
                case DISPOSSESSION -> dispossessionCount++;
                case THROW_IN_WON -> throwInCount++;
                case CORNER_WON -> cornerCount++;
                case GOAL_KICK_AWARDED -> goalKickCount++;
            }
        }

        System.out.println("Shoot: " + shootCount / 10.0 + "%");
        System.out.println("Foul: " + foulCount / 10.0 + "%");
        System.out.println("Offside: " + offsideCount / 10.0 + "%");
        System.out.println("Dispossession: " + dispossessionCount / 10.0 + "%");
        System.out.println("Throw-in: " + throwInCount / 10.0 + "%");
        System.out.println("Corner: " + cornerCount / 10.0 + "%");
        System.out.println("Goal kick: " + goalKickCount / 10.0 + "%");
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

    private static Match createDummyMatch(Team teamA, Team teamB) {
        Referee referee = new Referee("Ref", "Erat", "Romania", LocalDate.of(1980, 1, 1));
        return new Match(teamA, teamB, referee);
    }
}