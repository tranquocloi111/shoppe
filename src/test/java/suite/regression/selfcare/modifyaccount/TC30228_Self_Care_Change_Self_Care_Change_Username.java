package suite.regression.selfcare.modifyaccount;


import framework.utils.RandomCharacter;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.SelfCareSettingContentPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

public class TC30228_Self_Care_Change_Self_Care_Change_Username extends BaseTest {
/*
Author: Tran Quoc Loi
 */

    @Test(enabled = true, description = "TC30228 Change username by selfcare", groups = "SelfCare")
    public void TC30228_Self_Care_Change_Self_Care_Change_Username() {

        test.get().info("Step 1 : Create a General customer ");
        String path = "src\\test\\resources\\xml\\SelfCare\\viewaccount\\TC30222_CreateOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);


        test.get().info("Step 3: Access detail screen");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 4: Get username and email");
        String userName = owsActions.username;
        String email = CareTestBase.page().getEmail();

        test.get().info("Step 5: Login to the self care screen ");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, owsActions.customerNo);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 6: Access my account detail ");
        MyPersonalInformationPage.myAccountSection.getInstance().clickViewOrChangeMyAccountDetails();
        SelfCareTestBase.page().verifyMyAccountDetailPageIsDisplayed();

        test.get().info("Step 7: Change email and username");

        String newEmailAddress = "tc" + RandomCharacter.getRandomAlphaNumericString(9) + "@hsntech.com";
        String newUserName = "test" + RandomCharacter.getRandomAlphaNumericString(9) + "@hsntech.com";
        String expectedMssg = "Your changes have been saved.";
        MyAccountDetailsPage.ContactDetailSecton.getInstance().changeEmailAddress(newEmailAddress);
        MyAccountDetailsPage.SecurityDetailSecton.getInstance().changeUsername(newUserName);
        MyAccountDetailsPage.getInstance().clickUpdateBtn();
        Assert.assertEquals(SelfCareTestBase.page().successfulMessageStack().get(0), expectedMssg);


        test.get().info("Step 8 : load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();


        test.get().info("Step 9 : go to detail page and verify the email is changed correctly");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        email = CareTestBase.page().getEmail();
        Assert.assertEquals(email, newEmailAddress);

        test.get().info("Step 10 : go to detail page and verify the email is changed correctly");
        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();
        userName = SelfCareSettingContentPage.SelfCareSettingSection.getInstance().getUserName();
        Assert.assertEquals(userName, newUserName);



    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }


}
