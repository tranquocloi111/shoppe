package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 27/08/2019
 */
public class TC3370_Self_Care_WS_Subscription_not_found_SC_029 extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();

    @Test(enabled = true, description = "TC3370_Self Care WS Subscription not found SC 029", groups = "SelfCareWS")
    public void TC3370_Self_Care_WS_Subscription_not_found_SC_029() {
        test.get().info("Step 1 : Create a CC Customer with FC 1 bundle and NK2720");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC1BundleAndNK2720();
        customerNumber = owsActions.customerNo;

        test.get().info("Submit get bundle request with invalid Subscription Number");
        String invalidSubscriptionNumber = "07398190821";
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetBundleRequest(customerNumber, invalidSubscriptionNumber );

        test.get().info("Verify the false Response Data");
        ErrorResponseEntity falseResponse = buildFalseResponseOfGetBundleWithInvalidSubscription();
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, falseResponse);

    }

    private ErrorResponseEntity buildFalseResponseOfGetBundleWithInvalidSubscription(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("ERROR");
        falseResponse.setFaultString("Validation Errors");
        falseResponse.setExceptionMsg("Validation Errors");
        falseResponse.setExceptionCauseMsg("Validation Errors");

        List<ErrorResponseEntity.SelfCareServiceMultiExceptionEntity> sCSMultiExceptionList = new ArrayList<>();
        ErrorResponseEntity.SelfCareServiceMultiExceptionEntity sCSMultiException = new ErrorResponseEntity.SelfCareServiceMultiExceptionEntity("SC_029",
                "Subscription does not belong to Account",
                "ERROR");
        sCSMultiExceptionList.add(sCSMultiException);

        falseResponse.setSCSMultiExceptionMessages(sCSMultiExceptionList);

        return falseResponse;
    }

}
