package suite.regression.tropicana;

import logic.pages.selfcare.MyPersonalInformationPage;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;

public class TC4667_SC_Validation_For_Tropicana_Bundle_In_My_Tariff_Page extends BaseTest {
    private String customerNumber = "15758";
    private Date newStartDate;
    private String username;
    private String password;
    String mpnOf1stSubscription;

    @Test(enabled = true, description = "TC 4667 SC - Validation for Tropicana bundle in My Tariff page", groups = "Tropicana")
    public void TC4667_SC_Validation_For_Tropicana_Bundle_In_My_Tariff_Page() {
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

        test.get().info("Step 10 : Verify Tropicana bundle is added as a Month as expected");

        test.get().info("Step 10 : Hover mouse on tooltip of Tropicana bundle");

    }
}
