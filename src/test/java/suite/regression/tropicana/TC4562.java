package suite.regression.tropicana;

import logic.business.helper.RemoteJobHelper;
import org.testng.annotations.Test;
import suite.BaseTest;

public class TC4562 extends BaseTest {

    @Test(enabled = true, description = "TC 4682 HUB - Validation for Bonus bundles excluded from Deal Extract", groups = "Tropicana")
    public void TC4682_HUB_Validation_For_Bonus_Bundles_Excluded_From_Deal_Extract() {
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
//        test.get().info("Step 7 : Submit Provision job");
//        BaseTest.updateThePDateAndBillDateForSO("");
//        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 8 : Submit DoDealXMLExtract Job");
        RemoteJobHelper.getInstance().runDoDealXMLExtractJob();

        test.get().info("Step 9 : Open extracted XML file and validate the existence of  bundles under Permitted Bundle Group. ");


    }
}
