package suite.regression.selfcare.viewaccount;

import logic.business.ws.ows.OWSActions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.selfcare.SelfCareTestBase;

public class TC33313_B19_Self_Care_Validate_Log_Off extends BaseTest {


    @Test(enabled = true, description = "TC33313 self care validate log off", groups = "SelfCare")
    public void TC33313_B19_Self_Care_Validate_Log_Off() {
        test.get().info("Step 1: Create a CC customer with no bundle and sim only");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_NC_no_bundle_and_sim_only";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;


        test.get().info("Step 2: Login in to selfcare page");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 3: Click log off");
        SelfCareTestBase.page().clickLogOffLink();

        test.get().info("Step 4: verify browser url is correct");
        String url = "https://www.tescomobile.com/my-account-logout";
        Assert.assertEquals(SelfCareTestBase.page().getCurrentUrl(),url);

    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }


}
