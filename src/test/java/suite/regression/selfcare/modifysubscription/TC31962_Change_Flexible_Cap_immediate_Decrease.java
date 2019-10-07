package suite.regression.selfcare.modifysubscription;


import framework.utils.Log;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.find.UnbilledSumaryPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.AddASafeTyBufferPage;
import logic.pages.selfcare.ChangeMySafetyBufferPage;
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

public class TC31962_Change_Flexible_Cap_immediate_Decrease extends BaseTest {


    @Test(enabled = true, description = "TC31962 change flexible cap immediate decrease", groups = "SelfCare")
    public void TC31962_Change_Flexible_Cap_immediate_Decrease() {

        test.get().info("Step 1: Create a CC customer");
        String path = "src\\test\\resources\\xml\\selfcare\\modifysubscription\\TC31962_createOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getSubscription(owsActions.orderIdNo, "FC Mobile 1");
        String customerNumber = owsActions.customerNo;
        String subno = owsActions.serviceRef;


        test.get().info(" Step 2: Login SelfCare  ");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, "password1", customerNumber);

        test.get().info("Step 3: verify my tariff details page is displayed");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 4: click add a safety buffer");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").clickChangeMySafetyBufferBtn();

        test.get().info("Step 5: verify add a safety buffer");
        ChangeMySafetyBufferPage.getInstance().selectASafetyBufferByCode("20");

        test.get().info("Step 6: verify decrease warning message is displayed");
        String mssg = "Your safety buffer will decrease from your next bill date, you cannot decrease it immediately.";
        Assert.assertEquals(mssg, ChangeMySafetyBufferPage.getInstance().getDescreaseWarningMessage());

        test.get().info("Step 7: verify confirming your changes displayes correct result");
        Assert.assertEquals("£40.00", ChangeMySafetyBufferPage.getInstance().getPreviousSafetyBuffer());
        Assert.assertEquals("£20.00", ChangeMySafetyBufferPage.getInstance().getNewSafetyBuffer());
        ChangeMySafetyBufferPage.getInstance().clickSaveBtn();

        test.get().info("Step 8: load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);


        test.get().info("Step 9:access the service order item");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 10:verify service order status is provision wait");
        HashMap<String, String> serviceOrder = ServiceOrderEntity.dataServiceOrderForChangePassword("Change Bundle", "Provision Wait");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(serviceOrder), 1);
        String soID = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");

        test.get().info("Step 11: update th PDate and BillDate for provision wait SO");
        BillingActions.getInstance().updateThePDateAndBillDateForChangeBundle(soID);

        test.get().info("Step 12: submit the do provision services batch job");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();


        test.get().info("Step 13: access the service order item");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 14: verify service order status is completed task");
        serviceOrder = ServiceOrderEntity.dataServiceOrderForChangePassword("Change Bundle", "Completed Task");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(serviceOrder), 1);


        test.get().info("Step 15: access the subscription page");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 16: open the subscription detail for first subscription");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);

        test.get().info("Step 17: verify the new SB is presented old SB is needed");
        HashMap<String, String> otherProduct = OtherProductEntiy.dataForOtherBundleProduct
                ("FLEXCAP - [04000-SB-A]", "Bundle", "Flexible Cap - £40 - [£40 safety buffer]", "£0.00", TimeStamp.Today(), TimeStamp.Today());

        HashMap<String, String> otherProduct1 = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("FLEXCAP - [02000-SB-A]", "Bundle", "Flexible Cap - £20 - [£20 safety buffer]", "£0.00", TimeStamp.Today());

        HashMap<String, String> otherProduct2 = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("NK-2720", "Device", "Nokia 2720", "£0.00", TimeStamp.Today());

        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct1), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct2), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable(), 3);

        test.get().info("Step 18: open the unbilled summary content for customer");
        MenuPage.LeftMenuPage.getInstance().clickUnBilledSummaryItem();

        test.get().info("Step 19: save unbilled summary image for all subscription");
        subno = subno + "  FC Mobile 1";
        saveUnbilledSummaryImageForAllSubscription(customerNumber, subno);



    }

    public void saveUnbilledSummaryImageForAllSubscription(String customerNumber, String subno) {
        if (!UnbilledSumaryPage.getInstance().getFirstFilter().equalsIgnoreCase(subno.trim())) {
            UnbilledSumaryPage.getInstance().selectFilter(subno);
            UnbilledSumaryPage.getInstance().clickFindNowBrn();
        }
        Log.info("Check unbilled summary image for 1 subscrption manually from ");
        String imgFile =String.format("TC31962_%s_HubNet.jpg", customerNumber);
        UnbilledSumaryPage.getInstance().saveFileFromWebRequest(imgFile);
    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }
}
