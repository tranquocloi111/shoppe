package suite.regression.selfcarews.getbundle;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 7/10/2019
 */
public class TC32204_Self_Care_WS_Invalid_External_System_ID_SC_007 extends BaseTest {
    private String subscriptionNumber;

    @Test(enabled = true, description = "TC32200_Self_Care_WS_Get_Bundle_Account_not_found_SC_004", groups = "SelfCareWS.GetBundle")
    public void TC32204_Self_Care_WS_Invalid_External_System_ID_SC_007() {
        test.get().info("1. Create an onlines CC Customer with FC 1 bundle of SB and sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
        String customerNumber = owsActions.customerNo;

        test.get().info("2. Login to HUBNet then search Customer by customer number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("3. Record latest subscription number for customer");
        subscriptionNumber = CareTestBase.page().recordLatestSubscriptionNumberForCustomer();
        //=============================================================================
        test.get().info("3. Submit get bundle request with invalid Invalid External System ID");
        SWSActions swsActions = new SWSActions();
        String requestFilePath = "src\\test\\resources\\xml\\sws\\getbundle\\Get_bundle_Invalid_ExternalSystemID_request.xml";
        Xml response = swsActions.submitGetByCustomerAndSubscriptionNumbersRequest(requestFilePath, customerNumber, subscriptionNumber);

        test.get().info("4. Verify self care ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponseData());

    }

    private ErrorResponseEntity buildFaultResponseData() {
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("ERROR");
        falseResponse.setFaultString("Validation Errors");
        falseResponse.setExceptionMsg("Validation Errors");
        falseResponse.setExceptionCauseMsg("Validation Errors");

        List<ErrorResponseEntity.SelfCareServiceMultiExceptionEntity> sCSMultiExceptionList = new ArrayList<>();
        ErrorResponseEntity.SelfCareServiceMultiExceptionEntity sCSMultiException = new ErrorResponseEntity.SelfCareServiceMultiExceptionEntity(
                "SC_007",
                "Invalid External System ID",
                "ERROR");
        sCSMultiExceptionList.add(sCSMultiException);

        falseResponse.setSCSMultiExceptionMessages(sCSMultiExceptionList);

        return falseResponse;
    }

}
