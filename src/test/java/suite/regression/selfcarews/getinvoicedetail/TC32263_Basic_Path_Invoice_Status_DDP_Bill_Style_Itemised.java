package suite.regression.selfcarews.getinvoicedetail;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 8/10/2019
 */
public class TC32263_Basic_Path_Invoice_Status_DDP_Bill_Style_Itemised extends BaseTest {
    String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus20Days();
    private String subscriptionNumber;
    private String invoiceNumber;

    @Test(enabled = true, description = "TC32263_Basic_Path_Invoice_Status_DDP_Bill_Style_Itemised", groups = "SelfCareWS.GetInvoiceDetail")
    public void TC32263_Basic_Path_Invoice_Status_DDP_Bill_Style_Itemised() {
        test.get().info("Step 1 :create a DD customer with FC 2 Bundles and NK2720");
        OWSActions owsActions = new OWSActions();
        owsActions.createADDCustomerWithFC2BundlesAndNK2720();
        customerNumber = owsActions.customerNo;

        test.get().info("2. Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("3. Update bill group payment collection date to 10 days later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("4. Set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("5. Update the start date of customer");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("6. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        //===========================================================================
        test.get().info("7. Record latest subscription number for customer");
        subscriptionNumber = CareTestBase.page().recordLatestSubscriptionNumberForCustomer();

        test.get().info("8. Edit billing detail invoice style value");
        String billStyle = "Itemised";
        CareTestBase.page().editBillingDetailInvoiceStyleValue(billStyle);

        test.get().info("9. Submit send DDI request job");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();

        test.get().info("10. Verify customer DDI status changed to Inactive");
        verifyCustomerDDIStatusChangedToInactive();

        test.get().info("11. Open service order details for send DDI to BACS item");
        openServiceOrderDetailsForSendDDIToBACSItem();

        test.get().info("12. Verify Service Order Status is Sent and DDI Reference has value");
        String DDIReference = verifyServiceOrderStatusIsSentAndDIReferenceHasValue();
        //===========================================================================
        test.get().info("13. Update Customer DDI details in database");
        updateCustomerDDIDetailsInDatabase(DDIReference, BillingActions.getHmbrid(customerNumber), newStartDate.toString());

        test.get().info("14. Submit do refill bc job");
        submitDoRefillBCJob();

        test.get().info("15. Submit do refill nc job");
        submitDoRefillNCJob();

        test.get().info("16. Submit do bundle renew job");
        submitDoBundleRenewJob();

        test.get().info("17. draft bill run");
        submitDraftBillRun();

        test.get().info("18. Submit confirm bill run");
        submitConfirmBillRun();

        test.get().info("19. Update invoice due date today");
        BillingActions.updateInvoiceDueDate(customerNumber, TimeStamp.Today());

        test.get().info("20. Submit payment allocation batch job");
        RemoteJobHelper.getInstance().submitPaymentAllocationBatchJobRun();

        test.get().info("21. Run direct debit batch job to send DDI");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();

        test.get().info("22. Back to Customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("23. Verify invoice status changed to Direct Debit Pending");
        verifyInvoiceStatusIsUpdatedToDirectDebitPending();

        test.get().info("24. Get invoice number");
        invoiceNumber = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getInvoiceNumber();
        //===========================================================================
        test.get().info("25. Submit get invoice detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetInvoiceDetailRequest(customerNumber);

        test.get().info("26. Verify get invoice detail request");
        subscriptionNumber = CareTestBase.page().recordLatestSubscriptionNumberForCustomer();
        String tempFile = "src\\test\\resources\\xml\\sws\\getinvoicedetail\\TC32263_Response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyGetInvoiceDetailResponse(response, tempFile, customerNumber, newStartDate, subscriptionNumber, invoiceNumber);
    }

    private void verifyInvoiceStatusIsUpdatedToDirectDebitPending() {
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage invoicesContentPage = InvoicesContentPage.getInstance();
        Assert.assertEquals(1, invoicesContentPage.getRowNumberOfInvoiceTable());
        Assert.assertEquals("Direct Debit Pending", invoicesContentPage.getStatusByIndex(1));
    }

}
