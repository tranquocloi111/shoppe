package suite.regression.tropicana;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

public class TC4773_UAT_HUBSOW_445_SCWS_Maintain_Bundle_Request_Rejected_Removing_An_Existing_Permitted_Bundle extends BaseTest {
    private String customerNumber;
    private Date newStartDate;
    private String username;
    private String password;
    private String subscription1;
    private String subscription2;
    private String serviceOrderId;

    @Test(enabled = true, description = "TC4773_UAT_HUBSOW_445_SCWS_Maintain_Bundle_Request_Rejected_Removing_An_Existing_Permitted_Bundle", groups = "Tropicana")
    public void TC4773_UAT_HUBSOW_445_SCWS_Maintain_Bundle_Request_Rejected_Removing_An_Existing_Permitted_Bundle(){
        test.get().info("Step 1 : Create a Customer Subscription already has a Family Perk Bundle and a Tropicana");
        String path = "src\\test\\resources\\xml\\tropicana\\TC4773_createOrder.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

//        test.get().info("Step 5 : Update Customer Start Date");
//        newStartDate = TimeStamp.TodayMinus15Days();
//        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6: Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscription1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        subscription2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        test.get().info("Step 7 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        test.get().info("Step 9 : Add Bonus Bundle to Subscription And remove Family Perk");
        selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4773_001_request.xml";
        Xml xml = swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        test.get().info("Step 10 : Verify Request rejected if same request contain  add a Bonus Bundle Group and Remove a Family Perk  in the same Request");
        Assert.assertEquals("UBE_002", xml.getTextByTagName("code"));
        Assert.assertEquals("Invalid mix of bundle types in request", xml.getTextByTagName("description"));

        test.get().info("Step 11 : Add Bonus Bundle to Subscription And remove Permitted Perk");
        selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4773_002_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        test.get().info("Step 12 : Verify Request rejected if same request contain add a Bonus Bundle Group and Remove a Permitted Bundle Group  in the same Request");
        Assert.assertEquals("UBE_002", xml.getTextByTagName("code"));
        Assert.assertEquals("Invalid mix of bundle types in request", xml.getTextByTagName("description"));

    }
}
