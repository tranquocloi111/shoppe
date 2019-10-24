package suite.regression.selfcarews.getinvoicedetail;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 9/10/2019
 */
public class TC32270_Invoice_Status_Confirmed_Bill_Style_VAT_Multiple_Subscriptions_External_System_Admin extends BaseTest {
    String FCSubscription;
    String NCSubscription;
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus20Days();
    private String subscriptionNumber;
    private String invoiceNumber;
    private List<String> subscriptionNumberList = new ArrayList<>();
    private Date invoiceDueDate;

    @Test(enabled = true, description = "TC32270_Invoice_Status_Confirmed_Bill_Style_VAT_Multiple_Subscriptions_External_System_Admin", groups = "SelfCareWS.GetInvoiceDetail")
    public void TC32270_Invoice_Status_Confirmed_Bill_Style_VAT_Multiple_Subscriptions_External_System_Admin() {
        test.get().info("Step 1 : Create a CC Customer with 2 subscriptions order");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWith2SubscriptionOrder();
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
        test.get().info("7. Get all subscription number");
        getAllSubscriptionNumber();

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

        test.get().info("14. Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("15. Verify customer has 1 invoice generated");
        invoiceNumber = CareTestBase.page().verifyCustomerHas1InvoiceGenerated();
        //===========================================================================
        test.get().info("16. Submit get invoice detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetInvoiceDetailRequest(customerNumber);

        test.get().info("17. Get invoice due date");
        getInvoiceDueDate();

        test.get().info("18. Verify get invoice detail request");
        String tempFile = "src\\test\\resources\\xml\\sws\\getinvoicedetail\\TC32270_Response.xml";
        verifyGetInvoiceDetailResponse(response, tempFile, customerNumber, newStartDate, invoiceNumber);
    }

    private void verifyGetInvoiceDetailResponse(Xml response, String tempFile, String customerNumber, Date newStartDate, String invoiceNumber) {
        String actualFile = Common.saveXmlFile(customerNumber + "_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(response.toString())));
        String chargeId = response.getTextByXpath("//chargeId", 0);
        String chargeId2 = response.getTextByXpath("//chargeId", 1);
        String chargeId3 = response.getTextByXpath("//chargeId", 2);
        String chargeId4 = response.getTextByXpath("//chargeId", 3);
        Date invoiceDueDate = BaseTest.paymentCollectionDateEscapeNonWorkDay(10);

        String sDateFrom = Parser.parseDateFormate(newStartDate, TimeStamp.DateFormatXml());
        String sDateTo = Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DateFormatXml());
        String sDateFrom2 = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DateFormatXml());
        String sDateTo2 = Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DateFormatXml());
        String sDateIssued = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DateFormatXml());
        String sDateDue = Parser.parseDateFormate(invoiceDueDate, TimeStamp.DateFormatXml());

        String XmlValue = Common.readFile(tempFile).replace("$accountNumber$", customerNumber)
                .replace("$subscriptionNumber$", FCSubscription)
                .replace("$subscriptionNumber2$", NCSubscription)
                .replace("$invoiceNumber$", invoiceNumber)
                .replace("$dateFrom$", sDateFrom)
                .replace("$dateTo$", sDateTo)
                .replace("$dateFrom2$", sDateFrom2)
                .replace("$dateTo2$", sDateTo2)
                .replace("$dateIssued$", sDateIssued)
                .replace("$dateDue$", sDateDue)
                .replace("$chargeId$", chargeId)
                .replace("$chargeId2$", chargeId2)
                .replace("$chargeId3$", chargeId3)
                .replace("$chargeId4$", chargeId4);

        String expectedResponse = Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(XmlValue)));

        Assert.assertEquals(1, Common.compareFile(expectedResponse, actualFile).size());
    }

    private void getInvoiceDueDate() {
        invoiceDueDate = BaseTest.paymentCollectionDateEscapeNonWorkDay(10);
    }

    private void getAllSubscriptionNumber() {
        subscriptionNumberList = CareTestBase.getAllSubscription();
        for (String subscription : subscriptionNumberList) {
            if (subscription.endsWith("Mobile FC")) {
                FCSubscription = subscription.split(" ")[0];
            } else if (subscription.endsWith("Mobile NC")) {
                NCSubscription = subscription.split(" ")[0];
            }
        }
    }
}
