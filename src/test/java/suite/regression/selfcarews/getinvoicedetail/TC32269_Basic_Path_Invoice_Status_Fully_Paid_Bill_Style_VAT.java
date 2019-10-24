package suite.regression.selfcarews.getinvoicedetail;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 9/10/2019
 */
public class TC32269_Basic_Path_Invoice_Status_Fully_Paid_Bill_Style_VAT extends BaseTest {
    String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus20Days();
    private String subscriptionNumber;
    private String invoiceNumber;

    @Test(enabled = true, description = "TC32269_Basic_Path_Invoice_Status_Fully_Paid_Bill_Style_VAT", groups = "SelfCareWS.GetInvoiceDetail")
    public void TC32269_Basic_Path_Invoice_Status_Fully_Paid_Bill_Style_VAT() {
        test.get().info("1. Create an onlines CC Customer with FC 1 bundle of SB and sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
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
        String billStyle = "VAT";
        CareTestBase.page().editBillingDetailInvoiceStyleValue(billStyle);

        test.get().info("9. Submit do refill bc job");
        submitDoRefillBCJob();

        test.get().info("10. Submit do refill nc job");
        submitDoRefillNCJob();

        test.get().info("11. Submit do bundle renew job");
        submitDoBundleRenewJob();

        test.get().info("12. draft bill run");
        submitDraftBillRun();

        test.get().info("13. Submit confirm bill run");
        submitConfirmBillRun();
        //===========================================================================

        test.get().info("14. Update invoice due date today");
        BillingActions.updateInvoiceDueDate(customerNumber, TimeStamp.Today());

        test.get().info("15. Submit payment allocation batch job");
        RemoteJobHelper.getInstance().submitPaymentAllocationBatchJobRun();
        //===========================================================================

        test.get().info("16. Run credit card batch job");
        RemoteJobHelper.getInstance().submitCreditCardBatchJobRun();

        test.get().info("17. Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("18. Verify invoice status changed to Fully Paid after CC payment");
        invoiceNumber = verifyInvoiceStatusChangedToFullyPaidAfterCCPayment();
        //===========================================================================
        test.get().info("19. Submit get invoice detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetInvoiceDetailRequest(customerNumber);

        test.get().info("20. Verify get invoice detail request");
        subscriptionNumber = CareTestBase.page().recordLatestSubscriptionNumberForCustomer();
        String tempFile = "src\\test\\resources\\xml\\sws\\getinvoicedetail\\TC32269_Response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyGetInvoiceDetailResponse(response, tempFile, customerNumber, newStartDate, subscriptionNumber, invoiceNumber);
    }
}
