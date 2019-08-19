package suite.regression.selfcare.modifyaccount;


import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.DateTimeHelper;
import logic.business.helper.EmailHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.selfcare.MyPasswordPage;
import logic.utils.Common;
import logic.utils.TimeStamp;
import org.joda.time.DateTime;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TC33325_B06_Forgotten_username extends BaseTest {
/*
Author: Tran Quoc Loi
Common content page
detailcontentpage
remotejob helper
DateTime helper
 */

    @Test(enabled = true, description = "TC33325 Self Care Forgotten Username ", groups = "SelfCare")
    public void TC33325_B06_Forgotten_username() {


        test.get().info("Step 1 : Create a General customer ");
        String path = "src\\test\\resources\\xml\\SelfCare\\viewaccount\\TC30222_CreateOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile NC 1");

        String subscriptionNo1 = owsActions.serviceRef;
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : load user in the hub net ");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);

        test.get().info("Step 3 : Access the detail page");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 4 : get birthday, post code and mobile number");
        Date doB = DateTimeHelper.getInstance().parseStringToDate(CommonContentPage.CustomerSummarySectionPage.getInstance().getBirthDay(), TimeStamp.DATE_FORMAT);
        String birthDate = DateTimeHelper.getInstance().changeformatDate(doB, TimeStamp.DATE_FORMAT_IN_PDF);

        String mobileNumber = DetailsContentPage.BillingInformationSectionPage.getInstance().getMasterMPN();
        String postCode = CommonContentPage.CustomerSummarySectionPage.getInstance().getPostCode();

        test.get().info("Step 5 : login to SelfCare and click the forgot Username link");
        SelfCareTestBase.page().openSelfCareLoginPageThenClickForgotUsernameLink();

        test.get().info("Step 6 : Input the security information");
        MyPasswordPage.getInstance().inputAccountHolderMobileNumber(mobileNumber);
        MyPasswordPage.getInstance().inputPostCode(postCode.replaceAll("\\s", ""));
        MyPasswordPage.getInstance().inputBirthDate(birthDate);
        MyPasswordPage.getInstance().clickContinueBtn();

        test.get().info("Step 7: verify the successfull message");
        List<String> messages = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(2, messages.size());
        Assert.assertEquals("Your username has been sent to your e-mail address.", messages.get(0));
        Assert.assertEquals("Your request has also been sent to your mobile via SMS. This may take up to 1 hour.", messages.get(1));

        test.get().info("Step 8: load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 9: access the service order content page");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 10: Verify Ad hoc SMS Messages SO");
        HashMap<String, String> expectedServiceOrder = ServiceOrderEntity.dataServiceOrderForChangePassword("Ad-hoc SMS Messages", "Created");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(expectedServiceOrder), 1);
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(expectedServiceOrder);
        String numberServiceOrder = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);

        test.get().info("Step 11: Verify context info of SMS service order is correct in db");
        String description = "User Name Request (" + subscriptionNo1 + ")";
        String bodyContent = String.format("<SMSREQUEST><MPN>%s</MPN><BODY>Tesco Mobile: Your username for your Pay monthly online account is %s</BODY></SMSREQUEST>", subscriptionNo1, owsActions.username);
        List<String> smsList = CommonActions.verifyContextInfoOfSMSServiceOrderIsCorrectInDb(numberServiceOrder, description);
        String firstResult = smsList.get(0).replace("\n", "");
        firstResult = firstResult.substring(firstResult.indexOf("=") + 1).replace("}", "");
        Assert.assertEquals(firstResult, bodyContent);

        test.get().info("Step 12: Run request job");
        RemoteJobHelper.getInstance().runSMSRequestJob();

        test.get().info("Step 13: verify sms is sent");

        smsList = CommonActions.getSMSIsSent(numberServiceOrder, description);
        Assert.assertTrue(!smsList.isEmpty());


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
