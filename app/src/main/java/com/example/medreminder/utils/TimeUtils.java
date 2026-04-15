package com.example.medreminder.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

    /**
     * Formats hour (0-23) and minute to display string like "08:00 AM" or "2:30 PM"
     */
    public static String format12Hour(int hour24, int minute) {
        String amPm = hour24 >= 12 ? "PM" : "AM";
        int hour12 = hour24 % 12;
        if (hour12 == 0) hour12 = 12;
        return String.format(Locale.getDefault(), "%d:%02d %s", hour12, minute, amPm);
    }

    /**
     * Parses a user-entered time string into a 2-element int array [hour24, minute].
     * Supports formats:
     *   "10:00 PM", "10:00PM", "10:00 pm"
     *   "14:00" (24h -> converts to 2:00 PM internally, stores as 14)
     *   "10:00" (no AM/PM -> assumes AM)
     *   "8", "8:00"
     * Returns null if unparseable.
     */
    public static int[] parse(String input) {
        if (input == null) return null;
        input = input.trim();
        if (input.isEmpty()) return null;

        // Detect AM/PM
        String upper = input.toUpperCase(Locale.ROOT);
        boolean hasPM = upper.contains("PM");
        boolean hasAM = upper.contains("AM");

        // Strip AM/PM text
        String cleaned = upper.replaceAll("[APM ]", "").trim();
        if (cleaned.isEmpty()) return null;

        int hour, minute;

        if (cleaned.contains(":")) {
            String[] parts = cleaned.split(":");
            try {
                hour = Integer.parseInt(parts[0].trim());
                minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            try {
                hour = Integer.parseInt(cleaned);
                minute = 0;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (minute < 0 || minute > 59) return null;

        // Convert to 24-hour
        if (hasPM || hasAM) {
            // User specified AM/PM — treat hour as 12-hour
            if (hour < 1 || hour > 12) return null;
            if (hasPM && hour != 12) hour += 12;
            if (hasAM && hour == 12) hour = 0;
        } else {
            // No AM/PM specified
            if (hour > 23) return null;
            // If hour <= 12, assume AM (store as-is since 0-12 are valid 24h morning hours)
            // If hour > 12, it's already 24h format (e.g., 14:00)
        }

        return new int[]{hour, minute};
    }
}
