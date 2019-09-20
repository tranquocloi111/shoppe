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
public class TC32041_Self_Care_WS_Insufficient_Access_Rights_Default_External_System_ID extends BaseTest {
    @Test(enabled = true, description = "TC32041_Self_Care_WS_Insufficient_Access_Rights_Default_External_System_ID", groups = "SelfCareWS")
    public void TC32041_Self_Care_WS_Insufficient_Access_Rights_Default_External_System_ID() {
        test.get().info("Create an onlines CC customer with FC 1 bundle and NK2720");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC1BundleAndNK2720();
        String username = owsActions.username;
        String password = owsActions.password;

        test.get().info("Submit get account authority request with invalid Password");
        SWSActions swsActions = new SWSActions();
        String getAccountAuthRequest = "src\\test\\resources\\xml\\sws\\getaccountauthority\\TC32041_Request.xml";
        Xml response = swsActions.submitGetAccountAuthorityRequest(getAccountAuthRequest, password, username);

        test.get().info("Verify fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_036");
        falseResponse.setFaultString("Insufficient Access Rights NONE for this service.");
        falseResponse.setCodeAttribute("SC_036");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setDescription("Insufficient Access Rights NONE for this service.");
//        falseResponse.setExceptionMsg("Insufficient Access Rights NONE for this service.");
//        falseResponse.setExceptionCauseMsg("Insufficient Access Rights NONE for this service.");

        return falseResponse;
    }
}
