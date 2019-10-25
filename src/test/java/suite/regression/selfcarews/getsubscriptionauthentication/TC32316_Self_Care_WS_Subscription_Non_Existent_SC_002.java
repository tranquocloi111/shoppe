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
public class TC32316_Self_Care_WS_Subscription_Non_Existent_SC_002 extends BaseTest {

    @Test(enabled = true, description = "TC32316_Self_Care_WS_Subscription_Non_Existent_SC_002", groups = "SelfCareWS.GetSubscriptionAuthentication")
    public void TC32316_Self_Care_WS_Subscription_Non_Existent_SC_002() {
        test.get().info("1. Submit Get Subscription Authority request with invalid Subscription Number");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetSubscriptionAuthorityRequest("0");

        test.get().info("2. Verify self care WS fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }

    private ErrorResponseEntity buildFaultResponse() {
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_002");
        falseResponse.setCodeAttribute("SC_002");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setFaultString("Subscription Number not found");
        falseResponse.setDescription("Subscription Number not found");
        falseResponse.setExceptionMsg("Subscription Number not found");
        falseResponse.setExceptionCauseMsg("Subscription Number not found");

        return falseResponse;
    }
}
