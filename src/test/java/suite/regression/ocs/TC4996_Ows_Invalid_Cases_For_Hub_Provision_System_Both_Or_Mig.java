package suite.regression.ocs;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;

public class TC4996_Ows_Invalid_Cases_For_Hub_Provision_System_Both_Or_Mig extends BaseTest {

    @Test(enabled = true, description = "TC4996_Ows_Invalid_Cases_For_Hub_Provision_System_Both_Or_MIG", groups = "OCS")
    public void TC4996_Ows_Invalid_Cases_For_Hub_Provision_System_Both_Or_MIG() {
        CommonActions.updateHubProvisionSystem("B");
        test.get().info("Step 1 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (Null) Tariff PS (HPIN) - Friendly MPN (Yes)");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC4996_Hpin_Request.xml";
        Xml xml = owsActions.createOcsCustomerRequest(path, false,"null", "07372223320", "");
        verifyInvalidOcsCustomerCreatedWithPsIsNull(xml);

        test.get().info("Step 2 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (OCS) - Tariff PS (HPIN) - Friendly MPN (-)");
        xml = owsActions.createOcsCustomerRequest(path, false,"OCS");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 3 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (OCS) - Tariff PS (HPIN) - Friendly MPN (Yes)");
        xml = owsActions.createOcsCustomerRequest(path, false,"OCS", "07372223320");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 4 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (OCS) - Customer (Existing) - Tariff PS (HPIN) - Friendly MPN (Yes)");
        xml = owsActions.createOcsCustomerRequest(path, false,"OCS", "07372223320", "47598996");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 5 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (OCS) - Customer (Upgrade) - Tariff PS (HPIN) - Friendly MPN (Yes)");
        String upgradePath = "src\\test\\resources\\xml\\ocs\\TC4996_upgrade_ocs_order.xml";
        xml = owsActions.createOcsCustomerRequest(upgradePath, false,"OCS", "07988083390", "47598996");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 6 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (HPIN) - Tariff PS (HPIN) - Friendly MPN (Yes)");
        xml = owsActions.createOcsCustomerRequest(path, false,"HPIN", "07372223320");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 7 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (Null) - Tariff PS (Both) - Friendly MPN (Yes)");
        path = "src\\test\\resources\\xml\\ocs\\TC4996_Ocs_Request_Both.xml";
        xml = owsActions.createOcsCustomerRequest(path, false,"null", "");
        verifyInvalidOcsCustomerCreatedWithPsIsNull(xml);

        test.get().info("Step 8 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (HPIN) - Tariff PS (OCS) - Friendly MPN (-)");
        path = "src\\test\\resources\\xml\\ocs\\TC4996_Request_Ocs_Typee.xml";
        xml = owsActions.createOcsCustomerRequest(path, false,"HPIN");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 9 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (HPIN) - Tariff PS (OCS) - Friendly MPN (Yes)");
        xml = owsActions.createOcsCustomerRequest(path, false,"HPIN", "07372223320");
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
