package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;

/**
 * User: Nhi Dinh
 * Date: 16/09/2019
 */
public class TC32035_Self_Care_WS_Get_Account_Auth_Correct_username_incorrect_password_GAA_000 extends BaseTest {
    @Test(enabled = true, description = "TC32035_Self Care WS Get Account Auth Correct username incorrect password GAA 000", groups = "SelfCareWS")
    public void TC32035_Self_Care_WS_Get_Account_Auth_Correct_username_incorrect_password_GAA_000(){
        test.get().info("Create an onlines CC customer with FC no bundle but has sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
        String username = owsActions.username;

        test.get().info("Submit get account authority request with invalid Password");
        SWSActions swsActions = new SWSActions();
        String getAccountAuthRequest = "src\\test\\resources\\xml\\sws\\getaccountauthority\\Get_Account_Auth_Request.xml";
        Xml response = swsActions.submitGetAccountAuthorityRequest(getAccountAuthRequest, username, "0");

        test.get().info("Verify fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("GAA_000");
        falseResponse.setFaultString("Username and password do not match");
        falseResponse.setDescription("Username and password do not match");
        falseResponse.setExceptionMsg("Username and password do not match");
        falseResponse.setExceptionCauseMsg("Username and password do not match");

        return falseResponse;
    }
}
