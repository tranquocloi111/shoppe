package suite.regression.selfcarews.getinvoice;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.find.InvoicesContentPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.List;

import static logic.utils.TimeStamp.*;

/**
 * User: Nhi Dinh
 * Date: 7/10/2019
 */
public class TC32240_Self_Care_WS_Get_Invoice_Number_Not_Provided extends BaseTest {
    String invoiceNumber;
    String customerNumber;
    String subscriptionNumber;
    private Date newStartDate = TimeStamp.TodayMinus20Days();

    @Test(enabled = true, description = "TC32240_Self_Care_WS_Get_Invoice_Number_Not_Provided", groups = "SelfCareWS.GetInvoice")
    public void TC32240_Self_Care_WS_Get_Invoice_Number_Not_Provided() {
        test.get().info("1. Create an onlines CC customer with FC no bundle but has sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFCNoBundleButHasSimOnly();
        String customerNumber = owsActions.customerNo;

        test.get().info("2. Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("3. Update bill group payment collection date to 10 days later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("4. Set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("5. Update the start date of customer");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //===========================================================================
        test.get().info("6. Submit do refill bc job");
        submitDoRefillBCJob();

        test.get().info("7. Submit do refill nc job");
        submitDoRefillNCJob();

        test.get().info("8. Submit do bundle renew job");
        submitDoBundleRenewJob();

        test.get().info("9. draft bill run");
        submitDraftBillRun();

        test.get().info("10. Submit confirm bill run");
        submitConfirmBillRun();

        test.get().info("11. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("12. Verify customer has 1 invoice generated");
        invoiceNumber = CareTestBase.page().verifyCustomerHas1InvoiceGenerated();
        //===========================================================================
        test.get().info("13. Submit get invoice request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetInvoiceRequest(customerNumber);

        test.get().info("14. Verify get invoice response");
        subscriptionNumber = CareTestBase.page().recordLatestSubscriptionNumberForCustomer();
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyGetInvoiceResponse(response, customerNumber, subscriptionNumber + "  Mobile FC", newStartDate);

        test.get().info("15. Open invoice details screen");
        CareTestBase.page().openInvoiceDetailsScreen();

        test.get().info("16. Verify PDF content of second invoice is correct");
        verifyPDFContentOfSecondInvoiceIsCorrect(customerNumber);
    }

    private void verifyPDFContentOfSecondInvoiceIsCorrect(String customerNumber) {
        String downloadedPDFFile = BaseTest.getDownloadInvoicePDFFile(customerNumber);
        String failedAssertMessage = "Failed to assert value: ";

        List<String> pdfList_Page1 = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile, 1, 1);

        String expectFirstBill = "Your first bill   £15.00 Clubcard";
        Assert.assertTrue(pdfList_Page1.contains(expectFirstBill), failedAssertMessage + expectFirstBill);

        List<String> pdf_Page3 = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile, 3, 3);
        String summaryOfUserCharges = "Summary of charges";
        String userCharges = String.format("User charges for %s  Mobile FC (£10 Tariff 12 Month Contract)", subscriptionNumber);

        Assert.assertTrue(pdf_Page3.contains(summaryOfUserCharges), failedAssertMessage + summaryOfUserCharges);
        Assert.assertTrue(pdf_Page3.contains(userCharges), failedAssertMessage + userCharges);

        String sNewStartDate = Parser.parseDateFormate(newStartDate, DATE_FORMAT_IN_PDF);
        String sToday = Parser.parseDateFormate(Today(), DATE_FORMAT_IN_PDF);
        String sToDayMinus1Day = Parser.parseDateFormate(TodayMinus1Day(), DATE_FORMAT_IN_PDF);
        String sTodayPlus1MonthMinus1Day = Parser.parseDateFormate(TodayPlus1MonthMinus1Day(), DATE_FORMAT_IN_PDF);

        String monthlySubscription1 = String.format("Monthly subscription %s %s 10.00", sNewStartDate, sToDayMinus1Day);
        String monthlySubscription2 = String.format("Monthly subscription %s %s 10.00", sToday, sTodayPlus1MonthMinus1Day);
        Assert.assertTrue(pdf_Page3.contains(monthlySubscription1), failedAssertMessage + monthlySubscription1);
        Assert.assertTrue(pdf_Page3.contains(monthlySubscription2), failedAssertMessage + monthlySubscription2);

        Assert.assertTrue(pdf_Page3.contains("Date Type Reference Amount (£)"), failedAssertMessage + "Date Type Reference Amount (£)");
        Assert.assertTrue(pdf_Page3.contains(String.format("%s Online/Telesales -15.00", sNewStartDate)), failedAssertMessage + "Online/Telesales");
        Assert.assertTrue(pdf_Page3.contains("Total Payments -15.00"), failedAssertMessage + "Total Payments -15.00");

    }


}