package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.HashMap;

public class BundleChargesEnity {

    public static HashMap<String, String> bundleChargesEnity(Date dateFrom, Date dateTo, String description, String amount) {
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Date From", Parser.parseDateFormate(dateFrom, TimeStamp.DATE_FORMAT4));
        so.put("Date To", Parser.parseDateFormate(dateTo, TimeStamp.DATE_FORMAT4));
        so.put("Description", description);
        so.put("Amount", amount);

        return so;
    }

}
