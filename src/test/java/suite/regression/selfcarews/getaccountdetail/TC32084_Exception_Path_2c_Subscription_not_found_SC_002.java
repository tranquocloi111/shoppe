package suite.regression.selfcarews.getaccountdetail;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;

/**
 * User: Nhi Dinh
 * Date: 17/09/2019
 */
public class TC32084_Exception_Path_2c_Subscription_not_found_SC_002 extends BaseTest {
    @Test(enabled = true, description = "TC32084_Exception Path 2c Subscription not found SC 002", groups = "SelfCareWS.GetAccountDetail")
    public void TC32084_Exception_Path_2c_Subscription_not_found_SC_002(){
        String invalidSubscription= "125698745200";

        test.get().info("Submit get account detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountDetailsBySubsRequest(invalidSubscription);

        test.get().info("Verify fault response data");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }


    private ErrorResponseEntity buildFaultResponse(){
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
