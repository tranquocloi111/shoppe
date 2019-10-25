package suite.regression.selfcarews.getinvoice;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;

/**
 * User: Nhi Dinh
 * Date: 8/10/2019
 */
public class TC32257_Self_Care_WS_Exception_Path_Get_Invoice_No_Invoice extends BaseTest {

    @Test(enabled = true, description = "TC32257_Self_Care_WS_Exception_Path_Get_Invoice_No_Invoice", groups = "SelfCareWS.GetInvoice")
    public void TC32257_Self_Care_WS_Exception_Path_Get_Invoice_No_Invoice() {

        test.get().info("1. Create an onlines CC Customer with FC 1 bundle of SB and sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
        String customerNumber = owsActions.customerNo;

        test.get().info("13. Submit get invoice request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetInvoiceRequest(customerNumber);

        test.get().info("14. Verify self care WS fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }

    private ErrorResponseEntity buildFaultResponse() {
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_009");
        falseResponse.setCodeAttribute("SC_009");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setFaultString("Invoice not found");
        falseResponse.setDescription("Invoice not found");
        falseResponse.setExceptionMsg("Invoice not found");
        falseResponse.setExceptionCauseMsg("Invoice not found");

        return falseResponse;
    }
}
