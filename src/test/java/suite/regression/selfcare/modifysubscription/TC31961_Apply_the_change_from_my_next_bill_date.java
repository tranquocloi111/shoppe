package suite.regression.selfcare.modifysubscription;


import framework.utils.Log;
import logic.business.db.billing.BillingActions;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.find.UnbilledSumaryPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.care.options.ChangeSafetyBufferPage;
import logic.pages.selfcare.AddASafeTyBufferPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.HashMap;
import java.util.List;

public class TC31961_Apply_the_change_from_my_next_bill_date extends BaseTest {


    @Test(enabled = true, description = "TC31961 apply the change from my next bill date", groups = "SelfCare")
    public void TC31961_Apply_the_change_from_my_next_bill_date() {

        test.get().info("Step 1 : Create a CC customer");
        String path = "src\\test\\resources\\xml\\selfcare\\modifysubscription\\TC31961_createOrder.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getSubscription(owsActions.orderIdNo, "FC Mobile 1");
        String customerNumber = owsActions.customerNo;
        String subno = owsActions.serviceRef;


        test.get().info("Step 2: Login SelfCare  ");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 3: verify my tariff details page is displayed");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 4: click add a safety buffer");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").clickAddASafetyBufferBtn();

        test.get().info("Step 5: verify add a safety buffer");
        SelfCareTestBase.page().verifyAddASaftyBufferSafetyBufferPage();
        Assert.assertEquals(AddASafeTyBufferPage.getInstance().getMobilePhoneNumber(), subno + " - FC Mobile 1");
        Assert.assertEquals(AddASafeTyBufferPage.getInstance().getInfoNextAllowanceDate(), AddASafeTyBufferPage.getInstance().calculateNextAllowanceDate());
        Assert.assertEquals(AddASafeTyBufferPage.getInstance().getThirdPartyMssg(), "Find out more about safety buffers.");
        Assert.assertTrue(AddASafeTyBufferPage.getInstance().isLinkHasAThirdPartyTermAndConditionPage());

        test.get().info("Step 6: verify only safety buffers are available and no high usage limites");
        Assert.assertTrue(AddASafeTyBufferPage.getInstance().areAllItemsSafetyBuffer());

        test.get().info("Step 7: verify message statting when to apply the change");
        String message = "This change will apply from your next bill date on 23/" + Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF3).substring(0, 2);
        Assert.assertEquals(message, AddASafeTyBufferPage.getInstance().getSelectSafetyBufferMessage());

        test.get().info("Step 8: select a safety buffer option");
        AddASafeTyBufferPage.getInstance().selectSafetyBuffer("£40 safety buffer");

        test.get().info("Step 9: verify confirming your changes displays correct result");
        Assert.assertEquals("None", AddASafeTyBufferPage.getInstance().getPreviousSafetyBuffer());
        Assert.assertEquals("£40.00", AddASafeTyBufferPage.getInstance().getNewSafetyBuffer());

        test.get().info("Step 11: click save button");
        AddASafeTyBufferPage.getInstance().clickSaveBtn();

        test.get().info("Step 12: Verify my tariff page details page displayed with the successful message");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();
        List<String> mssg = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(2, mssg.size());
        Assert.assertEquals("You’ve successfully added a safety buffer.", mssg.get(0));

        Assert.assertEquals("Your changes will take effect from 23/" + Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF3), mssg.get(1));

        test.get().info("Step 13: load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 14: open the services orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 15: verify service order status is provision wait");
        HashMap<String, String> expectedServiceOrder = ServiceOrderEntity.dataServiceOrderForChangePassword("Change Bundle", "Provision Wait");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(expectedServiceOrder), 1);

        String serviceOrderID = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");

        test.get().info("Step 16: update th PDate and BillDate for provision wait SO");
        BillingActions.getInstance().updateThePDateAndBillDateForChangeBundle(serviceOrderID);

        test.get().info("Step 17: submit the do provision services batch job");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();


        test.get().info("Step 18: access the service order item");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 19: verify service order status is completed task");
        expectedServiceOrder = ServiceOrderEntity.dataServiceOrderForChangePassword("Change Bundle", "Completed Task");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(expectedServiceOrder), 1);


        test.get().info("Step 20: access the subscription page");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 21: open the subscription detail for first subscription");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);

        test.get().info("Step 22: verify the new SB is presented old SB is needed");
        HashMap<String, String> otherProduct = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("FLEXCAP - [04000-SB-A]", "Bundle", "Flexible Cap - £40 - [£40 safety buffer]", "£0.00", TimeStamp.Today());

        HashMap<String, String> otherProduct1 = OtherProductEntiy.dataForOtherBundleProduct
                ("FLEXCAP - [10000-HU-A]", "Bundle", "Flexible Cap - £100 - [£100 High usage limit]", "£0.00", TimeStamp.Today(), TimeStamp.Today());
        HashMap<String, String> otherProduct2 = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("NK-2720", "Device", "Nokia 2720", "£0.00", TimeStamp.Today());

        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct1), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct2), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable(), 3);

        test.get().info("Step 23: open the unbilled summary content for customer");
        MenuPage.LeftMenuPage.getInstance().clickUnBilledSummaryItem();

        test.get().info("Step 24: save unbilled summary image for all subscription");
        subno = subno + "  FC Mobile 1";
        saveUnbilledSummaryImageForAllSubscription(customerNumber, subno);

        test.get().info("Step 25: Login selfcare without pin");
        SelfCareTestBase.page().LoginIntoSelfCarePageWithOutPin(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 26: verify my tariff details page is displayed");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

    }

    public void saveUnbilledSummaryImageForAllSubscription(String customerNumber, String subno) {
        if (!UnbilledSumaryPage.getInstance().getFirstFilter().equalsIgnoreCase(subno.trim())) {
            UnbilledSumaryPage.getInstance().selectFilter(subno);
            UnbilledSumaryPage.getInstance().clickFindNowBrn();
        }
        Log.info("Check unbilled summary image for 1 subscrption manually from ");
        String imgFile = String.format("TC31961_%s_HubNet.jpg", customerNumber);
        UnbilledSumaryPage.getInstance().saveFileFromWebRequest( imgFile);
    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }
}
