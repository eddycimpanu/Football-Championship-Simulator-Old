package org.example.domain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public class TeamStrength {

    private final Map<Position, Double> offensiveWeights;
    private final Map<Position, Double> defensiveWeights;

    public TeamStrength() {
        this(defaultOffensiveWeights(), defaultDefensiveWeights());
    }

    public TeamStrength(Map<Position, Double> offensiveWeights, Map<Position, Double> defensiveWeights) {
        this.offensiveWeights = offensiveWeights;
        this.defensiveWeights = defensiveWeights;
    }

    private static Map<Position, Double> defaultOffensiveWeights() {
        Map<Position, Double> weights = new EnumMap<>(Position.class);
        weights.put(Position.GOALKEEPER, 0.0);
        weights.put(Position.DEFENDER, 0.5);
        weights.put(Position.MIDFIELDER, 1.5);
        weights.put(Position.ATTACKER, 2.0);
        return weights;
    }

    private static Map<Position, Double> defaultDefensiveWeights() {
        Map<Position, Double> weights = new EnumMap<>(Position.class);
        weights.put(Position.GOALKEEPER, 0.0);
        weights.put(Position.DEFENDER, 2.0);
        weights.put(Position.MIDFIELDER, 1.0);
        weights.put(Position.ATTACKER, 0.5);
        return weights;
    }

    public double offensiveSkill(Team team) {
        return weightedAverage(team, Player::getPassing, offensiveWeights);
    }

    public double defensiveSkill(Team team) {
        return weightedAverage(team, Player::getDefending, defensiveWeights);
    }

    private double weightedAverage(Team team, ToIntFunction<Player> attribute, Map<Position, Double> weights) {
        List<Player> players = team.getStartingLineUp();
        double weightedSum = 0;
        double totalWeight = 0;

        for (Player player : players) {
            double weight = weights.getOrDefault(player.getPosition(), 0.0);
            weightedSum += attribute.applyAsInt(player) * weight;
            totalWeight += weight;
        }

        return totalWeight == 0 ? 0 : weightedSum / totalWeight;
    }
}
