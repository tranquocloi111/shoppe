package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.util.HashMap;

public class CreditAgreementPaymentsEntiy {

    public static HashMap<String, String> getCreditAgreementPaymentWithAdHoc(String subNo1) {
        HashMap<String, String> creditAgreement = new HashMap<String, String>();
        creditAgreement.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        creditAgreement.put("Details", "Ad-hoc Payment");
        creditAgreement.put("Subscription", subNo1);
        creditAgreement.put("Amount", "£120.00");

        return creditAgreement;
    }

    public static HashMap<String, String> getCreditAgreementPaymentWithInitialPayment(String subNo1) {
        HashMap<String, String> creditAgreement = new HashMap<String, String>();
        creditAgreement.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        creditAgreement.put("Details", "Initial Payment");
        creditAgreement.put("Subscription", subNo1);
        creditAgreement.put("Amount", "£16.00");

        return creditAgreement;
    }
}
