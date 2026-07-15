package org.example.rating;

import java.util.HashMap;
import java.util.Map;

public class StatImportance {

    private final Map<String, Double> importance;

    public StatImportance() {
        this.importance = new HashMap<>();
    }

    public StatImportance set(String statName, double value) {
        importance.put(statName, value);
        return this;
    }

    public double get(String statName) {
        return importance.getOrDefault(statName, 0.0);
    }
}