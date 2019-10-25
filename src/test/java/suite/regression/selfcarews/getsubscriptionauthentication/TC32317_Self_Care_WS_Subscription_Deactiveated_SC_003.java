package suite.regression.selfcarews.getsubscriptionauthentication;

import framework.utils.Xml;
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
 * Date: 10/10/2019
 */
public class TC32317_Self_Care_WS_Subscription_Deactiveated_SC_003 extends BaseTest {
    Date newStartDate = TimeStamp.TodayMinus20Days();
    String FCSubscription;
    String NCSubscription;
    private List<String> subscriptionNumberList = new ArrayList<>();

    @Test(enabled = true, description = "TC32317_Self_Care_WS_Subscription_Deactiveated_SC_003", groups = "SelfCareWS.GetSubscriptionAuthentication")
    public void TC32317_Self_Care_WS_Subscription_Deactiveated_SC_003() {
        test.get().info("1 : Create a CC Customer with 2 subscriptions order");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWith2SubscriptionOrder();
        String customerNumber = owsActions.customerNo;

        test.get().info("2. Load Customer in Hub Net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("3. Get All Subscriptions Number");
        getAllSubscriptionNumber();

        test.get().info("4. Deactivate subscription NC and return to customer");
        CareTestBase.deactivateSubscription(NCSubscription + "  Mobile NC");

        test.get().info("5. Submit Get Subscription Authority request with invalid Subscription Number");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetSubscriptionAuthorityRequest(NCSubscription);

        test.get().info("6. Verify self care WS fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }

    private ErrorResponseEntity buildFaultResponse() {
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

    private void getAllSubscriptionNumber() {
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
