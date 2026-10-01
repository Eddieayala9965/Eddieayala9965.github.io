package com.example.weighttracker_ayala;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// converts weight entry dates between the UI's MM/dd/yyyy display format and the
// database's sortable yyyy-MM-dd storage format
public class DateFormats {

    private static final String DISPLAY_PATTERN = "MM/dd/yyyy";
    private static final String STORAGE_PATTERN = "yyyy-MM-dd";

    private static SimpleDateFormat displayFormat() {
        SimpleDateFormat format = new SimpleDateFormat(DISPLAY_PATTERN, Locale.US);
        format.setLenient(false);
        return format;
    }

    private static SimpleDateFormat storageFormat() {
        SimpleDateFormat format = new SimpleDateFormat(STORAGE_PATTERN, Locale.US);
        format.setLenient(false);
        return format;
    }

    // converts a UI-facing MM/dd/yyyy date into the sortable yyyy-MM-dd storage format
    public static String toStorage(String displayDate) {
        return storageFormat().format(parse(displayFormat(), displayDate));
    }

    // converts a stored yyyy-MM-dd date back into the UI-facing MM/dd/yyyy format
    public static String toDisplay(String storedDate) {
        return displayFormat().format(parse(storageFormat(), storedDate));
    }

    // parses with the given format, rejecting anything invalid, out of range, or with
    // leftover characters, instead of silently accepting junk
    private static Date parse(SimpleDateFormat format, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Date must not be null or empty");
        }
        String trimmed = value.trim();
        ParsePosition position = new ParsePosition(0);
        Date parsed = format.parse(trimmed, position);
        if (parsed == null || position.getIndex() != trimmed.length()) {
            throw new IllegalArgumentException("Invalid date: " + value);
        }
        return parsed;
    }
}
