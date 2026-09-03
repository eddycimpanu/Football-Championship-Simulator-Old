package org.example.domain;

public sealed interface MatchEvent permits
        ShotAttempt, PassAttempt, Foul, CardShown, GoalScored, OwnGoal,
        Save, Corner, Offside, TackleWon, Interception, PenaltyAwarded {

    int minute();
    int second();
    Team team();
    Player primaryPlayer();
    Player secondaryPlayer();

    String describe();
}
