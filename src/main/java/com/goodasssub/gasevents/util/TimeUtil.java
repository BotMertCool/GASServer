package com.goodasssub.gasevents.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static long convertToTimeInMillis(String input) {
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
    }

    public static String formatTime(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Time in milliseconds cannot be negative.");
        }

        final long ONE_SECOND = 1000;
        final long ONE_MINUTE = ONE_SECOND * 60;
        final long ONE_HOUR = ONE_MINUTE * 60;
        final long ONE_DAY = ONE_HOUR * 24;
        final long ONE_YEAR = ONE_DAY * 365;

        if (millis >= ONE_YEAR) {
            long years = millis / ONE_YEAR;
            return years + " year" + (years > 1 ? "s" : "");
        } else if (millis >= ONE_DAY) {
            long days = millis / ONE_DAY;
            return days + " day" + (days > 1 ? "s" : "");
        } else if (millis >= ONE_HOUR) {
            long hours = millis / ONE_HOUR;
            long minutes = (millis % ONE_HOUR) / ONE_MINUTE;
            return hours + " hour" + (hours > 1 ? "s" : "") + " and " + minutes + " minute" + (minutes > 1 ? "s" : "");
        } else if (millis >= ONE_MINUTE) {
            long minutes = millis / ONE_MINUTE;
            long seconds = (millis % ONE_MINUTE) / ONE_SECOND;
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " and " + seconds + " second" + (seconds > 1 ? "s" : "");
        } else if (millis >= ONE_SECOND) {
            long seconds = millis / ONE_SECOND;
            return seconds + " second" + (seconds > 1 ? "s" : "");
        } else {
            return "0 seconds";
        }
    }
}
