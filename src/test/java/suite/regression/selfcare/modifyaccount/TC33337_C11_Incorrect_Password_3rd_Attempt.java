package suite.regression.selfcare.modifyaccount;


import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.SelfCareSettingContentPage;
import logic.pages.care.find.SelfCareSettingDetailPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

public class TC33337_C11_Incorrect_Password_3rd_Attempt extends BaseTest {


    @Test(enabled = true, description = "TC33337 C11 Incorrect Password 3rd Attempt", groups = "SelfCare")
    public void TC33337_C11_Incorrect_Password_3rd_Attempt() {

        test.get().info("Step 1 : Create a CC customer with 3 subscriptions");
        String path = "src\\test\\resources\\xml\\SelfCare\\viewaccount\\TC30222_CreateOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;
        String username = owsActions.username;

        test.get().info("Step 2 : login to selfcare page 1 attempt");
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, "passwordwwrong", customerNumber);

        test.get().info("Step 3: Verify the error message ");
        String message = "The login details you entered are not correct. Please try again.";
        Assert.assertEquals(SelfCareTestBase.page().errorMessageStack().get(0), message);

        test.get().info("Step 4 : login to selfcare page 2 attempt");
        SelfCareTestBase.page().LoginIntoSelfCarePageFail(username, "passwordwwrong2", customerNumber);

        test.get().info("Step 5: Verify the error message ");
        message = "The login details you entered are not correct. Please try again.";
        Assert.assertEquals(SelfCareTestBase.page().errorMessageStack().get(0), message);

        test.get().info("Step 6 : login to selfcare page 3 attempt");
        SelfCareTestBase.page().LoginIntoSelfCarePageFail(username, "passwordwwrong3", customerNumber);

        test.get().info("Step 7: Verify the error message ");
        message = "Retry limit has been exceeded, your account is suspended. Click here to reset your password.";
        Assert.assertEquals(SelfCareTestBase.page().errorMessageStack().get(0), message);

        test.get().info("Step 8: Click URL to reset Password ");
        SelfCareTestBase.page().clickLinkByText("Click here to reset your password");
        SelfCareTestBase.page().verifyForgotenPasswordPageDisplayed();

        test.get().info("Step 9: Load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();

        test.get().info("Step 10: Verify User has status is suspend");
        String expectedUserStatus = SelfCareSettingContentPage.SelfCareSettingSection.getInstance().getUserStatusByUserName(username);
        Assert.assertEquals(expectedUserStatus, "Suspended");

        test.get().info("Step 11: Go to self care setting detail and verify User has status is suspended");
        SelfCareSettingContentPage.SelfCareSettingSection.getInstance().clickLinkByText(username);
        expectedUserStatus = SelfCareSettingDetailPage.SelfCareSettingDetailSection.getInstance().getUserStatus();
        Assert.assertEquals(expectedUserStatus, "Suspended");
    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }
}
