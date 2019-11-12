package suite.regression.selfcarews.maintainpayment;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

public class TC32843_Exception_Path_5a_Update_Direct_Debit_VOCA_validation_fail_SC_030 extends BaseTest {
    String customerNumber;

    @Test(enabled = true, description = "TC32743 exception path 5a update direct debit VOCA validation fail SC 030", groups = "SelfCareWS.Payment")
    public void TC32843_Exception_Path_5a_Update_Direct_Debit_VOCA_validation_fail_SC_030() {
        //-----------------------------------------
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_DD_customer_with_FC_2_bundles_and_NK2720";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : load customer in hub net ");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3 : Build maintain payment detail request ");
         path = "src\\test\\resources\\xml\\sws\\maintainpayment\\TC32843_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildPaymentDetailRequest( customerNumber, path);

        test.get().info("Step 4:  submit the request to webservice");
        Xml response = swsActions.submitTheRequest();

        test.get().info("Step 5:  verify selfcare ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());




    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_030");
        falseResponse.setFaultString("Bank account number and/or bank sort code are invalid");
        falseResponse.setCodeAttribute("SC_030");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setDescription("Bank account number and/or bank sort code are invalid");
        falseResponse.setExceptionMsg("Bank account number and/or bank sort code are invalid");
        falseResponse.setExceptionCauseMsg("Bank account number and/or bank sort code are invalid");

        return falseResponse;
    }

}




