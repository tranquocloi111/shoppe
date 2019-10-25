package logic.business.entities.selfcare;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.HashMap;

public class UsageDetailsEnity  {


    public static HashMap<String,String> getMonthlyChargesEnity(String status, String description, Date dateFrom, Date dateTo,String amount)
    {
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Status", status);
        event.put("Description", description);
        event.put("Date From",Parser.parseDateFormate(dateFrom,TimeStamp.DATE_FORMAT_IN_PDF));
        event.put("Date To", Parser.parseDateFormate(dateTo,TimeStamp.DATE_FORMAT_IN_PDF));
        event.put("Amount", amount);
        return event;
    }
    public static  HashMap<String,String> getBundleChargesEnity(  Date dateFrom, Date dateTo,String description,String amount)
    {
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Description", description);
        event.put("Date From",Parser.parseDateFormate(dateFrom,TimeStamp.DATE_FORMAT_IN_PDF));
        event.put("Date To", Parser.parseDateFormate(dateTo,TimeStamp.DATE_FORMAT_IN_PDF));
        event.put("Amount", amount);
        return event;
    }
    public static  HashMap<String,String> getBundleChargesEnity(  Date dateFrom, String description,String amount)
    {
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Description", description);
        event.put("Date From",Parser.parseDateFormate(dateFrom,TimeStamp.DATE_FORMAT_IN_PDF));
        event.put("Date To", "");
        event.put("Amount", amount);
        return event;
    }
    public static HashMap<String,String> getUsageChargesEnity(String callType,String quantity, String cost,String amount)
    {
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Call Type", callType);
        event.put("Quantity", quantity);
        event.put("Cost", cost);
        event.put("Amount", amount);
        return event;
    }
}
