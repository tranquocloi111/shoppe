package suite.regression.selfcarews.getaccountsummary;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 30/09/2019
 */
public class TC32124_Self_Care_WS_Get_Account_Summary_Active_Account_and_Bundles_InactiveSub_FALSE extends BaseTest {
    Date newStartDate = TimeStamp.TodayMinus20Days();
    private List<String> subscriptionNumberList = new ArrayList<>();
    String NC2Subscription;
    String FCSubscription;
    String NCSubscription;


    @Test(enabled = true, description = "TC32046_Alternative Path 1a Deactivated Subscription Flexible Cap", groups = "SelfCareWS.GetAccountSummary")
    public void TC32124_Self_Care_WS_Get_Account_Summary_Active_Account_and_Bundles_InactiveSub_FALSE(){
        test.get().info("1. Create an online CC customer with 3 subscriptions 1FC 2NC");
        OWSActions owsActions = new OWSActions();
        String createOrder_TC32091 = "src\\test\\resources\\xml\\ows\\onlines_CC_customer_with_3_subscriptions(1FC_2NC).xml";
        owsActions.createGeneralCustomerOrder(createOrder_TC32091);
        String customerNumber = owsActions.customerNo;

        test.get().info("2. Create new billing group");
        createNewBillingGroup();

        test.get().info("3. Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("4. Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("5. Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //=============================================================================

        test.get().info("6. Login to HUBNet then search Customer by customer number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("7. Get All Subscriptions Number");
        getAllSubscriptionNumber();

        test.get().info("8. Verify Customer Start Date and Billing Group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("9. Deactivate NC subscription");
        CareTestBase.deactivateSubscription(NC2Subscription);

        test.get().info("10. Reload customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("11. Verify NC Subscription status is inactive");
        CareTestBase.verifySubscriptionStatus(NC2Subscription, "Inactive");

        test.get().info("12. Submit get account detail request by subscription number");
        SWSActions swsActions = new SWSActions();
        String getAccountSummaryRequest = "src\\test\\resources\\xml\\sws\\getaccount\\Get_Account_Summary_Request.xml";
        Xml response = swsActions.submitAccountSummaryWithFlagRequest(getAccountSummaryRequest, customerNumber, "false");

        test.get().info("13. Build Expected Account Summary Response Data");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getaccount\\TC32124_response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String expectedResponseFile = selfCareWSTestBase.buildResponseData(sampleResponseFile, newStartDate, TimeStamp.TodayPlus1Month(), customerNumber, subscriptionNumberList);

        test.get().info("14. Verify Get Account Summary Response");
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
