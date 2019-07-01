package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.util.HashMap;

public class CreditAgreementsGirdEntity {


    public static HashMap<String, String> getCreditAgreementGird(String subNo1) {
        HashMap<String, String> creditAgreement = new HashMap<String, String>();
        creditAgreement.put("Start Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        creditAgreement.put("Description", "Credit Agreement");
        creditAgreement.put("Subscription", subNo1);
        creditAgreement.put("Balance", "£368.00");
        creditAgreement.put("Status", "Active");

        return creditAgreement;
    }

}
