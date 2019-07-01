package logic.utils;

import java.sql.Date;
import java.time.LocalDate;

public class TimeStamp {

    public final static String DATE_FORMAT = "dd MMM yyyy";
    public final static String DATE_FORMAT2 = "yyyyMMdd";
    public final static String DATE_FORMAT3 = "dd-MMM-yy";
    public final static String DATE_FORMAT_IN_PDF = "dd/MM/yyyy";
    public final static String DATE_FORMAT_IN_PDF2 = "dd-MMM-yyyy";
    public final static String DATE_FORMAT_IN_PDF3 = "MM/yyyy";



    public static Date Today() {
        return Date.valueOf(LocalDate.now());
    }

    public static Date TodayMinus1Day(){
        return Date.valueOf(LocalDate.now().minusDays(1));
    }

    public static Date TodayMinus1Month(){
        return Date.valueOf(LocalDate.now().minusMonths(1));
    }

    public static Date TodayMinus1MonthMinus1Day(){
        return Date.valueOf(LocalDate.now().minusMonths(1).minusDays(1));
    }

    public static Date TodayMinus2Days()
    {
        return Date.valueOf(LocalDate.now().minusDays(2));
    }

    public static Date TodayPlus1Day() {
        return Date.valueOf(LocalDate.now().plusDays(1));
    }

    public static Date TodayMinus3Days(){
        return Date.valueOf(LocalDate.now().minusDays(3));
    }

    public static Date TodayMinus4Days(){
        return Date.valueOf(LocalDate.now().minusDays(4));
    }

    public static Date TodayMinus5Days(){
        return Date.valueOf(LocalDate.now().minusDays(5));
    }

    public  static Date TodayMinus6Days(){
        return Date.valueOf(LocalDate.now().minusDays(6));
    }

    public static Date TodayMinus7Days(){
        return Date.valueOf(LocalDate.now().minusDays(7));
    }

    public static Date TodayMinus8Days(){
        return Date.valueOf(LocalDate.now().minusDays(8));
    }

    public static Date TodayMinus13Days(){
        return Date.valueOf(LocalDate.now().minusDays(13));
    }

    public static Date TodayMinus15Days()
    {
        return Date.valueOf(LocalDate.now().minusDays(15));
    }

    public static Date TodayPlus1Month(){
        return Date.valueOf(LocalDate.now().plusMonths(1));
    }

    public static Date TodayPlus2Month(){
        return Date.valueOf(LocalDate.now().plusMonths(2));
    }

    public static Date TodayPlus2MonthMinus1Day(){
        return Date.valueOf(LocalDate.now().plusMonths(2).minusDays(1));
    }

    public static Date TodayPlus1MonthMinus1Day()
    {
        return Date.valueOf(LocalDate.now().plusMonths(1).minusDays(1));
    }

    public static Date TodayPlus1MonthPlus1Day(){
        return Date.valueOf(LocalDate.now().plusMonths(1).plusDays(1));
    }

    public static Date iMinDate(){
        return TodayMinus13Days();
    }

    public static Date iMaxDate()
    {
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

    public static Date TodayMinus30Days() {
        return Date.valueOf(LocalDate.now().minusDays(30));
    }

    public static Date TodayMinus35Days() {
        return Date.valueOf(LocalDate.now().minusDays(35));
    }

    public static Date TodayPlus1YearMinus1Day(){
        return Date.valueOf(LocalDate.now().plusYears(1).minusDays(1));
    }

    public static Date TodayPlus1MonthMinusToday(){
        return Date.valueOf(LocalDate.now().plusMonths(1).minusDays(LocalDate.now().getMonthValue()));
    }

}
