package suite.regression.batchjobs;

//import javafx.util.Pair;
import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TC3_Batch_Produce_Invoice_for_2nd_Bill_Cycle extends BaseTest {
    private String customerNumber = "15758";
    private Date newStartDate;
    private String username;
    private String password;
    private String serviceRefOf1stSubscription;
    private String serviceRefOf2stSubscription;
    private String serviceOrderId;
    String discountGroupCodeOfMobileRef1;
    String discountGroupCodeOfMobileRef2;


    @Test(enabled = true, description = "TC3_Batch_Produce_Invoice_for_2nd_Bill_Cycle", groups = "BillRun")
    public void TC3_Batch_Produce_Invoice_for_2nd_Bill_Cycle(){
        test.get().info("Step 1 : Create a CC customer with order");
        OWSActions owsActions = new OWSActions();

        test.get().info("Step 2 : Create new billing group start 1 month ago");
        createNewBillingGroupStart1MonthAgo();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");

        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update Customer Start Date");
        newStartDate = Date.valueOf(TimeStamp.Today().toLocalDate().plusMonths(-1).plusDays(-20));
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 7 : Verify customer start date and billing group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 8 : Verify customer bill style is set to Summary");
        verifyCustomerBillStyleIsSetToSummary();

        test.get().info("Step 9 : Verify all discount bundle entries align with bill run calendar entires");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 1");
        serviceRefOf2stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 2");

        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " FC Mobile 1");
        discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyFCDiscountBundleBeforeChangingBundle(discountGroupCodeOfMobileRef1);

        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf2stSubscription + " FC Mobile 2");
        discountGroupCodeOfMobileRef2 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyNCDiscountBundleBeforeChangingBundle(discountGroupCodeOfMobileRef2);

        test.get().info("Step 10 : Run refill job");
        BaseTest.submitDoRefillBCJob();
        BaseTest.submitDoRefillNCJob();
        BaseTest.submitDoBundleRenewJob();

        test.get().info("Step 11 : Verify new discount bundle entries have been created by first bill run");
        verifyNewDiscountBundleEntriesHaveBeenCreatedByFirstBillRun();

        test.get().info("Step 12 : Submit draft bill run");
        BaseTest.submitDraftBillRun();

        test.get().info("Step 13 : Submit confirm bill run");
        BaseTest.submitConfirmBillRun();

        test.get().info("Step 14 : Verify One Invoice Generated With Issue Date Of Today");
        verifyOneInvoiceGeneratedWithIssueDateOfToday();

        test.get().info("Step 15 : Verify Invoice PDF Content Is Correct");
        verifyInvoicePDFContentIsCorrect();

        test.get().info("Step 16 : Run refill job again");
        BaseTest.submitDoRefillBCJob();
        BaseTest.submitDoRefillNCJob();
        BaseTest.submitDoBundleRenewJob();

        test.get().info("Step 17 : Verify New Discount Bundle Entries Have Been Created By Second Bill Run");
        verifyNewDiscountBundleEntriesHaveBeenCreatedBySecondBillRun();

        test.get().info("Step 18 : Change Back Bill Run Calendar RunDates");
        changeBackBillRunCalendarRunDates();

        test.get().info("Step 19 : Submit draft bill run");
        BaseTest.submitDraftBillRun();

        test.get().info("Step 20 : Submit confirm bill run");
        BaseTest.submitConfirmBillRun();

        test.get().info("Step 21 : verify Second Invoice Generated With Issue Date Of Today");
        verifySecondInvoiceGeneratedWithIssueDateOfToday();

        test.get().info("Step 22 : verify PDF Content Of Second Invoice Is Correct");
        verifyPDFContentOfSecondInvoiceIsCorrect();

    }

    private void createNewBillingGroupStart1MonthAgo(){
        int days = getDaysOfStart1MonthAgo();
        BillingActions.getInstance().createNewBillingGroup(days, true, -1);
    }

    private void verifyFCDiscountBundleBeforeChangingBundle(String discountGroupCode){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(11, discountBundles.size());

        BaseTest.verifyFCDiscountBundles(discountBundles, newStartDate, "FLX17");

        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");

        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");

        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TM150");
    }

    private void verifyNCDiscountBundleBeforeChangingBundle(String discountGroupCode){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);

        Assert.assertEquals(9, discountBundles.size());
        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TM250");

        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");

        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TM1GB");
    }

    private void verifyCustomerBillStyleIsSetToSummary(){
        Assert.assertEquals("Summary", DetailsContentPage.BillingInformationSectionPage.getInstance().getBillStyle());
    }

    private void verifyNewDiscountBundleEntriesHaveBeenCreatedByFirstBillRun(){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(12, discountBundles.size());
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "FC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "FLX17", "ACTIVE"));

        discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef2);
        Assert.assertEquals(9, discountBundles.size());
    }

    private void verifyNewDiscountBundleEntriesHaveBeenCreatedBySecondBillRun(){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(16, discountBundles.size());
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "TM500", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "TMT5K", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "TM150", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "FC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "FLX17", "ACTIVE"));

        discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef2);
        Assert.assertEquals(12, discountBundles.size());
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "TM250", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "TMT5K", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "TM1GB", "ACTIVE"));
    }

    private void verifyOneInvoiceGeneratedWithIssueDateOfToday(){
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(1, grid.getRowNumberOfInvoiceTable());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), grid.getIssued());

        grid.clickInvoiceNumberByIndex(1);
    }

    private void verifySecondInvoiceGeneratedWithIssueDateOfToday(){
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(2, grid.getRowNumberOfInvoiceTable());

        grid.clickInvoiceNumberByIndex(1);
    }

    private void verifyInvoicePDFContentIsCorrect(){
        String downloadedPDFFile = BaseTest.getDownloadInvoicePDFFile(customerNumber);
        List<String> pdfList = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile,1,1);

        String expectFirstBill = "Your First Bill £27.50".toLowerCase();
        Assert.assertTrue(pdfList.contains(expectFirstBill));

        String expectBillDate = String.format("Billed To Date: %s", Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), "dd-MMM-yyyy")).toLowerCase();
        Assert.assertTrue(pdfList.contains(expectBillDate));

        pdfList = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile,3);
        Assert.assertTrue(pdfList.contains(String.format("User charges for %s FC Mobile 1 (£10 Tariff 12 Month Contract)", serviceRefOf1stSubscription)));
        Assert.assertTrue(pdfList.contains(String.format("User charges for %s NC Mobile 3 (£10 SIM Only Tariff)", serviceRefOf2stSubscription)));
        Assert.assertTrue(pdfList.contains(String.format("Total charges for %s 20.00", serviceRefOf1stSubscription)));
        Assert.assertTrue(pdfList.contains(String.format("Total charges for %s 20.00", serviceRefOf2stSubscription)));
    }

    private void verifyPDFContentOfSecondInvoiceIsCorrect(){
        String downloadedPDFFile = BaseTest.getDownloadInvoicePDFFile(customerNumber);
        List<String> pdfList = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile,1,1);

        String expectFirstBill = "Your Total Bill £55.00".toLowerCase();
        Assert.assertTrue(pdfList.contains(expectFirstBill));

        pdfList = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile,3);
        Assert.assertTrue(pdfList.contains(String.format("User charges for %s FC Mobile 1 (£10 Tariff 12 Month Contract)", serviceRefOf1stSubscription)));
        Assert.assertTrue(pdfList.contains(String.format("User charges for %s NC Mobile 3 (£10 SIM Only Tariff)", serviceRefOf2stSubscription)));
        Assert.assertTrue(pdfList.contains(String.format("Total charges for %s 10.00", serviceRefOf1stSubscription)));
        Assert.assertTrue(pdfList.contains(String.format("Total charges for %s 10.00", serviceRefOf2stSubscription)));
    }

    private void changeBackBillRunCalendarRunDates(){
        int days = getDaysOfStart1MonthAgo();
        OracleDB.SetToNonOEDatabase().executeNonQuery(String.format("update billruncalendar set rundate=trunc(SYSDATE - %d) where rundate=trunc(SYSDATE) and billinggroupid=%d", days, BillingActions.tempBillingGroupHeader.getKey()));
        OracleDB.SetToNonOEDatabase().executeNonQuery("update billruncalendar set rundate=trunc(SYSDATE) where rundate=trunc(SYSDATE + 1) and billinggroupid=" + BillingActions.tempBillingGroupHeader.getKey());
    }

    private int getDaysOfStart1MonthAgo(){
        LocalDate firstRunDate = LocalDate.now().plusMonths(-1);
        LocalDate today = LocalDate.now();
        return Math.toIntExact(Math.abs(ChronoUnit.DAYS.between(today, firstRunDate)));
    }
}
