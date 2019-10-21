package suite.regression.tropicana;

import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;

public class TC4667_SC_Validation_For_Tropicana_3_Types_Bundle_In_My_Tariff_Page extends BaseTest {
    private String customerNumber = "14549";
    private Date newStartDate;
    private String username;
    private String password;
    String subscription1;
    String subscription2;
    String subscription3;
    String subscription4;
    String serviceOrderId;

    @Test(enabled = true, description = "TC4667_SC_Validation_For_Tropicana_3_Types_Bundle_In_My_Tariff_Page", groups = "Tropicana")
    public void TC4667_SC_Validation_For_Tropicana_3_Types_Bundle_In_My_Tariff_Page() {
        test.get().info("Step 1 : Create a customer has Subscription with Tropicana bundle");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\tropicana\\TC4667_data_text_min_type_request.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update Customer Start Date");
        newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscription1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        subscription2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");
        subscription3 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 3");
        subscription4 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 4");

        test.get().info("Step 7 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4667_min_type_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription3);

        selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4667_text_type_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription4);

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

        test.get().info("Step 12 : Verify Tropicana data bundle type is added as a Month as expected");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobileTariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 2");
        Assert.assertEquals(mobileTariff.getFamilyPerkStack().get(0).getText(), String.format("Family perk - 250MB per month   ACTIVE  as of  %s", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT4)));

        test.get().info("Step 13 : Verify Hover your cursor over the question mark image next to the data bundle to open the tooltip");
        Assert.assertEquals(mobileTariff.getFamilyPerkHelpIconText(), "This is the data bundle you get with your Bonus subscription.");

        test.get().info("Step 14 : Verify Tropicana mins bundle type is added as a Month as expected");
        mobileTariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 3");
        Assert.assertEquals(mobileTariff.getLoyaltyStack().get(0).getText(), String.format("Loyalty Bundle - 250 Mins per month   ACTIVE  as of  %s", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT4)));

        test.get().info("Step 15 : Verify Hover your cursor over the question mark image next to the mins bundle to open the tooltip");
        Assert.assertEquals(mobileTariff.getLoyaltyHelpIconText(0), "This is the data bundle you get with your Bonus subscription.");

        test.get().info("Step 14 : Verify Tropicana text bundle type is added as a Month as expected");
        mobileTariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 4");
        Assert.assertEquals(mobileTariff.getLoyaltyStack().get(0).getText(), String.format("Loyalty Bundle - 250 Texts (Capped)   ACTIVE  as of  %s", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT4)));

        test.get().info("Step 15 : Verify Hover your cursor over the question mark image next to the text bundle to open the tooltip");
        Assert.assertEquals(mobileTariff.getLoyaltyHelpIconText(0), "This is the data bundle you get with your Bonus subscription.");
    }
}
