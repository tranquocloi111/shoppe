package suite.regression.payment;

import framework.utils.Log;
import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.*;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TC2192_Process_Continuous_Credit_Card_Payment_Requests extends BaseTest {
    /*
     Tran Quoc Loi
     * */
    public String batchID = null;
    public String transCount = null;
    String fullName = "last926186493, first122103967";

    @Test(enabled = true, description = "TC2192 Process Continuous Credit Card Payment Requests", groups = "Payment")
    public void TC2192_Process_Continuous_Credit_Card_Payment_Requests() {
        test.get().info("Step 1 : create an online cc customer with FC 1 bundle of SB and sim only");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);
        String customerNumber = owsActions.customerNo;
        fullName = owsActions.lastName + "," + " " + owsActions.firstName;

        test.get().info("Step 2 : create new billing");
        super.createNewBillingGroup();
        updateBillGroupPaymentCollectionDateTo10DaysLater();
        setBillGroupForCustomer(customerNumber);
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        RemoteJobHelper.getInstance().submitDoRefillBcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoRefillNcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDraftBillRun();
        RemoteJobHelper.getInstance().submitConfirmBillRun();

        test.get().info("Step 3 : load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);

        test.get().info("Step 4 : verify invoice status confirm");
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        String invoiceNumber = InvoicesContentPage.getInstance().getInvoiceNumber();
        Assert.assertEquals(InvoicesContentPage.getInstance().getStatusByIndex(1), "Confirmed");

        test.get().info("Step 5 : update invoice due date");
        BillingActions.updateInvoiceDueDate(customerNumber);

        test.get().info("Step 6 : submit payment all location and credit card batch job");
        RemoteJobHelper.getInstance().submitPaymentAllocationBatchJobRun();
        RemoteJobHelper.getInstance().submitCreditCardBatchJobRun();

        test.get().info("Step 7 : write down the batch id and trans count");
        writeDownTheBatchIDAndTransCountFromJobProcessCreditCard(RemoteJobHelper.remoteJobId);

        test.get().info("Step 8 : Verify remote output from Process Credit Card matches those from job Batch Processing to REDS");
        Verify_remote_output_from_Process_Credit_Card_matches_those_from_job_Batch_Processing_to_REDS(RemoteJobHelper.remoteJobId);

        test.get().info("Step 9 : verify 1 credit card payment with 10 amount created");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickPaymentsLink();

        String refNo = PaymentDetailPage.paymentConentGrid.getInstance().getRefNoByType("Credit Card Payment");
        HashMap<String, String> paymentEnity = PaymentGridEntity.getPaymentEnity("Credit Card Payment", "£10.00");
        Assert.assertEquals(PaymentDetailPage.paymentConentGrid.getInstance().getNumberPaymentRecord(paymentEnity), 1);


        test.get().info("Step 10 :veriyfy detail payment enity");
        PaymentDetailPage.paymentConentGrid.getInstance().clickPaymentByType("Credit Card Payment");
        verifyAdHocPaymentTransactionDetail(invoiceNumber);

        test.get().info("Step 11 :veriyfy 1 Credit card receipt allocation was created");
        Verify1CreditCardRecepitAllocationWasCreated(refNo, customerNumber);

    }


    public void writeDownTheBatchIDAndTransCountFromJobProcessCreditCard(int remoteID) {
        String sql = String.format("select TEXT from remoteoutput where jobid = %s", remoteID);
        List sms = new ArrayList<>();
        sms = OracleDB.SetToNonOEDatabase().executeQueryReturnList(sql);
        List<String> result = new ArrayList<>();
        if (!sms.isEmpty()) {
            for (int y = 0; y < sms.size(); y++) {
                result.add(sms.get(y).toString());
                break;
            }
        }
        String list[] = result.get(0).split("\n");
        String line = null;
        for (int i = 0; i < list.length; i++) {
            if (list[i].startsWith("Batch id")) {
                line = list[i].trim();
                break;
            }
        }
        list = line.split(" ");
        batchID = list[2];
        transCount = list[5];
        Log.info("Remote Job Id:" + remoteID);

    }

    public void Verify_remote_output_from_Process_Credit_Card_matches_those_from_job_Batch_Processing_to_REDS(int remoteJobID) {
        String sql = String.format("select TEXT from remoteoutput where jobid = %s", remoteJobID + 1);
        List sms = new ArrayList<>();
        sms = OracleDB.SetToNonOEDatabase().executeQueryReturnList(sql);
        List<String> result = new ArrayList<>();
        if (!sms.isEmpty()) {
            for (int y = 0; y < sms.size(); y++) {
                result.add(sms.get(y).toString());
                break;
            }
        }
        String list[] = result.get(0).split("\n");
        String line = null;
        String batchNoLine = null;
        for (int i = 0; i < list.length; i++) {
            if (list[i].startsWith("Batch No:")) {
                batchNoLine = list[i].trim();
            }
            if (list[i].startsWith("Processed transactions:")) {
                line = list[i].trim();
            }
        }
        String batchNo = batchNoLine.split(" ")[2];
        String transC = line.split(" ")[2];

        Assert.assertEquals(batchID, batchNo);
        Assert.assertEquals(transC, transCount);
        Assert.assertTrue(result.get(0).contains("Error transactions    : 0"));
        Assert.assertTrue(result.get(0).contains("Transactions resulted in [APPROVE: STATCD=APPROVE (Approved) REDCD=00"));
        Assert.assertTrue(result.get(0).contains("(Approved or completed successfully)]: " + transCount));
    }

    public void verifyAdHocPaymentTransactionDetail(String invoiceNumber) {
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getReceiptType(), "Credit Card Payment");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getReceiptStatus(), "Fully Allocated");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getPaymentAmount(), "£10.00");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getPaymentCurrency(), "Great Britain Pound");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getPaymentMethod(), "Credit Card");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getCardNumber(), "************5100");
        HashMap<String, String> paymentEnity = PaymentGridEntity.getRecieptEnity(invoiceNumber, "£10.00", fullName);
        Assert.assertEquals(PaymentDetailPage.receiptAllocation.getInstance().getNumberReceiptRecord(paymentEnity), 1);
    }

    private void Verify1CreditCardRecepitAllocationWasCreated(String refNo, String customerNumber) {
        String sql = String.format("select t.DESCR, t.RootBUID from RECEIPTALLOCATION t where t.receiptid = %s", refNo);
        String descr = null;
        String rootbuid = null;
        try {
            ResultSet result = OracleDB.SetToNonOEDatabase().executeQuery(sql);
            while (result.next()) {
                descr = result.getString(1);
                rootbuid = result.getString(2);

            }
        } catch (Exception ex) {
        }


        Assert.assertTrue(descr.contains(fullName));
        Assert.assertEquals(customerNumber, rootbuid);
    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }
}