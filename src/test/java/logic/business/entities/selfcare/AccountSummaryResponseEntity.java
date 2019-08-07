package logic.business.entities.selfcare;

import java.util.HashMap;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 31/07/2019
 */
public class AccountSummaryResponseEntity {
    public String accountNumber;
    public String accountName;
    public String startDate;
    public String endDate;
    public String clubcardNumber;
    public String nextBillDate;
    public List<HashMap<String, String>> Subscriptions;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {

        this.accountName = accountName;
    }

    public String getNextBillDate() {
        return nextBillDate;
    }

    public void setNextBillDate(String nextBillDate) {
        this.nextBillDate = nextBillDate;
    }

    public String getClubcardNumber() {
        return clubcardNumber;
    }

    public void setClubcardNumber(String clubcardNumber) {
        this.clubcardNumber = clubcardNumber;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public List<HashMap<String, String>> getSubscriptions() {
        return Subscriptions;
    }

    public void setSubscriptions(List<HashMap<String, String>> subscriptions) {
        Subscriptions = subscriptions;
    }


}
