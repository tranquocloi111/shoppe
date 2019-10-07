package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;

/**
 * User: Nhi Dinh
 * Date: 1/10/2019
 */
public class TC32155_Exception_Path_2d_Subscription_Number_not_valid_SC_001 extends BaseTest {

    @Test(enabled = true, description = "TC32155_Exception_Path_2d_Subscription_Number_not_valid_SC_001", groups = "SelfCareWS")
    public void TC32155_Exception_Path_2d_Subscription_Number_not_valid_SC_001(){
        test.get().info("2. Submit get account summary request with invalid Subscription Number");
        String invalidSubscriptionNumber = "12345#3321";
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountSummaryWithSubsRequest(invalidSubscriptionNumber);

        test.get().info("3. Verify the false Response Data");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }

    private ErrorResponseEntity buildFaultResponse(){
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
