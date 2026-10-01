package com.example.weighttracker_ayala;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class DateFormatsTest {

    @Test
    public void toStorage_roundTripsThroughToDisplay() {
        assertEquals("01/15/2026", DateFormats.toDisplay(DateFormats.toStorage("01/15/2026")));
    }

    @Test
    public void toDisplay_roundTripsThroughToStorage() {
        assertEquals("2026-01-15", DateFormats.toStorage(DateFormats.toDisplay("2026-01-15")));
    }

    @Test
    public void toStorage_sortsCorrectlyAcrossYearBoundary() {
        String dec31 = DateFormats.toStorage("12/31/2025");
        String jan1 = DateFormats.toStorage("01/01/2026");

        assertTrue(dec31.compareTo(jan1) < 0);
    }

    @Test
    public void toStorage_rejectsInvalidMonthAndDay() {
        assertThrows(() -> DateFormats.toStorage("13/45/2026"));
    }

    @Test
    public void toStorage_rejectsNull() {
        assertThrows(() -> DateFormats.toStorage(null));
    }

    @Test
    public void toStorage_rejectsEmpty() {
        assertThrows(() -> DateFormats.toStorage(""));
        assertThrows(() -> DateFormats.toStorage("   "));
    }

    @Test
    public void toDisplay_rejectsInvalidDate() {
        assertThrows(() -> DateFormats.toDisplay("2026-13-45"));
    }

    @Test
    public void toDisplay_rejectsNullAndEmpty() {
        assertThrows(() -> DateFormats.toDisplay(null));
        assertThrows(() -> DateFormats.toDisplay(""));
    }

    private static void assertThrows(Runnable action) {
        try {
            action.run();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }
}
