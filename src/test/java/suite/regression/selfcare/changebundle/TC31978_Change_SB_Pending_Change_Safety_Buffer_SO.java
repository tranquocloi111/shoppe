package suite.regression.selfcare.changebundle;

import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.*;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.LiveBillEstimateContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.care.options.ChangeSafetyBufferPage;
import logic.pages.selfcare.ChangeMySafetyBufferPage;
import logic.pages.selfcare.MonthlyBundlesAddChangeOrRemovePage;
import logic.pages.selfcare.MyPersonalInformationPage;
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

public class TC31978_Change_SB_Pending_Change_Safety_Buffer_SO extends BaseTest {
    String serviceRefOf1stSubscription;
    String serviceOrderID;
    String serviceOrderID2;
    String discountBundleGroupCode;
    String customerNumber;

    @Test(enabled = true, description = "TC31988   change Bundle Permitted pending change safety buffer SO", groups = "SelfCare")
    public void TC31978_Change_SB_Pending_Change_Safety_Buffer_SO() {

        String path = "src\\test\\resources\\xml\\selfcare\\changebundle\\TC31978_createOrder_request";
        test.get().info("Step 1 : Create a customer with 2 NC subscription");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile Ref 1");
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

        test.get().info("Step 7 : Verify customer data is updated ");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 8 :  Change safety buffer from options panels");
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();
        ServiceOrdersPage.SelectSubscription.getInstance().selectAction("Change Safety Buffer");
        ServiceOrdersPage.getInstance().clickNextButton();


        test.get().info("Step 9 : Verify change safety buffer information is correct");
        Assert.assertEquals(serviceRefOf1stSubscription + " Mobile Ref 1", ServiceOrdersPage.ChangeBundle.getInstance().getSubscriptionNumber());
        Assert.assertEquals(String.format("%s (%s days from today)", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF), TimeStamp.TodayMinusTodayMinus1MonthMinus1Day()), ServiceOrdersPage.ChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("£2.50", ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().getlblCurrentOverageCapAmount());

        test.get().info("Step 10: Change current safety buffer to HUL and click the next button");
        changeCurrentSafetyBufferToHULAndClickTheNextButton();

        test.get().info("Step 11: verify confirm change bundle page and click next");
        verifyComfirmChangeBundlePageAndClickNext();

        test.get().info("Step 12: open service order page");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 13 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Provision Wait");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        Assert.assertEquals(1, size);

        test.get().info("Step 14 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");


        test.get().info("Step 15 : Verify SO details data are updated");
        Assert.assertEquals("Provision Wait", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals("£2.50 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("£50 High usage limit;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());

        Assert.assertEquals(3, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        HashMap<String, String> enity = EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);
        enity = EventEntity.dataForEventServiceOrder("SMS Request Completed", "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        test.get().info("Step 16 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 17 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();


        test.get().info("Step 18 : Click add or change safety buffer on my tariff page");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickChangeMySafetyBufferBtn();
        SelfCareTestBase.page().verifyChangeMySafetyBufferPage();

        test.get().info("Step 19 : change safety buffer for customer");
        ChangeMySafetyBufferPage.getInstance().selectSafetyBuffer("£7.50 safety buffer");
        ChangeMySafetyBufferPage.getInstance().selectWhenWouldLikeToChangeMethod("Change it now but only until my next bill date");

        test.get().info("Step 20 : verify the change safety buffer message is correct");

        String message = String.format("Your safety buffer will change now and go back to £2.50 on %s.\r\n"
                        + "While our systems update you might still get texts about your original safety buffer."
                        + " To find out your balance, just call 4488 free from your Tesco Mobile phone or go to our iPhone and Android app.",
                Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT6), ChangeMySafetyBufferPage.getInstance().getComfirmMessage());


        test.get().info("Step 21 : click save changes ");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().clickSaveBtn();

        test.get().info("Step 22 :verify my tariff details page displayed with correct data");
        verifyMyTariffDetailPageDisplayedWithCorrectData();

        test.get().info("Step 23 : Open service orders page in hub net for customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 24: open service order page");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 25 : Verify the new SO is completed and existing SO is still in provision wait");
        temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Completed Task");
        size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        serviceOrderID2 = ServiceOrdersContentPage.getInstance().getServiceOrderidByTypeAndIndex("Change Bundle", 1);
        Assert.assertEquals(1, size);
        Assert.assertEquals("Provision Wait", ServiceOrdersContentPage.getInstance().getStatusByServiceOrderID(serviceOrderID2));

        test.get().info("Step 26 : Verify the provision wait change bundle detail is correct");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderID2);
        Assert.assertEquals("Provision Wait", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals("£2.50 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("£50 High usage limit;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());

        Assert.assertEquals(3, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());

        enity = EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        enity = EventEntity.dataForEventServiceOrder("SMS Request Completed", "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        test.get().info("Step 27 : Verify context info of sms service order is correct in DB");
        verifyContextInfoOfSMSServiceOrderIsCorrectInDB();


        test.get().info("Step 28: open service order page");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 29 : Verify new change safety buffer bundle is completed");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdByIndex(1);
        Assert.assertEquals("Completed Task", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals("£2.50 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("£7.50 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());

        Assert.assertEquals(4, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());

        enity = EventEntity.dataForEventServiceOrder("Refill Amount: £5.00 - Completed", "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);



    }
    private void verifyContextInfoOfSMSServiceOrderIsCorrectInDB() {
        Assert.assertTrue(CommonActions.getSMSIsSent(serviceOrderID2, "") != null);
        String mess = String.format("<SMSGateway><sms:sendSms><smsRequest><type>OFCOM</type><mpn>%s</mpn><message>Tesco Mobile: You have removed your safety buffer. This takes effect from %s.</message>",
                serviceRefOf1stSubscription, Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT6));
        List<String> str = CommonActions.getContextInfoOfSMSServiceOrderIsCorrectInDb(serviceOrderID2, "");
        String result = str.get(2).substring(str.get(2).indexOf("<SMSGateway>"), str.get(2).indexOf("</smsRequest>"));
        Assert.assertEquals(mess, result);
    }

    private void verifyMyTariffDetailPageDisplayedWithCorrectData() {
        List<String> alert = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(1, alert.size());
        Assert.assertEquals("You’ve successfully changed your safety buffer.", alert.get(0));
        Assert.assertEquals(String.format("Your safety buffer has been increased to £7.50 until %s",
                Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getSafetyBuffer());

    }


    private void changeCurrentSafetyBufferToHULAndClickTheNextButton() {

        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().unSelectBundlesByName(BundlesToSelectEntity.getSafetyBuffersAToSelect(), "£2.50 safety buffer");
        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().selectBundlesByName(BundlesToSelectEntity.getSafetyBuffersAToSelect(), "£50 High usage limit(No safety buffer)");
        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().selectBundlesByName("Tick here to consent to EU Data Charges");
        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().selectWhenToApplyChangeText("From next bill date (permanent)");
        ChangeSafetyBufferPage.ChangeSafetyBuffer.getInstance().clickNextButton();

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
        Assert.assertEquals("£7.50 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());

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
        Assert.assertNotEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

        otherProductEnity = OtherProductEntiy.dataForOtherBundleProductNoStartDate("FLEXCAP - [00500-SB-A]", "Bundle");
        Assert.assertNotEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

        otherProductEnity = OtherProductEntiy.dataForOtherBundleProductNoStartDate("FLEXCAP - [00750-SB-A]", "Bundle");
        Assert.assertNotEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

        otherProductEnity = OtherProductEntiy.dataForOtherBundleProduct("BUNDLER - [250MB-DATA-250-FC]", "Bundle", "Discount Bundle Recurring - [Monthly 250MB data allowance - 4G]");
        Assert.assertNotEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

    }

    private void verifyNewBundleWasDisplayedInLiveBillEstimation() {
        MenuPage.LeftMenuPage.getInstance().clickLiveBillEstimateItem();
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription billEstimatePerSubscription = new
                LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(serviceRefOf1stSubscription + "  Mobile Ref 1");
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

    private void verifyComfirmChangeBundlePageAndClickNext() {
        Assert.assertEquals("£2.50 safety buffer: £2.50 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getSafetyBufferBefore());
        Assert.assertEquals("£50 High usage limit: £50.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getSafetyBufferAfter());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF), ServiceOrdersPage.ConfirmChangeBundle.getInstance().getEffective());
        ServiceOrdersPage.getInstance().clickNextButton();

        Assert.assertEquals(String.format("*** Service Order has been set to Status of Provision Wait, and is due to be processed on %s ***", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)), ServiceOrdersPage.ServiceOrderComplete.getInstance().getMessage());
        ServiceOrdersPage.ServiceOrderComplete.getInstance().clickReturnToCustomer();

    }

}
