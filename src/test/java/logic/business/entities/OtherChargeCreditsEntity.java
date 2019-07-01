package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OtherChargeCreditsEntity {

    public String otherChargeCredit;
    public String startDate;
    public String endDate;
    public String tariff;
    public String usageType;
    public String status;

    public String getOtherChargeCredit() {
        return otherChargeCredit;
    }

    public void setOtherChargeCredit(String otherChargeCredit) {
        this.otherChargeCredit = otherChargeCredit;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static List<HashMap<String, String>> dataForOtherChargeCredits(Date endDate) {
        List<HashMap<String, String>> listOtherChargeCredit = new ArrayList<>();
        HashMap<String, String> chargeCredit1 = new HashMap<String, String>();
        chargeCredit1.put("Start Date", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT));
        chargeCredit1.put("End Date", Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT));

        listOtherChargeCredit.add(chargeCredit1);
        listOtherChargeCredit.add(chargeCredit1);
        return listOtherChargeCredit;
    }

    public static HashMap<String, String> getOCCForAgreementAdjustmentProducts(String subNo1, String serviceOrderId) {
        HashMap<String, String> chargeCredit = new HashMap<String, String>();
        chargeCredit.put("Other Charge Credit", String.format("%s (ref=%s)", subNo1, serviceOrderId));
        chargeCredit.put("Usage Type", "Agreement Adjustment Products");
        chargeCredit.put("End Date", Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT));
        chargeCredit.put("Tariff", "AGR-PAYMENT - Transfer as payment towards Credit Agreement - £120.00");

        return chargeCredit;
    }

}
