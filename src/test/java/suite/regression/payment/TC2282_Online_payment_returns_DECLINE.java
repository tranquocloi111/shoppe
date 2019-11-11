package suite.regression.payment;

import framework.utils.Xml;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;

import java.io.File;

public class TC2282_Online_payment_returns_DECLINE extends BaseTest {
    Xml request = null;
    Xml response;

    @Test(enabled = true, description = "TC2282 online payment return DECLINE", groups = "Payment")
    public void TC2282_Online_payment_returns_DECLINE() {
        String path = "src\\test\\resources\\xml\\payment\\TC1987_request";
        OWSActions owsActions = new OWSActions();
        String expireDate = "01" + Parser.parseDateFormate(TimeStamp.TodayPlus4Years(), TimeStamp.DATE_FORMAT7);
        owsActions.buildPaymentCreateOrder(expireDate, path);
        response = owsActions.submitTheRequest();
        verifyTheDeclineRespone();

    }


    private void verifyTheDeclineRespone() {
        Assert.assertEquals("12345", response.getTextByTagName("orderRef"));
        Assert.assertEquals("0", response.getTextByTagName("responseCode"));
        Assert.assertTrue( response.getTextByTagName("orderId")!="");
        Assert.assertEquals("CO_230", response.getTextByTagName("errorCode"));
        Assert.assertEquals("Payment Failure - Declined, please retry", response.getTextByTagName("errorDescription"));
        Assert.assertEquals("ERROR", response.getTextByTagName("errorType"));
        Assert.assertEquals("51", response.getTextByTagNameAndIndex("errorDetail",1));
        Assert.assertEquals("DECLINE", response.getTextByTagNameAndIndex("errorDetail",0));
    }

}
