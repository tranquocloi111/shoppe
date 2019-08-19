package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubscriptionEntity {
    public String subscriptionNumber;
    public String usageType;
    public String startDate;
    public String endDate;
    public String tariff;
    public String status;
    public String reference;
    public String orderDate;
    public String discountGroupCode;
    public String barring;
    public String gRG;

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getDiscountGroupCode() {
        return discountGroupCode;
    }

    public void setDiscountGroupCode(String discountGroupCode) {
        this.discountGroupCode = discountGroupCode;
    }

    public String getBarring() {
        return barring;
    }

    public void setBarring(String barring) {
        this.barring = barring;
    }

    public String getgRG() {
        return gRG;
    }

    public void setgRG(String gRG) {
        this.gRG = gRG;
    }

    public static List<HashMap<String, String>> dataForSummarySubscriptions(Date startDate, Date endDate) {
        List<HashMap<String, String>> listSummarySubscription = new ArrayList<>();
        HashMap<String, String> summarySubscriptions = new HashMap<String, String>();
        summarySubscriptions.put("Start Date", Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT));
        summarySubscriptions.put("End Date", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT));
        summarySubscriptions.put("Status", "Inactive");

        listSummarySubscription.add(summarySubscriptions);
        return listSummarySubscription;
    }
    public static List<HashMap<String, String>> dataForInactiveSubscriptions(String subscriptionNumber,Date startDate, Date endDate) {
        List<HashMap<String, String>> listInactiveSubscription = new ArrayList<>();
        HashMap<String, String> summarySubscriptions = new HashMap<String, String>();
        summarySubscriptions.put("subscriptionNumber", subscriptionNumber);
        summarySubscriptions.put("Start Date", Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT));
        summarySubscriptions.put("End Date", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT));
        summarySubscriptions.put("Status", "Inactive");

        listInactiveSubscription.add(summarySubscriptions);
        return listInactiveSubscription;
    }
    public static List<HashMap<String, String>> dataForActiveSubscriptions(String subscriptionNumber) {
        List<HashMap<String, String>> listActiveSubscription = new ArrayList<>();
        HashMap<String, String> summarySubscriptions = new HashMap<String, String>();
        summarySubscriptions.put("subscriptionNumber", subscriptionNumber);
        summarySubscriptions.put("Start Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        summarySubscriptions.put("End Date", "");
        summarySubscriptions.put("Status", "Active");
        summarySubscriptions.put("Barring", "Barring");
        summarySubscriptions.put("GRG", "GRG");

        listActiveSubscription.add(summarySubscriptions);
        return listActiveSubscription;
    }

}
