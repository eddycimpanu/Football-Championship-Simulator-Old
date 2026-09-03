package org.example.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StatsAggregator {

    private StatsAggregator() {
    }

    public static Map<Player, PlayerSeasonStats> aggregate(List<MatchEvent> events, int season) {
        Map<Player, PlayerSeasonStats> statsByPlayer = new LinkedHashMap<>();

        for (MatchEvent event : events) {
            switch (event) {
                case ShotAttempt shot ->
                        statsFor(statsByPlayer, shot.primaryPlayer(), season).addShot(shot.isOnTarget());
                case GoalScored goal -> {
                    statsFor(statsByPlayer, goal.primaryPlayer(), season).addGoal();
                    if (goal.secondaryPlayer() != null) {
                        statsFor(statsByPlayer, goal.secondaryPlayer(), season).addAssist();
                    }
                }
                case OwnGoal ownGoal -> statsFor(statsByPlayer, ownGoal.primaryPlayer(), season).addOwnGoal();
                case CardShown card -> {
                    PlayerSeasonStats stats = statsFor(statsByPlayer, card.primaryPlayer(), season);
                    if (card.cardType() == CardType.YELLOW) {
                        stats.addYellowCard();
                    } else {
                        stats.addRedCard();
                    }
                }
                case Foul foul -> {
                    if (foul.primaryPlayer() != null) {
                        statsFor(statsByPlayer, foul.primaryPlayer(), season).addFoulCommitted();
                    }
                }
                case Offside offside -> {
                    if (offside.primaryPlayer() != null) {
                        statsFor(statsByPlayer, offside.primaryPlayer(), season).addOffside();
                    }
                }
                case Save save -> statsFor(statsByPlayer, save.primaryPlayer(), season).addSave();
                case TackleWon tackle -> statsFor(statsByPlayer, tackle.primaryPlayer(), season).addTackleWon();
                case Interception interception ->
                        statsFor(statsByPlayer, interception.primaryPlayer(), season).addInterception();
                case PassAttempt pass ->
                        statsFor(statsByPlayer, pass.primaryPlayer(), season).addPass(pass.completed());
                case Corner corner -> {
                    // corner taken isn't tracked as a PlayerSeasonStats field
                }
                case PenaltyAwarded penalty -> {
                    // not tracked as a PlayerSeasonStats field; the resulting ShotAttempt/GoalScored still is
                }
            }
        }

        return statsByPlayer;
    }

    private static PlayerSeasonStats statsFor(Map<Player, PlayerSeasonStats> statsByPlayer, Player player, int season) {
        return statsByPlayer.computeIfAbsent(player, p -> new PlayerSeasonStats(p, p.getCurrentTeam(), season));
    }
}
