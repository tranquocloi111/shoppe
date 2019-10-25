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
public class TC32083_Self_Care_WS_Account_not_found extends BaseTest {
    @Test(enabled = true, description = "TC32083_Self Care WS Account not found", groups = "SelfCareWS.GetAccountDetail")
    public void TC32083_Self_Care_WS_Account_not_found(){
        String invalidAccountNumber= "0";

        test.get().info("Submit get account detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountDetailsRequest(invalidAccountNumber);

        test.get().info("Verify fault response data");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_004");
        falseResponse.setCodeAttribute("SC_004");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setFaultString("Account Number not found");
        falseResponse.setDescription("Account Number not found");
        falseResponse.setExceptionMsg("Account Number not found");
        falseResponse.setExceptionCauseMsg("Account Number not found");

        return falseResponse;
    }
}
