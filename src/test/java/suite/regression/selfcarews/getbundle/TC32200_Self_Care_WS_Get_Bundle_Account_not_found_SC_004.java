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
public class TC32200_Self_Care_WS_Get_Bundle_Account_not_found_SC_004 extends BaseTest {
    private String subscriptionNumber;

    @Test(enabled = true, description = "TC32200_Self_Care_WS_Get_Bundle_Account_not_found_SC_004", groups = "SelfCareWS.GetBundle")
    public void TC32200_Self_Care_WS_Get_Bundle_Account_not_found_SC_004() {
        test.get().info("1. Create an onlines CC Customer with FC 1 bundle of SB and sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
        String customerNumber = owsActions.customerNo;

        test.get().info("2. Login to HUBNet then search Customer by customer number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("3. Record latest subscription number for customer");
        subscriptionNumber = CareTestBase.page().recordLatestSubscriptionNumberForCustomer();
        //=============================================================================
        test.get().info("3. Submit get bundle request with invalid customer number");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetBundleRequest("0", subscriptionNumber);

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

        ErrorResponseEntity.SelfCareServiceMultiExceptionEntity sCSMultiException_1 = new ErrorResponseEntity.SelfCareServiceMultiExceptionEntity(
                "SC_029",
                "Subscription does not belong to Account",
                "ERROR");
        ErrorResponseEntity.SelfCareServiceMultiExceptionEntity sCSMultiException_2 = new ErrorResponseEntity.SelfCareServiceMultiExceptionEntity(
                "SC_004",
                "Account Number not found",
                "ERROR");
        sCSMultiExceptionList.add(sCSMultiException_1);
        sCSMultiExceptionList.add(sCSMultiException_2);

        falseResponse.setSCSMultiExceptionMessages(sCSMultiExceptionList);

        return falseResponse;
    }

}
