package logic.utils;

import framework.utils.Log;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;


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

    public static LocalDate convertToLocalDateViaMilisecond(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static Float parseToInt(String s){
        return Float.parseFloat(s);
    }

}
