package logic.utils;

import java.sql.Date;
import java.text.SimpleDateFormat;


public class Parser {
    static SimpleDateFormat format;
    public static String parseDateFormate(Date value, String dateFormat){
        try {
            format = new SimpleDateFormat(dateFormat);
            String dateString = format.format(value);
            return  dateString;
        }catch (Exception ex){
            Log.error(ex.getMessage());
        }
        return null;
    }


    public static void main(String[] args) throws InterruptedException {
        Date newStartDate = TimeStamp.TodayMinus15Days();

        System.out.println(parseDateFormate(newStartDate,"ddMMyyyy"));
    }
}
