package suite.regression.selfcare.changebundle;


import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.EventEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.AddOrChangeAFamilyPerkPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;


/**
 * User: Loi Tran
 * Date: 31/07/2019
 */
public class TC31886_SelfCare_Change_Bundle_Add_Family_Perk_Bundle_immediately extends BaseTest {
    @Test(enabled = true, description = "TC31886 Self Care Change bundle add family perk immediately ", groups = "SelfCare")
    public void TC31886_SelfCare_Change_Bundle_Add_Family_Perk_Bundle_immediately() {

        String TC31886_CREATE_ORDER = "src\\test\\resources\\xml\\selfcare\\changebundle\\TC1994_createOrder";
        test.get().info("Step 1 : Create a CC customer, having family perk Bundle");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(TC31886_CREATE_ORDER);
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile2 - NC");
        String sub = owsActions.serviceRef;


        test.get().info("Step 2 : Create the new billing group");
        BaseTest.createNewBillingGroupToMinus15days();

        test.get().info("Step 3: Update the payment collection date is 10");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5: Update the start date of customer");
        Date newStartDate = TimeStamp.TodayMinus10Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 7 : click my tariff detail link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 8 : click add or change a family park button for mobile 2");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile2 - NC").clickAddOrChangeAFamilyPerkBtn();
        SelfCareTestBase.page().verifyAddOrChangeAFamilyPerkIsDisplayed();


        test.get().info("Step 9 : Verify expected warning message displayed");
        String message = String.format("Your changes will apply immediately.");
        Assert.assertEquals(message, AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getWarningMessage());

        test.get().info("Step 10 : select 1 family perk bundle and accept terms and conditions");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().selectBundlesByName("Monthly Family perk - 150 Mins (Capped)");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().tickBoxToAcceptTheFamilyPerkTermsAndConditions();

        test.get().info("Step 11 : click save button");
        AddOrChangeAFamilyPerkPage.getInstance().clickSaveBtn();

        test.get().info("Step 12 : verify my tariff detail page displayed with correct data");
        verifyMyTariffDetailPageDisplayedWithCorrectData();


        test.get().info("Step 13: reload customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 14 : verify the bundle has been removed");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(sub + " Mobile2 - NC");
        String discountBundleGroupCode = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountBundleGroupCode);
        Assert.assertEquals(9, discountBundles.size());

        verifyNCDiscountBundles(discountBundles, "TM150");

    }

    protected static void verifyNCDiscountBundles(List<DiscountBundleEntity> allDiscountBundles, String partitionIdRef) {
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayPlus2MonthMinus16Days(), partitionIdRef, "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.TodayPlus1MonthMinus15Day(), TimeStamp.TodayPlus2MonthMinus16Days(), partitionIdRef, "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.TodayPlus2MonthMinus15Days(), TimeStamp.TodayPlus3MonthsMinus16Days(), partitionIdRef, "ACTIVE"));
    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }

    private void verifyMyTariffDetailPageDisplayedWithCorrectData() {
        List<String> alert = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(1, alert.size());
        Assert.assertEquals("Thanks, the bundle changes you’ve made have been successful.", alert.get(0));
        Assert.assertEquals(String.format("Monthly Family perk - 150 Mins (Capped)   ACTIVE  as of  %s",
                Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile2 - NC").getFamilyPerkStack().get(0).getText());

    }

}
