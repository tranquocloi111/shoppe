package suite.regression.selfcarews.getinvoicehistory;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;

/**
 * User: Nhi Dinh
 * Date: 10/10/2019
 */
public class TC32304_Self_Care_WS_Get_Invoice_History_Account_Not_found_SC_004 extends BaseTest {

    @Test(enabled = true, description = "TC32304_Self_Care_WS_Get_Invoice_History_Account_Not_found_SC_004", groups = "SelfCareWS.GetInvoiceHistory")
    public void TC32304_Self_Care_WS_Get_Invoice_History_Account_Not_found_SC_004() {
        test.get().info("1. Submit Get invoice history request with invalid CustomerNumber");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetInvoiceHistoryByAccountNoRequest("0");

        test.get().info("2. Verify self care WS fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }

    private ErrorResponseEntity buildFaultResponse() {
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
