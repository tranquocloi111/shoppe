package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.util.HashMap;

public class FinancialTransactionEnity {
    public static HashMap<String, String> dataFinancialTransactionForMakeAOneOffPayment(String detail, String credit) {
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Details", detail);
        so.put("Credit", credit);
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));

        return so;
    }
    public static HashMap<String, String> dataFinancialTransactionForMakeAOneOffPayment(String detail, String credit,String balance) {
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Details", detail);
        so.put("Balance", balance);
        so.put("Credit", credit);
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));

        return so;
    }
}
