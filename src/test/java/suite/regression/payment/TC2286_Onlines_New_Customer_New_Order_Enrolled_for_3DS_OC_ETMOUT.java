package suite.regression.payment;

import framework.config.Config;
import framework.utils.Xml;
import javafx.fxml.FXML;
import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.entities.PaymentGatewayEnity;
import logic.business.entities.PaymentGatewayRespondEnity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.Hansen3DSecurePage;
import logic.pages.selfcare.Test3DSecurePage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TC2286_Onlines_New_Customer_New_Order_Enrolled_for_3DS_OC_ETMOUT extends BaseTest {

    String orderId = null;
    String firsName = null;
    Xml respond;
    String url;

    @Test(enabled = true, description = "TC2286 onlines new customer new order enrolled for 3DS OC ETMOUT", groups = "Payment")
    public void TC2286_Onlines_New_Customer_New_Order_Enrolled_for_3DS_OC_ETMOUT() {
        test.get().info("Step 1 : send create order request");
        String filePath = "src\\test\\resources\\xml\\payment\\TC2286_createOrder";
        OWSActions owsActions = new OWSActions();
        respond = owsActions.submitRequestAndReturnRespond(filePath);
        orderId = respond.getTextByTagName("orderId");
        firsName = owsActions.firstName;

        test.get().info("Step 2 : verify create order response");
        Assert.assertEquals("CO_280", respond.getTextByTagName("errorCode"));

        test.get().info("Step 3 :accept term and condition for order");
        respond = owsActions.acceptTermsAndContionsForOrder();

        test.get().info("Step 4 :Verify response return with 3D secure require  info");
        verifyResponseReturnWith3DSecureRequiredInfo();
        String orderId = respond.getTextByTagName("orderId");

        test.get().info("Step 5 :open the 3D secure mock site");
        String secureUrl = Config.getProp("owsUrl").replace("order/orderService", "3DSecureShoppingSiteMock");
        loadPageByUrl(secureUrl);
        Hansen3DSecurePage.getInstance().inputSecureUrl(url);
        Hansen3DSecurePage.getInstance().clickSubmitBtn();

        test.get().info("Step 6 :input 3D secure URL come from response then click submit button");
        Hansen3DSecurePage.getInstance().switchFrameByName("issuer");
        Hansen3DSecurePage.getInstance().enter3DPassword("1234");
        Hansen3DSecurePage.getInstance().clickVerifyMe();

        test.get().info("Step 7:verify shipping site fail page is displayed");
        Assert.assertEquals("Shopping site FAIL page.", Hansen3DSecurePage.getInstance().getFailMessage());
        Assert.assertTrue(Hansen3DSecurePage.getInstance().getFailMessage() != "");
        Assert.assertEquals("CO_250", Hansen3DSecurePage.getInstance().getFailState());

        test.get().info("Step 8 :verify account has not bên created in hub net");
        CareTestBase.page().verifyCustomerIsNotExistInHubNet(orderId);

        test.get().info("Step 9 : send get order request");
        respond = owsActions.getOrder(orderId);

        test.get().info("Step 10 : verify response does not contain payment received event");
        Assert.assertEquals(0, respond.countAllElementsPresenceInXml("//orderEvent//@eventType", "eventType", "10 Payment Received"));

        test.get().info("Step 11 :Verify payment gateway request for order in OE db");
        List<PaymentGatewayEnity> result = BillingActions.getPaymentGatewayRequestForOrderInOEDb(orderId);
        List<String> requestID = verifyPaymentGateWay(result);

        test.get().info("Step 12 :Verify payment gateway respond for order in OE db");
        List<PaymentGatewayRespondEnity> respondResult = BillingActions.getPaymentGatewayRespondForOrderInOEDb(requestID);
        verifyPaymentGateWayRespond(respondResult);

    }


    public void verifyResponseReturnWith3DSecureRequiredInfo() {
        Assert.assertEquals("5", respond.getTextByTagName("responseCode"));
        Assert.assertEquals("3DS Required to complete payment", respond.getTextByTagName("responseMessage"));
        url = respond.getTextByTagName("URL");
        Assert.assertTrue(url.startsWith("http"));
        Assert.assertTrue(url.contains("/securepayment/"));
        Assert.assertEquals(orderId, respond.getTextByTagName("orderId"));
        Assert.assertEquals("CO_275", respond.getTextByTagName("errorCode"));
        Assert.assertEquals("INFO", respond.getTextByTagName("errorType"));
        Assert.assertEquals("Confirmation Required - 3D Secure Required", respond.getTextByTagName("errorDescription"));
        Assert.assertEquals("00", respond.getTextByTagNameAndIndex("errorDetail", 1));
        Assert.assertEquals("PAREQ", respond.getTextByTagNameAndIndex("errorDetail", 0));
    }

    public List<String> verifyPaymentGateWay(List<PaymentGatewayEnity> result) {
        List<String> requestId = new ArrayList<>();
        Assert.assertEquals(result.size(), 4);
        Assert.assertEquals(BillingActions.findPayemtGateWay(result, "OX", "ORDER", "Onlines"), 1);
        Assert.assertEquals(BillingActions.findPayemtGateWay(result, "PA", "ORDER", "Onlines"), 1);
        Assert.assertEquals(BillingActions.findPayemtGateWay(result, "OC", "ORDER", "Onlines"), 1);
        Assert.assertEquals(BillingActions.findPayemtGateWay(result, "PE", "ORDER", "Onlines"), 1);
        for (int i = 0; i < result.size(); i++) {
            requestId.add(result.get(i).getPaymentGTWRequestID());
        }
        result = BillingActions.getPaymentGatewayRequestForUserNameOEDb(firsName);

        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(BillingActions.findPayemtGateWay(result, "OA", "ORDER", "Onlines"), 1);
        requestId.add(result.get(0).getPaymentGTWRequestID());
        return requestId;
    }

    public void verifyPaymentGateWayRespond(List<PaymentGatewayRespondEnity> result) {
        Assert.assertEquals(result.size(), 5);
        Assert.assertEquals(BillingActions.findPaymentGateWayRespond(result, "PE", "APPROVE", "PAREQ", "00", "ENROLLED"), 1);
        Assert.assertEquals(BillingActions.findPaymentGateWayRespond(result, "PA", "APPROVE", "SUCCESS", "00", "CARD_SIGN_OK"), 1);
        Assert.assertEquals(BillingActions.findPayemtGateWayRespondByBankStatusIsNotEmpty(result, "OC", "TIMEOUT", "ETMOUT", "ACCEPT"), 1);
        Assert.assertEquals(BillingActions.findPaymentGateWayRespondByTokenStatus(result, "OA", "APPROVE", "APPROVE", "00", "ACTIVE"), 1);
        Assert.assertEquals(BillingActions.findPaymentGateWayRespondByGateWayStatusIsNull(result, "OX", "ERROR", "00"), 1);
    }

}
