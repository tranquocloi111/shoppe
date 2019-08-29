package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.EventEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 21/08/2019
 */
public class TC32581_Self_Care_WS_Change_SB_Bundle_immediately extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    private String subscriptionNumber;

    @Test(enabled = true, description = "TC32581 Self Care WS Change SB Bundle immediately", groups = "SelfCareWS")
    public void TC32581_Self_Care_WS_Change_SB_Bundle_immediately(){
        test.get().info("Step 1 : Create an onlines CC Customer with FC 1 bundle of SB and sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC1BundleAndSimOnly();
        customerNumber = owsActions.customerNo;

        test.get().info("Create new billing group start from today minus 15 days");
        createNewBillingGroupToMinus15days();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //=============================================================================

        test.get().info("Login to HUBNet then search Customer by customer number then open Customer Summary");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Verify Customer Start Date and Billing Group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Verify all discount bundle entries align with bill run calendar entires");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscriptionNumber = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        String discountGroupCode = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyDiscountBundleBeforeChangingBundle(discountGroupCode);
        //==============================================================================
        test.get().info("Submit maintain bundle request to Selfcare WS");
        String immediateMaintainBundleRequest = "src\\test\\resources\\xml\\sws\\maintainbundle\\Immediate_Maintain_Bundle_Request.xml";
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitMaintainBundleRequest(immediateMaintainBundleRequest, customerNumber, subscriptionNumber);

        test.get().info("Verify normal maintain bundle response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyImmediateMaintainBundleResponse(response);

        test.get().info("Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderChangeBundle());
        String serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        String serviceSubscription = ServiceOrdersContentPage.getInstance().getSubscriptionNumber(serviceOrder);

        test.get().info("Verify a new service order created for customer");
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderCompletedTask(serviceOrderId, serviceSubscription));
        Assert.assertEquals(1, serviceOrder.size());

        test.get().info("Click service order id to open details");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);

        test.get().info("Verify change bundle details and 6 events are correct");
        verifyChangeBundleDetailsAnd6EventsAreCorrect();

        test.get().info("Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Open details for the Mobile Ref 1 Subscription");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subscriptionNumber + " Mobile Ref 1");

        test.get().info("Verify 40 safety buffer has been added and 20 safety buffer has been removed to the subscription");
        HashMap<String, String> simOnlyProduct = OtherProductEntiy.dataForAnOtherBundleProduct("SIM-ONLY", "SIM-Only", "Standard/Micro SIM", "£0.00", newStartDate);
        HashMap<String, String> oldFC = OtherProductEntiy.dataForAnEndedOtherBundleProduct("FLEXCAP - [00250-SB-A]", "Bundle", "Flexible Cap - £2.5 - [£2.50 safety buffer]", "£0.00", newStartDate);
        HashMap<String, String> newFC = OtherProductEntiy.dataForAnOtherBundleProduct("FLEXCAP - [04000-SB-A]", "Bundle", "Flexible Cap - £40 - [£40 safety buffer]", "£0.00", TimeStamp.Today());

        List<HashMap<String,String>> otherProducts = new ArrayList<>();
        otherProducts.add(simOnlyProduct);
        otherProducts.add(oldFC);
        otherProducts.add(newFC);
        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage otherProductsGridSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance();

        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProduct(otherProducts.get(0)));
        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProduct(otherProducts.get(1)));
        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProduct(otherProducts.get(2)));

        test.get().info("Verify the discount bundle record updated");
        List<DiscountBundleEntity> bundleEntityList = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(10, bundleEntityList.size());

        Assert.assertEquals(1, BillingActions.getInstance()
                .findNewDiscountBundlesByCondition(bundleEntityList,"FC", TimeStamp.Today(), TimeStamp.TodayMinus16DaysAdd1Month(), "FLX29","04000-SB-A","ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance()
                .findNewDiscountBundlesByCondition(bundleEntityList,"FC", TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.TodayMinus16DaysAdd2Months(), "FLX29","04000-SB-A","ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance()
                .findDeletedDiscountBundlesByCondition(bundleEntityList, TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.TodayMinus16DaysAdd2Months(), Integer.parseInt(serviceOrderId), TimeStamp.TodayMinus15DaysAdd1Month(),"FC","FLX01","00250-SB-A"));

    }

    private void verifyDiscountBundleBeforeChangingBundle(String discountGroupCode){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(8, discountBundles.size());
        verifyFCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "FLX01");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TMT5K");
    }
    private void verifyChangeBundleDetailsAnd6EventsAreCorrect(){
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance(), "Yes");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber(), subscriptionNumber + " Mobile Ref 1");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff(), "FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate(), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved(), "£2.50 safety buffer");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded(), "£40 safety buffer");

        Assert.assertEquals(7, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());

        List<HashMap<String, String>> dataForEventChangeBundleImmediate = EventEntity.dataForEventChangeBundleImmediate();

        for(HashMap<String, String> data:dataForEventChangeBundleImmediate){
            Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(data), data.get("Description"));
        }

    }


}
