package org.example.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsAggregatorTest {

    @Test
    void aggregatedStatsMatchMatchResult() {
        Team teamA = MatchSmokeTest.createFakeTeam("Team A", 90);
        Team teamB = MatchSmokeTest.createFakeTeam("Team B", 88);
        Referee referee = new Referee("Ref", "Erat", "Romania", LocalDate.of(1980, 1, 1));

        Match match = new Match(teamA, teamB, referee, new Random(7L));
        match.simulateMatch();

        Map<Player, PlayerSeasonStats> statsByPlayer = StatsAggregator.aggregate(match.getEvents(), 2026);

        int goalsFromStats = 0;
        int ownGoalsFromStats = 0;
        int shotsTotalFromStats = 0;
        int shotsOnTargetFromStats = 0;

        for (PlayerSeasonStats stats : statsByPlayer.values()) {
            goalsFromStats += stats.getGoals();
            ownGoalsFromStats += stats.getOwnGoals();
            shotsTotalFromStats += stats.getShotsTotal();
            shotsOnTargetFromStats += stats.getShotsOnTarget();
        }

        int finalScore = match.getHomeScore() + match.getAwayScore();

        assertEquals(finalScore, goalsFromStats + ownGoalsFromStats);
        assertTrue(shotsOnTargetFromStats <= shotsTotalFromStats);
    }
}
