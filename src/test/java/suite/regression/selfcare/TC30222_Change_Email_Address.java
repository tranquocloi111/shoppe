package suite.regression.selfcare;


import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

public class TC30222_Change_Email_Address extends BaseTest {


    @Test(enabled = true, description = "TC30222 Create an onlines CC customer with NC no bundle and sim only", groups = "SelfCare")
    public void TC30222_Change_Email_Address() {

        test.get().info("Step 1 : Create a CC customer with 3 subscriptions");
        String path = "src\\test\\resources\\xml\\SelfCare\\viewaccount\\TC30222_CreateOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithNC1BundleAndSimOnly(path);

        test.get().info("Step 2 : load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 3: Access detail screen");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();


        test.get().info("Step 4: Change Email");
        String email = String.format("newMail%s@hsntech.com", randomNumberAndString());
        CareTestBase.page().clickEditLinkByIndex(1);
        DetailsContentPage.AddressInformationSection.getInstance().changeEmailAddress(email);
        CareTestBase.page().clickApplyBtn();

        test.get().info("Step 5: Login SelfCare  ");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, owsActions.customerNo);

        test.get().info("Step 6: Access the personal information");
        SelfCareTestBase.page().viewOrChangeMyAccountDetails();

        test.get().info("Step 7: Verify the email is changed");
        String actualemail = MyAccountDetailsPage.getInstance().getEmailAddress();
        Assert.assertEquals(email, actualemail);
    }
}
