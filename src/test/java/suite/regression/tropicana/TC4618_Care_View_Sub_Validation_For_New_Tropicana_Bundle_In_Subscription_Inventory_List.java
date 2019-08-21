package suite.regression.tropicana;

import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

public class TC4618_Care_View_Sub_Validation_For_New_Tropicana_Bundle_In_Subscription_Inventory_List extends BaseTest {
    private String customerNumber = "15758";
    private Date newStartDate;
    private String username;
    private String password;
    String mpnOf1stSubscription;

    @Test(enabled = true, description = "TC4618 SC - Care-VIEW-SUB- Validation for new Tropicana bundle in subscription inventory list", groups = "Tropicana")
    public void TC4618Care_View_Sub_Validation_For_New_Tropicana_Bundle_In_Subscription_Inventory_List(){
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

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue("FC Mobile 1");

        test.get().info("Step 6 : Tropicana bundle appears in subscription inventory list as expected");


    }
}
