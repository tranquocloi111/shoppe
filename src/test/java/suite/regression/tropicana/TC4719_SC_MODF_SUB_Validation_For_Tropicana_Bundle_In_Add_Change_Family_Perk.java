package suite.regression.tropicana;

import logic.pages.selfcare.AddOrChangeAFamilyPerkPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;

public class TC4719_SC_MODF_SUB_Validation_For_Tropicana_Bundle_In_Add_Change_Family_Perk extends BaseTest {
    String serviceRefOf1stSubscription;
    private String customerNumber = "15758";
    private Date newStartDate;
    private String username;
    private String password;

    @Test(enabled = true, description = "TC4719 SC-MODF-SUB - Validation for Tropicana bundle in Add/Change Family Perk page", groups = "Tropicana")
    public void TC4719_SC_MODF_SUB_Validation_For_Tropicana_Bundle_In_Add_Change_Family_Perk() {
//        test.get().info("Step 1 : Create a customer with NC and device");
//        OWSActions owsActions = new OWSActions();
//        owsActions.createAnOnlinesCCCustomerWith2FCFamilyPerkAndNK2720();
//
//        test.get().info("Step 2 : Create New Billing Group");
//        BaseTest.createNewBillingGroup();
//
//        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
//        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();
//
//        test.get().info("Step 4 : Set bill group for customer");
//        customerNumber = owsActions.customerNo;
//        BaseTest.setBillGroupForCustomer(customerNumber);
//
//        test.get().info("Step 4 : Update Customer Start Date");
//        newStartDate = TimeStamp.TodayMinus15Days();
//        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
//
//        test.get().info("Step 5 : Get Subscription Number");
//        CareTestBase.page().loadCustomerInHubNet(customerNumber);
//        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
//        mpnOf1stSubscription = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 1");

//        test.get().info("Step 6 : Add Bonus Bundle to Subscription");
//        SWSActions swsActions = new SWSActions();
//        String path = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
//        swsActions.submitMaintainBundleRequest(path,"","");
//
//        test.get().info("Step 7 : Submit Provision Service Job");
//        BaseTest.updateThePDateAndBillDateForSO("");
//        RemoteJobHelper.getInstance().runProvisionSevicesJob();

//        test.get().info("Step 8 : Login to self care");
//        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
//
//        test.get().info("Step 9 : Verify my personal information page is displayed");
//        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 10 : Click view or change my tariff details link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();

        test.get().info("Step 11 : Verify my tariff details page is displayed");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 13 : Click add or change a family perk button for mobile 1");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1");
        mobile1Tariff.clickAddOrChangeAFamilyPerkBtn();

        test.get().info("Step 14 : Verify mobile phone info is correct");
        AddOrChangeAFamilyPerkPage.InfoPage infoPage = AddOrChangeAFamilyPerkPage.InfoPage.getInstance();
        Assert.assertEquals(serviceRefOf1stSubscription + " - FC Mobile 1", infoPage.getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", infoPage.getTariff());
        Assert.assertEquals("500 mins, 5000 texts (FC)", infoPage.getMonthlyAllowance());
        Assert.assertTrue(infoPage.getMonthlyBundles().isEmpty());

        test.get().info("Step 15 : Verify expected warning message displayed");
        String message = String.format("Your changes will take effect from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"));
        Assert.assertEquals(message, AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getWarningMessage());

        test.get().info("Step 17 : Verify Tropicana bundle is included in Current Allowance as expected");
        Assert.assertEquals("650", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 1));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 1));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 2));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 2));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 4));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 4));

        test.get().info("Step 18 : Select a New Family Perk bundle then observe the New Allowance");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().selectBundlesByName("Family perk - 250MB per month");

        test.get().info("Step 19 : The new allowance is being current allowance plus selected  family perk bundle");
        Assert.assertEquals("650", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 1));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 1));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 2));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 2));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 4));
        Assert.assertEquals("250", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 4));

    }
}
