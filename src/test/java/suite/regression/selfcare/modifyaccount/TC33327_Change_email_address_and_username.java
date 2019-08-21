package suite.regression.selfcare.modifyaccount;


import framework.config.Config;
import framework.wdm.WdManager;
import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.EmailHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.SelfCareSettingContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import org.testng.Assert;
import org.testng.annotations.*;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;
import java.util.HashMap;
import java.util.List;

public class TC33327_Change_email_address_and_username extends BaseTest {
/*
Author: Tran Quoc Loi
 */


    OWSActions owsActions;
    @Test(enabled = true, description = "TC30227 Change email address and username by selfcare", groups = "SelfCare")
    public void TC33327_Change_email_address_and_username() {

        test.get().info("Step 1 : Create a General customer ");
        String newEmailAddress = Config.getProp("emailUsername");
        String path = "src\\test\\resources\\xml\\selfcare\\viewaccount\\TC30222_CreateOrder";
        owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);

        String customerNumber = owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile NC 1");
        String serviceRefOf1stSubscription = owsActions.serviceRef;

        test.get().info("Step 2: delete all email ");
        EmailHelper.getInstance().deleteAllEmailByFolderNameAndEmailSubject("TescoMobilePayMonthly", "Username Change ");
        EmailHelper.getInstance().deleteAllEmailByFolderNameAndEmailSubject("TescoMobilePayMonthly", "Have you logged in to My  ");

        test.get().info("Step 3: Login to self care and access my account detail");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        MyPersonalInformationPage.myAccountSection.getInstance().clickViewOrChangeMyAccountDetails();
        SelfCareTestBase.page().verifyMyAccountDetailPageIsDisplayed();

        test.get().info("Step 4: Change email and username");
        String expectedMssg = "Your changes have been saved.";
        MyAccountDetailsPage.ContactDetailSecton.getInstance().changeEmailAddress(newEmailAddress);
        MyAccountDetailsPage.ContactDetailSecton.getInstance().clickUseEmailAsUserNameCheckBox();
        MyAccountDetailsPage.getInstance().clickUpdateBtn();

        Assert.assertEquals(SelfCareTestBase.page().successfulMessageStack().get(0), expectedMssg);

        test.get().info("Step 5: verify the username has updated email is sent and correct");
        EmailHelper.getInstance().waitEmailByFolderNameAndEmailSubject("TescoMobilePayMonthly","Username Change ", 120);
        boolean flag=EmailHelper.getInstance().findStringInEmail("TescoMobilePayMonthly", "Username Change", newEmailAddress);
        Assert.assertTrue(flag);

        test.get().info("Step 6 : load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 7: go to detail page and verify the email is changed correctly");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        String email = CareTestBase.page().getEmail();
        Assert.assertEquals(email, newEmailAddress);

        test.get().info("Step 8: go to selfcaresetting page and verify the email is changed correctly");
        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();
        String userName = SelfCareSettingContentPage.SelfCareSettingSection.getInstance().getUserName();
        Assert.assertEquals(userName, newEmailAddress);

        test.get().info("Step 9: Verify expected SO is generated for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        HashMap<String, String> expectedServiceOrder = ServiceOrderEntity.dataServiceOrderForChangePassword("Ad-hoc SMS Messages", "Created");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(expectedServiceOrder), 1);

        test.get().info("Step 10: verify  sms change username and email SMS is created in the hitransactioneven table");
        List<String> smsList = CommonActions.getNumberSMSCreatedInHitransactionEventTable(serviceRefOf1stSubscription);
        String expectedResult = String.format("<SMSREQUEST><MPN>%s</MPN><BODY>Tesco Mobile: Your username for your Pay monthly online account has been changed to %s</BODY></SMSREQUEST>", serviceRefOf1stSubscription, newEmailAddress);

        Assert.assertEquals(smsList.size(), 1);
        Assert.assertTrue(smsList.get(0).contains(expectedResult));

        test.get().info("Step 11: Run request job");
        RemoteJobHelper.getInstance().runSMSRequestJob();

        test.get().info("Step 12:Download GRG SMS file");
        String pathFile = SelfCareTestBase.downloadGRGSMSFile();

        test.get().info("Step 13: Verify GRG temporary password is not recorded");
        SelfCareTestBase.verifyGRGTemporaryPasswordIsNotRecorded(pathFile);

        test.get().info("Step 14: Login to self care and access my account detail");
        SelfCareTestBase.page().LoginIntoSelfCarePage(newEmailAddress, "password1", customerNumber);
        MyPersonalInformationPage.myAccountSection.getInstance().clickViewOrChangeMyAccountDetails();
        SelfCareTestBase.page().verifyMyAccountDetailPageIsDisplayed();
    }

    @AfterMethod
    public void resetEmailForUsername() {
        String emailAddress = Config.getProp("emailUsername");
        SelfCareTestBase.page().LoginIntoSelfCarePage(emailAddress, "password1", owsActions.customerNo);
        MyPersonalInformationPage.myAccountSection.getInstance().clickViewOrChangeMyAccountDetails();
        SelfCareTestBase.page().verifyMyAccountDetailPageIsDisplayed();

        String newEmailAddress = "TC" + randomNumberAndString() + "@hasencx.com";
        MyAccountDetailsPage.ContactDetailSecton.getInstance().changeEmailAddress(newEmailAddress);
        MyAccountDetailsPage.getInstance().clickUpdateBtn();

        String expectedMssg = "Your changes have been saved.";
        Assert.assertEquals(SelfCareTestBase.page().successfulMessageStack().get(0), expectedMssg);
        WdManager.dismissWD();

    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }


}
