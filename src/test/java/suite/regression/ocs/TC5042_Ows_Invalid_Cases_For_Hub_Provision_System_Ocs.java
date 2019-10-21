package suite.regression.ocs;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;

public class TC5042_Ows_Invalid_Cases_For_Hub_Provision_System_Ocs extends BaseTest {

    @Test(enabled = true, description = "TC5042_Ows_Invalid_Cases_For_Hub_Provision_System_Ocs", groups = "OCS")
    public void TC5042_Ows_Invalid_Cases_For_Hub_Provision_System_Ocs() {
        CommonActions.updateHubProvisionSystem("O");
        test.get().info("Step 1 : Create a invalid ocs customer with  System PS(OCS) - OWS PSF (ignored) Tariff PS (HPIN) - Friendly MPN (Ignored)");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC4996_Hpin_Request.xml";
        Xml xml = owsActions.createOcsCustomerRequest(path,false, "", "07372223320");
        verifyInvalidOcsCustomerCreated(xml);

        test.get().info("Step 2 : Create a invalid ocs customer with  System PS(OCS) - OWS PSF (ignored) Tariff PS (HPIN) - Friendly MPN (Ignored)");
        //xml = owsActions.createOcsCustomerRequest(path,false, "", "");
        //verifyInvalidOcsCustomerCreated(xml);
    }

    private void verifyInvalidOcsCustomerCreated(Xml xml){
        Assert.assertEquals(xml.getTextByTagName("errorCode"), "CO_126");
        Assert.assertEquals(xml.getTextByTagName("errorType"), "ERROR");
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "Provisioning System cannot be determined. ");
    }
}
