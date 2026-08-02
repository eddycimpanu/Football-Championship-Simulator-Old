package org.example.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class RouletteWheel<T> {

    private final Map<T, Double> fitnessValues;

    public RouletteWheel() {
        this.fitnessValues = new LinkedHashMap<>();
    }

    public RouletteWheel<T> add(T option, double fitness) {
        fitnessValues.put(option, fitness);
        return this;
    }

    public T spin(Random random) {
        double totalFitness = fitnessValues.values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = random.nextDouble() * totalFitness;

        double cumulative = 0.0;
        for (Map.Entry<T, Double> entry : fitnessValues.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                return entry.getKey();
            }
        }

        return fitnessValues.keySet().iterator().next();
    }
}