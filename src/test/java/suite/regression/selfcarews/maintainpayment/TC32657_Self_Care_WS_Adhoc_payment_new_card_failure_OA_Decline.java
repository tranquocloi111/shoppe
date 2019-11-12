package suite.regression.selfcarews.maintainpayment;

import framework.utils.Xml;
import logic.business.entities.FinancialTransactionEnity;
import logic.business.entities.selfcare.MaintainPaymentResponseData;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.FinancialTransactionPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.util.HashMap;

public class TC32657_Self_Care_WS_Adhoc_payment_new_card_failure_OA_Decline extends BaseTest {
    String customerNumber;


    @Test(enabled = true, description = "TC32657 selfcare WS adhoc payment new card failure QA decline", groups = "SelfCareWS.Payment")
    public void TC32657_Self_Care_WS_Adhoc_payment_new_card_failure_OA_Decline() {
        //-----------------------------------------
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_one_bundle_and_sim_only";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo,"Mobile FC");
        String sub= owsActions.serviceRef;


        test.get().info("Step 2 : load customer in hub net ");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3 : Build maintain payment detail request ");
        //Here we give cvv number as 222 which means this card is not authorised.
        path = "src\\test\\resources\\xml\\sws\\maintainpayment\\TC2849_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildPaymentDetailRequestWithSubscriptionNumber(sub,customerNumber, path);

        test.get().info("Step 4  submit the request to webservice");
        Xml response= swsActions.submitTheRequest();

        test.get().info("Step 5  verify maintain payment response");
        Assert.assertEquals(response.getTextByTagName("accountNumber"),customerNumber);
        Assert.assertEquals(response.getTextByTagName("action"),"ADHOC_PAYMENT");
        Assert.assertEquals(response.getTextByTagName("responseCode"),"1");
        Assert.assertEquals(response.getTextByTagName("message"),"Red Authentication failed (status: DENY)");

    }

}


