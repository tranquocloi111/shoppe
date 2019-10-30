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


    @Test(enabled = true, description = "TC32636 selfcare ws maintain contact use update contact action with invalid username sc 038", groups = "SelfCare")
    public void TC32638_Self_Care_WS_New_Username_does_match_to_another_customer_account_username() {

        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWithOrder();
        String customerNumber = owsActions.customerNo;
        String firtUserName = owsActions.username;

        test.get().info("Step 1 : create another cc customer with order");
        OWSActions owsActions1 = new OWSActions();
        owsActions1.createACCCustomerWithOrder();


        test.get().info("Step 1 : Build maintain contact detail request ");
        String  path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC3526_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildContactDetailRequest(firtUserName,owsActions1.username,customerNumber, path);

        test.get().info("Step 1  submit the request to webservice");
        Xml response= swsActions.submitTheRequest();


        test.get().info("Step 1  verify selfcare ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());




    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_040");
        falseResponse.setFaultString("New Username already in use");
        falseResponse.setCodeAttribute("SC_040");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setDescription("New Username already in use");
        falseResponse.setExceptionMsg("New Username already in use");
        falseResponse.setExceptionCauseMsg("New Username already in use");

        return falseResponse;
    }

}


