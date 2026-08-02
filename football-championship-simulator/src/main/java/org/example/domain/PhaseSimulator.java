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
            case PENALTY:
                return simulatePenalty(attackingTeam, defendingTeam, match);
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
                .add(PhaseState.SHOOT, Math.max(4 + attackSkill * 0.45 - defenseSkill * 0.15, 1))
                .add(PhaseState.FOUL, Math.max(10 + defenseSkill * 0.1, 1))
                .add(PhaseState.FREE_KICK, Math.max(13, 1))
                .add(PhaseState.DISPOSSESSION, Math.max(20 + defenseSkill * 0.4 - attackSkill * 0.3, 1))
                .add(PhaseState.THROW_IN, Math.max(12, 1))
                .add(PhaseState.CORNER, Math.max(9, 1))
                .add(PhaseState.GOAL_KICK, Math.max(2, 1))
                .add(PhaseState.OWN_GOAL, Math.max(0.3, 0.1));

        PhaseState result = wheel.spin(random);

        if (result == PhaseState.FOUL) {
            match.recordFoul(attackingTeam);
        } else if (result == PhaseState.FREE_KICK) {
            boolean isOffside = random.nextInt(100) < 25;
            if (isOffside) {
                match.recordOffside(attackingTeam);
            }
        } else if (result == PhaseState.OWN_GOAL) {
            Player ownGoalScorer = pickHeaderShooter(match.getOnFieldPlayers(defendingTeam));
            match.addOwnGoal(ownGoalScorer);
            return PhaseState.GOAL;
        }

        return result;
    }

    private PhaseState simulateFreeKick(Team attackingTeam, Team defendingTeam, Match match) {
        Player taker = attackingTeam.getFreeKickTaker();

        RouletteWheel<String> wheel = new RouletteWheel<String>()
                .add("shoot", Math.max(15 + taker.getShooting() * 0.2, 1))
                .add("cross", Math.max(40, 1))
                .add("shortPass", Math.max(35, 1));

        String choice = wheel.spin(random);

        return switch (choice) {
            case "shoot" -> resolveDirectShot(defendingTeam, match, taker);
            case "cross" -> resolveCross(attackingTeam, defendingTeam, match, taker);
            default -> PhaseState.ATTACK;
        };
    }

    private PhaseState simulateCorner(Team attackingTeam, Team defendingTeam, Match match) {
        match.recordCorner(attackingTeam);

        Player taker = attackingTeam.getCornerTaker();

        boolean isCross = random.nextInt(100) < 75;
        if (!isCross) {
            return PhaseState.ATTACK;
        }

        return resolveCross(attackingTeam, defendingTeam, match, taker);
    }

    private PhaseState simulateCross(Team attackingTeam, Team defendingTeam, Match match) {
        Player crosser = attackingTeam.getCornerTaker();
        return resolveCross(attackingTeam, defendingTeam, match, crosser);
    }

    private PhaseState simulatePenalty(Team attackingTeam, Team defendingTeam, Match match) {
        Player taker = attackingTeam.getPenaltyTaker();
        return resolveDirectShot(defendingTeam, match, taker);
    }

    private PhaseState resolveCross(Team attackingTeam, Team defendingTeam, Match match, Player crosser) {
        double crossSkill = withLuckFactor(crosser.getPassing());
        double aerialDefenseSkill = withLuckFactor(getTeamAverageSkill(defendingTeam, "defending"));

        RouletteWheel<String> wheel = new RouletteWheel<String>()
                .add("defended", Math.max(45 + aerialDefenseSkill * 0.3 - crossSkill * 0.2, 1))
                .add("headerShot", Math.max(35 + crossSkill * 0.3 - aerialDefenseSkill * 0.15, 1))
                .add("foul", Math.max(10, 1))
                .add("ownGoal", Math.max(0.5, 0.1));

        String outcome = wheel.spin(random);

        switch (outcome) {
            case "defended":
                return PhaseState.DISPOSSESSION;
            case "foul": {
                Player offender = pickFoulOffender(match.getOnFieldPlayers(defendingTeam));
                CardType card = decideCardGiven(offender, match);
                if (card != null) {
                    match.addCard(offender, card);
                }
                boolean insideBox = random.nextInt(100) < 60;
                return insideBox ? PhaseState.PENALTY : PhaseState.FREE_KICK;
            }
            case "ownGoal": {
                Player ownGoalScorer = pickHeaderShooter(match.getOnFieldPlayers(defendingTeam));
                match.addOwnGoal(ownGoalScorer);
                return PhaseState.GOAL;
            }
            case "headerShot": {
                List<Player> attackingPlayers = match.getOnFieldPlayers(attackingTeam);
                Player shooter = pickHeaderShooter(attackingPlayers);
                Player goalkeeper = findGoalkeeper(match.getOnFieldPlayers(defendingTeam));

                ShotResult result = decideShotOutcome(shooter, goalkeeper);
                switch (result) {
                    case GOAL:
                        match.addGoal(shooter, crosser);
                        return PhaseState.GOAL;
                    case SAVE:
                        return decideReboundOutcome();
                    case MISS:
                        return PhaseState.GOAL_KICK;
                    default:
                        throw new IllegalStateException("Unhandled shot result: " + result);
                }
            }
            default:
                throw new IllegalStateException("Unhandled cross outcome: " + outcome);
        }
    }

    private PhaseState resolveDirectShot(Team defendingTeam, Match match, Player shooter) {
        Player goalkeeper = findGoalkeeper(match.getOnFieldPlayers(defendingTeam));
        ShotResult result = decideShotOutcome(shooter, goalkeeper);

        switch (result) {
            case GOAL:
                match.addGoal(shooter);
                return PhaseState.GOAL;
            case SAVE:
                return decideReboundOutcome();
            case MISS:
                return PhaseState.GOAL_KICK;
            default:
                throw new IllegalStateException("Unhandled shot result: " + result);
        }
    }

    private PhaseState simulateFoul(Team attackingTeam, Team defendingTeam, Match match) {
        Player offender = pickFoulOffender(match.getOnFieldPlayers(defendingTeam));

        boolean insideBox = random.nextInt(100) < 18;

        CardType card = decideCardGiven(offender, match);
        if (card != null) {
            match.addCard(offender, card);
        }

        return insideBox ? PhaseState.PENALTY : PhaseState.FREE_KICK;
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
            return null;
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
                return PhaseState.GOAL_KICK;
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

    private Player pickHeaderShooter(List<Player> players) {
        RouletteWheel<Player> wheel = new RouletteWheel<>();

        for (Player player : players) {
            double weight = switch (player.getPosition()) {
                case ATTACKER -> 35;
                case DEFENDER -> 35;
                case MIDFIELDER -> 25;
                case GOALKEEPER -> 3;
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
                .add(PhaseState.CORNER, Math.max(40, 1))
                .add(PhaseState.DISPOSSESSION, Math.max(25, 1));

        return wheel.spin(random);
    }

    public ShotResult decideShotOutcome(Player shooter, Player goalkeeper) {
        double shooterSkill = withLuckFactor(shooter.getShooting());
        double goalkeeperSkill = withLuckFactor(goalkeeper.getGoalkeeping());

        double missFitness = 52 - shooterSkill * 0.15;
        double saveFitness = 22 + goalkeeperSkill * 0.32;
        double goalFitness = 11 + shooterSkill * 0.17 - goalkeeperSkill * 0.13;

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
                return 1 + random.nextInt(2);
            default:
                return 1;
        }
    }
}