package org.example.domain;

import java.util.Random;

public class PhaseSimulator {

    private final Random random;

    public PhaseSimulator() {
        this.random = new Random();
    }

    public PhaseState simulatePhase(PhaseState startingState, Team attackingTeam, Team defendingTeam,
                                    Match match, int minute) {
        switch (startingState) {
            case KICKOFF:
            case GOAL_KICK:
            case THROW_IN:
            case DISPOSSESSION:
            case ATTACK:
                return simulateAttack(attackingTeam, defendingTeam, match, minute);
            case FREE_KICK:
                return simulateFreeKick(attackingTeam, defendingTeam, match, minute);
            case CORNER:
                return simulateCorner(attackingTeam, defendingTeam, match, minute);
            case CROSS:
                return simulateCross(attackingTeam, defendingTeam, match, minute);
            case FOUL:
                return simulateFoul(attackingTeam, defendingTeam, match, minute);
            case SHOOT:
                return simulateShot(attackingTeam, defendingTeam, match, minute);
            default:
                throw new IllegalStateException("Unhandled phase state: " + startingState);
        }
    }

    private PhaseState simulateAttack(Team attackingTeam, Team defendingTeam, Match match, int minute) {
        // TODO: decideAttackOutcome() -> foul / offside / turnover / throw-in / corner / goal kick / own goal / shot
        // ex: match.recordOffside(attackingTeam);
        return null;
    }

    private PhaseState simulateFreeKick(Team attackingTeam, Team defendingTeam, Match match, int minute) {
        // TODO: decideFreeKickAction() -> shoot / cross / short pass (-> Attack)
        return null;
    }

    private PhaseState simulateCorner(Team attackingTeam, Team defendingTeam, Match match, int minute) {
        // TODO: decideCornerAction() -> cross / short pass (-> Attack)
        // match.recordCorner(attackingTeam);
        return null;
    }

    private PhaseState simulateCross(Team attackingTeam, Team defendingTeam, Match match, int minute) {
        // TODO: decideCrossOutcome() -> defended / foul / own goal
        return null;
    }

    private PhaseState simulateFoul(Team attackingTeam, Team defendingTeam, Match match, int minute) {
        // TODO: decideFoulLocation() -> penalty / free kick won
        // match.recordFoul(attackingTeam);
        // TODO: decideCardGiven() -> none / yellow / red
        // if (card != null) match.addCard(minute, offender, card);
        return null;
    }

    private PhaseState simulateShot(Team attackingTeam, Team defendingTeam, Match match, int minute) {
        // TODO: decideShotOutcome() -> miss / save / goal
        // match.recordShot(attackingTeam, onTarget);
        // if (goal) match.addGoal(minute, scorer, assistant);
        // TODO: daca save -> decideReboundOutcome() -> attack / corner / turnover
        return null;
    }

    public int decidePhaseDuration(PhaseState finalState) {
        // TODO: majoritatea 1 minut, unele stari mai mult (accidentari)
        return 1;
    }
}