package suite.regression.tropicana;

import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

public class TC4775_UAT_HUBSOW_445_SCWS_Maintain_Bundle_Request_Rejected_Removing_One_Off extends BaseTest {
    private String customerNumber = "15758";
    private Date newStartDate;
    private String subscription1;

    @Test(enabled = true, description = "TC4775 UAT-HUBSOW-445 - SCWS - Maintain Bundle - Request rejected if same request contain Adding a Tropicana Bundle Group and Removing One-off (WS_03)", groups = "Tropicana")
    public void TC4775_UAT_HUBSOW_445_SCWS_Maintain_Bundle_Request_Rejected_If_Same_Request_Contain_Adding_A_Tropicana_Bundle_Group_And_Removing_One_Off(){
        test.get().info("Step 1 : Create a Customer Subscription already has a One-off data bundle associated to a tariff in a subscription ");
        String path = "\\src\\test\\resources\\xml\\tropicana\\TC4617_TC001_request.xml";
        OWSActions owsActions = new OWSActions();
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

        test.get().info("Step 6 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4775_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription1);

        test.get().info("Step 7 : Verify Request rejected if same request contain Adding a Tropicana Bundle Group and Removing One-off");


    }
}
