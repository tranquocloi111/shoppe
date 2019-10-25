package suite.regression.selfcare.changebundle;

import framework.utils.RandomCharacter;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.*;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.care.options.ChangeSafetyBufferPage;
import logic.pages.selfcare.MonthlyBundlesAddChangeOrRemovePage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TC31938_B07_Change_Permitted_Bundle_Pending_Change_Safety_Buffer_SO extends BaseTest {
    String serviceRefOf1stSubscription;
    String serviceOrderID;
    String serviceOrderID2;
    String discountBundleGroupCode;
    String customerNumber;

    @Test(enabled = true, description = "TC31938 B07  change Bundle Permitted pending change safety buffer SO", groups = "SelfCare")
    public void TC31938_B07_Change_Permitted_Bundle_Pending_Change_Safety_Buffer_SO() {

        String path = "src\\test\\resources\\xml\\selfcare\\changebundle\\TC31938_B07_createOrder";
        test.get().info("Step 1 : Create a customer with 2 NC subscription");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo, "FC MPN");
        serviceRefOf1stSubscription = owsActions.serviceRef;

        test.get().info("Step 2 : Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3: Update the payment collection date is 10");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5: Update the start date of customer");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);


        test.get().info("Step 6: load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 7: Change a SB for the subscription");
        changeASBForTheSubscription();

        test.get().info("Step 8: verify change SB SO and detail");
        verifyChangeSBSOAndDetail();

        test.get().info("Step 9 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 10 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 11 : verify SB info SO and the details");
        Assert.assertEquals(String.format("£7.50 safety buffer    PENDING activation  as of  %s",
                Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC MPN").getSafetyBuffer());
        Assert.assertEquals(String.format("£5 safety buffer    PENDING removal  as of  %s",
                Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC MPN").getSecondSafetyBuffer().trim());

        test.get().info("Step 12 : Click add or change bundles on my tariff page");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC MPN").clickAddOrChangeABundleButton();
        SelfCareTestBase.page().verifyMonthlyBundlesAddChangeOrRemovePageDisplayed();

        test.get().info("Step 13 : change bundle for customer");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().unSelectBundlesByName("4G data - 1GB");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().selectBundlesByName("Monthly 250MB data allowance - 4G");

        test.get().info("Step 14 : click save changes ");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().clickSaveBtn();

        test.get().info("Step 15 :verify my tariff details page displayed with correct data");
        verifyMyTariffDetailPageDisplayedWithCorrectData();

        test.get().info("Step 16 : Open service orders page in hub net for customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 17 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Provision Wait");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        Assert.assertEquals(2, size);

        test.get().info("Step 18 : Open details screen for change bundle SO");
        serviceOrderID = ServiceOrdersContentPage.getInstance().getServiceOrderidByTypeAndIndex("Change Bundle", 1);
        serviceOrderID2 = ServiceOrdersContentPage.getInstance().getServiceOrderidByTypeAndIndex("Change Bundle", 0);
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderID2);


        test.get().info("Step 19 : Verify SO details data are updated");
        Assert.assertEquals(String.format("*** Service Order has been set to Status of Provision Wait, and is due to be processed on %s ***", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)), TasksContentPage.TaskPage.DetailsPage.getInstance().getEndOfWizardMessage());
        Assert.assertEquals("4G data - 1GB;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("Monthly 250MB data allowance - 4G;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        HashMap<String, String> enity = EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        Assert.assertTrue(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDateTimeByIndex(2).startsWith(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT)));

        test.get().info("Step 20: update the PDate and BillDate for provision wait SO");
        BillingActions.getInstance().updateThePDateAndBillDateForChangeBundle(serviceOrderID);
        BillingActions.getInstance().updateThePDateAndBillDateForChangeBundle(serviceOrderID2);

        test.get().info("Step 21: submit the do provision services batch job");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 22: verify the old bundle was removed and new bundle is active");
        verifyTheOldBundleWasRemovedAndNewBundleIsActive();

        test.get().info("Step 23: verify new bundle was displayed in live bill estimation");
        verifyNewBundleWasDisplayedInLiveBillEstimation();

    }


    private void verifyMyTariffDetailPageDisplayedWithCorrectData() {
        String nextBillRun = Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF);
        List<String> alert = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(2, alert.size());
        Assert.assertEquals("Thanks, the bundle changes you’ve made have been successful.", alert.get(0));
        Assert.assertEquals(String.format("Your changes will take effect from " + nextBillRun), alert.get(1));

    }


    private void changeASBForTheSubscription() {
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();
        ServiceOrdersPage.SelectSubscription.getInstance().selectAction("Change Safety Buffer");
        ServiceOrdersPage.getInstance().clickNextButton();
        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().unSelectBundlesByName(BundlesToSelectEntity.getSafetyBuffersAToSelect(), "£5 safety buffer");
        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().selectBundlesByName(BundlesToSelectEntity.getSafetyBuffersAToSelect(), "£7.50 safety buffer");
        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().selectWhenToApplyChangeText("From next bill date (permanent)");
        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().clickNextButton();
        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().clickNextButton();
        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().clickReturnToCustomer();
    }

    private void verifyChangeSBSOAndDetail() {
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 18 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Provision Wait");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        Assert.assertEquals(1, size);

        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 20 : Verify SO details data are updated");
        Assert.assertEquals(String.format("*** Service Order has been set to Status of Provision Wait, and is due to be processed on %s ***", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)), TasksContentPage.TaskPage.DetailsPage.getInstance().getEndOfWizardMessage());
        Assert.assertEquals("£5 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("£7.50 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

    }

    private void verifyTheOldBundleWasRemovedAndNewBundleIsActive() {

        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();


        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        Assert.assertEquals("Completed Task", ServiceOrdersContentPage.getInstance().getStatusByServiceOrderID(serviceOrderID));
        Assert.assertEquals("Completed Task", ServiceOrdersContentPage.getInstance().getStatusByServiceOrderID(serviceOrderID2));

        ServiceOrdersContentPage.getInstance().clickServiceOrderByType(serviceOrderID2);


        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();


        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);

        HashMap<String, String> otherProductEnity = OtherProductEntiy.dataForOtherBundleProduct("BUNDLER - [1GB-4GDATA-0750-FC]", "Bundle", TimeStamp.TodayMinus1Day());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

        otherProductEnity = OtherProductEntiy.dataForOtherBundleProduct("FLEXCAP - [00500-SB-A]", "Bundle", TimeStamp.TodayMinus1Day());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

        otherProductEnity = OtherProductEntiy.dataForOtherBundleProductNoStartDate("FLEXCAP - [00750-SB-A]", "Bundle");
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

        otherProductEnity = OtherProductEntiy.dataForOtherBundleProduct("BUNDLER - [250MB-DATA-250-FC]", "Bundle", "Discount Bundle Recurring - [Monthly 250MB data allowance - 4G]");
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

    }

    private void verifyNewBundleWasDisplayedInLiveBillEstimation() {
        MenuPage.LeftMenuPage.getInstance().clickLiveBillEstimateItem();
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription billEstimatePerSubscription = new
                LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(serviceRefOf1stSubscription + "  FC MPN");
        billEstimatePerSubscription.expand();
        billEstimatePerSubscription.clickBundleChargesExpandBtn();
        HashMap<String, String> enity = BundleChargesEnity.bundleChargesEnity(TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "Monthly 250MB data allowance - 4G for  " + serviceRefOf1stSubscription, "£5.00");
        Assert.assertEquals(LiveBillEstimateContentPage.BundleCharges.getInstance().getRowInBundleCharge(enity), 1);
        enity = BundleChargesEnity.bundleChargesEnity(TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "Monthly 250MB data allowance - 4G for  " + serviceRefOf1stSubscription, "£5.00");
        Assert.assertEquals(LiveBillEstimateContentPage.BundleCharges.getInstance().getRowInBundleCharge(enity), 1);

        enity = BundleChargesEnity.bundleChargesEnity(TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "£7.50 safety buffer for   " + serviceRefOf1stSubscription, "£0.00");
        Assert.assertEquals(LiveBillEstimateContentPage.BundleCharges.getInstance().getRowInBundleCharge(enity), 1);
        enity = BundleChargesEnity.bundleChargesEnity(TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "£7.50 safety buffer for   " + serviceRefOf1stSubscription, "£0.00");
        Assert.assertEquals(LiveBillEstimateContentPage.BundleCharges.getInstance().getRowInBundleCharge(enity), 1);

    }

}
