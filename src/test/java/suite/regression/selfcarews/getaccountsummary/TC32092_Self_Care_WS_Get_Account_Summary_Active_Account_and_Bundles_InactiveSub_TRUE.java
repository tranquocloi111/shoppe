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
 * Date: 18/09/2019
 */
public class TC32092_Self_Care_WS_Get_Account_Summary_Active_Account_and_Bundles_InactiveSub_TRUE extends BaseTest {
    Date newStartDate = TimeStamp.TodayMinus20Days();
    private List<String> subscriptionNumberList = new ArrayList<>();
    String FCSubscription;
    String NCSubscription;

    @Test(enabled = true, description = "TC32091_Active Account with 2 Subscriptions having Perk Gain Alert Inactive Subscription Flag TRUE", groups = "SelfCareWS.GetAccountSummary")
    public void TC32092_Self_Care_WS_Get_Account_Summary_Active_Account_and_Bundles_InactiveSub_TRUE(){
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWith2SubscriptionOrder();
        String customerNumber = owsActions.customerNo;
        //========================================================================
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
        //=============================================================================
        String deactivateReason = "Customer Cancelled";

        test.get().info("Deactivate NC subscription");
        CareTestBase.deactivateSubscription(NCSubscription + "  Mobile NC");

        test.get().info("Verify NC Subscription status is inactive");
        CareTestBase.verifySubscriptionStatus(NCSubscription, "Inactive");

        test.get().info("Submit Get Account Summary Request to SelfCare WS");
        SWSActions swsActions = new SWSActions();
        String getAccountSummaryRequest = "src\\test\\resources\\xml\\sws\\getaccount\\Get_Account_Summary_Admin_Request.xml";
        Xml response = swsActions.submitAccountSummaryWithFlagRequest(getAccountSummaryRequest, customerNumber, "true");

        test.get().info("Build Expected Account Summary Response Data");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getaccount\\TC32092_response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String expectedResponseFile = selfCareWSTestBase.buildResponseData(sampleResponseFile, newStartDate,TimeStamp.Today(), TimeStamp.TodayPlus1Month(), customerNumber, subscriptionNumberList);

        test.get().info("Verify Get Account Summary Response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponseFile, response);

    }

    private void getAllSubscriptionNumber(){
        subscriptionNumberList = CareTestBase.getAllSubscription();
        for (String subscription : subscriptionNumberList) {
            if (subscription.endsWith("Mobile FC")) {
                FCSubscription = subscription.split(" ")[0];
            } else if (subscription.endsWith("Mobile NC")) {
                NCSubscription = subscription.split(" ")[0];
            }
        }
    }
}
