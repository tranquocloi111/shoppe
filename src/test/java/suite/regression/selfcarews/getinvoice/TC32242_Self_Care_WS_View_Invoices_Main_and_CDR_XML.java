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
 * Date: 8/10/2019
 */
public class TC32242_Self_Care_WS_View_Invoices_Main_and_CDR_XML extends BaseTest {
    String invoiceNumber;
    String customerNumber;
    String subscriptionNumber;
    private Date newStartDate = TimeStamp.TodayMinus20Days();

    @Test(enabled = true, description = "TC32242_Self_Care_WS_View_Invoices_Main_and_CDR_XML", groups = "SelfCareWS.GetInvoice")
    public void TC32242_Self_Care_WS_View_Invoices_Main_and_CDR_XML() {
        test.get().info("1. Create a CC Customer with order");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWithOrder();
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
        selfCareWSTestBase.verifyGetInvoiceResponse(response, customerNumber, subscriptionNumber + "  Mobile Ref 1", newStartDate);
        //===========================================================================
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
        String userCharges = String.format("User charges for %s  Mobile Ref 1 (£10 Tariff 12 Month Contract)", subscriptionNumber);

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

        String AdjmtChgsAndCrdtsSumary = "Adjustments, charges and credits";
        String AdjmtChargesAndCredits1 = String.format("%s %s Nokia 2720 for %s 0.00", sNewStartDate, sNewStartDate, subscriptionNumber);
        String AdjmtChargesAndCredits2 = String.format("%s %s Monthly 500MB data allowance for %s 5.00", sNewStartDate, sToDayMinus1Day, subscriptionNumber);
        String AdjmtChargesAndCredits3 = String.format("%s %s £2.50 safety buffer for %s 0.00", sNewStartDate, sToDayMinus1Day, subscriptionNumber);
        String AdjmtChargesAndCredits4 = String.format("%s %s Monthly 500MB data allowance for %s 5.00", sToday, sTodayPlus1MonthMinus1Day, subscriptionNumber);
        String AdjmtChargesAndCredits5 = String.format("%s %s £2.50 safety buffer for %s 0.00", sToday, sTodayPlus1MonthMinus1Day, subscriptionNumber);
        String PaymentsReceived = "Payments received";

        Assert.assertTrue(pdf_Page3.contains(AdjmtChgsAndCrdtsSumary), failedAssertMessage + AdjmtChgsAndCrdtsSumary);
        Assert.assertTrue(pdf_Page3.contains(AdjmtChargesAndCredits1), failedAssertMessage + AdjmtChargesAndCredits1);
        Assert.assertTrue(pdf_Page3.contains(AdjmtChargesAndCredits2), failedAssertMessage + AdjmtChargesAndCredits2);
        Assert.assertTrue(pdf_Page3.contains(AdjmtChargesAndCredits3), failedAssertMessage + AdjmtChargesAndCredits3);
        Assert.assertTrue(pdf_Page3.contains(AdjmtChargesAndCredits4), failedAssertMessage + AdjmtChargesAndCredits4);
        Assert.assertTrue(pdf_Page3.contains(AdjmtChargesAndCredits5), failedAssertMessage + AdjmtChargesAndCredits5);
        Assert.assertTrue(pdf_Page3.contains(PaymentsReceived), failedAssertMessage + PaymentsReceived);

        Assert.assertTrue(pdf_Page3.contains(String.format("%s Online/Telesales -15.00", sNewStartDate)), failedAssertMessage + "Online/Telesales");
        Assert.assertTrue(pdf_Page3.contains("Total Payments -15.00"), failedAssertMessage + "Total Payments -15.00");

    }

}
