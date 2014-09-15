package com.zupcat.util;

import com.zupcat.model.PersistentObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Pack of common date operations
 */
public final class TimeUtils {

//    public static long getNormalizedCurrentTimeMillisForBeginingOfDay() {
//        final GregorianCalendar calendar = new GregorianCalendar();
//        calendar.setTime(getCalendar().getTime());
//
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//
//        return calendar.getTimeInMillis();
//    }
//
//
//    public static long getNormalizedCurrentTimeMillisForEndingOfDay() {
//        final GregorianCalendar calendar = new GregorianCalendar();
//        calendar.setTime(getCalendar().getTime());
//
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
//        calendar.set(Calendar.SECOND, 59);
//        calendar.set(Calendar.MILLISECOND, 999);
//
//        return calendar.getTimeInMillis();
//    }
//
//    public static long getNormalizedCurrentTimeMillis() {
//        return getCalendar().getTimeInMillis();
//    }

    public static Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
    }

    //    public static int getNowDayOfMonth() {
//        return getCalendar().get(Calendar.DAY_OF_MONTH);
//    }
//
//    public static int getNowWeek() {
//        return getCalendar().get(Calendar.WEEK_OF_YEAR);
//    }
//
//    public static int getNowMonth() {
//        return getCalendar().get(Calendar.MONTH);
//    }
//
//    public static String getNowYearAndWeek() {
//        return (buildStandardToday() / 10000) + ":" + getNowWeek();
//    }
//
//    public static String getNowYearAndMonth() {
//        return (buildStandardToday() / 10000) + ":" + getNowMonth();
//    }
//
//    public static int getLastWeek(final int week) {
//        int lastWeek;
//        if (week == 1) { // primer semana del anio
//            Calendar calendar = getCalendar();
//            calendar.add(Calendar.WEEK_OF_YEAR, -1);
//            lastWeek = calendar.get(Calendar.WEEK_OF_YEAR);
//        } else {
//            lastWeek = week - 1;
//        }
//        return lastWeek;
//    }

    public static int getDaysSinceStandardModificationTime(final long l) {
        return getMinutesSinceStandardModificationTime(l) / (60 * 24);
    }

    /**
     * Returns the date from parameter 'l', but set to 00:00:00.000 Hs.
     */
    public static Date getInitialDateFromStandardModificationTime(final long l) {
        final Calendar calendar = getCalendar();
        calendar.setTime(getDateFromStandardModificationTime(l));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static long getCurrentMillis() {
        return getCalendar().getTime().getTime();
    }

    public static int getMinutesSinceStandardModificationTime(final long l) {
        if (l == 0l) {
            return 0;
        }

        final SimpleDateFormat dateFormat = new SimpleDateFormat(PersistentObject.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        final GregorianCalendar now = new GregorianCalendar();
        final GregorianCalendar modified = new GregorianCalendar();

        try {
            now.setTime(dateFormat.parse("" + TimeUtils.buildStandardModificationTime()));
            modified.setTime(dateFormat.parse("" + l));

        } catch (final ParseException e) {
            throw new RuntimeException("Problems parsing date from [" + l + "]: " + e.getMessage(), e);
        }

        return ((int) (Math.abs(now.getTime().getTime() - modified.getTime().getTime()) / 60000));
    }

    public static Date getDateFromStandardModificationTime(final long l) {
        try {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PersistentObject.DATE_FORMAT);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            return simpleDateFormat.parse(Long.toString(l));

        } catch (final ParseException _parseException) {
            throw new RuntimeException("Problems parsing date from [" + l + "]: " + _parseException.getMessage(), _parseException);
        }
    }


    /**
     * @return YYYYMMDD
     */
    public static int buildStandardToday() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        return Integer.parseInt(simpleDateFormat.format(getCalendar().getTime()));
    }

    public static long buildStandardModificationTimeForFirstTimeOfDay() {
        final long _0s = 100000000000l;
        return (buildStandardModificationTime() / _0s) * _0s;
    }

    public static long buildStandardModificationTime() {
        return buildStandardModificationTime(getCalendar().getTime());
    }

    public static long buildStandardModificationTime(final Date date) {
        return buildStandardModificationTime(date, RandomUtils.getInstance().getRandomInt(99));
    }

    public static long buildStandardModificationTime(final Date date, final int postFix) {
        final StringBuilder builder = new StringBuilder(17);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PersistentObject.DATE_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        builder.append(simpleDateFormat.format(date));

        if (postFix < 10) {
            builder.append("0");
        }
        builder.append(postFix);

        return Long.parseLong(builder.toString());
    }

//    public static long getTimeForNextDay(final DayOfWeek dayOfWeek, final boolean onNextWeek) {
//
//        final Calendar calendar = getCalendar();
//        final Date now = calendar.getTime();
//
//        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek.getCalendarDayConstantValue());
//        if (onNextWeek)
//            calendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR) + 1);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//
//        final Date closureTime = calendar.getTime();
//        return closureTime.getTime() - now.getTime();
//    }
}
