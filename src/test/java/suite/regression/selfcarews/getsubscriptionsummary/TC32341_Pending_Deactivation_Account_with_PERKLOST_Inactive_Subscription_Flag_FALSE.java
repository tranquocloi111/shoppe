package suite.regression.selfcarews.getsubscriptionsummary;

import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.options.DeactivateSubscriptionPage;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.List;

import static suite.regression.care.CareTestBase.getAllSubscriptionsNumber;

/**
 * User: Nhi Dinh
 * Date: 14/10/2019
 */
public class TC32341_Pending_Deactivation_Account_with_PERKLOST_Inactive_Subscription_Flag_FALSE extends BaseTest {
    private Date newStartDate = TimeStamp.TodayMinus20Days();
    private String FCMobile1Subscription;
    private String FCMobile2Subscription;

    @Test(enabled = true, description = "TC32341_Pending_Deactivation_Account_with_PERKLOST_Inactive_Subscription_Flag_FALSE", groups = "SelfCareWS.GetSubscriptionSummary")
    public void TC32341_Pending_Deactivation_Account_with_PERKLOST_Inactive_Subscription_Flag_FALSE() {
        test.get().info("Step 1 : Create an online CC customer with FC 2 bundles and NK2720");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC2BundlesAndNK2720();
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        //=============================================================================
        test.get().info("7. Get Subscription Numbers");
        getSubscriptionNumber();

        test.get().info("8. Deactivate FC Mobile 1 subscription and return to customer");
        deactivateFCMobile1SubscriptionAndReturnToCustomer(FCMobile1Subscription + "  FC Mobile 1");


    }

    private void deactivateFCMobile1SubscriptionAndReturnToCustomer(String subscription) {
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().selectDeactivateBySubscription(subscription);
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().clickNextButton();
    }

    private void getSubscriptionNumber() {
        List<String> subList = getAllSubscriptionsNumber();
        FCMobile1Subscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 1");
        FCMobile2Subscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 2");
    }
}


