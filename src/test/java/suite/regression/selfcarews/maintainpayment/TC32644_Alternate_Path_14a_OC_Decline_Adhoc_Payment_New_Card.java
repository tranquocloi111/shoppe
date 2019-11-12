package suite.regression.selfcarews.maintainpayment;

import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.entities.MaintainContactResponseData;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.SelfCareSettingContentPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

public class TC32644_Alternate_Path_14a_OC_Decline_Adhoc_Payment_New_Card extends BaseTest {
  String customerNumber;


    @Test(enabled = true, description = "TC32644 selfcare ws maintain contact only user name provided sms", groups = "SelfCareWS.Payment")
    public void TC32644_Alternate_Path_14a_OC_Decline_Adhoc_Payment_New_Card() {
        //-----------------------------------------

        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_one_bundle_and_sim_only";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : Build maintain payment detail request ");
        path = "src\\test\\resources\\xml\\sws\\maintainpayment\\TC32644_request";
        SWSActions swsActions = new SWSActions();
        String endDate= "01"+Parser.parseDateFormate(TimeStamp.TodayPlus4Years(),TimeStamp.DATE_FORMAT7);
        swsActions.buildPaymentDetailRequest(endDate,customerNumber, path);

        test.get().info("Step 3:  submit the request to webservice");
        Xml response= swsActions.submitTheRequest();

        test.get().info("Step 4: verify maintain payment response");
        Assert.assertEquals(response.getTextByTagName("responseCode"),"1");
        Assert.assertEquals(response.getTextByTagName("action"),"ADHOC_PAYMENT");
        Assert.assertEquals(response.getTextByTagName("accountNumber"),customerNumber);
        Assert.assertEquals(response.getTextByTagName("message"),"ReD gateway denied payment (status: DENY)");



    }


}


