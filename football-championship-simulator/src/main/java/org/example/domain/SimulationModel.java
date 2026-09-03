package org.example.domain;

/**
 * Every tunable coefficient used by {@link PhaseSimulator}. Grouped by the phase they resolve.
 * All values are RouletteWheel fitness weights (bigger = more likely) unless noted otherwise.
 */
public final class SimulationModel {

    private SimulationModel() {
    }

    // ===== COMMON =====

    public static final double MIN_WHEEL_WEIGHT = 1.0; // floor applied to every wheel entry so no branch drops to zero/negative odds
    public static final double OWN_GOAL_WEIGHT_FLOOR = 0.1; // lower floor used only for the rare own-goal branches (their base weight is already below MIN_WHEEL_WEIGHT)
    public static final int PERCENT_SCALE = 100; // scale for every percentage-chance roll: random.nextInt(PERCENT_SCALE) < threshold

    // ===== ATTACK RESOLUTION (simulateAttack) =====

    public static final double ATTACK_SHOOT_BASE_WEIGHT = 5.0; // base odds of the attack ending in a shot
    public static final double ATTACK_SHOOT_ATTACK_SKILL_COEFFICIENT = 0.45; // how much attacker skill raises the chance of a shot
    public static final double ATTACK_SHOOT_DEFENSE_SKILL_COEFFICIENT = 0.15; // how much defender skill lowers the chance of a shot
    public static final double ATTACK_FOUL_BASE_WEIGHT = 42.0; // base odds of the attack ending in a foul
    public static final double ATTACK_FOUL_DEFENSE_SKILL_COEFFICIENT = 0.1; // how much defender skill raises the chance of a foul
    public static final double ATTACK_FREE_KICK_WEIGHT = 13.0; // fixed odds of the attack directly earning a free kick
    public static final double ATTACK_DISPOSSESSION_BASE_WEIGHT = 20.0; // base odds of the defense winning the ball
    public static final double ATTACK_DISPOSSESSION_DEFENSE_SKILL_COEFFICIENT = 0.4; // how much defender skill raises dispossession odds
    public static final double ATTACK_DISPOSSESSION_ATTACK_SKILL_COEFFICIENT = 0.3; // how much attacker skill lowers dispossession odds
    public static final double ATTACK_THROW_IN_WEIGHT = 12.0; // fixed odds of the ball going out for a throw-in
    public static final double ATTACK_CORNER_WEIGHT = 18.0; // fixed odds of the ball going out for a corner
    public static final double ATTACK_GOAL_KICK_WEIGHT = 2.0; // fixed odds of the ball going out for a goal kick
    public static final double ATTACK_OWN_GOAL_WEIGHT = 0.3; // fixed odds of the attack ending in an own goal
    public static final int ATTACK_OFFSIDE_CHANCE_PERCENT = 62; // chance that a FREE_KICK result from an attack is actually offside

    // ===== FREE KICK (simulateFreeKick) =====

    public static final double FREE_KICK_SHOOT_BASE_WEIGHT = 15.0; // base odds the taker shoots directly
    public static final double FREE_KICK_SHOOT_SKILL_COEFFICIENT = 0.2; // how much the taker's shooting skill raises the direct-shot odds
    public static final double FREE_KICK_CROSS_WEIGHT = 40.0; // fixed odds the taker crosses into the box
    public static final double FREE_KICK_SHORT_PASS_WEIGHT = 35.0; // fixed odds the taker plays a short pass, resuming open play

    // ===== CORNER (simulateCorner) =====

    public static final int CORNER_CROSS_CHANCE_PERCENT = 75; // chance the corner is played as a cross rather than short

    // ===== CROSS (resolveCross) =====

    public static final double CROSS_DEFENDED_BASE_WEIGHT = 45.0; // base odds the cross is defended away
    public static final double CROSS_DEFENDED_DEFENSE_SKILL_COEFFICIENT = 0.3; // how much aerial defense skill raises the defended odds
    public static final double CROSS_DEFENDED_CROSS_SKILL_COEFFICIENT = 0.2; // how much the crosser's passing skill lowers the defended odds
    public static final double CROSS_HEADER_SHOT_BASE_WEIGHT = 35.0; // base odds the cross leads to a header shot
    public static final double CROSS_HEADER_SHOT_CROSS_SKILL_COEFFICIENT = 0.3; // how much the crosser's passing skill raises header-shot odds
    public static final double CROSS_HEADER_SHOT_DEFENSE_SKILL_COEFFICIENT = 0.15; // how much aerial defense skill lowers header-shot odds
    public static final double CROSS_FOUL_WEIGHT = 10.0; // fixed odds the cross is stopped by a foul
    public static final double CROSS_OWN_GOAL_WEIGHT = 0.5; // fixed odds the cross results in an own goal
    public static final int CROSS_FOUL_INSIDE_BOX_CHANCE_PERCENT = 5; // chance a foul during a cross is given as a penalty rather than a free kick

    // ===== FOUL (simulateFoul, decideCardGiven, pickFoulOffender) =====

    public static final int FOUL_INSIDE_BOX_CHANCE_PERCENT = 1; // chance a FOUL phase result is given as a penalty rather than a free kick
    public static final int CARD_NONE_THRESHOLD_PERCENT = 78; // rolls below this get no card at all
    public static final int CARD_YELLOW_THRESHOLD_PERCENT = 100; // rolls at/above CARD_NONE and below this get yellow; at/above this get straight red (100 = no nominal straight red, only second-yellow escalation)
    public static final int SECOND_YELLOW_CARD_THRESHOLD = 1; // number of prior yellows this match that turns the next yellow into a red
    public static final double FOUL_OFFENDER_WEIGHT_DEFENDER = 40.0; // likelihood a defender is picked as the offender
    public static final double FOUL_OFFENDER_WEIGHT_MIDFIELDER = 35.0; // likelihood a midfielder is picked as the offender
    public static final double FOUL_OFFENDER_WEIGHT_ATTACKER = 15.0; // likelihood an attacker is picked as the offender
    public static final double FOUL_OFFENDER_WEIGHT_GOALKEEPER = 2.0; // likelihood the goalkeeper is picked as the offender
    public static final double CARDED_OFFENDER_WEIGHT_MULTIPLIER = 0.4; // weight cut applied to a player already booked this match, modeling extra caution after a booking

    // ===== SHOT (decideShotOutcome, pickShooter, pickHeaderShooter, decideAssist, decideReboundOutcome) =====

    public static final double SHOT_MISS_BASE_FITNESS = 95.0; // base odds of missing the target entirely
    public static final double SHOT_MISS_SHOOTER_SKILL_COEFFICIENT = 0.15; // how much shooter skill lowers the miss odds
    public static final double SHOT_GOAL_BASE_FITNESS = 12.0; // base odds of scoring
    public static final double SHOT_GOAL_SHOOTER_SKILL_COEFFICIENT = 0.17; // how much shooter skill raises the goal odds
    public static final double SHOOTER_WEIGHT_ATTACKER = 50.0; // likelihood an attacker takes an open-play shot
    public static final double SHOOTER_WEIGHT_MIDFIELDER = 25.0; // likelihood a midfielder takes an open-play shot
    public static final double SHOOTER_WEIGHT_DEFENDER = 8.0; // likelihood a defender takes an open-play shot
    public static final double SHOOTER_WEIGHT_GOALKEEPER = 1.0; // likelihood the goalkeeper takes an open-play shot
    public static final double HEADER_SHOOTER_WEIGHT_ATTACKER = 35.0; // likelihood an attacker gets on the end of a cross
    public static final double HEADER_SHOOTER_WEIGHT_DEFENDER = 35.0; // likelihood a defender gets on the end of a cross
    public static final double HEADER_SHOOTER_WEIGHT_MIDFIELDER = 25.0; // likelihood a midfielder gets on the end of a cross
    public static final double HEADER_SHOOTER_WEIGHT_GOALKEEPER = 3.0; // likelihood the goalkeeper gets on the end of a cross
    public static final int ASSIST_CHANCE_PERCENT = 65; // chance a goal is credited with an assist at all
    public static final double ASSIST_WEIGHT_MIDFIELDER = 40.0; // likelihood a midfielder gets the assist
    public static final double ASSIST_WEIGHT_ATTACKER = 35.0; // likelihood an attacker gets the assist
    public static final double ASSIST_WEIGHT_DEFENDER = 15.0; // likelihood a defender gets the assist
    public static final double ASSIST_WEIGHT_GOALKEEPER = 2.0; // likelihood the goalkeeper gets the assist
    public static final double REBOUND_ATTACK_WEIGHT = 35.0; // odds a save falls back to the attacking team in open play
    public static final double REBOUND_CORNER_WEIGHT = 40.0; // odds a save is parried behind for a corner
    public static final double REBOUND_DISPOSSESSION_WEIGHT = 25.0; // odds a save is cleared away to the defending team

    // ===== GOALKEEPER (goalkeeper's own influence on decideShotOutcome) =====

    public static final double GOALKEEPER_SAVE_BASE_FITNESS = 18.0; // base odds the shot is saved
    public static final double GOALKEEPER_SAVE_SKILL_COEFFICIENT = 0.25; // how much goalkeeping skill raises the save odds
    public static final double GOALKEEPER_GOAL_PREVENTION_COEFFICIENT = 0.13; // how much goalkeeping skill lowers the shooter's goal odds

    // ===== LUCK & SKILL RANGE (withLuckFactor) =====

    public static final int LUCK_ROLL_RANGE = 21; // width of the luck roll (0..20, recentered below)
    public static final int LUCK_ROLL_OFFSET = 10; // recenters the luck roll to -10..+10
    public static final double SKILL_MIN_CLAMP = 1.0; // lowest effective skill value after luck is applied
    public static final double SKILL_MAX_CLAMP = 99.0; // highest effective skill value after luck is applied

    // ===== PHASE DURATION & DEAD TIME (decidePhaseDuration, decideDeadTimeSeconds) =====
    // A phase's duration is BASE_PHASE_SECONDS, plus PER_SHOT_SECONDS for each shot it contains,
    // plus the dead-time cost of every stoppage-causing event it contains. Dead time is also what
    // accumulates into stoppage time at the end of the half.

    public static final int BASE_PHASE_SECONDS = 45; // baseline length of a phase with no notable stoppage (buildup, passing, a tackle)
    public static final int PER_SHOT_SECONDS = 6; // extra seconds per shot attempt in the phase (the strike + immediate reaction)
    public static final int NO_DEAD_TIME_SECONDS = 0; // event types that don't stop the clock
    public static final int CORNER_DEAD_TIME_SECONDS = 25; // retrieving the ball, walking to the flag, setting up the delivery
    public static final int FOUL_DEAD_TIME_SECONDS = 20; // referee's decision, players contesting, free-kick setup
    public static final int OFFSIDE_DEAD_TIME_SECONDS = 12; // flag raised, play stopped, reset
    public static final int CARD_DEAD_TIME_SECONDS = 15; // card shown, players protesting
    public static final int GOAL_DEAD_TIME_SECONDS = 45; // celebration and the walk back for kickoff
}
