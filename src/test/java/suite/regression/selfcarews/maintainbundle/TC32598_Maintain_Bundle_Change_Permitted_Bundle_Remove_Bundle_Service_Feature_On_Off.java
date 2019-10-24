package suite.regression.selfcarews.maintainbundle;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.EventEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 22/08/2019
 */
public class TC32598_Maintain_Bundle_Change_Permitted_Bundle_Remove_Bundle_Service_Feature_On_Off extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    private String subscriptionNumber;
    String serviceOrderId;
    String discountGroupCode;

    @Test(enabled = true, description = "TC32598 Maintain Bundle Change Permitted Bundle Remove Bundle Service Feature On Off", groups = "SelfCareWS.MaintainBundle")
    public void TC32598_Maintain_Bundle_Change_Permitted_Bundle_Remove_Bundle_Service_Feature_On_Off(){
        test.get().info("Create Customer Order with FC 3 Bundles and NK2720");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC3BundleAndSimOnly();
        customerNumber = owsActions.customerNo;

        test.get().info("Create new billing group start from today");
        createNewBillingGroup();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //=============================================================================

        test.get().info("Login to HUBNet then search Customer by customer number then open Customer Summary");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Refresh Customer data in HUB Net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Verify Customer Start Date and Billing Group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Verify all discount bundle entries align with bill run calendar entires");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscriptionNumber = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        discountGroupCode = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyDiscountBundleBeforeChangingBundle(newStartDate, discountGroupCode);

        test.get().info("Back to subscription content page");
        MenuPage.BreadCrumbPage.getInstance().clickParentLink();

        test.get().info("Verify subscription 1 service is turned on");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subscriptionNumber + " Mobile Ref 1");
        Assert.assertEquals("4G Service=ON", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());

        //==============================================================================
        test.get().info("Submit maintain bundle request only Customer Number and Subscription Number");
        SWSActions swsActions = new SWSActions();
        String maintain_bundle_request = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC32598_maintain_remove_nextbilldate_request.xml";
        Xml response = swsActions.submitMaintainBundleRequest(maintain_bundle_request, customerNumber, subscriptionNumber);

        test.get().info("Verify normal maintain bundle response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyNormalMaintainBundleResponse(response);

        //==============================================================================
        test.get().info("Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderChangeBundle());
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        String serviceSubscription = ServiceOrdersContentPage.getInstance().getSubscriptionNumber(serviceOrder);

        test.get().info("Verify a new Provision Wait service order is created for customer");
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderProvisionWaitChangeBundle(serviceOrderId, serviceSubscription));
        Assert.assertEquals(1, serviceOrder.size());

        test.get().info("Click service order to open details");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);

        test.get().info("Verify change bundle so details are correct");
        verifyChangeBundleSODetailAreCorrect();
        //=============================================================================
        test.get().info("Update provision date of change bundle service order");
        BillingActions.getInstance().updateProvisionDateOfChangeBundleServiceOrder(serviceOrderId);

        test.get().info("Run Provision Service Job");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        //=============================================================================
        test.get().info("Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Refresh current customer dat in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        //=============================================================================
        test.get().info("Verify a new Completed Task service order is created for customer");
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderCompletedTask(serviceOrderId, serviceSubscription));
        Assert.assertEquals(1, serviceOrder.size());

        test.get().info("Click service order id to open details");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);

        test.get().info("Verify change bundle so details are correct and 3 events generated after submit provision services job");
        verifyChangeBundleDetailsAreCorrectAndEventsGenerated();

        //=============================================================================
        test.get().info("Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Open subscriptions content for customer");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Verify subscription 1 service feature is none");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subscriptionNumber + " Mobile Ref 1");
        Assert.assertEquals("None", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());

        test.get().info("Verify the permitted bundle is removed");
        verifyThePermittedBundleIsRemoved();

    }

    private void verifyDiscountBundleBeforeChangingBundle(Date newStartDate ,String  discountGroupCode){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);

        Assert.assertEquals(14, discountBundles.size());
        verifyFCDiscountBundles(discountBundles, newStartDate, "FLX11");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");
        verifyNCDataDiscountBundles(discountBundles, "TMDAT", "250MB-DATA-250-FC", "ACTIVE");
        verifyNCDataDiscountBundles(discountBundles, "TMDAT", "500MB-DATA-500-FC", "ACTIVE");
    }

    private void verifyNCDataDiscountBundles(List<DiscountBundleEntity> discountBundles, String partitionIdRef, String bundleCode, String status)
    {
        BillingActions.getInstance().findDiscountBundlesByCondition(discountBundles, "NC", newStartDate, TimeStamp.TodayPlus1MonthMinus1Day(), partitionIdRef, bundleCode, status);
        BillingActions.getInstance().findDiscountBundlesByCondition(discountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), partitionIdRef, bundleCode, status);
        BillingActions.getInstance().findDiscountBundlesByCondition(discountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), partitionIdRef, bundleCode, status);

    }

    private void verifyChangeBundleSODetailAreCorrect(){
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance(), "Yes");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber(), subscriptionNumber + " Mobile Ref 1");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff(), "FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate(), Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals("",TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved(), "Monthly 250MB data allowance - 4G");
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getDescription(), "Change Bundle");
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(),"Provision Wait");
        serviceOrderId =  TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getSoID();

        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(
                EventEntity.dataForEventServiceOrderCreated()
        ));

    }

    private void verifyChangeBundleDetailsAreCorrectAndEventsGenerated(){
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance(), "Yes");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber(), subscriptionNumber + " Mobile Ref 1");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff(), "FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate(), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved(), "Monthly 250MB data allowance - 4G");

        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(
                EventEntity.dataForEventServiceOrderCreated()
        ));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(
                EventEntity.dataForEventServiceOrder("Successful Add", "Completed Task")));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(
                EventEntity.dataForEventServiceOrder("O2SOA: getAccountSummary: Request completed", "Completed Task")));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(
                EventEntity.dataForEventServiceOrder("Service Feature 4G Service is removed", "Completed Task")
        ));
    }

    private void verifyThePermittedBundleIsRemoved(){
        Assert.assertEquals(4, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());

        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(
                OtherProductEntiy.dataForAnEndedOtherBundleProduct("BUNDLER - [250MB-DATA-250-FC]",
                        "Bundle",
                        "Discount Bundle Recurring - [Monthly 250MB data allowance - 4G]",
                        "£5.00", newStartDate)));

        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        verifyNCDataDiscountBundles(discountBundles, "TMDAT", "250MB-DATA-250-FC", "DELETED");
        verifyNCDataDiscountBundles(discountBundles, "TMDAT", "250MB-DATA-250-FC", "ACTIVE");
    }
}
