package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.ErrorResponseEntity;
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
 * Date: 1/10/2019
 */
public class TC32156_Self_Care_WS_Get_Account_Summary_Inactive_Sub_number_SC_003 extends BaseTest {
    Date newStartDate = TimeStamp.TodayMinus20Days();
    private List<String> subscriptionNumberList = new ArrayList<>();
    String NC2Subscription;
    String FCSubscription;
    String NCSubscription;

    @Test(enabled = true, description = "TC32156_Self_Care_WS_Get_Account_Summary_Inactive_Sub_number_SC_003", groups = "SelfCareWS")
    public void TC32156_Self_Care_WS_Get_Account_Summary_Inactive_Sub_number_SC_003(){
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
        getAllSubscriptionNumber(owsActions);

        test.get().info("Verify Customer Start Date and Billing Group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Deactivate NC subscription");
        CareTestBase.deactivateSubscription(NC2Subscription);

        test.get().info("Reload customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Verify NC Subscription status is inactive");
        CareTestBase.verifySubscriptionStatus(NC2Subscription, "Inactive");

        test.get().info("Update Customer End Date");
        CommonActions.updateCustomerEndDate(customerNumber, TimeStamp.TodayMinus1Day());

        test.get().info("Submit get account summary request by inactive subscription number");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountSummaryWithSubsRequest(NC2Subscription);

        test.get().info("Verify fault response data");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());

    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_003");
        falseResponse.setCodeAttribute("SC_003");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setFaultString("Subscription Number not active");
        falseResponse.setDescription("Subscription Number not active");
        falseResponse.setExceptionMsg("Subscription Number not active");
        falseResponse.setExceptionCauseMsg("Subscription Number not active");

        return falseResponse;
    }


    private void getAllSubscriptionNumber(OWSActions owsActions){
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
