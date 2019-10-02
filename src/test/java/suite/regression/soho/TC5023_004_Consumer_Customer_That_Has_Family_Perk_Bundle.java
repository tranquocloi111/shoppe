package suite.regression.soho;

import logic.business.entities.BundlesToSelectEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.find.SummaryContentsPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ChangeBundlePage;
import logic.pages.care.options.ConfirmChangeBundlePage;
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

public class TC5023_004_Consumer_Customer_That_Has_Family_Perk_Bundle extends BaseTest{
    private String customerNumber = "9671";
    private Date newStartDate;
    private String username;
    private String password;
    private String subNo1;
    private String subNo2;

    @Test(enabled = true, description = "TC5023_004_Consumer_Customer_That_Has_Family_Perk_Bundle", groups = "Soho")
    public void TC5023_004_Consumer_Customer_That_Has_Family_Perk_Bundle() {
        test.get().info("Step 1 :  Consumer customer that has family perk");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC5023_002_request_residential_type.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 6 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");
        subNo2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 2");

        test.get().info("Step 8 : Add Family Perk Bundle");
        changeFamilyPerkBundle();

        test.get().info("Step 9 : Verify A Family Perk Bundle Was Added To Subscription 2");
        VerifyAFamilyPerkBundleWasAddedToSubscription2();

        test.get().info("Step 10 : Login to SelfCare");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password, customerNumber);

        test.get().info("Step 11 : Navigate to the My tariff and credit agreement documents  page");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();

        test.get().info("Step 12 : Click on the button Add or change a Bundle button");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile2Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile 2");
        mobile2Tariff.clickAddOrChangeABundleButton();

        MonthlyBundlesAddChangeOrRemovePage monthlyBundle = MonthlyBundlesAddChangeOrRemovePage.getInstance();
        Assert.assertEquals(monthlyBundle.getCurrentBundleDescriptionByCellValue("Family perk", 1), "Family perk - 150 Mins per month");
        Assert.assertEquals(monthlyBundle.getCurrentBundleDescriptionByCellValue("Family perk", 2), "150");
        Assert.assertEquals(monthlyBundle.getCurrentBundleDescriptionByCellValue("Family perk", 5), "£0.00");

        test.get().info("Step 12 : Click on the button Add or View One Off Bundle button");
        mobile2Tariff.clickBackBtn();
        mobile2Tariff.clickAddOrViewOneOffBundlesButton();
        Assert.assertEquals(monthlyBundle.getCurrentBundleDescriptionByCellValue("Family perk", 1), "Family perk - 150 Mins per month");
        Assert.assertEquals(monthlyBundle.getCurrentBundleDescriptionByCellValue("Family perk", 2), "150");
        Assert.assertEquals(monthlyBundle.getCurrentBundleDescriptionByCellValue("Family perk", 5), "£0.00");
    }

    private void verifyInformationColorBoxHeaderBusiness(){
        SummaryContentsPage summaryContentsPage = SummaryContentsPage.getInstance();
        for (int i = 0; i < summaryContentsPage.getBackGroundColorOfHeader().size(); i++) {
            Assert.assertEquals(summaryContentsPage.getBackGroundColorOfHeader().get(i), "rgba(255, 220, 0, 1)");
        }
    }

    private void changeFamilyPerkBundle(){
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(subNo2 + " Mobile 2", "Change Family Perk");

        ChangeBundlePage changeBundle = ChangeBundlePage.getInstance();
        Assert.assertEquals(changeBundle.getSubscriptionNumber(), subNo2 + " Mobile 2");
        Assert.assertEquals(changeBundle.getCurrentTariff(), "£10 Tariff 12 Month Contract");
        Assert.assertEquals(changeBundle.getPackagedBundle(), "Bundle - 500 mins, 5000 texts (FC)");
        String expectNextBillDate = String.format("%s (%d days from today)",Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"), TimeStamp.todayPlus1MonthMinusToday());
        Assert.assertEquals(changeBundle.getNextBillDateForThisAccount(), expectNextBillDate);
        Assert.assertEquals(changeBundle.getInfo(), "No Current Bundles");

        Assert.assertTrue(changeBundle.bundleExists(BundlesToSelectEntity.getFamilyPerkBundles()));
        changeBundle.selectBundlesByName(BundlesToSelectEntity.getFamilyPerkBundles(),"Family perk - 150 Mins per month - £0.00 per Month (Recurring)");
        changeBundle.clickNextButton();

        ConfirmChangeBundlePage confirmChangeBundlePage = ConfirmChangeBundlePage.getInstance();
        Assert.assertEquals(confirmChangeBundlePage.getSubscriptionNumber(), subNo2 + " Mobile 2");
        Assert.assertEquals(confirmChangeBundlePage.getNextBillDateForThisAccount(), expectNextBillDate);
        Assert.assertEquals(confirmChangeBundlePage.getCurrentTariff(), "FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}");
        Assert.assertEquals(changeBundle.getPackagedBundle(), "Bundle - 500 mins, 5000 texts (FC)");
        Assert.assertEquals(confirmChangeBundlePage.getInfoBefore(), "No Current Recurring Bundles");
        Assert.assertEquals(confirmChangeBundlePage.getTotalRecurringBundleChargeBefore(), "£0.00 per month");
        Assert.assertEquals(confirmChangeBundlePage.getTotalRecurringBundleChargeAfter(), "£0.00 per month");
        Assert.assertEquals(confirmChangeBundlePage.getRecurringBundlesChargeDifference(),"No Change");
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(),"dd/MM/yyyy"),confirmChangeBundlePage.getEffective());

        confirmChangeBundlePage.clickNextButton();
        confirmChangeBundlePage.clickReturnToCustomer();
    }

    private void VerifyAFamilyPerkBundleWasAddedToSubscription2(){
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo2 + " Mobile 2");
        HashMap<String, String> newOtherProduct = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("BUNDLER - [150-FMIN-0-FC]", "Bundle", "Discount Bundle Recurring - [Family perk - 150 Mins per month]", "£0.00", TimeStamp.Today());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(newOtherProduct));
    }
}
