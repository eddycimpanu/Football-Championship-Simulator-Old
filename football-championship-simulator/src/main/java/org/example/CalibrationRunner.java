package org.example;

import org.example.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Runs a batch of simulated matches between two evenly matched teams and prints how the engine's
 * output compares to real top-flight football. Read-only: does not change any simulation constant.
 */
public class CalibrationRunner {

    private static final int MATCH_COUNT = 1000;
    private static final long SEED = 42L;
    private static final int BALANCED_BASE_SKILL = 85;

    public static void main(String[] args) {
        Random random = new Random(SEED);
        Team teamA = createBalancedTeam("Team A", BALANCED_BASE_SKILL);
        Team teamB = createBalancedTeam("Team B", BALANCED_BASE_SKILL);
        Referee referee = new Referee("Ref", "Calibration", "Romania", LocalDate.of(1980, 1, 1));

        Stats goals = new Stats();
        Stats shots = new Stats();
        Stats shotsOnTarget = new Stats();
        Stats corners = new Stats();
        Stats fouls = new Stats();
        Stats yellows = new Stats();
        Stats reds = new Stats();
        Stats offsides = new Stats();
        Stats penalties = new Stats();
        Map<String, Integer> scoreCounts = new LinkedHashMap<>();
        int crashedMatches = 0;

        for (int i = 0; i < MATCH_COUNT; i++) {
            Match match = new Match(teamA, teamB, referee, random);
            try {
                match.simulateMatch();
            } catch (IllegalStateException e) {
                // Known pre-existing bug: a goalkeeper sent off with no substitution logic can leave a
                // team with no keeper on the field. Not fixed here — excluded from the sample, counted below.
                crashedMatches++;
                continue;
            }

            int homeScore = match.getHomeScore();
            int awayScore = match.getAwayScore();

            goals.add(homeScore + awayScore);
            shots.add(match.getHomeShotsTotal() + match.getAwayShotsTotal());
            shotsOnTarget.add(match.getHomeShotsOnTarget() + match.getAwayShotsOnTarget());
            corners.add(match.getHomeCorners() + match.getAwayCorners());
            fouls.add(match.getHomeFouls() + match.getAwayFouls());
            yellows.add(match.getHomeYellowCards() + match.getAwayYellowCards());
            reds.add(match.getHomeRedCards() + match.getAwayRedCards());
            offsides.add(match.getHomeOffsides() + match.getAwayOffsides());
            penalties.add(match.getHomePenalties() + match.getAwayPenalties());

            scoreCounts.merge(homeScore + "-" + awayScore, 1, Integer::sum);
        }

        int completedMatches = MATCH_COUNT - crashedMatches;

        System.out.println("Calibrare: " + MATCH_COUNT + " meciuri cerute, seed=" + SEED
                + ", ambele echipe la skill de baza " + BALANCED_BASE_SKILL + " (pe post, cf. applyPositionSkills).");
        System.out.println("Referintele sunt valori tipice de liga de top (medii pe meci, ambele echipe combinate) -");
        System.out.println("goluri si cornere verificate live; restul sunt repere larg citate in analitica fotbalistica.");
        if (crashedMatches > 0) {
            System.out.printf("ATENTIE: %d din %d meciuri (%.1f%%) au esuat cu IllegalStateException (\"No goalkeeper "
                    + "found on the field\") - bug preexistent, rosu la portar fara substitutie. Excluse din statistici;%n"
                    + "media/dev.std de mai jos sunt pe cele %d meciuri finalizate.%n",
                    crashedMatches, MATCH_COUNT, crashedMatches * 100.0 / MATCH_COUNT, completedMatches);
        }
        System.out.println();

        printTable(goals, shots, shotsOnTarget, corners, fouls, yellows, reds, offsides, penalties);
        System.out.println();
        printScoreDistribution(scoreCounts, completedMatches);
    }

    private record ReferenceRow(String label, Stats stats, double reference) {
    }

    private static void printTable(Stats goals, Stats shots, Stats shotsOnTarget, Stats corners,
                                    Stats fouls, Stats yellows, Stats reds, Stats offsides, Stats penalties) {
        List<ReferenceRow> rows = List.of(
                new ReferenceRow("Goluri/meci", goals, 2.75),
                new ReferenceRow("Suturi/meci", shots, 25.0),
                new ReferenceRow("Suturi pe poarta/meci", shotsOnTarget, 9.5),
                new ReferenceRow("Cornere/meci", corners, 10.0),
                new ReferenceRow("Fauluri/meci", fouls, 21.0),
                new ReferenceRow("Cartonase galbene/meci", yellows, 3.5),
                new ReferenceRow("Cartonase rosii/meci", reds, 0.18),
                new ReferenceRow("Ofsaiduri/meci", offsides, 3.3),
                new ReferenceRow("Penalty-uri/meci", penalties, 0.27)
        );

        System.out.printf("%-24s%10s%10s%12s%12s%n", "Metrica", "Medie", "Dev.std", "Referinta", "Diferenta");
        System.out.println("-".repeat(68));
        for (ReferenceRow row : rows) {
            double mean = row.stats().mean();
            double stdDev = row.stats().stdDev();
            double diffPercent = (mean - row.reference()) / row.reference() * 100;
            System.out.printf("%-24s%10.2f%10.2f%12.2f%11.1f%%%n",
                    row.label(), mean, stdDev, row.reference(), diffPercent);
        }
    }

    private static void printScoreDistribution(Map<String, Integer> scoreCounts, int totalMatches) {
        System.out.println("Distributia scorurilor finale (top 15 din " + scoreCounts.size() + " distincte):");
        scoreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(15)
                .forEach(entry -> {
                    double percent = entry.getValue() * 100.0 / totalMatches;
                    System.out.printf("  %-6s %4d meciuri (%5.1f%%)%n", entry.getKey(), entry.getValue(), percent);
                });
    }

    private static Team createBalancedTeam(String name, int baseSkill) {
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

    private static final class Stats {
        private final List<Integer> values = new ArrayList<>();

        void add(int value) {
            values.add(value);
        }

        double mean() {
            return values.stream().mapToInt(Integer::intValue).average().orElse(Double.NaN);
        }

        double stdDev() {
            double mean = mean();
            double variance = values.stream()
                    .mapToDouble(v -> (v - mean) * (v - mean))
                    .average()
                    .orElse(Double.NaN);
            return Math.sqrt(variance);
        }
    }
}
