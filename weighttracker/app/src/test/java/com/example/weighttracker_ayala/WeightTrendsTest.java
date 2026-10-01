package com.example.weighttracker_ayala;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class WeightTrendsTest {

    private static WeightEntry entry(double weight) {
        return new WeightEntry(0, 0, "", weight, "");
    }

    // takes weights oldest first and returns them newest first, like getWeightEntries()
    private static List<WeightEntry> newestFirst(double... chronologicalWeights) {
        List<WeightEntry> entries = new ArrayList<>();
        for (double weight : chronologicalWeights) {
            entries.add(entry(weight));
        }
        Collections.reverse(entries);
        return entries;
    }

    @Test
    public void movingAverage_multipleEntries() {
        List<WeightEntry> entries = newestFirst(10, 20, 30, 40);

        assertEquals(Arrays.asList(10.0, 15.0, 20.0, 30.0), WeightTrends.movingAverage(entries, 3));
    }

    @Test
    public void movingAverage_reversesNewestFirstInput() {
        List<WeightEntry> entries = Arrays.asList(entry(30), entry(20), entry(10));

        assertEquals(Arrays.asList(10.0, 15.0, 25.0), WeightTrends.movingAverage(entries, 2));
    }

    @Test
    public void movingAverage_emptyList() {
        assertTrue(WeightTrends.movingAverage(new ArrayList<>(), 7).isEmpty());
    }

    @Test
    public void movingAverage_singleEntry() {
        assertEquals(Arrays.asList(180.0), WeightTrends.movingAverage(newestFirst(180), 7));
    }

    @Test
    public void movingAverage_fewerEntriesThanWindow() {
        assertEquals(Arrays.asList(180.0, 179.0), WeightTrends.movingAverage(newestFirst(180, 178), 5));
    }

    @Test
    public void movingAverage_windowZeroOrNegative() {
        List<WeightEntry> entries = newestFirst(10, 20, 30);

        assertTrue(WeightTrends.movingAverage(entries, 0).isEmpty());
        assertTrue(WeightTrends.movingAverage(entries, -3).isEmpty());
    }

    @Test
    public void latestAverage_multipleEntries() {
        assertEquals(30.0, WeightTrends.latestAverage(newestFirst(10, 20, 30, 40), 3), 0.0001);
    }

    @Test
    public void latestAverage_emptyList() {
        assertEquals(0.0, WeightTrends.latestAverage(new ArrayList<>(), 7), 0.0001);
    }

    @Test
    public void latestAverage_singleEntry() {
        assertEquals(180.0, WeightTrends.latestAverage(newestFirst(180), 7), 0.0001);
    }

    @Test
    public void latestAverage_fewerEntriesThanWindow() {
        assertEquals(179.0, WeightTrends.latestAverage(newestFirst(180, 178), 5), 0.0001);
    }

    @Test
    public void latestAverage_windowZero() {
        assertEquals(0.0, WeightTrends.latestAverage(newestFirst(10, 20, 30), 0), 0.0001);
    }

    @Test
    public void trend_losing() {
        assertEquals(WeightTrends.Trend.LOSING, WeightTrends.trend(newestFirst(180, 180, 180, 177), 3));
    }

    @Test
    public void trend_gaining() {
        assertEquals(WeightTrends.Trend.GAINING, WeightTrends.trend(newestFirst(180, 180, 180, 183), 3));
    }

    @Test
    public void trend_holdingWithinTolerance() {
        assertEquals(WeightTrends.Trend.HOLDING, WeightTrends.trend(newestFirst(180, 180, 180, 180.15), 3));
    }

    @Test
    public void trend_emptyList() {
        assertEquals(WeightTrends.Trend.HOLDING, WeightTrends.trend(new ArrayList<>(), 7));
    }

    @Test
    public void trend_singleEntry() {
        assertEquals(WeightTrends.Trend.HOLDING, WeightTrends.trend(newestFirst(180), 7));
    }

    @Test
    public void trend_fewerEntriesThanWindow() {
        assertEquals(WeightTrends.Trend.LOSING, WeightTrends.trend(newestFirst(180, 178), 5));
    }

    @Test
    public void trend_windowZero() {
        assertEquals(WeightTrends.Trend.HOLDING, WeightTrends.trend(newestFirst(180, 170, 160), 0));
    }
}
