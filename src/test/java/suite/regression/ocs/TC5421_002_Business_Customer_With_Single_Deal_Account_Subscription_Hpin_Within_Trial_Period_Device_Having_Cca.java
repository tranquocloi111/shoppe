package suite.regression.ocs;

import framework.utils.RandomCharacter;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class TC5421_002_Business_Customer_With_Single_Deal_Account_Subscription_Hpin_Within_Trial_Period_Device_Having_Cca extends BaseTest {
    private String customerNumber = "47758425";
    private String orderId = "8701636";
    private String subNo1 = "07647064770";
    private String subNo2 = "07647064770";
    private OWSActions owsActions;
    private Date newStartDate;

    @Test(enabled = true, description = "TC5421_002_Business_Customer_With_Single_Deal_Account_Subscription_Hpin_Within_Trial_Period_Device_Having_Cca", groups = "OCS")
    public void TC5421_002_Business_Customer_With_Single_Deal_Account_Subscription_Hpin_Within_Trial_Period_Device_Having_Cca() {
        test.get().info("Step 1 : Create a Consumer customer with single deal account, subscription is on HPIN within trial period, device having CCA");
        CommonActions.updateHubProvisionSystem("H");
        owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC5421_Single_Deal_HPIN_CCA_Business.xml";
        owsActions.createOcsCustomerRequestAcceptUrl(path,2, "HPIN");

        test.get().info("Step 2 : Create new billing group");
        createNewBillingGroup();

        test.get().info("Step 3 : Update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        orderId = owsActions.orderIdNo;
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update start date for customer");
        newStartDate = TimeStamp.TodayMinus1MonthMinus20Day();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Verify Create Ocs Account async task is not displayed");
        checkCreateOcsAccountCommand();

        test.get().info("Step 7 : Login to Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");
        subNo2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 2");
        verifyOcsKeyOfSubscription();

        test.get().info("Step 8 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateSubscriptionWithoutEtc();

        test.get().info("Step 9 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo2));

        test.get().info("Step 10 : Verify Agreement Adjustment Products charge with a price equal to the Agreement ETC Override Amount is created.");
        verifyOtherChargesCreditsAreCorrect();

        test.get().info("Step 11 : Select Live Bill Estimate. Verify subscription charges.");
        verifyLiveBillEstimateAreCorrect();

        test.get().info("Step 12 : Navigate to Service Orders screen. Validate the Deactivate Subscription Service Order.");
        verifyServiceOrdersAreCreatedCorrectly();

        test.get().info("Step 13 : Run Inclusive Spend Refill for Billing Capped and Network Capped. Run Discount Bundle Renewal job.");
        submitDoRefillBCJob();
        submitDoRefillNCJob();
        submitDoBundleRenewJob();

        test.get().info("Step 14 : Submit Bill Run Job");
        submitDraftBillRun();
        submitConfirmBillRun();

        test.get().info("Step 15 : Verify One Invoice Generated With Issue Date Of Today");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        verifyOneInvoiceGeneratedWithIssueDateOfToday();
        verifyInvoiceDetail();
    }

    private void checkCreateOcsAccountCommand(){
        boolean isExist = false;
        List asyncCommand =  CommonActions.getAsynccommand(orderId);
        for (int i = 0; i < asyncCommand.size(); i++) {
            if (((HashMap) asyncCommand.get(i)).containsValue("CREATE_OCS_ACCOUNT")) {
                isExist = true;
                break;
            }
        }
        Assert.assertFalse(isExist);
    }

    private void verifyLiveBillEstimateAreCorrect(){
        MenuPage.LeftMenuPage.getInstance().clickLiveBillEstimateItem();
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription billEstimatePerSubscription = new LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(subNo2 + "  Mobile 2");
        billEstimatePerSubscription.expand();

        //subscription 1 should be active and it has payment and device in Adjustments, Charges and Credits
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription.AdjustmentsChargesAndCredits adjustmentsChargesAndCredits =  billEstimatePerSubscription.new AdjustmentsChargesAndCredits(subNo2 + "  Mobile 2");
        adjustmentsChargesAndCredits.expand();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("The balance of the device credit agreement", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT4), Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT4), "£649.00")));
        lists.add(new ArrayList<>(Arrays.asList("The balance of the device credit agreement", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT4), Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT4), "£330.00")));
        Assert.assertEquals(Common.compareLists(adjustmentsChargesAndCredits.getAllValueAdjustmentsOrders(), lists), 2);
    }

    private void verifyOtherChargesCreditsAreCorrect(){
        MenuPage.LeftMenuPage.getInstance().clickOtherChargesCreditsItem();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("Agreement Adjustment Products", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT), "AGR-ETC - The balance of the device credit agreement - £649.00")));
        lists.add(new ArrayList<>(Arrays.asList("Agreement Adjustment Products", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT), "AGR-ETC - The balance of the device credit agreement - £330.00")));
        OtherChargesCreditsContent otherChargesCreditsContent = OtherChargesCreditsContent.getInstance();
        Assert.assertEquals(Common.compareLists(otherChargesCreditsContent.getAllValueOfOtherChargesCredits(), lists), 2);
    }

    private void verifyServiceOrdersAreCreatedCorrectly(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("Deactivate Subscription Task", "Completed Task", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));
        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        Assert.assertEquals(Common.compareLists(serviceOrders.getAllValueOfServiceOrder(), lists), 1);

        serviceOrders.clickServiceOrderByType("Deactivate Subscription Task");
        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getDescription(), "Deactivate Subscription Task");

        Assert.assertEquals(detailsPage.getSubscriptionNumber(), subNo2 + " Mobile 2 (Inactive)");
        Assert.assertEquals(detailsPage.getDeactivationReason(), "C&C Parcel cancelled");
        Assert.assertEquals(detailsPage.getBarringStatusOutbound(), "Provision Completed");
        Assert.assertEquals(detailsPage.getBarringStatusBothWay(), "Provision Completed");

        TasksContentPage.TaskPage.EventsGridSectionPage eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        Assert.assertEquals(eventsGridSectionPage.getRowNumberOfEventGird(),11);

        eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        List<List<String>> eventsLists = new ArrayList<>();
        eventsLists.add(new ArrayList<>(Arrays.asList("PPB: SetSubscriberRatePlan: Request completed", "Completed Task")));
        eventsLists.add(new ArrayList<>(Arrays.asList("PPB: DeleteSubscription: Request completed", "Completed Task")));
        eventsLists.add(new ArrayList<>(Arrays.asList("O2SOA: getAccountSummary: Request completed", "Completed Task")));
        Assert.assertEquals(Common.compareLists(eventsGridSectionPage.getAllValueOfEvents(), eventsLists), 8);
    }

    private void verifyOneInvoiceGeneratedWithIssueDateOfToday(){
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(1, grid.getRowNumberOfInvoiceTable());
        grid.clickInvoiceNumberByIndex(1);
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), grid.getIssued());
    }

    private void verifyInvoiceDetail(){
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().clickViewPDFBtn();
        String fileName = String.format("%s_%s.pdf", RandomCharacter.getRandomNumericString(9), customerNumber);
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().savePDFFile(fileName);

        List<String> listInvoiceContent = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(fileName,1);
        Assert.assertTrue(listInvoiceContent.contains(String.format("User charges for %s  Upgrade Mobile (£10 Tariff 36 Month Contract)", subNo2)));
        Assert.assertTrue(listInvoiceContent.contains(String.format("Monthly subscription %s %s 10.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT4), Parser.parseDateFormate(TimeStamp.getExactDate(TimeStamp.OCTOBER, TimeStamp.NOVEMBER), TimeStamp.DATE_FORMAT4))));
        Assert.assertTrue(listInvoiceContent.contains(String.format("Total charges for %s 10.00", subNo2)));
        Assert.assertTrue(listInvoiceContent.contains("Total user charges 70.00"));
        Assert.assertTrue(listInvoiceContent.contains("Total Adjustments, charges & credits 660.00"));
        //Assert.assertTrue(listInvoiceContent.contains(String.format("%s Online/Telesales -15.00", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT4))));
        Assert.assertTrue(listInvoiceContent.contains("Total Payments -30.00"));
    }

    private void verifyOcsKeyOfSubscription(){
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo1 + " Mobile 1");
        verifyOcsSubscriptionDetails(newStartDate, "HPIN", "", "");

        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo2 + " Mobile 2");
        verifyOcsSubscriptionDetails(newStartDate, "HPIN", "", "");
    }

}
