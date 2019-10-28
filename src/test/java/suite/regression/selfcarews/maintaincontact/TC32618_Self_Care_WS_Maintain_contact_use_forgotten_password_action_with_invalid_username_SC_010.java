package suite.regression.selfcarews.maintaincontact;

import framework.utils.Xml;
import logic.business.db.OracleDB;
import logic.business.entities.ErrorResponseEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.HashMap;

public class TC32618_Self_Care_WS_Maintain_contact_use_forgotten_password_action_with_invalid_username_SC_010 extends BaseTest {
    String serviceOrderID;
    String currentPWD;

//    @Test(enabled = true, description = "TC32618 Selfcare webservice maitain contact use forgotten password action with invalid userbane SC010", groups = "SelfCare")
    public void TC32618_Self_Care_WS_Maintain_contact_use_forgotten_password_action_with_invalid_username_SC_010() {

        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_sim_only";
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
        path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC2329_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildMaintainContactRequest(customerNumber, path);

        test.get().info("Step 1  submit the request to webservice");
        Xml response= swsActions.submitTheRequest();


        test.get().info("Step 1  verify selfcare ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());




    }

    private ErrorResponseEntity buildFaultResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_010");
        falseResponse.setFaultString("Invalid username, password");
        falseResponse.setCodeAttribute("SC_010");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setDescription("Invalid username, password");
        falseResponse.setExceptionMsg("Invalid username, password");
        falseResponse.setExceptionCauseMsg("Invalid username, password");

        return falseResponse;
    }

}


