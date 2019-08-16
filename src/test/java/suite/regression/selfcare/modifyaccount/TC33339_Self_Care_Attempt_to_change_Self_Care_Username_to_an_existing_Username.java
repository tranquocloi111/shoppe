package suite.regression.selfcare.modifyaccount;


import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.SelfCareSettingContentPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

public class TC33339_Self_Care_Attempt_to_change_Self_Care_Username_to_an_existing_Username extends BaseTest {
/*
Author: Tran Quoc Loi
*/

    @Test(enabled = true, description = "TC33339 self care attempt to change selfcare usename to an existing username", groups = "SelfCare")
    public void TC33339_Self_Care_Attempt_to_change_Self_Care_Username_to_an_existing_Username() {

        test.get().info("Step 1 : Create a CC customer ");
        String path = "src\\test\\resources\\xml\\SelfCare\\viewaccount\\TC30222_CreateOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create a another CC customer ");
        OWSActions owsActions1 = new OWSActions();
        owsActions1.createGeneralCustomerOrder(path);


        test.get().info("Step 3: load user1 in the hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);

        test.get().info("Step 4: Access detail screen");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 5: get Email");
        String email = DetailsContentPage.AddressInformationPage.getInstance().getEmail();

        test.get().info("Step 6: Access Self care setting screen");
        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();

        test.get().info("Step 7: get username");
        String username = SelfCareSettingContentPage.SelfCareSettingSection.getInstance().getUserName();

        test.get().info("Step 8: Login SelfCare  by user 1");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, owsActions.customerNo);

        test.get().info("Step 9: Access the personal information");
        SelfCareTestBase.page().viewOrChangeMyAccountDetails();

        test.get().info("Step 10: Verify my account details page result is displayed correctly");
        Assert.assertEquals(email, MyAccountDetailsPage.ContactDetailSecton.getInstance().getEmailAddress());
        Assert.assertEquals(username, MyAccountDetailsPage.SecurityDetailSecton.getInstance().getUsername());

        test.get().info("Step 11: Change username and email");
        MyAccountDetailsPage.SecurityDetailSecton.SecurityDetailSecton.getInstance().changeUsername(owsActions1.username);
        MyAccountDetailsPage.getInstance().clickUpdateBtn();

        test.get().info("Step 12: Verify error mssg change username and email");
        String expectedErrorMssg = "The requested Username already exists in our system. Please select a different Username.";
        Assert.assertEquals(expectedErrorMssg, SelfCareTestBase.page().errorMessageStack().get(0));

        test.get().info("Step 13: load user 2 in the hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 14: Access Self care setting screen");
        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();

        test.get().info("Step 15: verify username is not changed");
        Assert.assertEquals(SelfCareSettingContentPage.SelfCareSettingSection.getInstance().getUserName(), username);


    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }
}
