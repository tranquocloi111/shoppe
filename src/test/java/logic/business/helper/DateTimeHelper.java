package logic.business.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeHelper {

    public static DateTimeHelper getInstance() {
        return new DateTimeHelper();
    }


    public Date parseStringToDate(String value, String format) {
        SimpleDateFormat f = new SimpleDateFormat("dd MMM yyyy");
        Date date = null;
        try {
            date = f.parse(value);

        } catch (Exception ex) {

        }
        return date;
    }

    public String changeformatDate(Date value, String format) {
        SimpleDateFormat f1 = new SimpleDateFormat("dd/MM/yyyy");
        return f1.format(value);
    }


    public static void main(String[] args) throws InterruptedException {

    }
}

