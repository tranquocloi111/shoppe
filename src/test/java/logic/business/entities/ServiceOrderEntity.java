package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceOrderEntity {
    public String id;
    public String date;
    public String status;
    public String type;
    public String subscription;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public static List<HashMap<String, String>> dataServiceOrderDataForDeactivateAccount() {
        List<HashMap<String, String>> listOrderServer = new ArrayList<>();
        HashMap<String, String> so1 = new HashMap<String, String>();
        so1.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so1.put("Type", "Deactivate Account");
        so1.put("Status", "Provision Wait");

        HashMap<String, String> so2 = new HashMap<String, String>();
        so2.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so2.put("Type", "Deactivate Account");
        so2.put("Status", "Completed Task");

        HashMap<String, String> so3 = new HashMap<String, String>();
        so3.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so3.put("Type", "Deactivate Subscription Task");
        so3.put("Status", "Completed Task");

        listOrderServer.add(so1);
        listOrderServer.add(so2);
        listOrderServer.add(so3);

        return listOrderServer;
    }

    public static List<HashMap<String, String>> dataServiceOrderProvisionWaitChangeBundle(String serviceOrderID, String serviceSubscription) {
        List<HashMap<String, String>> listOrderServer = new ArrayList<>();
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so.put("Id", serviceOrderID);
        so.put("Subscription", serviceSubscription);
        so.put("Type", "Change Bundle");
        so.put("Status", "Provision Wait");
        listOrderServer.add(so);

        return listOrderServer;
    }

    public static List<HashMap<String, String>> dataServiceOrderCompletedTask(String serviceOrderID, String serviceSubscription) {
        List<HashMap<String, String>> listOrderServer = new ArrayList<>();
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so.put("Id", String.valueOf(serviceOrderID));
        so.put("Subscription", String.valueOf(serviceSubscription));
        so.put("Type", "Change Bundle");
        so.put("Status","Completed Task");
        listOrderServer.add(so);

        return listOrderServer;
    }

    public static List<HashMap<String, String>> dataServiceOrderChangeBundle() {
        List<HashMap<String, String>> listOrderServer = new ArrayList<>();
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so.put("Type", "Change Bundle");
        listOrderServer.add(so);

        return listOrderServer;
    }

    public static HashMap<String, String> dataServiceOrderApplyFinancialTransaction() {
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Status", "Completed Task");
        so.put("Type", "Apply Financial Transaction");

        return so;
    }


    public static List<HashMap<String, String>> dataFCServiceOrderProvisionWait(String serviceSubscription) {
        List<HashMap<String, String>> listOrderServer = new ArrayList<>();
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so.put("Subscription", serviceSubscription);
        so.put("Type", "Change Bundle");
        so.put("Status","Provision Wait");
        listOrderServer.add(so);

        return listOrderServer;
    }

    public static List<HashMap<String, String>> dataNCServiceOrderProvisionWait(String serviceSubscription) {
        List<HashMap<String, String>> listOrderServer = new ArrayList<>();
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so.put("Subscription", serviceSubscription);
        so.put("Type", "Change Bundle");
        so.put("Status","Provision Wait");
        listOrderServer.add(so);

        return listOrderServer;
    }

    public static List<HashMap<String, String>> dataServiceOrderCompletedTaskChangeBundle(String serviceSubscription) {
        List<HashMap<String, String>> listOrderServer = new ArrayList<>();
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so.put("Subscription", serviceSubscription);
        so.put("Type", "Change Bundle");
        so.put("Status","Completed Task");
        listOrderServer.add(so);

        return listOrderServer;
    }

    public static HashMap<String, String> dataServiceOrder(String serviceSubscription, String type, String status) {
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so.put("Subscription", serviceSubscription);
        so.put("Type", type);
        so.put("Status", status);

        return so;
    }

    public static HashMap<String, String> dataServiceOrderForChangePassword( String type, String status) {
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so.put("Type", type);
        so.put("Status", status);

        return so;
    }

    public static List<HashMap<String, String>> dataServiceOrderBySub(String serviceSubscription) {
        List<HashMap<String, String>> listOrderServer = new ArrayList<>();
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Subscription", serviceSubscription);
        listOrderServer.add(so);

        return listOrderServer;
    }

    public static List<HashMap<String, String>> dataServiceOrderBySubAndType(String serviceSubscription, String type) {
        List<HashMap<String, String>> listOrderServer = new ArrayList<>();
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Subscription", serviceSubscription);
        so.put("Type", type);
        listOrderServer.add(so);

        return listOrderServer;
    }

    public static List<HashMap<String, String>> dataServiceOrderBySubAndType(String serviceSubscription, String type, String status) {
        List<HashMap<String, String>> listOrderServer = new ArrayList<>();
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Subscription", serviceSubscription);
        so.put("Type", type);
        so.put("Status",status);
        listOrderServer.add(so);

        return listOrderServer;
    }

    public static HashMap<String, String> dataServiceOrderFinancialTransaction() {
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Status", "Completed Task");
        so.put("Type", "Ad-hoc Payment");
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        return so;
    }

    public static HashMap<String, String> dataServiceOrderCreditCardPayment(String status,String type) {
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Status", status);
        so.put("Type", type);
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        return so;
    }
    public static HashMap<String, String> dataServiceOrderChangePaymentMethod(String status,String type) {
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Status", status);
        so.put("Type", type);
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        return so;
    }

    public static HashMap<String, String> dataServiceOrder(String status, String type) {
        HashMap<String, String> so = new HashMap<String, String>();
        so.put("Status", status);
        so.put("Type", type);
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        return so;
    }
}
