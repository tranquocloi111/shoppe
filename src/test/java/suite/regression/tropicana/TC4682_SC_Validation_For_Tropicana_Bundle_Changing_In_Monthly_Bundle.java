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
import logic.pages.selfcare.MonthlyBundlesAddChangeOrRemovePage;
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

public class TC4682_SC_Validation_For_Tropicana_Bundle_Changing_In_Monthly_Bundle extends BaseTest {
    private String customerNumber = "15758";
    private Date newStartDate;
    private String username;
    private String password;
    private String subscription1;
    private String subscription2;
    private String serviceOrderId;

    @Test(enabled = true, description = "TC4682 SC - Validation for Tropicana bundle changing in Monthly Bundle", groups = "Tropicana")
    public void TC4682_SC_Validation_For_Tropicana_Bundle_Changing_In_Monthly_Bundle(){
        test.get().info("Step 1 : Create a Customer has 2 Subscription that has Tropicana bundle and has no Tropicana");
        OWSActions owsActions = new OWSActions();
        String path = "\\src\\test\\resources\\xml\\tropicana\\TC4682_request.xml";
        owsActions.createGeneralCustomerOrder(path);

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
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        test.get().info("Step 7 : Submit Provision Wait");
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderBySubAndType(subscription2, "Change Bundle"));
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        BaseTest.updateThePDateAndBillDateForSO(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 6 : Login to self care");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password, customerNumber);

        test.get().info("Step 7 : Verify my personal information page is displayed");
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 8 : Click view or change my tariff details link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();

        test.get().info("Step 9 : Verify my tariff details page is displayed");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 10 : Click add or change bundle button for monthly bundle without tropicana");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1");
        mobile1Tariff.clickAddOrChangeABundleButton();

        test.get().info("Step 11 : Current Bundle section is added to Monthly Bundle page as expected");
        VerifyMonthlyBundlesAddChangeOrRemovePageResultIsCorrect();

        test.get().info("Step 12 : Select new monthly bundle");
        //No Tropicana section is displayed as expected
        MonthlyBundlesAddChangeOrRemovePage monthlyBundle = MonthlyBundlesAddChangeOrRemovePage.getInstance();
        monthlyBundle.selectBundlesByName("Monthly 250MB inclusive data allowance");

        test.get().info("Step 13 : Reload new tariff has Tropicana bundle");
        monthlyBundle.clickBackButton();

        test.get().info("Step 14 : Click add or change bundle button for monthly bundle with tropicana");
        mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 2");
        mobile1Tariff.clickAddOrChangeABundleButton();

        test.get().info("Step 15 : Current Bundle section is added to Monthly Bundle page as expected");
        VerifyMonthlyBundlesAddChangeOrRemovePageResultIsCorrect();

        test.get().info("Step 16 : Verify  new monthly bundle and there's an equivalent Tropicana bundle be identified");
        monthlyBundle.selectBundlesByName("Monthly 250MB inclusive data allowance");
        //verify

        test.get().info("Step 17 : Verify new monthly bundle and there's NO equivalent Tropicana bundle be identified");
        monthlyBundle.unSelectBundlesByName("Monthly 250MB inclusive data allowance");
        monthlyBundle.selectBundlesByName("Monthly 500MB Data Allowance - (Upgrade from 250MB)");
        //verify

        test.get().info("Step 18 : Verify Select 2 new monthly bundles");
        monthlyBundle.selectBundlesByName("Monthly 250MB inclusive data allowance");
        //verify

    }

    private void VerifyMonthlyBundlesAddChangeOrRemovePageResultIsCorrect(){
        AddOrChangeAFamilyPerkPage.InfoPage infoPage = AddOrChangeAFamilyPerkPage.InfoPage.getInstance();
        Assert.assertEquals(subscription1 + " - FC Mobile 1", infoPage.getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", infoPage.getTariff());
        Assert.assertEquals("500 mins, 5000 texts (FC)", infoPage.getMonthlyAllowance());
        Assert.assertTrue(infoPage.getThisMonthAllowanceExpiryDate().startsWith(Parser.parseDateFormate(Date.valueOf(TimeStamp.TodayMinus1Day().toLocalDate().plusMonths(1)),"dd/MM/yyyy")));
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
