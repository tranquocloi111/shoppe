package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.HashMap;

public class AllocationEnity {
    public static HashMap<String, String> dataAllocationEnity(Date date, String type, String amount) {
        HashMap<String, String> enity = new HashMap<String, String>();
        enity.put("Amount", amount);
        enity.put("Date", Parser.parseDateFormate(date, TimeStamp.DATE_FORMAT));
        enity.put("Type", type);

        return enity;
    }
}
