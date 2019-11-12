package suite.regression.selfcarews.maintaincontact;

import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

public class TC32636_Self_Care_WS_Maintain_contact_use_update_contact_action_with_invalid_username_SC_038 extends BaseTest {
    String serviceOrderID;


    @Test(enabled = true, description = "TC32636 selfcare ws maintain contact use update contact action with invalid username sc 038", groups = "SelfCareWS.MaintainContact")
    public void TC32636_Self_Care_WS_Miantain_contact_use_update_contact_action_with_invalid_username_SC_038() {

        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_one_bundle_and_sim_only";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;

       String invalidUserName= String.format("NewName%s@hsntech.com", RandomCharacter.getRandomNumericString(9));

        test.get().info("Step 2: Build maintain contact detail request ");
        path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC3518_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildForgottenPasswordRequest(invalidUserName,customerNumber, path);

        test.get().info("Step 3:  submit the request to webservice");
        Xml response= swsActions.submitTheRequest();


        test.get().info("Step 4:  verify selfcare ws fault response");
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


