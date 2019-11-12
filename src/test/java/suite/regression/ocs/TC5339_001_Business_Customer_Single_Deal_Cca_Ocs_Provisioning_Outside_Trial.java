package suite.regression.ocs;

import framework.utils.RandomCharacter;
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
import java.util.HashMap;
import java.util.List;


public class TC5339_001_Business_Customer_Single_Deal_Cca_Ocs_Provisioning_Outside_Trial extends BaseTest {
    private String customerNumber = "47763560";
    private String orderId = "8702002";
    private String subNo1 = "07647064770";
    private OWSActions owsActions;
    private Date newStartDate = TimeStamp.TodayMinus1MonthMinus20Day();;
    private String discountGroupCode;

    @Test(enabled = true, description = "TC5339_001_Business_Customer_Single_Deal_Cca_Ocs_Provisioning_Outside_Trial", groups = "OCS")
    public void TC5339_001_Business_Customer_Single_Deal_Cca_Ocs_Provisioning_Outside_Trial() {
        test.get().info("Step 1 : Business customer, single deal, CCA, OCS provisioning, outside trial");
        CommonActions.updateHubProvisionSystem("O");
        owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC5339_Single_Deal_OCS_CCA_Business.xml";
        owsActions.createOcsCustomerRequestAcceptUrl(path,2, "OCS");

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

        test.get().info("Step 6 : Verify Create Ocs Account async task is displayed");
        CareTestBase.page().checkCreateOcsAccountCommand(orderId, true);

        test.get().info("Step 7 : Login to Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");

        test.get().info("Step 8 : Validate the Subscription details screen in HUB .NET");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage generalSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        discountGroupCode = generalSectionPage.getDiscountGroupCode();
        verifyOcsSubscriptionDetails("OCS", discountGroupCode + "S", discountGroupCode + "A" , newStartDate);

        test.get().info("Step 9 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateAccountWithOutReturnRefund(TimeStamp.Today());

        test.get().info("Step 10 : Verify the subscription status is Inactive");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo1));

        test.get().info("Step 11 : Verify Agreement Adjustment Products charge with a price equal to the Agreement ETC Override Amount is created.");
        verifyOtherChargesCreditsAreCorrect();

        test.get().info("Step 12 : Select Live Bill Estimate. Verify subscription charges.");
        verifyLiveBillEstimateAreCorrect();

        test.get().info("Step 13 : Navigate to Service Orders screen. Validate the Deactivate Subscription Service Order.");
        verifyServiceOrdersAreCreatedCorrectly();
    }

    private void verifyLiveBillEstimateAreCorrect(){
        MenuPage.LeftMenuPage.getInstance().clickLiveBillEstimateItem();
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription billEstimatePerSubscription = new LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(subNo1 + "  Mobile 1");
        billEstimatePerSubscription.expand();

        //subscription 1 should be active and it has payment and device in Adjustments, Charges and Credits
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription.AdjustmentsChargesAndCredits adjustmentsChargesAndCredits =  billEstimatePerSubscription.new AdjustmentsChargesAndCredits(subNo1 + "  Mobile 1");
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

        Assert.assertEquals(detailsPage.getSubscriptionNumber(), subNo1 + " Mobile 1 (Inactive)");
        Assert.assertEquals(detailsPage.getDeactivationReason(), "C&C Parcel cancelled");
        Assert.assertEquals(detailsPage.getBarringStatusOutbound(), "Provision Requested");
        Assert.assertEquals(detailsPage.getBarringStatusBothWay(), "Provision Requested");

        TasksContentPage.TaskPage.EventsGridSectionPage eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        List<List<String>> eventsLists = new ArrayList<>();
        eventsLists.add(new ArrayList<>(Arrays.asList("Task created", "Open Service Order")));
        eventsLists.add(new ArrayList<>(Arrays.asList("OCS ChangeSubLifeCycle:Request completed", "Completed Task")));
        eventsLists.add(new ArrayList<>(Arrays.asList("Task Completed", "Completed Task")));
        Assert.assertEquals(Common.compareLists(eventsGridSectionPage.getAllValueOfEvents(), eventsLists), 3);
    }
}
