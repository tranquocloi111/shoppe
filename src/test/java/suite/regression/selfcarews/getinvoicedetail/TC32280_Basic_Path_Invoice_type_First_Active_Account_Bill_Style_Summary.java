package suite.regression.selfcarews.getinvoicedetail;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 10/10/2019
 */
public class TC32280_Basic_Path_Invoice_type_First_Active_Account_Bill_Style_Summary extends BaseTest {
    String customerNumber;
    String subscriptionNumber;
    String invoiceNumber;
    private Date newStartDate = TimeStamp.TodayMinus20Days();
    private Date invoiceDueDate;

    @Test(enabled = true, description = "TC32280_Basic_Path_Invoice_type_First_Active_Account_Bill_Style_Summary", groups = "SelfCareWS.GetInvoiceDetail")
    public void TC32280_Basic_Path_Invoice_type_First_Active_Account_Bill_Style_Summary() {
        test.get().info("Step 1 : Create an onlines CC Customer with FC 1 bundle of SB and sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC1BundleAndSimOnly();
        customerNumber = owsActions.customerNo;

        test.get().info("2. Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("3. Update bill group payment collection date to 10 days later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("4. Set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("5. Update the start date of customer");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //===========================================================================

        test.get().info("6. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("7. Edit billing detail invoice style value");
        String billStyle = "Summary";
        CareTestBase.page().editBillingDetailInvoiceStyleValue(billStyle);

        test.get().info("8. Submit do refill bc job");
        submitDoRefillBCJob();

        test.get().info("9. Submit do refill nc job");
        submitDoRefillNCJob();

        test.get().info("10. Submit do bundle renew job");
        submitDoBundleRenewJob();

        test.get().info("11. Submit draft bill run");
        submitDraftBillRun();

        test.get().info("12. Submit confirm bill run");
        submitConfirmBillRun();
        //===========================================================================

        test.get().info("13. Submit get invoice detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetInvoiceDetailRequest(customerNumber);

        test.get().info("14. Get invoice due date");
        getInvoiceDueDate();

        test.get().info("15. Verify get invoice detail request");
        subscriptionNumber = CareTestBase.page().recordLatestSubscriptionNumberForCustomer();
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        invoiceNumber = InvoicesContentPage.getInstance().getInvoiceNumber();

        String tempFile = "src\\test\\resources\\xml\\sws\\getinvoicedetail\\TC32280_Response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyGetInvoiceDetailResponse(response, tempFile, customerNumber, newStartDate, subscriptionNumber, invoiceNumber);
    }

    private void getInvoiceDueDate() {
        invoiceDueDate = BaseTest.paymentCollectionDateEscapeNonWorkDay(10);
    }

}
