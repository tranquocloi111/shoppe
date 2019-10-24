package suite.regression.selfcarews.getaccountauthority;

import framework.utils.Xml;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.utils.Common;
import logic.utils.XmlUtils;
import org.testng.annotations.Test;
import suite.BaseTest;

/**
 * User: Nhi Dinh
 * Date: 16/09/2019
 */
public class TC32030_Self_Care_WS_Basic_Path_Get_Account_Auth_Active_Account extends BaseTest {
    @Test(enabled = true, description = "TC32030_Self Care WS Basic Path Get Account Auth Active Account", groups = "SelfCareWS.GetAccountAuthority")
    public void TC32030_Self_Care_WS_Basic_Path_Get_Account_Auth_Active_Account(){
        test.get().info("Create an online CC customer with FC 1 bundle of SB and simonly");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
        String username = owsActions.username;
        String password = owsActions.password;
        String accountNumber = owsActions.customerNo;

        //===============================================================================
        test.get().info("Build and submit Get Account Auth Request");
        SWSActions swsActions = new SWSActions();
        String getAccountAuthRequest = "src\\test\\resources\\xml\\sws\\getaccountauthority\\Get_Account_Auth_Request.xml";
        Xml response = swsActions.submitGetAccountAuthorityRequest(getAccountAuthRequest, username, password);
        //===============================================================================
        test.get().info("Build expected response of Account Auth Response");
        String expectedResponse = buildExpectedResponseOfAccountAuthResponse(accountNumber);

        test.get().info("Verify self care ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(accountNumber, expectedResponse, response);
    }

    private String buildExpectedResponseOfAccountAuthResponse(String accountNumber){
        String filePath = "src\\test\\resources\\xml\\sws\\getaccountauthority\\Account_Auth_Response.xml";
        String file = Common.readFile(filePath).replace("$accountNumber$", accountNumber);
        return Common.saveXmlFile(accountNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
    }
}
