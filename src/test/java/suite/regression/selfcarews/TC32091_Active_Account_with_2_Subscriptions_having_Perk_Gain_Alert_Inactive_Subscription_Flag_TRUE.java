package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.DeactivateSubscriptionPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 17/09/2019
 */
public class TC32091_Active_Account_with_2_Subscriptions_having_Perk_Gain_Alert_Inactive_Subscription_Flag_TRUE extends BaseTest {
    Date newStartDate = TimeStamp.TodayMinus20Days();
    private List<String> subscriptionNumberList = new ArrayList<>();
    String NC2Subscription;
    String FCSubscription;
    String NCSubscription;

    @Test(enabled = true, description = "TC32091_Active Account with 2 Subscriptions having Perk Gain Alert Inactive Subscription Flag TRUE", groups = "SelfCareWS")
    public void TC32091_Active_Account_with_2_Subscriptions_having_Perk_Gain_Alert_Inactive_Subscription_Flag_TRUE(){
        test.get().info("Create an online CC customer with 3 subscriptions 1FC 2NC");
        OWSActions owsActions = new OWSActions();
        String createOrder_TC32091 = "src\\test\\resources\\xml\\ows\\onlines_CC_customer_with_3_subscriptions(1FC_2NC).xml";
        owsActions.createGeneralCustomerOrder(createOrder_TC32091);
        String customerNumber = owsActions.customerNo;

        test.get().info("Create new billing group");
        createNewBillingGroup();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //=============================================================================

        test.get().info("Login to HUBNet then search Customer by customer number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Get All Subscriptions Number");
        getAllSubscriptionNumber();

        test.get().info("Verify Customer Start Date and Billing Group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Deactivate NC subscription");
        CareTestBase.deactivateSubscription(NC2Subscription + "  Mobile NC 2");

        test.get().info("Reload customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Verify NC Subscription status is inactive");
        CareTestBase.verifySubscriptionStatus(NC2Subscription, "Inactive");
        //==============================================================================
        test.get().info("Submit get account summary request");
        SWSActions swsActions = new SWSActions();
        String getAccountSummaryRequest = "src\\test\\resources\\xml\\sws\\getaccount\\Get_Account_Summary_Admin_Request.xml";
        Xml response = swsActions.submitAccountSummaryWithFlagRequest(getAccountSummaryRequest, customerNumber, "true");
        //==============================================================================
        test.get().info("Build account summary response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getaccount\\TC32091_response.xml";
        String expectedResponseFile = selfCareWSTestBase.buildResponseData(sampleResponseFile, newStartDate,TimeStamp.Today(), TimeStamp.TodayPlus1Month(), customerNumber,subscriptionNumberList);

        test.get().info("Verify get account summary response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponseFile, response);

    }

    private void getAllSubscriptionNumber(){
        subscriptionNumberList = CareTestBase.getAllSubscription();
        for (String subscription : subscriptionNumberList) {
            if (subscription.endsWith("Mobile FC")) {
                FCSubscription = subscription.split(" ")[0];
            } else if (subscription.endsWith("Mobile NC")) {
                NCSubscription = subscription.split(" ")[0];
            }else {
                NC2Subscription = subscription.split(" ")[0];
            }
        }
    }


}
