package org.example.domain;

import java.util.List;
import java.util.Random;

public class PhaseSimulator {

    private final Random random;
    private final TeamStrength teamStrength;

    public PhaseSimulator(Random random) {
        this.random = random;
        this.teamStrength = new TeamStrength();
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
        double attackSkill = withLuckFactor(teamStrength.offensiveSkill(attackingTeam));
        double defenseSkill = withLuckFactor(teamStrength.defensiveSkill(defendingTeam));

        RouletteWheel<PhaseState> wheel = new RouletteWheel<PhaseState>()
                .add(PhaseState.SHOOT, Math.max(SimulationModel.ATTACK_SHOOT_BASE_WEIGHT
                        + attackSkill * SimulationModel.ATTACK_SHOOT_ATTACK_SKILL_COEFFICIENT
                        - defenseSkill * SimulationModel.ATTACK_SHOOT_DEFENSE_SKILL_COEFFICIENT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(PhaseState.FOUL, Math.max(SimulationModel.ATTACK_FOUL_BASE_WEIGHT
                        + defenseSkill * SimulationModel.ATTACK_FOUL_DEFENSE_SKILL_COEFFICIENT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(PhaseState.FREE_KICK, Math.max(SimulationModel.ATTACK_FREE_KICK_WEIGHT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(PhaseState.DISPOSSESSION, Math.max(SimulationModel.ATTACK_DISPOSSESSION_BASE_WEIGHT
                        + defenseSkill * SimulationModel.ATTACK_DISPOSSESSION_DEFENSE_SKILL_COEFFICIENT
                        - attackSkill * SimulationModel.ATTACK_DISPOSSESSION_ATTACK_SKILL_COEFFICIENT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(PhaseState.THROW_IN, Math.max(SimulationModel.ATTACK_THROW_IN_WEIGHT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(PhaseState.CORNER, Math.max(SimulationModel.ATTACK_CORNER_WEIGHT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(PhaseState.GOAL_KICK, Math.max(SimulationModel.ATTACK_GOAL_KICK_WEIGHT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(PhaseState.OWN_GOAL, Math.max(SimulationModel.ATTACK_OWN_GOAL_WEIGHT, SimulationModel.OWN_GOAL_WEIGHT_FLOOR));

        PhaseState result = wheel.spin(random);

        if (result == PhaseState.FOUL) {
            match.recordEvent(new Foul(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, null, null));
        } else if (result == PhaseState.FREE_KICK) {
            boolean isOffside = random.nextInt(SimulationModel.PERCENT_SCALE) < SimulationModel.ATTACK_OFFSIDE_CHANCE_PERCENT;
            if (isOffside) {
                match.recordEvent(new Offside(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, null, null));
            }
        } else if (result == PhaseState.OWN_GOAL) {
            Player ownGoalScorer = pickHeaderShooter(match.getOnFieldPlayers(defendingTeam));
            match.recordEvent(new OwnGoal(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, ownGoalScorer, null));
            return PhaseState.OWN_GOAL;
        }

        return result;
    }

    private PhaseState simulateFreeKick(Team attackingTeam, Team defendingTeam, Match match) {
        Player taker = attackingTeam.getFreeKickTaker();

        RouletteWheel<String> wheel = new RouletteWheel<String>()
                .add("shoot", Math.max(SimulationModel.FREE_KICK_SHOOT_BASE_WEIGHT
                        + taker.getShooting() * SimulationModel.FREE_KICK_SHOOT_SKILL_COEFFICIENT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add("cross", Math.max(SimulationModel.FREE_KICK_CROSS_WEIGHT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add("shortPass", Math.max(SimulationModel.FREE_KICK_SHORT_PASS_WEIGHT, SimulationModel.MIN_WHEEL_WEIGHT));

        String choice = wheel.spin(random);

        return switch (choice) {
            case "shoot" -> resolveDirectShot(attackingTeam, defendingTeam, match, taker);
            case "cross" -> resolveCross(attackingTeam, defendingTeam, match, taker);
            default -> PhaseState.ATTACK;
        };
    }

    private PhaseState simulateCorner(Team attackingTeam, Team defendingTeam, Match match) {
        Player taker = attackingTeam.getCornerTaker();
        match.recordEvent(new Corner(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, taker, null));

        boolean isCross = random.nextInt(SimulationModel.PERCENT_SCALE) < SimulationModel.CORNER_CROSS_CHANCE_PERCENT;
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
        return resolveDirectShot(attackingTeam, defendingTeam, match, taker);
    }

    private PhaseState resolveCross(Team attackingTeam, Team defendingTeam, Match match, Player crosser) {
        double crossSkill = withLuckFactor(crosser.getPassing());
        double aerialDefenseSkill = withLuckFactor(teamStrength.defensiveSkill(defendingTeam));

        RouletteWheel<String> wheel = new RouletteWheel<String>()
                .add("defended", Math.max(SimulationModel.CROSS_DEFENDED_BASE_WEIGHT
                        + aerialDefenseSkill * SimulationModel.CROSS_DEFENDED_DEFENSE_SKILL_COEFFICIENT
                        - crossSkill * SimulationModel.CROSS_DEFENDED_CROSS_SKILL_COEFFICIENT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add("headerShot", Math.max(SimulationModel.CROSS_HEADER_SHOT_BASE_WEIGHT
                        + crossSkill * SimulationModel.CROSS_HEADER_SHOT_CROSS_SKILL_COEFFICIENT
                        - aerialDefenseSkill * SimulationModel.CROSS_HEADER_SHOT_DEFENSE_SKILL_COEFFICIENT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add("foul", Math.max(SimulationModel.CROSS_FOUL_WEIGHT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add("ownGoal", Math.max(SimulationModel.CROSS_OWN_GOAL_WEIGHT, SimulationModel.OWN_GOAL_WEIGHT_FLOOR));

        String outcome = wheel.spin(random);

        switch (outcome) {
            case "defended":
                return PhaseState.DISPOSSESSION;
            case "foul": {
                Player offender = pickFoulOffender(match.getOnFieldPlayers(defendingTeam), match);
                match.recordEvent(new Foul(match.getCurrentMinute(), match.getCurrentSecond(), defendingTeam, offender, null));
                CardType card = decideCardGiven(offender, match);
                if (card != null) {
                    match.recordEvent(new CardShown(match.getCurrentMinute(), match.getCurrentSecond(), defendingTeam, offender, null, card));
                }
                boolean insideBox = random.nextInt(SimulationModel.PERCENT_SCALE) < SimulationModel.CROSS_FOUL_INSIDE_BOX_CHANCE_PERCENT;
                if (insideBox) {
                    match.recordEvent(new PenaltyAwarded(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, offender, null));
                }
                return insideBox ? PhaseState.PENALTY : PhaseState.FREE_KICK;
            }
            case "ownGoal": {
                Player ownGoalScorer = pickHeaderShooter(match.getOnFieldPlayers(defendingTeam));
                match.recordEvent(new OwnGoal(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, ownGoalScorer, null));
                return PhaseState.OWN_GOAL;
            }
            case "headerShot": {
                List<Player> attackingPlayers = match.getOnFieldPlayers(attackingTeam);
                Player shooter = pickHeaderShooter(attackingPlayers);
                Player goalkeeper = findGoalkeeper(match.getOnFieldPlayers(defendingTeam));

                ShotResult result = decideShotOutcome(shooter, goalkeeper);
                match.recordEvent(new ShotAttempt(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, shooter, goalkeeper, result));

                switch (result) {
                    case GOAL:
                        match.recordEvent(new GoalScored(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, shooter, crosser));
                        return PhaseState.GOAL;
                    case SAVE:
                        match.recordEvent(new Save(match.getCurrentMinute(), match.getCurrentSecond(), defendingTeam, goalkeeper, shooter));
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

    private PhaseState resolveDirectShot(Team attackingTeam, Team defendingTeam, Match match, Player shooter) {
        Player goalkeeper = findGoalkeeper(match.getOnFieldPlayers(defendingTeam));
        ShotResult result = decideShotOutcome(shooter, goalkeeper);
        match.recordEvent(new ShotAttempt(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, shooter, goalkeeper, result));

        switch (result) {
            case GOAL:
                match.recordEvent(new GoalScored(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, shooter, null));
                return PhaseState.GOAL;
            case SAVE:
                match.recordEvent(new Save(match.getCurrentMinute(), match.getCurrentSecond(), defendingTeam, goalkeeper, shooter));
                return decideReboundOutcome();
            case MISS:
                return PhaseState.GOAL_KICK;
            default:
                throw new IllegalStateException("Unhandled shot result: " + result);
        }
    }

    private PhaseState simulateFoul(Team attackingTeam, Team defendingTeam, Match match) {
        Player offender = pickFoulOffender(match.getOnFieldPlayers(defendingTeam), match);

        boolean insideBox = random.nextInt(SimulationModel.PERCENT_SCALE) < SimulationModel.FOUL_INSIDE_BOX_CHANCE_PERCENT;

        CardType card = decideCardGiven(offender, match);
        if (card != null) {
            match.recordEvent(new CardShown(match.getCurrentMinute(), match.getCurrentSecond(), defendingTeam, offender, null, card));
        }

        if (insideBox) {
            match.recordEvent(new PenaltyAwarded(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, offender, null));
        }

        return insideBox ? PhaseState.PENALTY : PhaseState.FREE_KICK;
    }

    private Player pickFoulOffender(List<Player> players, Match match) {
        RouletteWheel<Player> wheel = new RouletteWheel<>();

        for (Player player : players) {
            double weight = switch (player.getPosition()) {
                case DEFENDER -> SimulationModel.FOUL_OFFENDER_WEIGHT_DEFENDER;
                case MIDFIELDER -> SimulationModel.FOUL_OFFENDER_WEIGHT_MIDFIELDER;
                case ATTACKER -> SimulationModel.FOUL_OFFENDER_WEIGHT_ATTACKER;
                case GOALKEEPER -> SimulationModel.FOUL_OFFENDER_WEIGHT_GOALKEEPER;
            };
            if (countYellowCardsThisMatch(player, match) >= SimulationModel.SECOND_YELLOW_CARD_THRESHOLD) {
                weight *= SimulationModel.CARDED_OFFENDER_WEIGHT_MULTIPLIER;
            }
            wheel.add(player, weight);
        }

        return wheel.spin(random);
    }

    private CardType decideCardGiven(Player offender, Match match) {
        int roll = random.nextInt(SimulationModel.PERCENT_SCALE);

        if (roll < SimulationModel.CARD_NONE_THRESHOLD_PERCENT) {
            return null;
        }

        boolean isSecondYellow = countYellowCardsThisMatch(offender, match) >= SimulationModel.SECOND_YELLOW_CARD_THRESHOLD;
        if (isSecondYellow) {
            return CardType.RED;
        }

        if (roll < SimulationModel.CARD_YELLOW_THRESHOLD_PERCENT) {
            return CardType.YELLOW;
        }

        return CardType.RED;
    }

    private int countYellowCardsThisMatch(Player player, Match match) {
        return (int) match.getEvents().stream()
                .filter(event -> event instanceof CardShown card
                        && card.primaryPlayer().equals(player)
                        && card.cardType() == CardType.YELLOW)
                .count();
    }

    private PhaseState simulateShot(Team attackingTeam, Team defendingTeam, Match match) {
        List<Player> attackingPlayers = match.getOnFieldPlayers(attackingTeam);
        Player shooter = pickShooter(attackingPlayers);
        Player goalkeeper = findGoalkeeper(match.getOnFieldPlayers(defendingTeam));

        ShotResult result = decideShotOutcome(shooter, goalkeeper);
        match.recordEvent(new ShotAttempt(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, shooter, goalkeeper, result));

        switch (result) {
            case GOAL:
                Player assistant = decideAssist(shooter, attackingPlayers);
                match.recordEvent(new GoalScored(match.getCurrentMinute(), match.getCurrentSecond(), attackingTeam, shooter, assistant));
                return PhaseState.GOAL;
            case SAVE:
                match.recordEvent(new Save(match.getCurrentMinute(), match.getCurrentSecond(), defendingTeam, goalkeeper, shooter));
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
                case ATTACKER -> SimulationModel.SHOOTER_WEIGHT_ATTACKER;
                case MIDFIELDER -> SimulationModel.SHOOTER_WEIGHT_MIDFIELDER;
                case DEFENDER -> SimulationModel.SHOOTER_WEIGHT_DEFENDER;
                case GOALKEEPER -> SimulationModel.SHOOTER_WEIGHT_GOALKEEPER;
            };
            wheel.add(player, weight);
        }

        return wheel.spin(random);
    }

    private Player pickHeaderShooter(List<Player> players) {
        RouletteWheel<Player> wheel = new RouletteWheel<>();

        for (Player player : players) {
            double weight = switch (player.getPosition()) {
                case ATTACKER -> SimulationModel.HEADER_SHOOTER_WEIGHT_ATTACKER;
                case DEFENDER -> SimulationModel.HEADER_SHOOTER_WEIGHT_DEFENDER;
                case MIDFIELDER -> SimulationModel.HEADER_SHOOTER_WEIGHT_MIDFIELDER;
                case GOALKEEPER -> SimulationModel.HEADER_SHOOTER_WEIGHT_GOALKEEPER;
            };
            wheel.add(player, weight);
        }

        return wheel.spin(random);
    }

    private Player decideAssist(Player shooter, List<Player> teammates) {
        boolean hasAssist = random.nextInt(SimulationModel.PERCENT_SCALE) < SimulationModel.ASSIST_CHANCE_PERCENT;
        if (!hasAssist) {
            return null;
        }

        RouletteWheel<Player> wheel = new RouletteWheel<>();
        for (Player player : teammates) {
            if (player == shooter) continue;

            double weight = switch (player.getPosition()) {
                case MIDFIELDER -> SimulationModel.ASSIST_WEIGHT_MIDFIELDER;
                case ATTACKER -> SimulationModel.ASSIST_WEIGHT_ATTACKER;
                case DEFENDER -> SimulationModel.ASSIST_WEIGHT_DEFENDER;
                case GOALKEEPER -> SimulationModel.ASSIST_WEIGHT_GOALKEEPER;
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
                .add(PhaseState.ATTACK, Math.max(SimulationModel.REBOUND_ATTACK_WEIGHT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(PhaseState.CORNER, Math.max(SimulationModel.REBOUND_CORNER_WEIGHT, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(PhaseState.DISPOSSESSION, Math.max(SimulationModel.REBOUND_DISPOSSESSION_WEIGHT, SimulationModel.MIN_WHEEL_WEIGHT));

        return wheel.spin(random);
    }

    public ShotResult decideShotOutcome(Player shooter, Player goalkeeper) {
        double shooterSkill = withLuckFactor(shooter.getShooting());
        double goalkeeperSkill = withLuckFactor(goalkeeper.getGoalkeeping());

        double missFitness = SimulationModel.SHOT_MISS_BASE_FITNESS
                - shooterSkill * SimulationModel.SHOT_MISS_SHOOTER_SKILL_COEFFICIENT;
        double saveFitness = SimulationModel.GOALKEEPER_SAVE_BASE_FITNESS
                + goalkeeperSkill * SimulationModel.GOALKEEPER_SAVE_SKILL_COEFFICIENT;
        double goalFitness = SimulationModel.SHOT_GOAL_BASE_FITNESS
                + shooterSkill * SimulationModel.SHOT_GOAL_SHOOTER_SKILL_COEFFICIENT
                - goalkeeperSkill * SimulationModel.GOALKEEPER_GOAL_PREVENTION_COEFFICIENT;

        RouletteWheel<ShotResult> wheel = new RouletteWheel<ShotResult>()
                .add(ShotResult.MISS, Math.max(missFitness, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(ShotResult.SAVE, Math.max(saveFitness, SimulationModel.MIN_WHEEL_WEIGHT))
                .add(ShotResult.GOAL, Math.max(goalFitness, SimulationModel.MIN_WHEEL_WEIGHT));

        return wheel.spin(random);
    }

    private double withLuckFactor(double baseValue) {
        int luck = random.nextInt(SimulationModel.LUCK_ROLL_RANGE) - SimulationModel.LUCK_ROLL_OFFSET;
        return clamp(baseValue + luck, SimulationModel.SKILL_MIN_CLAMP, SimulationModel.SKILL_MAX_CLAMP);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public int decidePhaseDuration(List<MatchEvent> phaseEvents) {
        long shotCount = phaseEvents.stream().filter(ShotAttempt.class::isInstance).count();
        return SimulationModel.BASE_PHASE_SECONDS
                + (int) (shotCount * SimulationModel.PER_SHOT_SECONDS)
                + decideDeadTimeSeconds(phaseEvents);
    }

    public int decideDeadTimeSeconds(List<MatchEvent> phaseEvents) {
        return phaseEvents.stream().mapToInt(this::deadTimeFor).sum();
    }

    private int deadTimeFor(MatchEvent event) {
        return switch (event) {
            case Corner corner -> SimulationModel.CORNER_DEAD_TIME_SECONDS;
            case Foul foul -> SimulationModel.FOUL_DEAD_TIME_SECONDS;
            case Offside offside -> SimulationModel.OFFSIDE_DEAD_TIME_SECONDS;
            case CardShown card -> SimulationModel.CARD_DEAD_TIME_SECONDS;
            case GoalScored goal -> SimulationModel.GOAL_DEAD_TIME_SECONDS;
            case OwnGoal ownGoal -> SimulationModel.GOAL_DEAD_TIME_SECONDS;
            case ShotAttempt shot -> SimulationModel.NO_DEAD_TIME_SECONDS;
            case Save save -> SimulationModel.NO_DEAD_TIME_SECONDS;
            case PassAttempt pass -> SimulationModel.NO_DEAD_TIME_SECONDS;
            case TackleWon tackle -> SimulationModel.NO_DEAD_TIME_SECONDS;
            case Interception interception -> SimulationModel.NO_DEAD_TIME_SECONDS;
            case PenaltyAwarded penalty -> SimulationModel.NO_DEAD_TIME_SECONDS;
        };
    }
}
