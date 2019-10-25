package suite.regression.selfcarews.getaccountsummary;

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
public class TC32153_Self_Care_WS_Get_Account_Summary_Account_does_not_exist_SC_004 extends BaseTest {
    @Test(enabled = true, description = "TC32153_Self_Care_WS_Get_Account_Summary_Account_does_not_exist_SC_004", groups = "SelfCareWS.GetAccountSummary")
    public void TC32153_Self_Care_WS_Get_Account_Summary_Account_does_not_exist_SC_004(){
        String invalidAccountNumber= "0";

        test.get().info("Submit get account detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountDetailsRequestWithFlag(invalidAccountNumber, "true");

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
