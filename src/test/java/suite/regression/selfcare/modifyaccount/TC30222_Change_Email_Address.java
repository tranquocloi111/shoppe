package suite.regression.selfcare.modifyaccount;


import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

public class TC30222_Change_Email_Address extends BaseTest {


    @Test(enabled = true, description = "TC30222 Create an onlines CC customer with NC no bundle and sim only", groups = "SelfCare")
    public void TC30222_Change_Email_Address() {

        test.get().info("Step 1 : Create a CC customer with 3 subscriptions");
        String path = "src\\test\\resources\\xml\\selfcare\\viewaccount\\TC30222_CreateOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);

        test.get().info("Step 3: Access detail screen");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 4: Change Email");
        String email = String.format("newMail%s@hsntech.com", randomNumberAndString());
        CareTestBase.page().clickEditBtnByIndex(1);
        DetailsContentPage.AddressInformationPage.getInstance().changeEmail(email);
        CareTestBase.page().clickApplyBtn();

        test.get().info("Step 5: Login SelfCare  ");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, owsActions.customerNo);

        test.get().info("Step 6: Access the personal information");
        SelfCareTestBase.page().viewOrChangeMyAccountDetails();

        test.get().info("Step 7: Verify the email is changed");
        String actualemail = MyAccountDetailsPage.ContactDetailSecton.getInstance().getEmailAddress();
        Assert.assertEquals(email, actualemail);
    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][] { { "gc" }, { "ff" }, { "ie" } };
    }
}
