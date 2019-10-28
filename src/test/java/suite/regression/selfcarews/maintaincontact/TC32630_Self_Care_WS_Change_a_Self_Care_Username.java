package suite.regression.selfcarews.maintaincontact;

import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.entities.MaintainContactResponseData;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.SelfCareSettingContentPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

public class TC32630_Self_Care_WS_Change_a_Self_Care_Username extends BaseTest {
  String customerNumber;


//    @Test(enabled = true, description = "TC32631 selfcare ws maintain contact only user name provided sms", groups = "SelfCare")
    public void TC32630_Self_Care_WS_Change_a_Self_Care_Username() {

        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWithOrder();
        customerNumber = owsActions.customerNo;
        String firstUserName = owsActions.username;

        test.get().info("Step 1 : Create a second customer ");
        owsActions.createGeneralCustomerOrder("src\\test\\resources\\xml\\ows\\onlines_CC_customer_with_order.xml",firstUserName);
        customerNumber = owsActions.customerNo;
        String anotherUserName = owsActions.username;

        test.get().info("Step 1 : Load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 1 :verify customer email address and username");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(owsActions.email, DetailsContentPage.AddressInformationPage.getInstance().getEmail());
        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();
        Assert.assertEquals(SelfCareSettingContentPage.SelfCareSettingSection.getInstance().getUserName(),owsActions.username);

        test.get().info("Step 1 :verify bill type is summary");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("Summary", DetailsContentPage.BillingInformationSectionPage.getInstance().getBillStyle());

        test.get().info("Step 1 : Build maintain contact detail request ");
        String path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC3526_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildContactDetailRequest(firstUserName,anotherUserName,customerNumber, path);

        test.get().info("Step 1  submit the request to webservice");
        Xml response= swsActions.submitTheRequest();


        test.get().info("Step 1  verify selfcare ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());




    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_040");
        falseResponse.setFaultString("New Username already in use");
        falseResponse.setCodeAttribute("SC_040");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setDescription("New Username already in use");
        falseResponse.setExceptionMsg("New Username already in use");
        falseResponse.setExceptionCauseMsg("New Username already in use");

        return falseResponse;
    }


}


