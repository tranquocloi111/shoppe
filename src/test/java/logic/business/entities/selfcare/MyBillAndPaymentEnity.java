package logic.business.entities.selfcare;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyBillAndPaymentEnity {

    public String date;
    public String Details;
    public String Payments;
    public String Balance;

    public void setDate(String date) {
        this.date = date;
    }

    public void setDetails(String details) {
        Details = details;
    }

    public void setPayments(String payments) {
        Payments = payments;
    }

    public void setBalance(String balance) {
        Balance = balance;
    }
    public String getDate() {
        return date;
    }

    public String getDetails() {
        return Details;
    }

    public String getPayments() {
        return Payments;
    }

    public String getBalance() {
        return Balance;
    }

    public static HashMap<String, String> dataForMyBillsAndPayment(String details,String payments, String balance) {
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Details", details);
        event.put("Balance", balance);
        event.put("Payments",payments);
        event.put("Date", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT));

        return event;
    }
}
