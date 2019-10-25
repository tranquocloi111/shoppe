package suite.regression.selfcarews.getsubscriptionauthentication;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;

/**
 * User: Nhi Dinh
 * Date: 10/10/2019
 */
public class TC32318_Self_Care_WS_Subscription_Invalid_Alphanumeric_SC_001 extends BaseTest {

    @Test(enabled = true, description = "TC32318_Self_Care_WS_Subscription_Invalid_Alphanumeric_SC_001", groups = "SelfCareWS.GetSubscriptionAuthentication")
    public void TC32318_Self_Care_WS_Subscription_Invalid_Alphanumeric_SC_001() {
        test.get().info("1. Submit Get Subscription Authority request with invalid Subscription Number");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetSubscriptionAuthorityRequest("0788774716A");

        test.get().info("2. Verify self care WS fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }

    private ErrorResponseEntity buildFaultResponse() {
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_001");
        falseResponse.setCodeAttribute("SC_001");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setFaultString("Invalid Subscription Number");
        falseResponse.setDescription("Invalid Subscription Number");
        falseResponse.setExceptionMsg("Invalid Subscription Number");
        falseResponse.setExceptionCauseMsg("Invalid Subscription Number");

        return falseResponse;
    }
}
