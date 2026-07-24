# Match Simulation - Decision Tree

## Simulation Structure

The simulation is driven by phases, not minutes:

- The match runs as a continuous loop of **phases**. Each phase is one
  pass through the state machine below, starting from whatever state the
  previous phase ended in.
- Each phase is assigned a **duration in minutes** (usually 1, but longer
  for special cases like injury stoppages or VAR checks). This duration
  is added to the match clock.
- The loop continues until the match clock reaches 90+ minutes
  (plus stoppage time).
- The minute assigned to a phase is used to timestamp any `MatchEvent`
  generated during that phase (goal, card, etc.) - it does not control
  how many phases occur, the phases themselves control how the clock
  advances.

## Phase state machine

```mermaid
flowchart TD
    KICKOFF[STATE: Kickoff] --> ATTACK[STATE: Attack]
    GOALKICK[STATE: Goal Kick] --> ATTACK
    THROWIN[STATE: Throw-in] --> ATTACK
    DISPOSSESSION[STATE: Dispossession] --> ATTACK

    FREEKICK[STATE: Free Kick] --> FK1{Shoot, cross, or short pass?}
    FK1 -- Shoot --> SHOOT[STATE: Shoot]
    FK1 -- Cross --> CROSS[STATE: Cross]
    FK1 -- Short pass --> ATTACK

    CORNER[STATE: Corner] --> C1{Cross or short pass?}
    C1 -- Cross --> CROSS
    C1 -- Short pass --> ATTACK

    ATTACK --> A1{How does the attack resolve?}
    A1 -- Foul --> FOUL[STATE: Foul]
    A1 -- Offside --> AOffside[FINAL: Offside]
    A1 -- Defender wins ball --> ATurnover[FINAL: Dispossession - opponent]
    A1 -- Out for throw-in --> AThrowin[FINAL: Throw-in Won]
    A1 -- Out for corner --> ACorner[FINAL: Corner Won]
    A1 -- Out for goal kick --> AGoalkick[FINAL: Goal Kick Awarded]
    A1 -- Own goal --> OWNGOAL[FINAL: Own Goal]
    A1 -- Shot taken --> A2{Foot or header?}
    A2 -- Foot --> SHOOT
    A2 -- Header --> SHOOT

    CROSS --> CR1{How does the cross resolve?}
    CR1 -- Defended --> CRTurnover[FINAL: Dispossession - opponent]
    CR1 -- Foul --> FOUL
    CR1 -- Own goal --> OWNGOAL

    FOUL --> F1{Inside box or outside box?}
    F1 -- Inside box --> PENALTY[Penalty]
    F1 -- Outside box --> FFreekick[FINAL: Free Kick Won]
    PENALTY --> SHOOT

    SHOOT --> S1{Miss, save, or goal?}
    S1 -- Goal --> GOAL[FINAL: GOAL]
    S1 -- Miss --> SMiss[FINAL: Goal Kick Awarded]
    S1 -- Save --> S2{What happens to the rebound?}
    S2 -- Rebound to attacker --> ATTACK
    S2 -- Parried behind --> ACorner2[FINAL: Corner Won]
    S2 -- Cleared to opponent --> ATurnover2[FINAL: Dispossession - opponent]
```

## Notes

- **Fouls** can result from various causes (tackling, obstruction, handball) -
  all resolve identically through the `FOUL` state. The exact cause can be
  recorded as event detail for stats/display, but doesn't change the state
  transition.
- **Cards** (yellow/red) are decided independently at the `FOUL` state via a
  separate `decideCardGiven()` function. This does not affect which state
  comes next (inside/outside box still determines Penalty vs Free Kick),
  it only affects player discipline stats and, for a red card, the number
  of players remaining on the pitch for the rest of the match.
- **Own goals** are tracked separately from regular goals (`addOwnGoal()`
  instead of `addGoal()`), and credit the score to the opposing team.

## Corresponding decision functions

| Node in the diagram | Function (approximate) |
|---|---|
| Shoot, cross, or short pass? | `decideFreeKickOrCornerAction()` |
| How does the attack resolve? | `decideAttackOutcome(attackingTeam, defendingTeam)` |
| Foot or header? | `decideShotType(shooter)` |
| How does the cross resolve? | `decideCrossOutcome(winger, defender)` |
| Inside box or outside box? | `decideFoulLocation()` |
| Card for the offender? | `decideCardGiven(offender)` |
| Miss, save, or goal? | `decideShotOutcome(shooter, goalkeeper)` |
| What happens to the rebound? | `decideReboundOutcome()` |

## Implementation note

Each decision function:
1. Takes as input the relevant players for that specific situation (not the team's overall OVR)
2. Calculates a probability (using their OVR)
3. Generates a random number
4. Compares the number to the calculated probability and decides the outcome

These are built one at a time, tested in isolation, starting with the simplest
branch (attack -> shot -> goal/miss/save) before adding fouls, crosses, and
set pieces.