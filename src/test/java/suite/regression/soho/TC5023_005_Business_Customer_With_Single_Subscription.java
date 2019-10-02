package suite.regression.soho;

import logic.business.entities.BundlesToSelectEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SummaryContentsPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ChangeBundlePage;
import logic.pages.care.options.ConfirmChangeBundlePage;
import logic.pages.selfcare.MonthlyBundlesAddChangeOrRemovePage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.pages.selfcare.OrderConfirmationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.HashMap;

public class TC5023_005_Business_Customer_With_Single_Subscription extends BaseTest{
    private String customerNumber = "9671";
    private String username;
    private String password;
    private String subNo1;

    @Test(enabled = true, description = "TC5023_003_Business_Customer_That_Has_Family_Perk_Bundle", groups = "Soho")
    public void TC5023_003_Business_Customer_That_Has_Family_Perk_Bundle() {
        test.get().info("Step 1 :  Business customer that has sing subscription");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC5023_005_request_business_type_single_sub.xml";
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

        test.get().info("Step 7 : Verify color and information of business information tab ");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        SummaryContentsPage.BusinessInformationPage business = SummaryContentsPage.BusinessInformationPage.getInstance();
        verifyInformationColorBoxHeaderBusiness();
        Assert.assertTrue(business.isBusinessPresent());
        Assert.assertEquals(business.getBusinessName(), "Tom Cruise");

        test.get().info("Step 10 : Login to SelfCare");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password, customerNumber);

        test.get().info("Step 11 : Navigate to the Order Confirmation page");
        MyPersonalInformationPage.MyPreviousOrdersPage.getInstance().clickViewByIndex(1);
        OrderConfirmationPage.OrderDetails orderDetails = OrderConfirmationPage.OrderDetails.getInstance();
        Assert.assertEquals(orderDetails.getMessageOfOrderConfirmation(), "Enjoy a free Perk when you add another contract to your account and whoever you add can choose one too! Click here to find out more.");

    }

    private void verifyInformationColorBoxHeaderBusiness(){
        SummaryContentsPage summaryContentsPage = SummaryContentsPage.getInstance();
        for (int i = 0; i < summaryContentsPage.getBackGroundColorOfHeader().size(); i++) {
            Assert.assertEquals(summaryContentsPage.getBackGroundColorOfHeader().get(i), "rgba(255, 220, 0, 1)");
        }
    }

}
