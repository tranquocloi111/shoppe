package framework.report;

import logic.pages.HomePage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Helper {

    private void Helper(){

    }

    public static Helper getInstance() {
        return new Helper();
    }
    public static void isDateFormat(String date){

    }

    public  Date convertStringToDate(String dateString){
        Date date = null;
        SimpleDateFormat ft = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        try {
             date = ft.parse(dateString);
        }catch (Exception ex) {
            System.out.println(ex);
        }
        return date;
    }
}
