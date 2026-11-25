package com.clubsync.util;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {
    private static final String DEFAULT_PATTERN = "dd/MM/yyyy HH:mm";
    private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat(DEFAULT_PATTERN, Locale.FRENCH);
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("dd", Locale.FRENCH);
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMM", Locale.FRENCH);
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.FRENCH);
    private static final SimpleDateFormat DATE_SHORT_FORMAT = new SimpleDateFormat("dd/MM", Locale.FRENCH);

    private DateUtils() {}

    public static String formatDateTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return formatDateTime(timestamp.toDate());
    }

    public static synchronized String formatDateTime(Date date) {
        if (date == null) return "";
        return DEFAULT_FORMAT.format(date);
    }

    public static synchronized String getDay(Timestamp timestamp) {
        if (timestamp == null) return "";
        return DAY_FORMAT.format(timestamp.toDate());
    }

    public static synchronized String getMonthShort(Timestamp timestamp) {
        if (timestamp == null) return "";
        return MONTH_FORMAT.format(timestamp.toDate()).toUpperCase(Locale.FRENCH).replace(".", "");
    }

    public static synchronized String getTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return TIME_FORMAT.format(timestamp.toDate());
    }

    public static synchronized String getDateShort(Timestamp timestamp) {
        if (timestamp == null) return "";
        return DATE_SHORT_FORMAT.format(timestamp.toDate());
    }

    public static synchronized Date parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return DEFAULT_FORMAT.parse(text.trim());
        } catch (ParseException e) {
            return null;
        }
    }
}
