package suite.regression.selfcarews.getinvoicedetail;

import framework.utils.Log;
import framework.utils.Xml;
import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.SubscriptionEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.DeactivateSubscriptionPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.sql.ResultSet;

/**
 * User: Nhi Dinh
 * Date: 9/10/2019
 */
public class TC32277_Get_Invoice_Details_Invoice_Status_Final_Inactive_Account_Bill_Style_Summary_ETC_not_overriden extends BaseTest {
    String subscriptionNumber;
    String subscriptionNumberValue;
    String newSubscriptionNumber;
    String orderWebServiceId;
    String invoiceId;
    String invoiceNumber;
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus35Days();

    @Test(enabled = true, description = "TC32277_Get_Invoice_Details_Invoice_Status_Final_Inactive_Account_Bill_Style_Summary_ETC_not_overriden", groups = "SelfCareWS.GetInvoiceDetail")
    public void TC32277_Get_Invoice_Details_Invoice_Status_Final_Inactive_Account_Bill_Style_Summary_ETC_not_overriden() {
        test.get().info("1. Create an onlines CC Customer with FC 1 bundle of SB and sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
        customerNumber = owsActions.customerNo;

        test.get().info("2. Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("3. Update bill group payment collection date to 10 days later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("4. Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("5. Update the start date of customer");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //===========================================================================
        test.get().info("6. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("7. Record latest subscription for customer");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscriptionNumberValue = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(1);
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        subscriptionNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();

        test.get().info("8. Deactivate account ETC not override");
        deactivateAccountETCNotOverride();

        test.get().info("9. Verify customer end date and subscription end date changed to today");
        Date endDate = TimeStamp.Today();
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), CommonContentPage.CustomerSummarySectionPage.getInstance().getCustomerSummaryEndDate());
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGridSectionPage.getInstance().getRowNumberOfSubscriptionsTable());
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptions(SubscriptionEntity.dataForSummarySubscriptions(newStartDate, endDate)).size());
        //===========================================================================

        test.get().info("10. Update customer end date");
        endDate = TimeStamp.TodayMinus2Days();
        CommonActions.updateCustomerEndDate(customerNumber, endDate);

        test.get().info("11. Verify customer end date updated successfully");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), CommonContentPage.CustomerSummarySectionPage.getInstance().getCustomerSummaryEndDate());
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), CommonContentPage.SubscriptionsGridSectionPage.getInstance().getEndDateByIndex(1));
        //===========================================================================
        test.get().info("12. Submit draft bill run");
        submitDraftBillRun();

        test.get().info("13. Submit confirm bill run");
        submitConfirmBillRun();

        test.get().info("14. Verify the final invoice generated");
        verifyTheFinalInvoiceGenerated();

        test.get().info("15. Get invoice charge id");
        getInvoiceChargeId();

        test.get().info("19. Submit get invoice detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetInvoiceDetailRequest(customerNumber);

        test.get().info("20. Verify get invoice detail request");
        String tempFile = "src\\test\\resources\\xml\\sws\\getinvoicedetail\\TC32277_Response.xml";
        verifyGetInvoiceDetailResponse(response, tempFile, customerNumber, newStartDate, subscriptionNumber, invoiceNumber);
    }

    private void verifyGetInvoiceDetailResponse(Xml response, String tempFile, String customerNumber, Date newStartDate, String subscriptionNumber, String invoiceNumber) {
        String actualFile = Common.saveXmlFile(customerNumber + "_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(response.toString())));
        String chargeId = response.getTextByXpath("//chargeId", 0);
        String chargeId2 = response.getTextByXpath("//chargeId", 1);

        String invoiceStatus = response.getTextByTagName("invoiceStatus");
        Date invoiceDueDate = BaseTest.paymentCollectionDateEscapeNonWorkDay(10);

        String sDateFrom = Parser.parseDateFormate(newStartDate, TimeStamp.DateFormatXml());
        String sDateTo = Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DateFormatXml());
        String sDateFrom2 = Parser.parseDateFormate(TimeStamp.TodayMinus1Month(), TimeStamp.DateFormatXml());
        String sDateTo2 = Parser.parseDateFormate(TimeStamp.TodayMinus2Days(), TimeStamp.DateFormatXml());
        String sDateToLastMonth = Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus1Day(), TimeStamp.DateFormatXml());
        String sDateIssued = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DateFormatXml());
        String sDateDue = Parser.parseDateFormate(invoiceDueDate, TimeStamp.DateFormatXml());

        String XmlValue = Common.readFile(tempFile).replace("$accountNumber$", customerNumber)
                .replace("$subscriptionNumber$", subscriptionNumber)
                .replace("$invoiceNumber$", invoiceNumber)
                .replace("$dateFrom$", sDateFrom)
                .replace("$dateTo$", sDateTo)
                .replace("$dateToLastMonth$", sDateToLastMonth)
                .replace("$dateFrom2$", sDateFrom2)
                .replace("$dateTo2$", sDateTo2)
                .replace("$dateIssued$", sDateIssued)
                .replace("$dateDue$", sDateDue)
                .replace("$chargeId$", chargeId)
                .replace("$chargeId2$", chargeId2);

        String expectedResponse = Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(XmlValue)));

        Assert.assertEquals(1, Common.compareFile(expectedResponse, actualFile).size());
    }

    private void getInvoiceChargeId() {
        invoiceId = BillingActions.getInstance().getInvoiceIdByInvoiceNumber(invoiceNumber);
        try {
            String sql = String.format("select invoicechargeid from invoicecharge where chargeamtbill = '10' and invoiceid =%s order by 1 desc", invoiceId);
            ResultSet rs = OracleDB.SetToNonOEDatabase().executeQuery(sql);
            String.valueOf(OracleDB.getValueOfResultSet(rs, "invoiceid"));
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
    }

    private void verifyTheFinalInvoiceGenerated() {
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        invoiceNumber = InvoicesContentPage.getInstance().getInvoiceNumber();

//        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);
        Assert.assertEquals("FINAL", BillingActions.getInstance().getInvoiceTypeByInvoiceNumber(invoiceNumber));
    }

    private void deactivateAccountETCNotOverride() {
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().clickNextButton();
        Assert.assertEquals("72.00", ServiceOrdersPage.ReturnsAndEtcPage.getInstance().getETCAmountValue());

        ServiceOrdersPage.ReturnsAndEtcPage.getInstance().selectWaiveETCReasonByIndexAndValue(0, "Contract Upgrade");
        ServiceOrdersPage.ReturnsAndEtcPage.getInstance().setNotes("automation");
        ServiceOrdersPage.ReturnsAndEtcPage.getInstance().clickNextButton();

        DeactivateSubscriptionPage.ConfirmDeactivatingSubscription.getInstance().clickNextButton();

        ServiceOrdersPage.ServiceOrderComplete.getInstance().clickReturnToCustomer();
    }
}
