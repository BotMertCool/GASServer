package com.goodasssub.gasevents.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static long convertToTimeInMillis(String input) {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            String numberPart = input.replaceAll("[^0-9]", "");

            long timeValue = Long.parseLong(numberPart);

            String unit = input.replaceAll("[0-9]", "").toLowerCase();

            return switch (unit) {
                case "y" -> // years
                    currentTimeMillis + TimeUnit.DAYS.toMillis(timeValue * 365);
                case "h" -> // minutes
                    currentTimeMillis + TimeUnit.HOURS.toMillis(timeValue);
                case "m" -> // minutes
                    currentTimeMillis + TimeUnit.MINUTES.toMillis(timeValue);
                case "s" -> // seconds
                    currentTimeMillis + TimeUnit.SECONDS.toMillis(timeValue);
                case "d" -> // days
                    currentTimeMillis + TimeUnit.DAYS.toMillis(timeValue);
                default -> throw new IllegalArgumentException("Invalid time unit: " + unit);
            };
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Invalid time unit");
        }
    }

    public static String formatTime(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Time in milliseconds cannot be negative.");
        }

        millis += 999;

        final long ONE_SECOND = 1000;
        final long ONE_MINUTE = ONE_SECOND * 60;
        final long ONE_HOUR = ONE_MINUTE * 60;
        final long ONE_DAY = ONE_HOUR * 24;
        final long ONE_YEAR = ONE_DAY * 365;

        StringBuilder result = new StringBuilder();

        if (millis >= ONE_YEAR) {
            long years = millis / ONE_YEAR;
            result.append(years).append(" year").append(years > 1 ? "s" : "");
            millis %= ONE_YEAR;
        }
        if (millis >= ONE_DAY) {
            if (!result.isEmpty()) result.append(", ");
            long days = millis / ONE_DAY;
            result.append(days).append(" day").append(days > 1 ? "s" : "");
            millis %= ONE_DAY;
        }
        if (millis >= ONE_HOUR) {
            if (!result.isEmpty()) result.append(", ");
            long hours = millis / ONE_HOUR;
            result.append(hours).append(" hour").append(hours > 1 ? "s" : "");
            millis %= ONE_HOUR;
        }
        if (millis >= ONE_MINUTE) {
            if (!result.isEmpty()) result.append(", ");
            long minutes = millis / ONE_MINUTE;
            result.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
            millis %= ONE_MINUTE;
        }
        if (millis >= ONE_SECOND) {
            if (!result.isEmpty()) result.append(", ");
            long seconds = millis / ONE_SECOND;
            result.append(seconds).append(" second").append(seconds > 1 ? "s" : "");
        }
        
        if (result.isEmpty()) {
            result.append("0 seconds");
        }

        return result.toString();
    }
}
