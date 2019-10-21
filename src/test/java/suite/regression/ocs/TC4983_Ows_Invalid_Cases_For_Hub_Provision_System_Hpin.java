package suite.regression.ocs;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;

public class TC4983_Ows_Invalid_Cases_For_Hub_Provision_System_Hpin extends BaseTest {

    @Test(enabled = true, description = "TC4983_Ows_Invalid_Cases_For_Hub_Provision_System_Hpin", groups = "OCS")
    public void TC4983_Ows_Invalid_Cases_For_Hub_Provision_System_Hpin() {
        CommonActions.updateHubProvisionSystem("H");
        test.get().info("Step 1 : Create a invalid ocs customer with  System PS(HPIN) - OWS PSF (Null) Tariff PS (OCS) - Friendly MPN (Any)");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC4996_Request_Ocs_Typee.xml";
        Xml xml = owsActions.createOcsCustomerRequest(path, false,"null", "");
        verifyInvalidOcsCustomerCreatedWithPsIsNull(xml);

        test.get().info("Step 2 : Create a invalid ocs customer with  System PS(HPIN) - OWS PSF (HPIN) Tariff PS (OCS) - Friendly MPN (Any)");
        xml = owsActions.createOcsCustomerRequest(path, false,"HPIN", "");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 3 : Create a invalid ocs customer with  System PS(HPIN) - OWS PSF (OCS) Tariff PS (OCS) - Friendly MPN (Any)");
        xml = owsActions.createOcsCustomerRequest(path, false,"OCS", "");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 4 : Create a invalid ocs customer with  System PS(HPIN) - OWS PSF (HPIN) Tariff PS (OCS) - Friendly MPN (Yes)");
        xml = owsActions.createOcsCustomerRequest(path, false,"HPIN", "07372223320");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 6 : Create a invalid ocs customer with  System PS(HPIN) - OWS PSF (Ignored) Tariff PS (OCS) - Friendly MPN (ignored)");
        xml = owsActions.createOcsCustomerRequest(path, false,"", "07372223320");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 7 : Create a invalid ocs customer with  System PS(HPIN) - OWS PSF (Ignored) Tariff PS (OCS) - Friendly MPN (ignored)");
        xml = owsActions.createOcsCustomerRequest(path, false,"", "");
        verifyInvalidOcsCustomerCreated(xml);
    }

    private void verifyInvalidOcsCustomerCreated(Xml xml){
        Assert.assertEquals(xml.getTextByTagName("errorCode"), "CO_126");
        Assert.assertEquals(xml.getTextByTagName("errorType"), "ERROR");
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "Provisioning System cannot be determined. ");
    }

    private void verifyInvalidOcsCustomerCreatedWithPsIsNull(Xml xml){
        Assert.assertEquals(xml.getTextByTagName("errorCode"), "CO_001");
        Assert.assertEquals(xml.getTextByTagName("errorType"), "ERROR");
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "cvc-attribute.3: The value 'null' of attribute 'provisioningSystem' on element 'ord1:createOrder' is not valid with respect to its type, 'provisioningSystemEnum'.");
    }
}
