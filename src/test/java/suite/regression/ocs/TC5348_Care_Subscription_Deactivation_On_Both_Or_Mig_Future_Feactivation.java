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


public class TC5348_Care_Subscription_Deactivation_On_Both_Or_Mig_Future_Feactivation extends BaseTest {
    private String customerNumber = "47765718";
    private String orderId = "8701636";
    private String subNo1 = "07647064770";
    private String subNo2 = "07647064770";
    private OWSActions owsActions;
    private Date newStartDate = TimeStamp.TodayMinus1MonthMinus20Day();;
    private String serviceId;

    @Test(enabled = true, description = "TC5348_Care_Subscription_Deactivation_On_Both_Or_Mig_Future_Feactivation", groups = "OCS")
    public void TC5348_Care_Subscription_Deactivation_On_Both_Or_Mig_Future_Feactivation() {
        test.get().info("Step 1 : Create a Customer account has subscription provisioned in BOTH_OR_MIG");
        CommonActions.updateHubProvisionSystem("B");
        owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC5348_Single_Deal_Device_Peripherals.xml";
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
        verifyOcsKeyOfSubscription();

        test.get().info("Step 8 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateAccountWithOutReturnRefund(TimeStamp.TodayPlus1Day());

        test.get().info("Step 9 : Verify the subscription status is Inactive");
        Assert.assertEquals("Active", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusByIndex(1));

        test.get().info("Step 10 : Navigate to Service Orders screen. Validate the Deactivate Subscription Service Order.");
        verifyServiceOrdersAreCreatedCorrectly();

        test.get().info("Step 11 : Run Provission Wait job");
        updateThePDateAndBillDateForSO(serviceId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 11 : Reload Care screen and verify Deactivate subscription");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo1));

        test.get().info("Step 10 : Validate the Deactivate Subscription Service Order after deactivating");
        verifyDeactiveServiceOrdersAreCreatedCorrectly();

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




    private void verifyServiceOrdersAreCreatedCorrectly(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("Deactivate Account", "Provision Wait", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));
        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        Assert.assertEquals(Common.compareLists(serviceOrders.getAllValueOfServiceOrder(), lists), 1);

        serviceId = serviceOrders.getServiceOrderidByType("Deactivate Account");
        serviceOrders.clickServiceOrderByType("Deactivate Account");
        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Provision Wait");
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getDescription(), "Deactivate Account");
        Assert.assertEquals(detailsPage.getDeactivationReason(), "C&C Parcel cancelled");
    }

    private void verifyOcsKeyOfSubscription(){
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo1 + " Mobile 1");
        verifyOcsSubscriptionDetails( "HPIN", "", "", newStartDate);
    }

    private void verifyDeactiveServiceOrdersAreCreatedCorrectly(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("Deactivate Subscription Task", "Completed Task", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));
        lists.add(new ArrayList<>(Arrays.asList("Deactivate Account", "Completed Task", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));
        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        Assert.assertEquals(Common.compareLists(serviceOrders.getAllValueOfServiceOrder(), lists), 2);

        serviceOrders.clickServiceOrderByType("Deactivate Subscription Task");
        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getDescription(), "Deactivate Subscription Task");

        Assert.assertEquals(detailsPage.getSubscriptionNumber(), subNo1 + " Mobile 1 (Inactive)");
        Assert.assertEquals(detailsPage.getDeactivationReason(), "C&C Parcel cancelled");
        Assert.assertEquals(detailsPage.getBarringStatusOutbound(), "Provision Completed");
        Assert.assertEquals(detailsPage.getBarringStatusBothWay(), "Provision Completed");

        TasksContentPage.TaskPage.EventsGridSectionPage eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        Assert.assertEquals(eventsGridSectionPage.getRowNumberOfEventGird(),10);

        eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        List<List<String>> eventsLists = new ArrayList<>();
        eventsLists.add(new ArrayList<>(Arrays.asList("PPB: SetSubscriberRatePlan: Request completed", "Completed Task")));
        eventsLists.add(new ArrayList<>(Arrays.asList("PPB: DeleteSubscription: Request completed", "Completed Task")));
        eventsLists.add(new ArrayList<>(Arrays.asList("O2SOA: getAccountSummary: Request completed", "Completed Task")));
        Assert.assertEquals(Common.compareLists(eventsGridSectionPage.getAllValueOfEvents(), eventsLists), 8);
    }
}
