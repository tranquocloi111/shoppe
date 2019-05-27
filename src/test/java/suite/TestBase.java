package suite;

import framework.utils.Log;
import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.utils.Parser;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class TestBase {


    public static void createNewBillingGroup() {
        BillingActions.getInstance().createNewBillingGroup(0, true, -1);
    }

    public static void updateBillGroupPaymentCollectionDateTo10DaysLater() {
        Date paymentCollectionDate = Date.valueOf(LocalDate.now().plusDays(10));
        BillingActions.getInstance().updateBillGroupPaymentCollectionDate(paymentCollectionDate, BillingActions.getInstance().tempBillingGroupHeader.getKey());
    }

    public static void setBillGroupForCustomer(String customerId) {
        BillingActions.getInstance().setBillGroupForCustomer(customerId, BillingActions.getInstance().tempBillingGroupHeader.getKey());
    }

    public static void waitForAsyncProcessComplete(String orderId) {
        Boolean taskFinished = false;
        try {
            String sql = String.format("select count(*) as backOfficTaskCount from asynccommandreq areq where areq.processkey = '%s'", orderId);
            int backOfficTaskCount = Integer.parseInt(String.valueOf(OracleDB.SetToOEDatabase().retriveDataInResultSet(OracleDB.SetToOEDatabase().executeQuery(sql), "backOfficTaskCount")));
            sql = String.format("select count(*) as successfullbackOfficTaskCount from asynccommandreq areq where areq.processkey = '%s' and areq.STATUS = 'COMPLETED_EXC_SUCCESS'", orderId);
            for (int i = 0; i < 300; i++) {
                int successfullbackOfficTaskCount = Integer.parseInt(String.valueOf(OracleDB.retriveDataInResultSet(OracleDB.SetToOEDatabase().executeQuery(sql), "successfullbackOfficTaskCount")));
                Thread.sleep(2000);
                if (backOfficTaskCount == successfullbackOfficTaskCount) {
                    taskFinished = true;
                    break;
                } else {
                    taskFinished = false;
                }
                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        if (!taskFinished) {
            Log.error("The task can't finish in 5 minutes");
        }

    }

//    public static void checkAsyncProcessIsCompleted(String orderId, ResponseOrderXml responseOrderXml) {
//        if (orderId == null) {
//            if (responseOrderXml == null) {
//                orderId = responseOrderXml.orderIdNode().getFirstChild().getNodeValue();
//            } else {
//                orderId = responseOrderXml.orderIdNode().getFirstChild().getNodeValue();
//            }
//
//        }
//
//        waitForAsyncProcessComplete(orderId);
//    }

    public static void updateCustomerStartDate(String cusId, Date newStartDate) {
        try {
            String sql = "{Call UPDATECUSTSTARTDATE(?,?)}";
            Connection connection = OracleDB.SetToNonOEDatabase().getConnection();
            CallableStatement cstmt = OracleDB.SetToNonOEDatabase().callableStatement();
            cstmt = connection.prepareCall(sql);
            cstmt.setString("icustid", cusId);
            cstmt.setString("istartdate_ddmmyyyy", Parser.parseDateFormate(newStartDate, "ddMMyyyy"));
            cstmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateCustomerEndDate(String cusId, Date newEndDate) {
        try {
            String sql = "{Call UPDATECUSTENDDATE(?,?)}";
            Connection connection = OracleDB.SetToNonOEDatabase().getConnection();
            CallableStatement cstmt = OracleDB.SetToNonOEDatabase().callableStatement();
            cstmt = connection.prepareCall(sql);
            cstmt.setString("icustid", cusId);
            cstmt.setString("ienddate", Parser.parseDateFormate(newEndDate, "ddMMyyyy"));
            cstmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateThePdateAndBillDateForSO(int serviceOrderId){
        BillingActions.getInstance().updateThePdateForSo(serviceOrderId);
        BillingActions.getInstance().updateTheBillDateForSo(serviceOrderId);
    }





}
