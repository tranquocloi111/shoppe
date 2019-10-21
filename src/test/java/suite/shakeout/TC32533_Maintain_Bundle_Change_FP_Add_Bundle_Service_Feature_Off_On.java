package suite.shakeout;

import framework.utils.Xml;
//import javafx.util.Pair;
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
import java.util.AbstractMap;
import java.util.List;

public class TC32533_Maintain_Bundle_Change_FP_Add_Bundle_Service_Feature_Off_On extends BaseTest{

    @Test(enabled = false, description = "TC32533 Maintain Bundle Change FP Add Bundle Service Feature Off On", groups = "Smoke")
    public void TC32533_Maintain_Bundle_Change_FP_Add_Bundle_Service_Feature_Off_On() {
        test.get().info("Step 1 : Create Order Having Family Perk Bundle");
        OWSActions owsActions = new OWSActions();
        owsActions.createOrderHavingFamilyPerkBundle();

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 6 : Verify customer start date and billing group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 7 : Verify all discount bundle entries align with bill run calendar entires");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        String serviceRefOf2stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");
        String discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();

        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(11, discountBundles.size());
        verifyFCDiscountBundles(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");
        verifySpecificNCDiscountBundles(discountBundles, "TD250");

        test.get().info("Step 8 : Back to subscription content page");
        MenuPage.BreadCrumbPage.getInstance().clickParentLink();

        test.get().info("Step 9 : Verify subscription 1 service feature is none");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");
        Assert.assertEquals("None", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());

        test.get().info("Step 10 : Verify subscription 2 service feature is none");
        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf2stSubscription + " Mobile Ref 2");
        Assert.assertEquals("None", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());

        test.get().info("Step 11 : Submit maintain bundle request to Selfcare WS");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitMaintainBundleRequest(customerNumber, serviceRefOf1stSubscription);

        test.get().info("Step 12 : Verify normal maintain bundle response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyNormalMaintainBundleResponse(response);

        test.get().info("Step 13 : Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 14 : Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 15 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        selfCareWSTestBase.verifyNormalMaintainBundleResponse(response);

        test.get().info("Step 16 : Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 17 : Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 18 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderChangeBundle());
        String serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        String serviceSubscription = ServiceOrdersContentPage.getInstance().getSubscriptionNumber(serviceOrder);

        test.get().info("Step 19 : Verify a new service order created for customer");
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderProvisionWaitChangeBundle(serviceOrderId, serviceSubscription));
        Assert.assertEquals(1, serviceOrder.size());

        test.get().info("Step 20 : Click service order id to open details");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);

        test.get().info("Step 21 : Verify change bundle so details are correct");
        verifyChangeBundleSoDetails(serviceSubscription);
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("Change Bundle", TasksContentPage.TaskSummarySectionPage.getInstance().getDescription());
        Assert.assertEquals("Provision Wait", TasksContentPage.TaskSummarySectionPage.getInstance().getStatus());

        serviceOrderId = TasksContentPage.TaskSummarySectionPage.getInstance().getSoID();
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventServiceOrderCreated()));

        test.get().info("Step 22 : Update Provision Date Of Change Bundle Service Order");
        CommonActions.updateProvisionDateOfChangeBundleServiceOrder(serviceOrderId);

        test.get().info("Step 23 : Run Provision Services Job");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 24 : Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 25 : Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 26 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderChangeBundle());
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        serviceSubscription = ServiceOrdersContentPage.getInstance().getSubscriptionNumber(serviceOrder);

        test.get().info("Step 25 : Verify a new service order created for customer");
        Assert.assertEquals(1, ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(ServiceOrderEntity.dataServiceOrderCompletedTask(serviceOrderId, serviceSubscription)));
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);

        test.get().info("Step 26 : Verify change bundle so details are correct and 4 events generated after submit provision services job");
        verifyChangeBundleSoDetails(serviceSubscription);
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals(8, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());

        AbstractMap.SimpleEntry<String, String> event = EventEntity.setEvents("Description", "Service Order created");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "PPB: AddSubscription: Request completed");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "PPB: DeleteSubscription: Request completed");
        Assert.assertEquals(2, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "O2SOA: getAccountSummary: Request completed");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "Service Feature 4G Service is added");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));

        test.get().info("Step 27 : Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 28 : Verify subscription 1 service feature is turned on");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");
        Assert.assertEquals("4G Service=ON", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());

        test.get().info("Step 29 : Verify the old FP bundle is removed and new FP bundle is added");
        Assert.assertEquals(4, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(2, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProducts(OtherProductEntiy.dataForOtherProductOldFPBundle(newStartDate)));

        discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(13, discountBundles.size());

        verifySpecificNCDiscountBundles(discountBundles, "TD250");
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", newStartDate, TimeStamp.TodayPlus1MonthMinus1Day(), "TD250", "DELETED"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "TMDAT", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "TMDAT", "ACTIVE"));

        test.get().info("Step 30 : Verify 2nd subscription service feature is none");
        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf2stSubscription + " Mobile Ref 2");
        Assert.assertEquals("None", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());
    }

    private void verifySpecificNCDiscountBundles(List<DiscountBundleEntity> allDiscountBundles, String partitionIdRef){
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), partitionIdRef, "DELETED"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), partitionIdRef, "DELETED"));
    }

    private void verifyChangeBundleSoDetails(String serviceSubscription){
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals(serviceSubscription + " Mobile Ref 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals("Family perk - 500MB per month - 4G", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("Family perk - 250MB per month", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
    }
}
