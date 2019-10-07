package logic.business.entities.selfcare;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.sql.Date;
import java.util.HashMap;

public class ClubCarDTransactionEnity {
    public String detail;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getClubCardPoint() {
        return clubCardPoint;
    }

    public void setClubCardPoint(String clubCardPoint) {
        this.clubCardPoint = clubCardPoint;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String clubCardPoint;
    public String amountPaid;
    public String date;


    public static HashMap<String, String> getClubCardTransactionEnity(Date date, String details, String amountPaid, String clubCardPoint) {
        HashMap<String, String> event = new HashMap<String, String>();
        event.put("Details", details);
        event.put("Clubcard points", clubCardPoint);
        event.put("Amount paid",amountPaid);
        event.put("Date", Parser.parseDateFormate(date,TimeStamp.DATE_FORMAT));

        return event;
    }
}
