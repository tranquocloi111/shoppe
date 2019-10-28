package suite.regression.selfcarews.maintaincontact;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

public class TC32620_Self_Care_WS_Exception_Path_Maintain_Contact_Invalid_Security_Answer_SC_014 extends BaseTest {
    String serviceOrderID;
    String currentPWD;

//    @Test(enabled = true, description = "TC32628 Selfcare webservice exception path maintain contact invalid security answer sc 014", groups = "SelfCare")
    public void TC32620_Self_Care_WS_Exception_Path_Maintain_Contact_Invalid_Security_Answer_SC_014() {

        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 1 : Load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 1 update bill notification to email");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        DetailsContentPage.BillingInformationSectionPage.getInstance().clickEditBtnBySection("Billing Information");
        DetailsContentPage.BillingInformationSectionPage.getInstance().changeBillNotification("Email");
        DetailsContentPage.BillingInformationSectionPage.getInstance().clickSaveBtn();

        test.get().info("Step 1 : Build forgotten password request ");
        path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC2336_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildContactDetailRequest(customerNumber, path);

        test.get().info("Step 1  submit the request to webservice");
        Xml response= swsActions.submitTheRequest();


        test.get().info("Step 1  verify selfcare ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());




    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_014");
        falseResponse.setFaultString("Invalid security answer");
        falseResponse.setCodeAttribute("SC_014");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setDescription("Invalid security answer");
        falseResponse.setExceptionMsg("Invalid security answer");
        falseResponse.setExceptionCauseMsg("Invalid security answer");

        return falseResponse;
    }

}


