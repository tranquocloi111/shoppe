package suite.regression.ocs;

import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
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
import java.util.List;


public class TC5339_002_Consumer_Customer_Multi_Deals_Cca_Ocs_Provionsing_Inside_Trial extends BaseTest {
    private String customerNumber = "47764477";
    private String orderId = "8702042";
    private String subNo1 = "07647064770";
    private String subNo2 = "07647064770";
    private OWSActions owsActions;
    private Date newStartDate = TimeStamp.TodayMinus20Days();
    private String discountGroupCode;

    @Test(enabled = true, description = "TC5339_002_Consumer_Customer_Multi_Deals_Cca_Ocs_Provionsing_Inside_Trial", groups = "OCS")
    public void TC5339_002_Consumer_Customer_Multi_Deals_Cca_Ocs_Provionsing_Inside_Trial() {
        test.get().info("Step 1 : Consumer customer, multi deals, CCA, OCS provionsing, inside trial");
        CommonActions.updateHubProvisionSystem("O");
        owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC5339_Multi_Deal_OCS_CCA_Consumer.xml";
        owsActions.createOcsCustomerRequestAcceptUrl(path,4, "OCS");

        test.get().info("Step 2 : Create new billing group");
        createNewBillingGroup();

        test.get().info("Step 3 : Update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        orderId = owsActions.orderIdNo;
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update start date for customer");
        newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Verify Create Ocs Account async task is not displayed");
        CareTestBase.page().checkCreateOcsAccountCommand(orderId, true);

        test.get().info("Step 7 : Login to Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");
        subNo2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 2");

        test.get().info("Step 8 : Validate the Subscription details screen in HUB .NET");
        verifyOcsKeyOfSubscription();

        test.get().info("Step 9 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateSubscription(false,true);

        test.get().info("Step 10 : Verify the subscription status is Inactive");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo2));

        test.get().info("Step 11 : Verify Agreement Adjustment Products charge with a price equal to the Agreement ETC Override Amount is created.");
        verifyOtherChargesCreditsAreCorrect(1);

        test.get().info("Step 12 : Select Live Bill Estimate. Verify subscription charges.");
        verifyLiveBillEstimateAreCorrect(subNo2, "Mobile 2");

        test.get().info("Step 13 : Navigate to Service Orders screen. Validate the Deactivate Subscription Service Order.");
        verifyServiceOrdersAreCreatedCorrectly(subNo2, "Mobile 2", 1);

        test.get().info("Step 14 : Deactivate subscription");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateSubscription(true, true);

        test.get().info("Step 15 : Verify the subscription status is Inactive");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo1));

        test.get().info("Step 16 : Verify Agreement Adjustment Products charge with a price equal to the Agreement ETC Override Amount is created.");
        verifyOtherChargesCreditsAreCorrect(2);

        test.get().info("Step 17 : Select Live Bill Estimate. Verify subscription charges.");
        verifyLiveBillEstimateAreCorrect(subNo1, "Mobile 1");

        test.get().info("Step 18 : Navigate to Service Orders screen. Validate the Deactivate Subscription Service Order.");
        verifyServiceOrdersAreCreatedCorrectly(subNo1, "Mobile 1" , 2);
    }

    private void verifyLiveBillEstimateAreCorrect(String subNo, String name){
        MenuPage.LeftMenuPage.getInstance().clickLiveBillEstimateItem();
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription billEstimatePerSubscription = new LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(subNo + "  " + name);
        billEstimatePerSubscription.expand();

        //subscription 1 should be active and it has payment and device in Adjustments, Charges and Credits
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription.AdjustmentsChargesAndCredits adjustmentsChargesAndCredits =  billEstimatePerSubscription.new AdjustmentsChargesAndCredits(subNo + "  " + name);
        adjustmentsChargesAndCredits.expand();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("The balance of the device credit agreement", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT4), Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT4), "£649.00")));
        lists.add(new ArrayList<>(Arrays.asList("The balance of the device credit agreement", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT4), Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT4), "£330.00")));
        Assert.assertEquals(Common.compareLists(adjustmentsChargesAndCredits.getAllValueAdjustmentsOrders(), lists), 0);
    }

    private void verifyOtherChargesCreditsAreCorrect(int quantity){
        MenuPage.LeftMenuPage.getInstance().clickOtherChargesCreditsItem();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("Miscellaneous Products", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT), "IMMEDIATE-REFUND - Customer Care refund issued - £304.50")));
        OtherChargesCreditsContent otherChargesCreditsContent = OtherChargesCreditsContent.getInstance();
        Assert.assertEquals(Common.compareLists(otherChargesCreditsContent.getAllValueOfOtherChargesCredits(), lists), quantity);
    }

    private void verifyServiceOrdersAreCreatedCorrectly(String subNo, String name, int quantity){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("Deactivate Subscription Task", "Completed Task", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));
        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        Assert.assertEquals(Common.compareLists(serviceOrders.getAllValueOfServiceOrder(), lists), quantity);

        serviceOrders.clickServiceOrderByType("Deactivate Subscription Task");
        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getDescription(), "Deactivate Subscription Task");

        Assert.assertEquals(detailsPage.getSubscriptionNumber(), subNo + String.format(" %s (Inactive)", name));
        Assert.assertEquals(detailsPage.getDeactivationReason(), "C&C Parcel cancelled");
        Assert.assertEquals(detailsPage.getBarringStatusOutbound(), "Provision Requested");
        Assert.assertEquals(detailsPage.getBarringStatusBothWay(), "Provision Requested");

        TasksContentPage.TaskPage.EventsGridSectionPage eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        List<List<String>> eventsLists = new ArrayList<>();
        eventsLists.add(new ArrayList<>(Arrays.asList("Task created", "Open Service Order")));
        eventsLists.add(new ArrayList<>(Arrays.asList("OCS ChangeSubLifeCycle:Request completed", "Completed Task")));
        eventsLists.add(new ArrayList<>(Arrays.asList("Task Completed", "Completed Task")));
        eventsLists.add(new ArrayList<>(Arrays.asList("Task waiting returns", "Pending Return")));
        eventsLists.add(new ArrayList<>(Arrays.asList("Agreement AGRTESCOMOBILE cancelled", "Pending Return")));
        Assert.assertEquals(Common.compareLists(eventsGridSectionPage.getAllValueOfEvents(), eventsLists), 6);
    }

    private void verifyOcsKeyOfSubscription(){
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo1 + " Mobile 1");
        SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage generalSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        discountGroupCode = generalSectionPage.getDiscountGroupCode();
        verifyOcsSubscriptionDetails("OCS", discountGroupCode + "S", discountGroupCode + "A", newStartDate);

        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo2 + " Mobile 2");
        discountGroupCode = generalSectionPage.getDiscountGroupCode();
        verifyOcsSubscriptionDetails("OCS", discountGroupCode + "S", discountGroupCode + "A", newStartDate);
    }
}
