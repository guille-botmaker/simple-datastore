package com.zupcat.util;

import com.zupcat.model.PersistentObject;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Pack of common date operations
 */
public final class TimeUtils {

    public static long getNormalizedCurrentTimeMillisForBeginingOfDay() {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(getCalendar().getTime());

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }


    public static long getNormalizedCurrentTimeMillisForEndingOfDay() {
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(getCalendar().getTime());

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTimeInMillis();
    }

    public static long getNormalizedCurrentTimeMillis() {
        return getCalendar().getTimeInMillis();
    }

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
//
//    public static int getDaysSinceStandardModificationTime(final long l) {
//        return getMinutesSinceStandardModificationTime(l) / (60 * 24);
//    }
//
//    /**
//     * Returns the days passed since the date from parameter 'l', but set to 00:00:00.000 Hs.
//     */
//    public static int getDaysPassedSinceInitialStandardModificationTime(final long l) {
//        final Date dateFrom = getInitialDateFromStandardModificationTime(l);
//        final Date today = getInitialDateFromStandardModificationTime(buildStandardModificationTime());
//
//        final DateTime dateFromDateTime = new DateTime(dateFrom);
//        final DateTime todayDateTime = new DateTime(today);
//        final int days = Days.daysBetween(dateFromDateTime, todayDateTime).getDays();
//
//        Logger.getLogger(TimeUtils.class.getPropertyName()).log(Level.SEVERE, "getDaysPassedSinceInitialStandardModificationTime. l [" + l + "], dateFrom [" + dateFrom + "], today [" + today + "], dateFromDateTime [" + dateFromDateTime + "], todayDateTime [" + todayDateTime + "], days [" + days + "]", new Exception());
//
//        return days;
//    }
//
//    /**
//     * Returns the date from parameter 'l', but set to 00:00:00.000 Hs.
//     */
//    public static Date getInitialDateFromStandardModificationTime(final long l) {
//        final Calendar calendar = getCalendar();
//        calendar.setTime(getDateFromStandardModificationTime(l));
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTime();
//    }
//
//    public static int getMinutesSinceStandardModificationTime(final long l) {
//
//        if (l == 0l)
//            return 0;
//
//        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
//
//        final GregorianCalendar now = new GregorianCalendar();
//        final GregorianCalendar modified = new GregorianCalendar();
//
//        try {
//            now.setTime(dateFormat.parse("" + TimeUtils.buildStandardModificationTime(new Date())));
//            modified.setTime(dateFormat.parse("" + l));
//
//        } catch (final ParseException e) {
//            throw new GAEException(ErrorType.PROGRAMMING, e);
//        }
//
//        return ((int) ((now.getTime().getTime() - modified.getTime().getTime()) / 60000));
//    }
//
//    public static Date getDateFromStandardModificationTime(final long l) {
//        try {
//            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PersistenceObject.DATE_FORMAT);
//            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//
//            return simpleDateFormat.parse(Long.toString(l));
//        } catch (final ParseException _parseException) {
//            throw new GAEException(ErrorType.PROGRAMMING, _parseException, "Problems parsing long to date [" + l
//                    + "]: " + _parseException.getMessage());
//        }
//    }
//
//    /**
//     * @return YYYYMMDD
//     */
//    public static int buildStandardToday() {
//        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
//        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
//
//        return Integer.parseInt(simpleDateFormat.format(new Date()));
//    }
//
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
