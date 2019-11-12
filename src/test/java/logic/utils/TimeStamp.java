package logic.utils;

import framework.config.Config;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

public class TimeStamp {

    public final static String DATE_FORMAT = "dd MMM yyyy";
    public final static String DATE_FORMAT2 = "yyyyMMdd";
    public final static String DATE_FORMAT3 = "dd-MMM-yy";
    public final static String DATE_FORMAT4 = "dd/MM/yyyy";
    public final static String DATE_FORMAT5 = "yyyyMMdd HH mm";
    public final static String DATE_FORMAT6 = "dd/MM";
    public final static String DATE_FORMAT7 = "yy";
    public final static String DATE_FORMAT_IN_PDF = "dd/MM/yyyy";
    public final static String DATE_FORMAT_IN_PDF2 = "dd-MMM-yyyy";
    public final static String DATE_FORMAT_IN_PDF3 = "MM/yyyy";
    public final static String DATE_FORMAT_XML = "yyyy-MM-dd";
    public final static String DATE_TIME_FORMAT = "dd/MM/yyyy hh:mma";
    public final static String DATE_FORMAT_XINVOICE = "dd/MMM/yyyy";
    public final static int JANUARY = 1;
    public final static int FEBRUARY = 2;
    public final static int MARCH = 3;
    public final static int APRIL = 4;
    public final static int MAY = 5;
    public final static int JUNE = 6;
    public final static int JULY = 7;
    public final static int AUGUST = 8;
    public final static int SEPTEMBER = 9;
    public final static int OCTOBER = 10;
    public final static int NOVEMBER = 11;
    public final static int DECEMBER = 12;

    public static Date Today() {
        return Date.valueOf(LocalDate.now());
    }

    public static Date TodayMinus1Day() {
        return Date.valueOf(LocalDate.now().minusDays(1));
    }

    public static Date TodayMinus1Month() {
        return Date.valueOf(LocalDate.now().minusMonths(1));
    }

    public static Date TodayMinus1MonthMinus1Day() {
        return Date.valueOf(LocalDate.now().minusMonths(1).minusDays(1));
    }

    public static Date TodayMinus2Days() {
        return Date.valueOf(LocalDate.now().minusDays(2));
    }

    public static Date TodayPlus1Day() {
        return Date.valueOf(LocalDate.now().plusDays(1));
    }

    public static Date TodayMinus3Days() {
        return Date.valueOf(LocalDate.now().minusDays(3));
    }

    public static Date TodayMinus4Days() {
        return Date.valueOf(LocalDate.now().minusDays(4));
    }

    public static Date TodayMinus5Days() {
        return Date.valueOf(LocalDate.now().minusDays(5));
    }

    public static Date TodayMinus6Days() {
        return Date.valueOf(LocalDate.now().minusDays(6));
    }

    public static Date TodayMinus7Days() {
        return Date.valueOf(LocalDate.now().minusDays(7));
    }

    public static Date TodayMinus8Days() {
        return Date.valueOf(LocalDate.now().minusDays(8));
    }

    public static Date TodayMinus13Days() {
        return Date.valueOf(LocalDate.now().minusDays(13));
    }

    public static Date TodayMinus15Days() {
        return Date.valueOf(LocalDate.now().minusDays(15));
    }

    public static Date TodayPlus1Month() {
        return Date.valueOf(LocalDate.now().plusMonths(1));
    }

    public static Date TodayPlus2Month() {
        return Date.valueOf(LocalDate.now().plusMonths(2));
    }

    public static Date TodayPlus2MonthMinus1Day() {
        return Date.valueOf(LocalDate.now().plusMonths(2).minusDays(1));
    }

    public static Date TodayPlus1MonthMinus1Day() {
        return Date.valueOf(LocalDate.now().plusMonths(1).minusDays(1));
    }

    public static Date TodayPlus1MonthPlus1Day() {
        return Date.valueOf(LocalDate.now().plusMonths(1).plusDays(1));
    }

    public static Date iMinDate() {
        return TodayMinus13Days();
    }

    public static Date iMaxDate() {
        return TodayMinus3Days();
    }

    public static Date TodayMinus20Days() {
        return Date.valueOf(LocalDate.now().minusDays(20));
    }

    public static Date TodayMinus14Days() {
        return Date.valueOf(LocalDate.now().minusDays(14));
    }

    public static Date TodayMinus10Days() {
        return Date.valueOf(LocalDate.now().minusDays(10));
    }


    public static Date TodayMinus15DaysAdd1Month() {
        return Date.valueOf(LocalDate.now().minusDays(15).plusMonths(1));
    }

    public static Date TodayMinus16DaysAdd2Months() {
        return Date.valueOf(LocalDate.now().minusDays(16).plusMonths(2));
    }

    public static Date TodayMinus16DaysAdd1Month() {
        return Date.valueOf(LocalDate.now().minusDays(16).plusMonths(1));
    }

    public static Date TodayMinus15DaysAdd2Months() {
        return Date.valueOf(LocalDate.now().minusDays(15).plusMonths(2));
    }

    public static Date TodayMinus16DaysAdd3Months() {
        return Date.valueOf(LocalDate.now().minusDays(16).plusMonths(3));
    }

    public static Date TodayMinus30Days() {
        return Date.valueOf(LocalDate.now().minusDays(30));
    }

    public static Date TodayMinus35Days() {
        return Date.valueOf(LocalDate.now().minusDays(35));
    }

    public static Date TodayPlus1YearMinus1Day() {
        return Date.valueOf(LocalDate.now().plusYears(1).minusDays(1));
    }

    public static long TodayPlus1MonthMinusToday() {
        long elapsedDays = ChronoUnit.DAYS.between(LocalDate.now().plusMonths(1), LocalDate.now());
        return Math.abs(elapsedDays);
    }

    public static Date TodayMinus1Hour() {
        return Date.valueOf(String.valueOf(LocalDateTime.now().minusHours(1).toLocalDate()));
    }


    public static String DateFormatXml() {
        return DATE_FORMAT_XML + Config.getProp("timeZone");
    }


    public static Date TodayPlus1Year() {
        return Date.valueOf(String.valueOf(LocalDateTime.now().plusYears(1).toLocalDate()));
    }


    public static long TodayMinusTodayMinus1MonthMinus1Day() {
        LocalDate day1 = LocalDate.now();
        LocalDate day2 = LocalDate.now().minusMonths(1).minusDays(1);
        return ChronoUnit.DAYS.between(day2, day1);
    }

    public static Date TodayPlus4Years() {
        return Date.valueOf(String.valueOf(LocalDateTime.now().plusYears(4).toLocalDate()));
    }

    public static String DateTimeFormatXml() {
//        String timeZone = Config.getProp("timeZone");
//        String format =  "yyyy-MM-dd HH:mm:ss.SSS" + timeZone;
//        return format;

        DateTimeZone timeZone = DateTimeZone.forID(Config.getProp("timeZoneId"));
        DateTime now = new DateTime(timeZone);
        return now.toString();
    }

    public static Date TodayMinus1DayPlus23Months() {
        return Date.valueOf(LocalDate.now().minusDays(1).plusMonths(23));
    }

    public static Date TodayPlusMonth(int numberOfMonth) {
        return Date.valueOf(LocalDate.now().plusMonths(numberOfMonth));
    }

    public static String TimeZone() {
        ZoneId zone = ZoneId.of(Config.getProp("timeZoneId"));
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zone);
        return zonedDateTime.getOffset().toString();
    }

    public static long todayPlus1MonthMinus1DayMinusToday() {
        LocalDate day1 = LocalDate.now();
        LocalDate day2 = LocalDate.now().plusMonths(1).minusDays(1);
        return ChronoUnit.DAYS.between(day2, day1);
    }
    public static Date TodayMinus1MonthMinus20Day() {
        return Date.valueOf(LocalDate.now().minusMonths(1).minusDays(20));
    }
    public static long todayPlus1MonthMinusToday() {
        LocalDate day1 = LocalDate.now();
        LocalDate day2 = LocalDate.now().plusMonths(1);
        return Math.abs(ChronoUnit.DAYS.between(day2, day1));
    }
    public static Date TodayPlus10Days() {
        return Date.valueOf(LocalDate.now().plusDays(10));
    }
    public static Date TodayPlus3Month() {
        return Date.valueOf(LocalDate.now().plusMonths(3));
    }
    public static Date TodayPlus4Month() {
        return Date.valueOf(LocalDate.now().plusMonths(4));
    }
    public static Date TodayPlus5Month() {
        return Date.valueOf(LocalDate.now().plusMonths(5));
    }
    public static Date TodayPlus6Month() {
        return Date.valueOf(LocalDate.now().plusMonths(6));
    }
    public static Date TodayPlus7Month() {
        return Date.valueOf(LocalDate.now().plusMonths(7));
    }
    public static Date TodayPlus8Month() {
        return Date.valueOf(LocalDate.now().plusMonths(8));
    }
    public static Date TodayPlus10Month() {
        return Date.valueOf(LocalDate.now().plusMonths(10));
    }
    public static Date TodayPlus9Month() {
        return Date.valueOf(LocalDate.now().plusMonths(9));
    }
    public static Date TodayPlus11Month() {
        return Date.valueOf(LocalDate.now().plusMonths(11));
    }
    public static Date TodayPlus12Month() {
        return Date.valueOf(LocalDate.now().plusMonths(12));
    }
    public static Date TodayPlus13Month() {
        return Date.valueOf(LocalDate.now().plusMonths(13));
    }
    public static Date TodayPlus14Month() {
        return Date.valueOf(LocalDate.now().plusMonths(14));
    }
    public static Date TodayPlus15Month() {
        return Date.valueOf(LocalDate.now().plusMonths(15));
    }
    public static Date TodayPlus16Month() {
        return Date.valueOf(LocalDate.now().plusMonths(16));
    }
    public static Date TodayPlus17Month() {
        return Date.valueOf(LocalDate.now().plusMonths(17));
    }
    public static Date TodayPlus18Month() {
        return Date.valueOf(LocalDate.now().plusMonths(18));
    }
    public static Date TodayPlus19Month() {
        return Date.valueOf(LocalDate.now().plusMonths(19));
    }
    public static Date TodayPlus20Month() {
        return Date.valueOf(LocalDate.now().plusMonths(20));
    }
    public static Date TodayPlus21Month() {
        return Date.valueOf(LocalDate.now().plusMonths(21));
    }

    public static Date TodayPlus22Month() {
        return Date.valueOf(LocalDate.now().plusMonths(22));
    }
    public static Date TodayPlusDayAndMonth(int day,int month) {
        return Date.valueOf(LocalDate.now().plusDays(day).plusMonths(month));
    }

    public static long minusTodayMinusMonth(int month) {
        LocalDate day1 = LocalDate.now();
        LocalDate day2 = LocalDate.now().minusMonths(month);
        return Math.abs(ChronoUnit.DAYS.between(day2, day1));
    }

    public static long todayMinusTodayMinusDate(Date date) {
        LocalDate day1 = LocalDate.now();
        LocalDate day2 = date.toLocalDate();
        return ChronoUnit.DAYS.between(day2, day1);
    }

    public static Date TodayMinusDayAndMonth(int day,int month) {
        return Date.valueOf(LocalDate.now().minusDays(day).minusMonths(month));
    }
    public static String TodayMinus1HourReturnFullFormat() {
        LocalDateTime now = LocalDateTime.now().minusHours(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String formatDateTime = now.format(formatter);

        return formatDateTime;
    }

    public static DateTime currentDateTimeByTimeZone() {
        DateTimeZone timeZone = DateTimeZone.forID(Config.getProp("timeZoneId"));
        DateTime now = new DateTime(timeZone);
        return now;
    }

    public static String TodayMinus10DatsReturnFullFormat() {
        LocalDateTime now = LocalDateTime.now().minusDays(10);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String formatDateTime = now.format(formatter);

        return formatDateTime;
    }

    public static String getMillisecond() {
        Clock clock = Clock.systemUTC();
        Duration tickDuration = Duration.ofNanos(250000);
        Clock clock1 = Clock.tick(clock, tickDuration);
        return String.valueOf( clock1.instant().toEpochMilli());
    }
    public static Date TodayPlus5Year() {
        return Date.valueOf(String.valueOf(LocalDateTime.now().plusYears(5).toLocalDate()));
    }
    public static Date TodayPlus1MonthMinus15Day() {
        return Date.valueOf(LocalDate.now().plusMonths(1).minusDays(15));
    }
    public static long todayPlus1MonthMinusTodayPlus1Day() {
        LocalDate day1 = LocalDate.now().plusDays(1);
        LocalDate day2 = LocalDate.now().plusMonths(1);
        return ChronoUnit.DAYS.between(day1, day2);
    }

    public static String getDateTimeByTimeZone() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TIME_FORMAT);

        java.util.Date date = new java.util.Date();
        DateTime currentDateTime = new DateTime(date);
        DateTimeZone dtZone = DateTimeZone.forID(Config.getProp("timeZoneId"));
        DateTime dateTimeByTimeZone = currentDateTime.withZone(dtZone);
        java.util.Date dateInAmerica = dateTimeByTimeZone.toLocalDateTime().toDate(); //Convert to LocalDateTime first

        System.out.println("dateInAmerica (Formatter) : " + formatter.format(dateInAmerica));

        return formatter.format(dateInAmerica);
    }
    public static Date TodayPlus2MonthMinus16Days() {
        return Date.valueOf(LocalDate.now().plusMonths(2).minusDays(16));
    }
    public static long TodayPlus2MonthMinus16DaysMinusToday() {
        LocalDate day1 = LocalDate.now();
        LocalDate day2 =LocalDate.now().plusMonths(2).minusDays(16);
        return ChronoUnit.DAYS.between(day1, day2);
    }
    public static Date TodayPlus1MonthMinus16Day() {
        return Date.valueOf(LocalDate.now().plusMonths(1).minusDays(16));
    }

    public static Date TodayPlus3MonthsMinus16Days() {
        return Date.valueOf(LocalDate.now().plusMonths(3).minusDays(16));
    }
    public static Date TodayPlus2MonthMinus15Days() {
        return Date.valueOf(LocalDate.now().plusMonths(2).minusDays(15));
    }

    public static Date getExactDate(int currentMonth, int expectedMonth){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);;
        int date = 1;
        calendar.set(year, currentMonth - 1, date);
        int maxCurrDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(year, expectedMonth - 1, date);
        int maxExpectedDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int day = maxExpectedDay - maxCurrDay;
        return Date.valueOf(LocalDate.now().plusMonths(expectedMonth - currentMonth).plusDays(day));
    }

    public static long getDateBetweenMonth(Date actual, Date expected) {
        LocalDate day1 = actual.toLocalDate();
        LocalDate day2 = expected.toLocalDate();
        return ChronoUnit.DAYS.between(day2, day1);
    }
}
