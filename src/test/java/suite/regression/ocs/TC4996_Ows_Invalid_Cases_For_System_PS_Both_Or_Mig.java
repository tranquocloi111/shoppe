package suite.regression.ocs;

import framework.utils.Xml;
import logic.business.ws.ows.OWSActions;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;

public class TC4996_Ows_Invalid_Cases_For_System_PS_Both_Or_Mig extends BaseTest {

    @Test(enabled = true, description = "TC4996_Ows_Invalid_Cases_For_System_PS_Both_Or_Mig ", groups = "OCS")
    public void TC4996_Ows_Invalid_Cases_For_System_PS_Both_Or_Mig() {
        test.get().info("Step 1 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (Null) Tariff PS(HPIN) - Friendly MPN ss(Yes)");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC4996_Invalid_Ocs_Request.xml";
        Xml xml = owsActions.createInvalidOcsCustomerRequest(path, "", "07372223320");
        Assert.assertEquals(xml.getTextByTagName("errorCode"), "CO_126");
        Assert.assertEquals(xml.getTextByTagName("errorType"), "ERROR");
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "Provisioning System cannot be determined. ");

        test.get().info("Step 2 : Create a invalid ocs customer with  System PS(Both Or MIG) - OWS PSF (OCS) - Tariff PS(HPIN) - Friendly MPN (-)");
        xml = owsActions.createInvalidOcsCustomerRequest(path, "OCS", "");
        Assert.assertEquals(xml.getTextByTagName("errorCode"), "CO_126");
        Assert.assertEquals(xml.getTextByTagName("errorType"), "ERROR");
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "Provisioning System cannot be determined. ");

    }
}
