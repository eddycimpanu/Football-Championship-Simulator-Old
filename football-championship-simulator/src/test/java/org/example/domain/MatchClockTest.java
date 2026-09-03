package org.example.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchClockTest {

    private static final int REGULATION_SECONDS = 90 * 60;

    @Test
    void regulationPeriodDurationSumsToAboutNinetyMinutes() {
        for (long seed : new long[]{1L, 7L, 42L, 100L, 2024L}) {
            Team teamA = MatchSmokeTest.createFakeTeam("Team A", 90);
            Team teamB = MatchSmokeTest.createFakeTeam("Team B", 88);
            Referee referee = new Referee("Ref", "Erat", "Romania", LocalDate.of(1980, 1, 1));

            Match match = new Match(teamA, teamB, referee, new Random(seed));
            match.simulateMatch();

            int regulationSeconds = match.getRegulationElapsedSeconds();

            // The loop only exits once elapsed time reaches REGULATION_SECONDS, so it can't undershoot.
            // The only slack is whichever single phase pushed it past the line, so bound the overshoot
            // by the most expensive possible phase: a corner that cascades into a headed goal.
            int maxSinglePhaseSeconds = SimulationModel.BASE_PHASE_SECONDS
                    + SimulationModel.PER_SHOT_SECONDS
                    + SimulationModel.CORNER_DEAD_TIME_SECONDS
                    + SimulationModel.GOAL_DEAD_TIME_SECONDS;

            assertTrue(regulationSeconds >= REGULATION_SECONDS,
                    "seed=" + seed + ": regulation time undershot 90 minutes: " + regulationSeconds + "s");
            assertTrue(regulationSeconds <= REGULATION_SECONDS + maxSinglePhaseSeconds,
                    "seed=" + seed + ": regulation time overshot by more than one phase: " + regulationSeconds + "s");
        }
    }
}
