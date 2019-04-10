package suite;

import logic.helper.BillingHelper;
import logic.utils.DB;
import logic.utils.Log;
import logic.utils.Parser;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class TestBase {

    public static void createNewBillingGroup() {
        BillingHelper.page().createNewBillingGroup(0, true, -1);
    }

    public static void updateBillGroupPaymentCollectionDateTo10DaysLater() {
        Date paymentCollectionDate = Date.valueOf(LocalDate.now().plusDays(10));
        BillingHelper.page().updateBillGroupPaymentCollectionDate(paymentCollectionDate, BillingHelper.page().tempBillingGroupHeader.getKey());
    }

    public static void setBillGroupForCustomer(String customerId) {
        BillingHelper.page().setBillGroupForCustomer(customerId, BillingHelper.page().tempBillingGroupHeader.getKey());
    }

    public static void waitForAsyncProcessComplete(String orderId) {
        Boolean taskFinished = false;
        try {
            String sql = String.format("select count(*) as backOfficTaskCount from asynccommandreq areq where areq.processkey = '%s'", orderId);
            int backOfficTaskCount = Integer.parseInt(String.valueOf(DB.SetToOEDatabase().retriveDataInResultSet(DB.SetToOEDatabase().executeQuery(sql), "backOfficTaskCount")));
            sql = String.format("select count(*) as successfullbackOfficTaskCount from asynccommandreq areq where areq.processkey = '%s' and areq.STATUS = 'COMPLETED_EXC_SUCCESS'", orderId);
            for (int i = 0; i < 300; i++) {
                int successfullbackOfficTaskCount = Integer.parseInt(String.valueOf(DB.SetToOEDatabase().retriveDataInResultSet(DB.SetToOEDatabase().executeQuery(sql), "successfullbackOfficTaskCount")));
                Thread.sleep(2000);
                if (backOfficTaskCount == successfullbackOfficTaskCount) {
                    taskFinished = true;
                    break;
                } else {
                    taskFinished = false;
                }
                System.out.println(successfullbackOfficTaskCount);
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
            Connection conn = DB.SetToNonOEDatabase().getConnection();
            CallableStatement cstmt = DB.SetToNonOEDatabase().callableStatement();
            cstmt = conn.prepareCall(sql);
            cstmt.setString("icustid", cusId);
            cstmt.setString("istartdate_ddmmyyyy", Parser.parseDateFormate(newStartDate, "ddMMyyyy"));
            cstmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


}
