package com.example.weighttracker_ayala;

import java.util.ArrayList;
import java.util.List;

public class WeightTrends {

    public enum Trend { LOSING, GAINING, HOLDING }

    // a change in the moving average smaller than this many lbs counts as holding
    private static final double TREND_TOLERANCE = 0.1;

    // entries arrive newest first from the database, so reverse them into chronological order
    private static List<Double> chronologicalWeights(List<WeightEntry> entries) {
        List<Double> weights = new ArrayList<>();
        for (int i = entries.size() - 1; i >= 0; i--) {
            weights.add(entries.get(i).getWeight());
        }
        return weights;
    }

    // one average per entry in chronological order. the first entries average whatever
    // points exist so far instead of waiting for a full window. empty if window <= 0
    public static List<Double> movingAverage(List<WeightEntry> entries, int window) {
        List<Double> averages = new ArrayList<>();
        if (window <= 0) {
            return averages;
        }

        List<Double> weights = chronologicalWeights(entries);
        double sum = 0;
        for (int i = 0; i < weights.size(); i++) {
            sum += weights.get(i);
            // drop the point that just left the window
            if (i >= window) {
                sum -= weights.get(i - window);
            }
            averages.add(sum / Math.min(i + 1, window));
        }
        return averages;
    }

    // most recent moving average, or 0 if there is none
    public static double latestAverage(List<WeightEntry> entries, int window) {
        List<Double> averages = movingAverage(entries, window);
        return averages.isEmpty() ? 0 : averages.get(averages.size() - 1);
    }

    // compares the latest moving average to the one before it, holding if there is only one
    public static Trend trend(List<WeightEntry> entries, int window) {
        List<Double> averages = movingAverage(entries, window);
        if (averages.size() < 2) {
            return Trend.HOLDING;
        }

        double change = averages.get(averages.size() - 1) - averages.get(averages.size() - 2);
        if (change > TREND_TOLERANCE) {
            return Trend.GAINING;
        }
        if (change < -TREND_TOLERANCE) {
            return Trend.LOSING;
        }
        return Trend.HOLDING;
    }
}
