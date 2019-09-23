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

public class TC4774_UAT_HUBSOW_445_SCWS_Maintain_Bundle_Request_Rejected_Adding_Permitted_Bundle extends BaseTest {
    private String customerNumber;
    private String subscription2;

    @Test(enabled = true, description = "UAT-HUBSOW-445 - SCWSs - Request rejected if same request contain Removing an existing Tropicana Bundle Group and Adding Permitted Bundle Group (WS_03)", groups = "Tropicana")
    public void TC4774_UAT_HUBSOW_445_SCWS_Maintain_Bundle_Request_Rejected_Adding_Permitted_Bundle(){
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

        test.get().info("Step 6 : Remove an existing Tropicana Bundle and add a Permitted Bundle Group in the same Request");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4774_request.xml";
        Xml xml = swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        test.get().info("Step 7 : Verify Request rejected if same request contain Removing an existing Tropicana Bundle Group and Adding Permitted Bundle Group");
        Assert.assertEquals("UBE_002", xml.getTextByTagName("code"));
        Assert.assertEquals("Invalid mix of bundle types in request", xml.getTextByTagName("description"));

    }
}
