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
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 29/08/2019
 */
public class TC35530_Self_Care_WS_Add_Family_Perk_Bundle_to_FC_tariff extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    private List<String> subscriptionNumberList;
    private String subscriptionNumber1;
    private String subscriptionNumber2;

    @Test(enabled = true, description = "TC35530 Self Care WS Add Family Perk Bundle to FC tariff", groups = "SelfCareWS")
    public void TC35530_Self_Care_WS_Add_Family_Perk_Bundle_to_FC_tariff() {
        test.get().info("Create an onlines CC Customer with 2 FC 1 Bundle and SIM-Only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWith2FC1BundleAndSimOnly();
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

        test.get().info("Refresh Customer data in HUB Net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Verify Customer Start Date and Billing Group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Verify all discount bundle entries align with bill run calendar entires");
        verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires();
        //=============================================================================
        test.get().info("Submit maintain bundle request only Customer Number and Subscription Number");
        getAllSubscriptions();
        SWSActions swsActions = new SWSActions();
        String maintain_bundle_request = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC35530_maintain_add_familiperk_bundle_immediate_request.xml";
        ;
        Xml response = swsActions.submitMaintainBundleRequest(maintain_bundle_request, customerNumber, subscriptionNumber1);

        test.get().info("Verify normal maintain bundle response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyImmediateMaintainBundleResponse(response);

        test.get().info("Verify 1 Completed Change bundle SO recorded generated");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderCompletedTaskChangeBundle(subscriptionNumber1));
        Assert.assertEquals(1, serviceOrder.size());

        test.get().info("Open details for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Verify change bundle SO details are correct");
        verifyChangeBundleSODetailsAreCorrect();

        test.get().info("Verify Change bundle SO have 2 events generated");
        verifyChangeBundleSOHave2EventsGenerated();
        //=============================================================================
        test.get().info("Find Customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Open details for FC Mobile 1 subscription");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subscriptionNumber1 + " FC Mobile 1");

        test.get().info("Verify 1 new bundle added to tariff in other products grid");
        verifyOneNewBundleAddedToTariffInOtherProductsGrid();
    }

    private void verifyOneNewBundleAddedToTariffInOtherProductsGrid() {
        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(
                OtherProductEntiy.dataBundleForOtherProduct("BUNDLER - [150-FMIN-0-FC]",
                        "Bundle",
                        "Discount Bundle Recurring - [Family perk - 150 Mins per month]",
                        "£0.00")));
    }

    private void verifyChangeBundleSOHave2EventsGenerated() {
        Assert.assertEquals(2, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventServiceOrderCompleted()));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventServiceOrderCreated()));
    }

    private void verifyChangeBundleSODetailsAreCorrect() {
        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(detailsPage.getSubscriptionNumber(), subscriptionNumber1 + " FC Mobile 1");
        Assert.assertEquals(detailsPage.getTariff(), "FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}");
        Assert.assertEquals(detailsPage.getProvisioningDate(), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(detailsPage.getNotificationOfLowBalance(), "Yes");
        Assert.assertEquals("", detailsPage.getBundlesRemoved());
        Assert.assertEquals(detailsPage.getBundlesAdded(), "Family perk - 150 Mins per month");
    }

    private void getAllSubscriptions() {
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        subscriptionNumberList = CareTestBase.getAllSubscription();
        for (String subscription : subscriptionNumberList) {
            if (subscription.contains("FC Mobile 1")) {
                subscriptionNumber1 = subscription.split(" ")[0];
            } else {
                subscriptionNumber2 = subscription.split(" ")[0];
            }
        }
    }

    private void verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires() {
        subscriptionNumberList = CareTestBase.getAllSubscription();
        for (String subscription : subscriptionNumberList) {
            MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
            MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
            CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subscription);
            String discountGroupCode = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
            verifyDiscountBundleBeforeChangingBundle(discountGroupCode);
        }
    }

    private void verifyDiscountBundleBeforeChangingBundle(String discountGroupCode) {
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);

        Assert.assertEquals(8, discountBundles.size());
        verifyFCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TMT5K");

    }
}
