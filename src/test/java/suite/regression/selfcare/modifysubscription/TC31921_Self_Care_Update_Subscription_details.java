package suite.regression.selfcare.modifysubscription;


import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

public class TC31921_Self_Care_Update_Subscription_details extends BaseTest {


    @Test(enabled = true, description = "TC31921 self care update subscription details", groups = "SelfCare")
    public void TC31921_Self_Care_Update_Subscription_details() {

        test.get().info("Step 1 : Create a CC customer");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_2_bundles_and_NK2720";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber= owsActions.customerNo;


        test.get().info("Step 5: Login SelfCare  ");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, owsActions.customerNo);

        test.get().info("verify my tariff details page is displayed");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("update description and click save button");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").updateDescription("EditUserName");

        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickSavePhoneUserNameBtn();

        test.get().info("Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("verify phone username in hub net");
        Assert.assertEquals(CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(1).split(" ")[1],"EditUserName");

        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);

        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getReference(),"EditUserName");


    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][] { { "gc" }, { "ff" }, { "ie" } };
    }
}
