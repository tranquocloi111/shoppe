package suite.regression.selfcarews.maintainpayment;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

public class TC32712_Basic_Path_Adhoc_Payment_non_account_holder_Address_line_1_invalid_SC_999_or_SC_032 extends BaseTest {
    String customerNumber;
    String financialTransactionContentRef;

    @Test(enabled = true, description = "TC32712 basic path adhoc payment non account holder address line 1 invalid SC 999 or SC032", groups = "SelfCareWS.Payment")
    public void TC32712_Basic_Path_Adhoc_Payment_non_account_holder_Address_line_1_invalid_SC_999_or_SC_032() {
        //-----------------------------------------
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_of_SB_and_sim_only";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : load customer in hub net ");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3 : Build maintain payment detail request ");
         path = "src\\test\\resources\\xml\\sws\\maintainpayment\\TC32712_request";
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
        falseResponse.setFaultCode("SC_999");
        falseResponse.setFaultString("Error processing request");
        falseResponse.setCodeAttribute("SC_999");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setDescription("Error processing request");
        falseResponse.setExceptionMsg("Error processing request");
        falseResponse.setExceptionCauseMsg("Error processing request");

        return falseResponse;
    }

}




