package org.example.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchDeterminismTest {

    @Test
    void sameSeedProducesIdenticalScoreAndEventSequence() {
        MatchResult first = simulateWithSeed(42L);
        MatchResult second = simulateWithSeed(42L);

        assertEquals(first.homeScore, second.homeScore);
        assertEquals(first.awayScore, second.awayScore);
        assertEquals(first.eventDescriptions, second.eventDescriptions);
    }

    private MatchResult simulateWithSeed(long seed) {
        Team teamA = MatchSmokeTest.createFakeTeam("Team A", 90);
        Team teamB = MatchSmokeTest.createFakeTeam("Team B", 88);
        Referee referee = new Referee("Ref", "Erat", "Romania", LocalDate.of(1980, 1, 1));

        Match match = new Match(teamA, teamB, referee, new Random(seed));
        match.simulateMatch();

        List<String> eventDescriptions = match.getEvents().stream()
                .map(MatchEvent::describe)
                .collect(Collectors.toList());

        return new MatchResult(match.getHomeScore(), match.getAwayScore(), eventDescriptions);
    }

    private record MatchResult(int homeScore, int awayScore, List<String> eventDescriptions) {
    }
}
