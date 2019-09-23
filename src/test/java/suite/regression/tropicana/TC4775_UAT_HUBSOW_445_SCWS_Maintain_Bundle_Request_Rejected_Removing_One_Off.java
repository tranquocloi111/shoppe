package suite.regression.tropicana;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
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

public class TC4775_UAT_HUBSOW_445_SCWS_Maintain_Bundle_Request_Rejected_Removing_One_Off extends BaseTest {
    private String customerNumber;
    private String subscription2;

    @Test(enabled = true, description = "TC4775_UAT_HUBSOW_445_SCWS_Maintain_Bundle_Request_Rejected_Removing_One_Off", groups = "Tropicana")
    public void TC4775_UAT_HUBSOW_445_SCWS_Maintain_Bundle_Request_Rejected_If_Same_Request_Contain_Adding_A_Tropicana_Bundle_Group_And_Removing_One_Off(){
        test.get().info("Step 1 : Create a Customer Subscription already has a Tropicana Bundle associated to a tariff in a subscription ");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\tropicana\\TC4682_request.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscription2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        test.get().info("Step 6 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4775_request.xml";
        Xml xml = swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        test.get().info("Step 7 : Verify Request rejected if same request contain Adding a Tropicana Bundle Group and Removing One-off");
        Assert.assertEquals("UBE_002", xml.getTextByTagName("code"));
        Assert.assertEquals("Invalid mix of bundle types in request", xml.getTextByTagName("description"));
    }
}
