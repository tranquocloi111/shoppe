package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.HashMap;

public class ReceiptAllocation {

    public static HashMap<String, String> dataForReceiptAllocation(String invoiceNo, Date dateAllocated, String amount) {
        HashMap<String, String> enity= new HashMap<String, String>(); ;
        enity.put("Invoice No",invoiceNo);
        enity.put("Date Allocated", Parser.parseDateFormate(dateAllocated, TimeStamp.DATE_FORMAT));
        enity.put("Amount", amount);
        return enity;
    }
}
