package suite.regression.selfcare.changebundle;

import logic.business.db.billing.CommonActions;
import logic.business.entities.*;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.LiveBillEstimateContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.care.options.ChangeSafetyBufferPage;
import logic.pages.care.options.TariffSearchPage;
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

public class TC31979_Change_SB_Pending_Change_Tariff_SO extends BaseTest {
    String serviceRefOf1stSubscription;
    String serviceOrderID;
    String serviceOrderID2;
    String discountBundleGroupCode;
    String customerNumber;

    @Test(enabled = true, description = "TC31988   change Bundle Permitted pending change safety buffer SO", groups = "SelfCare")
    public void TC31979_Change_SB_Pending_Change_Tariff_SO() {

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
        MenuPage.RightMenuPage.getInstance().clickChangeTariffLink();
        test.get().info("Step 8: Select subscription number from drop down");
        ServiceOrdersPage.ChangeBundle.getInstance().clickNextButton();

        test.get().info("Step 5: open the search tariff window");
        String title = ServiceOrdersContentPage.getInstance().getTitle();
        ServiceOrdersContentPage.getInstance().clicknewTariffSearchBtn();

        test.get().info("Step 7: Select Tariff by code then click next button");
        TariffSearchPage.getInstance().selectTariffByCode("FC12-1000-500SO");
        ServiceOrdersContentPage.getInstance().switchWindow(title, false);

        test.get().info("Step 8: click next button on change tariff wizad");
        ServiceOrdersContentPage.getInstance().clickNextBtn();

        test.get().info("Step 9: select specified bundles on change bundle screen then click then next button");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName("£5 safety buffer");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName("Loyalty Bundle - 100MB 4G Data - £0.00 per Month (Non-Recurring)");
        ServiceOrdersContentPage.getInstance().clickNextBtn();

        test.get().info("Step 10:Select recurring bundle to add immediately");
        ServiceOrdersContentPage.getInstance().clickNextBtn();
        ServiceOrdersContentPage.getInstance().clickReturnToCustomer();

        test.get().info("Step 12: open service order page");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 13 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Tariff", "Provision Wait");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        Assert.assertEquals(1, size);

        test.get().info("Step 14 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Tariff");

        test.get().info("Step 15 : Verify SO details data are updated");
        Assert.assertEquals("Provision Wait", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals("£2.50 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("£5 safety buffer;Loyalty Bundle - 100MB 4G Data;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());

        Assert.assertEquals(3, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        HashMap<String, String> enity = EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        Assert.assertTrue(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDateTimeByIndex(2).startsWith(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT)));


        test.get().info("Step 16 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 17 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();


        test.get().info("Step 18 : Click change my safety buffer");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickChangeMySafetyBufferBtn();
        SelfCareTestBase.page().verifyChangeMySafetyBufferPage();

        test.get().info("Step 18 : verify change my safety buffer customer information is correct");
        verifyChangeMySafetyBufferCustomerInformationIsCorrect();
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
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();


        test.get().info("Step 25 : Verify customer has 1 expected change bundle SO record");
        temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Completed Task");
        size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        Assert.assertEquals(1, size);
        serviceOrderID=ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Tariff");
        Assert.assertEquals("Provision Wait", ServiceOrdersContentPage.getInstance().getStatusByServiceOrderID(serviceOrderID));

        test.get().info("Step 14 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Tariff");

        test.get().info("Step 15 : Verify SO details data are updated");
        Assert.assertEquals("Provision Wait", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals("£2.50 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("£5 safety buffer;Loyalty Bundle - 100MB 4G Data;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());

        Assert.assertEquals(3, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        enity = EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        Assert.assertTrue(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDateTimeByIndex(2).startsWith(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT)));

        test.get().info("Step 15 : reload user in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Step 15 : open the service order link");
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


    private void verifyMyTariffDetailPageDisplayedWithCorrectData() {
        List<String> alert = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(1, alert.size());
        Assert.assertEquals("You’ve successfully changed your safety buffer.", alert.get(0));
        Assert.assertEquals(String.format("Your safety buffer has been increased to £7.50 until %s",
                Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getSafetyBuffer());

    }


    private void verifyChangeMySafetyBufferCustomerInformationIsCorrect() {
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().getMobilePhone(), serviceRefOf1stSubscription + " - Mobile Ref 1");
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().getNextAllowanceDate(), ChangeMySafetyBufferPage.getInstance().calculateNextAllowanceDate());
        Assert.assertEquals("£2.50", ChangeMySafetyBufferPage.getInstance().getMonthlySafetyBuffer());
    }

}
