package suite.regression.selfcare.changebundle;

import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.EventEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.AddOrChangeAFamilyPerkPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

public class TC31890_SelfCare_Change_Bundle_Add_Family_Perk_Subscription_is_not_eligible extends BaseTest {
    String sub;

    @Test(enabled = true, description = "TC31890 SelfCare change bundle add family perk subscription is not eligble", groups = "SelfCare")
    public void TC31890_SelfCare_Change_Bundle_Add_Family_Perk_Subscription_is_not_eligible() {
        String TC1999_CreateOrder = "src\\test\\resources\\xml\\SelfCare\\changebundle\\TC2000_CreateOrder";
        test.get().info("Step 1 : Create a customer with 2 NC subscription");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(TC1999_CreateOrder);
        sub = owsActions.serviceRef;
        String customerNumber=owsActions.customerNo;


        test.get().info("Step 2 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 3 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 4 : Click add or change a family perk page is correct");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").clickAddOrChangeAFamilyPerkBtn();

        test.get().info("Step 5 : Verify expected message displayed on add or change a family perk page");
        String message = "You could get free monthly Family Perks if you have 2 or more subscriptions on your account. Click here for more info.";
        Assert.assertEquals(AddOrChangeAFamilyPerkPage.getInstance().getBizMessage(),message);

        test.get().info("Step 6 : verify href of here link is correct");
        Assert.assertTrue(AddOrChangeAFamilyPerkPage.getInstance().getHereLinkHref().contains("http://www.tescomobile.com"));

        test.get().info("Step 7 : click here link");
        AddOrChangeAFamilyPerkPage.getInstance().clickHereLink();


    }


}
