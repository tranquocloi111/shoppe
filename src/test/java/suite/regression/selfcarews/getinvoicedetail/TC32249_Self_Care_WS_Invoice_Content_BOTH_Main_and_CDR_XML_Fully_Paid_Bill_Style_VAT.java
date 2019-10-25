package suite.regression.selfcarews.getinvoicedetail;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
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
public class TC32249_Self_Care_WS_Invoice_Content_BOTH_Main_and_CDR_XML_Fully_Paid_Bill_Style_VAT extends BaseTest {
    String invoiceNumber;
    String customerNumber;
    String subscriptionNumber;
    private Date newStartDate = TimeStamp.TodayMinus20Days();

    @Test(enabled = true, description = "TC32249_Self_Care_WS_Invoice_Content_BOTH_Main_and_CDR_XML_Fully_Paid_Bill_Style_VAT", groups = "SelfCareWS.GetInvoice")
    public void TC32249_Self_Care_WS_Invoice_Content_BOTH_Main_and_CDR_XML_Fully_Paid_Bill_Style_VAT() {
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
        //===========================================================================
        test.get().info("6. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("7. Edit billing detail invoice style value");
        String billStyle = "VAT";
        CareTestBase.page().editBillingDetailInvoiceStyleValue(billStyle);

        test.get().info("8. Verify billing style is correct");
        Assert.assertEquals("VAT", DetailsContentPage.BillingInformationSectionPage.getInstance().getBillStyle());

        //===========================================================================
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
        test.get().info("14. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("15. Update invoice due date");
        BillingActions.updateInvoiceDueDate(customerNumber, TimeStamp.Today());

        test.get().info("16: submit payment allocation batch Job");
        RemoteJobHelper.getInstance().submitPaymentAllocationBatchJobRun();
        //===========================================================================
        test.get().info("17 : Run Credit Card batch jobs");
        RemoteJobHelper.getInstance().submitCreditCardBatchJobRun();

        test.get().info("18. Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("19. Verify invoice status changed to Fully Paid after CC payment");
        invoiceNumber = verifyInvoiceStatusChangedToFullyPaidAfterCCPayment();
        //===========================================================================
        test.get().info("20. Submit get invoice request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetInvoiceRequest(customerNumber);

        test.get().info("21. Verify get invoice response");
        subscriptionNumber = CareTestBase.page().recordLatestSubscriptionNumberForCustomer();
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyGetInvoiceResponse(response, customerNumber, subscriptionNumber + "  Mobile Ref 1", newStartDate);
        //===========================================================================
        test.get().info("22. Open invoice details screen");
        CareTestBase.page().openInvoiceDetailsScreen();

        test.get().info("23. Verify PDF content of second invoice is correct");
        verifyPDFContentOfSecondInvoiceIsCorrect(customerNumber);
    }

    private void verifyPDFContentOfSecondInvoiceIsCorrect(String customerNumber) {
        String downloadedPDFFile = BaseTest.getDownloadInvoicePDFFile(customerNumber);
        String failedAssertMessage = "Failed to assert value: ";

        List<String> pdfList_Page1 = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile, 1, 1);

        String expectFirstBill = "Your first bill   £10.00 Clubcard";
        Assert.assertTrue(pdfList_Page1.contains(expectFirstBill), failedAssertMessage + expectFirstBill);
        String expectUserCharges = "User charges 20.00 Clubcard Number: **************2784";
        Assert.assertTrue(pdfList_Page1.contains(expectUserCharges), failedAssertMessage + expectUserCharges);
        String expectTotalCharge = "Total charge 20.00";
        Assert.assertTrue(pdfList_Page1.contains(expectTotalCharge), failedAssertMessage + expectTotalCharge);
        String expectPaymentReceived = "Payments received -10.00";
        Assert.assertTrue(pdfList_Page1.contains(expectPaymentReceived), failedAssertMessage + expectPaymentReceived);
        String expectAmountDue = "Amount due 10.00";
        Assert.assertTrue(pdfList_Page1.contains(expectAmountDue), failedAssertMessage + expectAmountDue);

        List<String> pdf_Page3 = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile, 3, 3);
        String summaryOfUserCharges = "Summary of charges";
        String userCharges = String.format("User charges for %s  Mobile Ref 1 (£10 Tariff 12 Month Contract)", subscriptionNumber);

        Assert.assertTrue(pdf_Page3.contains(summaryOfUserCharges), failedAssertMessage + summaryOfUserCharges);
        Assert.assertTrue(pdf_Page3.contains(userCharges), failedAssertMessage + userCharges);

        String line = "From Date To Date Cost Ex. VAT VAT Inc. VAT";
        Assert.assertTrue(pdf_Page3.contains(line), failedAssertMessage + line);

        String sNewStartDate = Parser.parseDateFormate(newStartDate, DATE_FORMAT_IN_PDF);
        String sToday = Parser.parseDateFormate(Today(), DATE_FORMAT_IN_PDF);
        String sToDayMinus1Day = Parser.parseDateFormate(TodayMinus1Day(), DATE_FORMAT_IN_PDF);
        String sTodayPlus1MonthMinus1Day = Parser.parseDateFormate(TodayPlus1MonthMinus1Day(), DATE_FORMAT_IN_PDF);


        String monthlySubscription1 = String.format("Monthly subscription %s %s 8.33 1.67 10.00", sNewStartDate, sToDayMinus1Day);
        String monthlySubscription2 = String.format("Monthly subscription %s %s 8.33 1.67 10.00", sToday, sTodayPlus1MonthMinus1Day);
        Assert.assertTrue(pdf_Page3.contains(monthlySubscription1), failedAssertMessage + monthlySubscription1);
        Assert.assertTrue(pdf_Page3.contains(monthlySubscription2), failedAssertMessage + monthlySubscription2);

        String safetyBuffer1 = String.format("%s %s £20 safety buffer for %s 0.00 0.00 0.00", sNewStartDate, sToDayMinus1Day, subscriptionNumber);
        String safetyBuffer2 = String.format("%s %s £20 safety buffer for %s 0.00 0.00 0.00", sToday, sTodayPlus1MonthMinus1Day, subscriptionNumber);
        Assert.assertTrue(pdf_Page3.contains(safetyBuffer1), failedAssertMessage + safetyBuffer1);
        Assert.assertTrue(pdf_Page3.contains(safetyBuffer2), failedAssertMessage + safetyBuffer2);

        Assert.assertTrue(pdf_Page3.contains("Total Payments -10.00"), failedAssertMessage + "Total Payments -10.00");
    }

}
