package org.example.domain;

import java.util.List;
import java.util.Random;

public class PhaseSimulator {

    private final Random random;

    public PhaseSimulator() {
        this.random = new Random();
    }

    public PhaseState simulatePhase(PhaseState startingState, Team attackingTeam, Team defendingTeam,
                                    Match match) {
        switch (startingState) {
            case KICKOFF:
            case GOAL_KICK:
            case THROW_IN:
            case DISPOSSESSION:
            case ATTACK:
                return simulateAttack(attackingTeam, defendingTeam, match);
            case FREE_KICK:
                return simulateFreeKick(attackingTeam, defendingTeam, match);
            case CORNER:
                return simulateCorner(attackingTeam, defendingTeam, match);
            case CROSS:
                return simulateCross(attackingTeam, defendingTeam, match);
            case FOUL:
                return simulateFoul(attackingTeam, defendingTeam, match);
            case SHOOT:
                return simulateShot(attackingTeam, defendingTeam, match);
            default:
                throw new IllegalStateException("Unhandled phase state: " + startingState);
        }
    }

    private PhaseState simulateAttack(Team attackingTeam, Team defendingTeam, Match match) {
        double attackSkill = withLuckFactor(getTeamAverageSkill(attackingTeam, "passing"));
        double defenseSkill = withLuckFactor(getTeamAverageSkill(defendingTeam, "defending"));

        RouletteWheel<PhaseState> wheel = new RouletteWheel<PhaseState>()
                .add(PhaseState.SHOOT, Math.max(2 + attackSkill * 0.35 - defenseSkill * 0.15, 1))
                .add(PhaseState.FOUL, Math.max(10 + defenseSkill * 0.1, 1))
                .add(PhaseState.OFFSIDE, Math.max(3, 1))
                .add(PhaseState.DISPOSSESSION, Math.max(20 + defenseSkill * 0.4 - attackSkill * 0.3, 1))
                .add(PhaseState.THROW_IN_WON, Math.max(12, 1))
                .add(PhaseState.CORNER_WON, Math.max(9, 1))
                .add(PhaseState.GOAL_KICK_AWARDED, Math.max(2, 1));

        PhaseState result = wheel.spin(random);

        if (result == PhaseState.FOUL) {
            match.recordFoul(attackingTeam);
        } else if (result == PhaseState.OFFSIDE) {
            match.recordOffside(attackingTeam);
        }

        return result;
    }

    private PhaseState simulateFreeKick(Team attackingTeam, Team defendingTeam, Match match) {
        // TODO: decideFreeKickAction() -> shoot / cross / short pass (-> Attack)
        return null;
    }

    private PhaseState simulateCorner(Team attackingTeam, Team defendingTeam, Match match) {
        // TODO: decideCornerAction() -> cross / short pass (-> Attack)
        // match.recordCorner(attackingTeam);
        return null;
    }

    private PhaseState simulateCross(Team attackingTeam, Team defendingTeam, Match match) {
        // TODO: decideCrossOutcome() -> defended / foul / own goal
        return null;
    }

    private PhaseState simulateFoul(Team attackingTeam, Team defendingTeam, Match match) {
        Player offender = pickFoulOffender(match.getOnFieldPlayers(defendingTeam));

        boolean insideBox = random.nextInt(100) < 18;

        CardType card = decideCardGiven(offender, match);
        if (card != null) {
            match.addCard(offender, card);
        }

        if (insideBox) {
            return PhaseState.PENALTY;
        } else {
            return PhaseState.FREE_KICK_WON;
        }
    }

    private Player pickFoulOffender(List<Player> players) {
        RouletteWheel<Player> wheel = new RouletteWheel<>();

        for (Player player : players) {
            double weight = switch (player.getPosition()) {
                case DEFENDER -> 40;
                case MIDFIELDER -> 35;
                case ATTACKER -> 15;
                case GOALKEEPER -> 2;
            };
            wheel.add(player, weight);
        }

        return wheel.spin(random);
    }

    private CardType decideCardGiven(Player offender, Match match) {
        int roll = random.nextInt(100);

        if (roll < 75) {
            return null; // fara cartonas
        }

        boolean isSecondYellow = countYellowCardsThisMatch(offender, match) >= 1;
        if (isSecondYellow) {
            return CardType.RED;
        }

        if (roll < 95) {
            return CardType.YELLOW;
        }

        return CardType.RED;
    }

    private int countYellowCardsThisMatch(Player player, Match match) {
        int count = 0;
        for (MatchEvent event : match.getEvents()) {
            if (event instanceof Card card) {
                if (card.getOffender().equals(player) && card.getType() == CardType.YELLOW) {
                    count++;
                }
            }
        }
        return count;
    }

    private PhaseState simulateShot(Team attackingTeam, Team defendingTeam, Match match) {
        List<Player> attackingPlayers = match.getOnFieldPlayers(attackingTeam);
        Player shooter = pickShooter(attackingPlayers);
        Player goalkeeper = findGoalkeeper(match.getOnFieldPlayers(defendingTeam));

        ShotResult result = decideShotOutcome(shooter, goalkeeper);

        switch (result) {
            case GOAL:
                Player assistant = decideAssist(shooter, attackingPlayers);
                match.addGoal(shooter, assistant);
                return PhaseState.GOAL;
            case SAVE:
                return decideReboundOutcome();
            case MISS:
                return PhaseState.GOAL_KICK_AWARDED;
            default:
                throw new IllegalStateException("Unhandled shot result: " + result);
        }
    }

    private Player pickShooter(List<Player> players) {
        RouletteWheel<Player> wheel = new RouletteWheel<>();

        for (Player player : players) {
            double weight = switch (player.getPosition()) {
                case ATTACKER -> 50;
                case MIDFIELDER -> 25;
                case DEFENDER -> 8;
                case GOALKEEPER -> 1;
            };
            wheel.add(player, weight);
        }

        return wheel.spin(random);
    }

    private Player decideAssist(Player shooter, List<Player> teammates) {
        boolean hasAssist = random.nextInt(100) < 65;
        if (!hasAssist) {
            return null;
        }

        RouletteWheel<Player> wheel = new RouletteWheel<>();
        for (Player player : teammates) {
            if (player == shooter) continue;

            double weight = switch (player.getPosition()) {
                case MIDFIELDER -> 40;
                case ATTACKER -> 35;
                case DEFENDER -> 15;
                case GOALKEEPER -> 2;
            };
            wheel.add(player, weight);
        }

        return wheel.spin(random);
    }

    private Player findGoalkeeper(List<Player> players) {
        for (Player player : players) {
            if (player.getPosition() == Position.GOALKEEPER) {
                return player;
            }
        }
        throw new IllegalStateException("No goalkeeper found on the field");
    }

    private PhaseState decideReboundOutcome() {
        RouletteWheel<PhaseState> wheel = new RouletteWheel<PhaseState>()
                .add(PhaseState.ATTACK, Math.max(35, 1))
                .add(PhaseState.CORNER_WON, Math.max(40, 1))
                .add(PhaseState.DISPOSSESSION, Math.max(25, 1));

        return wheel.spin(random);
    }

    public ShotResult decideShotOutcome(Player shooter, Player goalkeeper) {
        double shooterSkill = withLuckFactor(shooter.getShooting());
        double goalkeeperSkill = withLuckFactor(goalkeeper.getGoalkeeping());

        double missFitness = 55 - shooterSkill * 0.15;
        double saveFitness = 20 + goalkeeperSkill * 0.35;
        double goalFitness = 10 + shooterSkill * 0.15 - goalkeeperSkill * 0.15;

        RouletteWheel<ShotResult> wheel = new RouletteWheel<ShotResult>()
                .add(ShotResult.MISS, Math.max(missFitness, 1))
                .add(ShotResult.SAVE, Math.max(saveFitness, 1))
                .add(ShotResult.GOAL, Math.max(goalFitness, 1));

        return wheel.spin(random);
    }

    private double withLuckFactor(double baseValue) {
        int luck = random.nextInt(21) - 10;
        return clamp(baseValue + luck, 1, 99);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double getTeamAverageSkill(Team team, String attribute) {
        List<Player> players = team.getStartingLineUp();
        double sum = 0;
        for (Player player : players) {
            sum += switch (attribute) {
                case "passing" -> player.getPassing();
                case "defending" -> player.getDefending();
                case "shooting" -> player.getShooting();
                case "goalkeeping" -> player.getGoalkeeping();
                default -> 0;
            };
        }
        return sum / players.size();
    }

    public int decidePhaseDuration(PhaseState finalState) {
        switch (finalState) {
            case GOAL:
            case OWN_GOAL:
                return 1 + random.nextInt(2); // 1-2 minute (sarbatorire, reluare joc)
            case FREE_KICK_WON:
            case CORNER_WON:
            case THROW_IN_WON:
            case GOAL_KICK_AWARDED:
                return 1;
            default:
                return 1;
        }
    }

}