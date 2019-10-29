package suite.regression.selfcarews.maintainpayment;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

public class TC32690_Self_Care_WS_Invalid_Card_Number_SC_019 extends BaseTest {
    String customerNumber;
    String financialTransactionContentRef;

    @Test(enabled = true, description = "TC32670 basic path adhoc payment existing card existing card match", groups = "SelfCare")
    public void TC32670_Basic_Path_Adhoc_Payment_Existing_Card_Existing_Card_Match() {
        //-----------------------------------------
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWithOrder();
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : load customer in hub net ");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3 : Build maintain payment detail request ");
        String path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC2875_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildPaymentDetailRequest( customerNumber, path);

        test.get().info("Step 4  submit the request to webservice");
        Xml response = swsActions.submitTheRequest();

        test.get().info("Step 1  verify selfcare ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());




    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_019");
        falseResponse.setFaultString("Invalid card number");
        falseResponse.setCodeAttribute("SC_019");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setDescription("Invalid card number");
        falseResponse.setExceptionMsg("Invalid card number");
        falseResponse.setExceptionCauseMsg("Invalid card number");

        return falseResponse;
    }

}




