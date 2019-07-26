package suite;

import framework.utils.Pdf;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.SubscriptionEntity;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.options.ChangeSubscriptionNumberPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import static logic.utils.TimeStamp.*;

/**
 * User: Nhi Dinh
 * Date: 8/07/2019
 */
public class TC30029_Care_Change_of_MPN extends BaseTest{
    private Date newStartDate = TimeStamp.TodayMinus15Days();
    private String subscriptionNumber;
    private String newSubscriptionNumber;
    private String discountGroupCodeOfMobileRef1;
    private String customerNumber;
    @Test
    public void TC30029_Care_Change_of_MPN(){
        test.get().info("Step 1 : Create an online CC customer with FC 2 bundles and NK2720");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC2BundlesAndNK2720();
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 7: Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 8: Verify Customer Start Date and Billing Group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 9: Verify all discount bundle entries align with bill run calendar entries");
        verifyDiscountBundleEntriesIsCorrect();
        //=================================================================
        test.get().info("Step 10: Open change subscription number page");
        CareTestBase.page().openChangeSubscriptionNumberPage();

        test.get().info("Step 11: Verify all detail on subscription number is correct");
        verifyAllDetailOnSubscriptionNumberIsCorrect();

        test.get().info("Step 12: Update the subscription number and click next button");
        newSubscriptionNumber = CareTestBase.page().updateTheSubscriptionNumberAndClickNextButton();

        test.get().info("Step 13: Verify confirm changing subscription number message is correct");
        CareTestBase.page().verifyConfirmChangingSubscriptionNumberMessageIsCorrect(subscriptionNumber,newSubscriptionNumber);

        test.get().info("Step 14: Verify service order complete page and click return to customer button");
        CareTestBase.page().verifyServiceOrderCompletePageAndClickReturnToCustomerButton();

        //====================================================================================
        test.get().info("Step 15: Verify the old subscription is inactive");
        verifyTheOldSubscriptionIsInactive();

        test.get().info("Step 16: Verify the new subscription is Active");
        verifyTheNewSubscriptionIsActive();

        test.get().info("Step 17: Verify discount bundle entries is correct");
        verifyDiscountBundleEntriesIsCorrect();

        //====================================================================================
        test.get().info("Submit do-refill-bc job");
        submitDoRefillBCJob();

        test.get().info("Submit do-refill-nc job");
        submitDoRefillNCJob();

        test.get().info("Submit do-bundle-renew job");
        submitDoBundleRenewJob();

        test.get().info("Verify new discount bundle entries have been created");
        verifyNewDiscountBundleEntriesHaveBeenCreated();

        //===================================================================================
        test.get().info("Submit draft bill run");
        submitDraftBillRun();

        test.get().info("Submit confirm bill run");
        submitConfirmBillRun();

        test.get().info("Verify discount bundle entries are updated");
        verifyDiscountBundleEntriesAreUpdated();

        //===================================================================================
        test.get().info("Open invoice details screen");
        CareTestBase.page().openInvoiceDetailsScreen();

        test.get().info("Verify invoice details are correct");
        CareTestBase.page().verifyInvoiceDetailsAreCorrect(Parser.parseDateFormate(Today(), "dd MMM yyyy"), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), "dd MMM yyyy"), Parser.parseDateFormate(BaseTest.paymentCollectionDateEscapeNonWorkDay(10), "dd MMM yyyy"),"Confirmed");

        //===================================================================================
        test.get().info("Verify PDF File");

    }

    private void verifyDiscountBundleEntriesIsCorrect(){
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        subscriptionNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);

        Assert.assertEquals(11, discountBundles.size());
        verifyFCDiscountBundles(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TM5K");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TMDAT");
    }

    private void verifyAllDetailOnSubscriptionNumberIsCorrect(){
        ChangeSubscriptionNumberPage.ChangeSubscriptionNumber changeSubscriptionNumberForm = ChangeSubscriptionNumberPage.ChangeSubscriptionNumber.getInstance();
        Assert.assertEquals((subscriptionNumber + "  Mobile Ref 1"), changeSubscriptionNumberForm.getCurrentSubscriptionNumber());
        Assert.assertEquals("", changeSubscriptionNumberForm.getNewSubscriptionNumber());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), DATE_FORMAT_IN_PDF), changeSubscriptionNumberForm.getNextBillDate());
        Assert.assertEquals(Parser.parseDateFormate(Today(), DATE_FORMAT_IN_PDF), changeSubscriptionNumberForm.getChangeDate());
        Assert.assertEquals("", changeSubscriptionNumberForm.getNotes());
    }

    private void verifyTheOldSubscriptionIsInactive(){
        Date EndDate = TimeStamp.TodayPlus1MonthMinus1Day();
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getNumberOfSubscription(SubscriptionEntity.dataForInactiveSubscriptions((subscriptionNumber + "  Mobile Ref 1"),newStartDate, EndDate)));
    }

    private void verifyTheNewSubscriptionIsActive(){
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getNumberOfSubscription(SubscriptionEntity.dataForActiveSubscriptions((newSubscriptionNumber + "  Mobile Ref 1"), newStartDate)));
    }

    private void verifyNewDiscountBundleEntriesHaveBeenCreated(){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(12, discountBundles.size());
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "FC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(),"FLX17", "ACTIVE"));
    }

    private void verifyDiscountBundleEntriesAreUpdated(){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(12, discountBundles.size());
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "FC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(),"FLX17", "ACTIVE"));
        verifyFCDiscountBundles(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TM5K");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TMDAT");

        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
    }

    private void verifyPDFFile(){
        BaseTest.downloadInvoicePDFFile(customerNumber);
        List<String>  pdfList = Pdf.getInstance().getText(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getPathOfPdfFile(), 1, 1);
        String expectFirstBill = "Your first bill £15.00";
        Assert.assertTrue(pdfList.contains(expectFirstBill));

        String amountExpected = "Amount due 15.00";
        Assert.assertTrue(pdfList.contains(amountExpected));

        pdfList = Pdf.getInstance().getText(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getPathOfPdfFile(), 3);
        String summaryOfUserCharges = "Summary of charges";
        String userCharges = String.format("User charges for %s  Mobile Ref 1 (£10 Tariff 12 Month Contract)", subscriptionNumber);

        String sNewStartDate = Parser.parseDateFormate(newStartDate, DATE_FORMAT_IN_PDF);
        String sToday = Parser.parseDateFormate(Today(), DATE_FORMAT_IN_PDF);
        String sToDayMinus1Day = Parser.parseDateFormate(TodayMinus1Day(), DATE_FORMAT_IN_PDF);
        String sTodayPlus1MonthMinus1Day = Parser.parseDateFormate(TodayPlus1MonthMinus1Day(),DATE_FORMAT_IN_PDF);

        String monthlySubscription1 = String.format("Monthly subscription %s %s 10.00", sNewStartDate, sToDayMinus1Day);
        String monthlySubscription2 = String.format("Monthly subscription %s %s 10.00", sToday, sTodayPlus1MonthMinus1Day);

        String AdjmtChargesAndCredits1 = String.format("%s %s Nokia 2720 for %s 0.00", sNewStartDate, sNewStartDate, newSubscriptionNumber);
        String AdjmtChargesAndCredits2 = String.format("%s %s Monthly 500MB data allowance for %s 5.00", sNewStartDate, sToDayMinus1Day, newSubscriptionNumber);
        String AdjmtChargesAndCredits3 = String.format("%s %s £20 safety buffer for %s 0.00", sNewStartDate, sToDayMinus1Day, newSubscriptionNumber);
        String AdjmtChargesAndCredits4 = String.format("%s %s Monthly 500MB data allowance for %s 5.00", sToday, sTodayPlus1MonthMinus1Day, newSubscriptionNumber);
        String AdjmtChargesAndCredits5 = String.format("%s %s £20 safety buffer for %s 0.00", sToday, sTodayPlus1MonthMinus1Day, newSubscriptionNumber);

        Assert.assertTrue(pdfList.contains(summaryOfUserCharges));
        Assert.assertTrue(pdfList.contains(userCharges));

        Assert.assertTrue(pdfList.contains(monthlySubscription1));
        Assert.assertTrue(pdfList.contains(monthlySubscription2));

        Assert.assertTrue(pdfList.contains(AdjmtChargesAndCredits1));
        Assert.assertTrue(pdfList.contains(AdjmtChargesAndCredits2));
        Assert.assertTrue(pdfList.contains(AdjmtChargesAndCredits3));
        Assert.assertTrue(pdfList.contains(AdjmtChargesAndCredits4));
        Assert.assertTrue(pdfList.contains(AdjmtChargesAndCredits5));

    }
}
