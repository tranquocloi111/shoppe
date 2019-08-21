package suite.regression.selfcare.modifyaccount;


import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.EmailHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.SelfCareSettingContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.selfcare.MyPasswordPage;
import logic.utils.Common;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.HashMap;
import java.util.List;

public class TC33324_Self_Care_Forgotten_Password_for_an_suspended_Account extends BaseTest {
/*
Author: Tran Quoc Loi
 */

    @Test(enabled = true, description = "TC33324 Self Care Forgotten Password for an suspended Account ", groups = "SelfCare")
    public void TC33324_Self_Care_Forgotten_Password_for_an_suspended_Account() {

        test.get().info("Step 1 : delete all old data");
        String expectedFile = "src\\test\\resources\\txt\\TC30223_expectedEmail.txt";
        String actualFile = "src\\test\\resources\\txt\\TC30223_actualEmail.txt";
        Common.deleteFile(actualFile);
        EmailHelper.getInstance().deleteAllEmailByFolderNameAndEmailSubject("TescoMobilePayMonthly", " ");

        test.get().info("Step 2 : Create a General customer ");
        String path = "src\\test\\resources\\xml\\selfcare\\viewaccount\\TC30222_CreateOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile NC 1");

        String subscriptionNo1 = owsActions.serviceRef;
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 3 : load user in the hub net click forgot password link");
        SelfCareTestBase.page().openSelfCareLoginPageThenClickForgotPasswordLink();

        test.get().info("Step 4 : input user name");
        MyPasswordPage.getInstance().inputUsername(owsActions.username);
        MyPasswordPage.getInstance().clickContinueBtn();

        test.get().info("Step 5 : input security answer and submit");
        MyPasswordPage.getInstance().inputSecurityAnswer("Smith");
        MyPasswordPage.getInstance().clickContinueBtn();

        test.get().info("Step 6 : verify the successfull message");
        List<String> messages = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals("A new password has been sent to your e-mail address.", messages.get(0));
        Assert.assertEquals("Your request has also been sent to your mobile via SMS. This may take up to 1 hour.", messages.get(1));

        test.get().info("Step 7 : get temporary password from email ");
        EmailHelper.getInstance().waitEmailByFolderNameAndEmailSubject("TescoMobilePayMonthly", "Password reset ", 120);
        String temporaryPassEmail = EmailHelper.getInstance().extractPasswordEmailByFolderNameAndEmailSubject("TescoMobilePayMonthly", "Password reset");
        String link = EmailHelper.getInstance().extractLinkEmailByFolderNameAndEmailSubject("TescoMobilePayMonthly", "Password reset");

        test.get().info("Step 8: Verify reset password in hitransactionevent table");
        List<String> smsList = CommonActions.getNumberSMSCreatedInHitransactionEventTable(subscriptionNo1);
        Assert.assertEquals(smsList.size(), 1);

        String expectText = String.format("<SMSREQUEST><MPN>%s</MPN><BODY>Tesco Mobile: Your password for your Pay monthly online account has been reset to %s</BODY></SMSREQUEST>", subscriptionNo1, temporaryPassEmail.trim());
        String Text = smsList.get(0).substring(smsList.get(0).indexOf("=") + 1).replace("}", "");
        Assert.assertEquals(Text.trim(), expectText.trim());

        test.get().info("Step 9: Run request job");
        RemoteJobHelper.getInstance().runSMSRequestJob();

        test.get().info("Step 10:Download GRG SMS file");
        String pathFile = SelfCareTestBase.downloadGRGSMSFile();

        test.get().info("Step 11: Verify GRG temporary_password_is_not_recorded");
        SelfCareTestBase.verifyGRGTemporaryPasswordIsNotRecorded(pathFile);

        test.get().info("Step 12: Open and login the self care via the link in email");
        SelfCareTestBase.page().LoginIntoSelfCarePageByChangePasswordLink(owsActions.username, temporaryPassEmail, customerNumber, link);

        test.get().info("Step 13: delete all email Have you logged in to My Account");
        EmailHelper.getInstance().deleteAllEmailByFolderNameAndEmailSubject("TescoMobilePayMonthly", "Have you logged in to My Account?");

        test.get().info("Step 14: set up a new password in my password page");
        MyPasswordPage.getInstance().updateNewPassword(temporaryPassEmail, "password1");

        test.get().info("Step 15: Verify my personal informattion page displayed with successfull message");
        String expectedMssg = "Your password has been successfully changed.";
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();
        Assert.assertEquals(SelfCareTestBase.page().successfulMessageStack().get(0), expectedMssg);

        test.get().info("Step 16: Run request job");
        RemoteJobHelper.getInstance().runSMSRequestJob();

        test.get().info("Step 17: Verify second reset password in hitransactionevent table");
        smsList = CommonActions.getNumberSMSCreatedInHitransactionEventTable(subscriptionNo1);
        Assert.assertEquals(smsList.size(), 2);
        Text = smsList.get(1).substring(smsList.get(1).indexOf("=") + 1).replace("}", "");
        Assert.assertTrue(Text.contains("Tesco Mobile: Your password for your Pay monthly online account has been changed successfully"));
        Assert.assertFalse(Text.contains("password1"));

        test.get().info("Step 18 : Verify the second email format ");
        EmailHelper.getInstance().waitEmailByFolderNameAndEmailSubject("TescoMobilePayMonthly", "Change of your Pay Monthly Price Plans", 120);
        verifySecondEmail(expectedFile, actualFile);

        test.get().info("Step 19:Download GRG SMS file");
        pathFile = SelfCareTestBase.downloadGRGSMSFile();

        test.get().info("Step 20: Verify GRG temporary_password_is_not_recorded");
        SelfCareTestBase.verifyGRGTemporaryPasswordIsNotRecorded(pathFile);

        test.get().info("Step 21: load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 22: access the selfcare setting content page");
        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();

        test.get().info("Step 23: verify user status is active");
        Assert.assertEquals(SelfCareSettingContentPage.SelfCareSettingSection.getInstance().getUserStatusByUserName(owsActions.username), "Active");

        test.get().info("Step 24: Verify change password successfully mail sent to customer ");
        Assert.assertTrue(EmailHelper.getInstance().findStringInEmail("TescoMobilePayMonthly", " Tesco Mobile password", "Thank you for your request.  Your password has now been changed."));


    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }

    public void verifySecondEmail(String expectedFile, String actualFile) {
        Common.deleteFile(actualFile);
        Common.waitForFileDelete(120, actualFile);
        EmailHelper.getInstance().convertEmailToFile("TescoMobilePayMonthly", "Change of your Pay Monthly Price Plans", actualFile);
        Common.waitForFileExist(120, actualFile);
        List result = Common.compareFiles(expectedFile, actualFile, "Dear ");
        Assert.assertEquals(result.size(), 0);

    }

}
