package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdjustmentsChargesAndCreditsEntity {


    public static List<HashMap<String, String>> getTransferPaymentAdjustmentProducts(Date date) {
        List<HashMap<String, String>> listAdjustmentProducts = new ArrayList<>();
        HashMap<String, String> adjustmentProduct1 = new HashMap<String, String>();
        adjustmentProduct1.put("Date From", Parser.parseDateFormate(date, TimeStamp.DATE_FORMAT_IN_PDF));
        adjustmentProduct1.put("Description", "HTC Desire HD");
        adjustmentProduct1.put("Amount", "£99.00");

        HashMap<String, String> adjustmentProduct2 = new HashMap<String, String>();
        adjustmentProduct2.put("Date From", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF));
        adjustmentProduct2.put("Date To", Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT_IN_PDF));
        adjustmentProduct2.put("Description", "Transfer as payment towards Credit Agreement");
        adjustmentProduct2.put("Amount", "£120.00");

        listAdjustmentProducts.add(adjustmentProduct1);
        listAdjustmentProducts.add(adjustmentProduct2);

        return listAdjustmentProducts;
    }
}
