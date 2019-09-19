package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;

/**
 * User: Nhi Dinh
 * Date: 16/09/2019
 */
public class TC32046_Alternative_Path_1a_Deactivated_Subscription_Flexible_Cap extends BaseTest {

    @Test(enabled = true, description = "TC32046_Alternative Path 1a Deactivated Subscription Flexible Cap", groups = "SelfCareWS")
    public void TC32046_Alternative_Path_1a_Deactivated_Subscription_Flexible_Cap(){
        String customerNumber =  "6003";
        String subscriptionNumber = "07508336130";

        test.get().info("Submit get account detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountDetailsRequest(subscriptionNumber);

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
}
