package suite.regression.tropicana;

import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.selfcare.AddOrChangeAFamilyPerkPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.List;

public class TC4719_SC_MODF_SUB_Validation_For_Tropicana_Bundle_In_Add_Change_Family_Perk extends BaseTest {
    private String customerNumber;
    private Date newStartDate;
    private String username;
    private String password;
    String subscription1;
    String subscription2;
    String serviceOrderId;

    @Test(enabled = true, description = "TC4719_SC_MODF_SUB_Validation_For_Tropicana_Bundle_In_Add_Change_Family_Perk", groups = "Tropicana")
    public void TC4719_SC_MODF_SUB_Validation_For_Tropicana_Bundle_In_Add_Change_Family_Perk() {
        test.get().info("Step 1 : Create a customer subscription related tariff linked with a Tropicana bundle group");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\tropicana\\TC4682_request.xml";
        owsActions.createGeneralCustomerOrder(path);;

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscription1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        subscription2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        test.get().info("Step 6 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarepath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarepath, customerNumber, subscription2);

        test.get().info("Step 8 : Login to self care");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password, customerNumber);

        test.get().info("Step 9 : Verify my personal information page is displayed");
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 10 : Click view or change my tariff details link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();

        test.get().info("Step 11 : Verify my tariff details page is displayed");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 13 : Click add or change a family perk button for mobile 1");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 2");
        mobile1Tariff.clickAddOrChangeAFamilyPerkBtn();

        test.get().info("Step 14 : Verify mobile phone info is correct");
        AddOrChangeAFamilyPerkPage.InfoPage infoPage = AddOrChangeAFamilyPerkPage.InfoPage.getInstance();
        Assert.assertEquals(subscription2 + " - Mobile Ref 2", infoPage.getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", infoPage.getTariff());
        Assert.assertEquals(infoPage.getMonthlyBundles(), "Monthly 250MB data allowance - 4G");

        test.get().info("Step 15 : Verify expected warning message displayed");
        String message = "Your changes will apply immediately.";//String.format("Your changes will take effect from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"));
        Assert.assertEquals(message, AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getWarningMessage());

        test.get().info("Step 17 : Verify Tropicana bundle is included in Current Allowance as expected");
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 1));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 1));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 2));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 2));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 3));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 4));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 4));

        test.get().info("Step 18 : Select a New Family Perk bundle then observe the New Allowance");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().selectBundlesByName("Family perk - 250MB per month - 4G");

        test.get().info("Step 19 : The new allowance is being current allowance plus selected  family perk bundle");
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 1));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 1));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 2));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 2));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 3));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 4));
        Assert.assertEquals("750", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 4));

    }
}
