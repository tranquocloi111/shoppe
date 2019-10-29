package suite.regression.selfcarews.maintaincontact;

import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;

public class TC32639_Self_Care_WS_No_New_Email_address_or_New_Username_are_provided extends BaseTest {
    String serviceOrderID;


//    @Test(enabled = true, description = "TC32636 selfcare ws maintain contact use update contact action with invalid username sc 038", groups = "SelfCare")
    public void TC32638_Self_Care_WS_New_Username_does_match_to_another_customer_account_username() {


        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWithOrder();
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 1 : Load user in the hub net");
       String invalidUserName= String.format("NewName%s@hsntech.com", RandomCharacter.getRandomNumericString(9));


        test.get().info("Step 1 : Build maintain contact detail request ");
        String path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC3522_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildMaintainContactRequest(invalidUserName, path);

        test.get().info("Step 1  submit the request to webservice");
        Xml response= swsActions.submitTheRequest();


        test.get().info("Step 1  verify selfcare ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());




    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_042");
        falseResponse.setFaultString("No update details provided");
        falseResponse.setCodeAttribute("SC_042");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setDescription("No update details provided");
        falseResponse.setExceptionMsg("No update details provided");
        falseResponse.setExceptionCauseMsg("No update details provided");

        return falseResponse;
    }

}


