package suite.regression.tropicana;

import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.selfcare.AddOrChangeAFamilyPerkPage;
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

public class TC4682 extends BaseTest {
    private String customerNumber = "15758";
    private Date newStartDate;
    private String username;
    private String password;
    String mpnOf1stSubscription;


    @Test(enabled = true, description = "TC 4682 SC - Validation for Tropicana bundle changing in Monthly Bundle", groups = "Tropicana")
    public void TC4682_SC_Validation_For_Tropicana_Bundle_Changing_In_Monthly_Bundle(){
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
//        test.get().info("Step 7 : Submit ");
//        BaseTest.updateThePDateAndBillDateForSO("");
//        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 6 : Login to self care");
        username = "un779765118@hsntech.com";//owsActions.username;//"un037730976@hsntech.com";
        password = "password3";//owsActions.password;//"password3";
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password, customerNumber);

        test.get().info("Step 7 : Verify my personal information page is displayed");
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 8 : Click view or change my tariff details link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();

        test.get().info("Step 9 : Verify my tariff details page is displayed");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 10 : Click add or change bundle button for monthly bundle without tropicana");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1");
        mobile1Tariff.clickAddOrChangeABundleButton();

        test.get().info("Step 11 : Current Bundle section is added to Monthly Bundle page as expected");
        VerifyMonthlyBundlesAddChangeOrRemovePageResultIsCorrect();

        test.get().info("Step 12 : Reload new ");
        MonthlyBundlesAddChangeOrRemovePage monthlyBundle = MonthlyBundlesAddChangeOrRemovePage.getInstance();
        monthlyBundle.clickBackButton();

        test.get().info("Step 10 : Click add or change bundle button for monthly bundle with tropicana");
        mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 2");
        mobile1Tariff.clickAddOrChangeABundleButton();

        test.get().info("Step 11 : Current Bundle section is added to Monthly Bundle page as expected");
        VerifyMonthlyBundlesAddChangeOrRemovePageResultIsCorrect();

        test.get().info("Step 11 : Verify  new monthly bundle and there's an equivalent Tropicana bundle be identified");
        monthlyBundle.selectBundlesByName("Monthly 250MB inclusive data allowance");
        //verify

        test.get().info("Step 11 : Verify new monthly bundle and there's NO equivalent Tropicana bundle be identified");
        monthlyBundle.unSelectBundlesByName("Monthly 250MB inclusive data allowance");
        monthlyBundle.selectBundlesByName("Monthly 500MB Data Allowance - (Upgrade from 250MB)");
        //verify

        test.get().info("Step 11 : Verify Select 2 new monthly bundles");
        monthlyBundle.selectBundlesByName("Monthly 250MB inclusive data allowance");
        //verify

    }

    private void VerifyMonthlyBundlesAddChangeOrRemovePageResultIsCorrect(){
        AddOrChangeAFamilyPerkPage.InfoPage infoPage = AddOrChangeAFamilyPerkPage.InfoPage.getInstance();
        //Assert.assertEquals(mpnOf1stSubscription + " - FC Mobile 1", infoPage.getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", infoPage.getTariff());
        Assert.assertEquals("500 mins, 5000 texts (FC)", infoPage.getMonthlyAllowance());
       // Assert.assertTrue(infoPage.getThisMonthAllowanceExpiryDate().startsWith(Parser.parseDateFormate(Date.valueOf(TimeStamp.TodayMinus1Day().toLocalDate().plusMonths(1)),"dd/MM/yyyy")));
        Assert.assertEquals("£20.00", infoPage.getMonthlySafetyBuffer());

        MonthlyBundlesAddChangeOrRemovePage monthlyBundle = MonthlyBundlesAddChangeOrRemovePage.getInstance();
        Assert.assertEquals("£7.50 per month", monthlyBundle.monthly1GBDataAllowancePrice());
        Assert.assertEquals("£5.00 per month", monthlyBundle.monthly500DataAllowancePrice());
        Assert.assertEquals("£5.00 per month", monthlyBundle.getMonthlyDataBundleByValue("Monthly 250MB data allowance - 4G"));
        Assert.assertEquals("£7.50 per month", monthlyBundle.totalPrice());

        String note = "* Note: Saving this change will cancel any pending bundle changes made previously.";
        Assert.assertEquals(note, monthlyBundle.noteSavingMessage());
        Assert.assertTrue(monthlyBundle.isNoteSavingMessageRed());

        String fairUsagePolicyMessage = "You can use all of this data in the UK and in Europe with Home From Home. Read our fair usage policy.";
        Assert.assertEquals(fairUsagePolicyMessage, monthlyBundle.fairUsagePolicyMessage());

        String underneathLinkText = "* Note: To add a one-off bundle, go to the One-off bundle page";
        Assert.assertEquals(underneathLinkText, monthlyBundle.underneathLinkText());
        Assert.assertTrue(monthlyBundle.underneathLinkDisplayed());

        Assert.assertEquals("Your bundle will be available for you to use from  " + Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"), monthlyBundle.bundleAvailableDateMessage());

        Assert.assertEquals("£10.00 per month", monthlyBundle.tariffCharge());
        Assert.assertEquals("£10.00 per month", monthlyBundle.totalMonthlyCharge());

    }

}
