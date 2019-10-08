package com.kong.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author kong
 */
public class DateUtil {

    private DateUtil() {}

    public static Integer getTodayMinSecond() {

        return (int)LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toEpochSecond(ZoneOffset.of("+8"));
    }

    public static Integer getTodayMaxSecond() {

        return (int)LocalDateTime.of(LocalDate.now(), LocalTime.MAX).toEpochSecond(ZoneOffset.of("+8"));
    }

    public static Integer getDayMinSecond(int daysToSubtract) {

        return (int)LocalDateTime.of(LocalDate.now().minusDays(daysToSubtract), LocalTime.MIN).toEpochSecond(ZoneOffset.of("+8"));
    }

    public static Integer getCurrentTime() {

        return (int)LocalDateTime.of(LocalDate.now(), LocalTime.now()).toEpochSecond(ZoneOffset.of("+8"));
    }

    public static String getCurrentTime(DateTimeFormatter formatter) {

        return LocalDateTime.now().format(formatter);
    }
}

