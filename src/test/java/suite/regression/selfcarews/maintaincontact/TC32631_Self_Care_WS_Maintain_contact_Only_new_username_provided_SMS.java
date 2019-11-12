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
import suite.regression.selfcare.SelfCareTestBase;

public class TC32631_Self_Care_WS_Maintain_contact_Only_new_username_provided_SMS extends BaseTest {
  String customerNumber;


    @Test(enabled = true, description = "TC32631 selfcare ws maintain contact only user name provided sms", groups = "SelfCareWS.MaintainContact")
    public void TC32631_Self_Care_WS_Miantain_contact_Only_new_username_provided_SMS() {
        //-----------------------------------------
        //Create an customer account with default values for new customer
        //Customer just need 1 FC subscription with sim-only, do not need any discount bundle actually
        //The default bill notification is SMS
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_one_bundle_and_sim_only";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;
        String newUserName = String.format("NewName%s@hsntech.com", RandomCharacter.getRandomNumericString(9));

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
        path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC3533_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildContactDetailRequest(owsActions.username,newUserName,customerNumber, path);

        test.get().info("Step 1  submit the request to webservice");
        Xml response= swsActions.submitTheRequest();


        test.get().info("Step 1  verify maintain contact response");
        Assert.assertEquals(response.getTextByTagName("message"),normalMaintainContactReponseData().getMessage());
        Assert.assertEquals(response.getTextByTagName("responseCode"),normalMaintainContactReponseData().getResponseCode());
        Assert.assertEquals(response.getTextByTagName("accountNumber"),normalMaintainContactReponseData().getAccountNumber());

        test.get().info("Step 1  verify only customer user name is updated and email address not change");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(owsActions.email, DetailsContentPage.AddressInformationPage.getInstance().getEmail());
        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();
        Assert.assertEquals(SelfCareSettingContentPage.SelfCareSettingSection.getInstance().getUserName(),newUserName);



    }

    private MaintainContactResponseData normalMaintainContactReponseData(){
        MaintainContactResponseData response = new MaintainContactResponseData();
        response.setAccountNumber(customerNumber);
        response.setMessage("");
        response.setResponseCode("0");

        return response;
    }

}


