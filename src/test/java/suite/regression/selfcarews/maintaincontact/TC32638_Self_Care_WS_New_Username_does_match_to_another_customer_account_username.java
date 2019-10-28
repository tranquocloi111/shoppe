package suite.regression.selfcarews.maintaincontact;

import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;

public class TC32638_Self_Care_WS_New_Username_does_match_to_another_customer_account_username extends BaseTest {
    String serviceOrderID;


//    @Test(enabled = true, description = "TC32636 selfcare ws maintain contact use update contact action with invalid username sc 038", groups = "SelfCare")
    public void TC32638_Self_Care_WS_New_Username_does_match_to_another_customer_account_username() {

        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_one_bundle_and_sim_only";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 1 : Load user in the hub net");
       String invalidUserName= String.format("NewName%s@hsntech.com", RandomCharacter.getRandomNumericString(9));


        test.get().info("Step 1 : Build maintain contact detail request ");
        path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC3518_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildForgottenPasswordRequest(invalidUserName, path);

        test.get().info("Step 1  submit the request to webservice");
        Xml response= swsActions.submitTheRequest();


        test.get().info("Step 1  verify selfcare ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());




    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_038");
        falseResponse.setFaultString("Invalid Username");
        falseResponse.setCodeAttribute("SC_038");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setDescription("Invalid Username");
        falseResponse.setExceptionMsg("Invalid Username");
        falseResponse.setExceptionCauseMsg("Invalid Username");

        return falseResponse;
    }

}


