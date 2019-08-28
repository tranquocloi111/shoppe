package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.util.HashMap;

public class PaymentGridEntity {


    public static HashMap<String, String> getPaymentEnity(String type,String amount) {
        HashMap<String, String> creditAgreement = new HashMap<String, String>();
        creditAgreement.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        creditAgreement.put("Type", type);
        creditAgreement.put("Amount", amount);
        return creditAgreement;
    }
    public static HashMap<String, String> getRecieptEnity(String invoiceNumber,String amount,String fullName) {
        HashMap<String, String> recieptEnity = new HashMap<String, String>();
        recieptEnity.put("Date Allocated", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        recieptEnity.put("Invoice Number", invoiceNumber);
        recieptEnity.put("Customer Name", fullName);
        recieptEnity.put("Amount", amount);
        return recieptEnity;
    }

}
