package suite.regression.selfcare;

import ch.ethz.ssh2.Session;
import framework.config.Config;
import framework.utils.Log;
import logic.business.db.OracleDB;
import logic.business.helper.FTPHelper;
import logic.pages.BasePage;

import java.io.File;
import java.util.Properties;

import logic.pages.selfcare.LoginPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Common;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

public class SelfCareTestBase extends BasePage {
    LoginPage loginPage;
    String userName;
    String passWord;

    private SelfCareTestBase() {
        loginPage = new LoginPage();
    }

    public static SelfCareTestBase page() {
        return new SelfCareTestBase();
    }

    public void LoginIntoSelfCarePage(String userName, String passWord, String customerId) {
        loginPage.navigateToSelfCarePage();
        loginPage.login(userName, passWord, customerId);
        waitForPageLoadComplete(10);
    }

    public void LoginIntoSelfCarePageByChangePasswordLink(String userName, String passWord, String customerId,String link) {
        loginPage.navigate(link);
        loginPage.login(userName, passWord, customerId);
        waitForPageLoadComplete(10);
    }

    public void LoginIntoSelfCarePageFail(String userName, String passWord, String customerId) {
        loginPage.relogin(userName, passWord, customerId);
        waitForPageLoadComplete(10);
    }
    public void openSelfCareLoginPageThenClickForgotPasswordLink() {
        loginPage.navigateToSelfCarePage();
        clickLinkByText("Forgotten your password? Click here.");
    }

    public void reLoginIntoSelfCarePage(String userName, String passWord, String customerId) {
        SelfCareTestBase.page().clickLogOffLink();
        loginPage.navigateToSelfCarePage();
        loginPage.relogin(userName, passWord, customerId);
        waitForPageLoadComplete(10);
    }

    public List<String> successfulMessageStack() {
        List<String> list = new ArrayList<>();
        for (WebElement li : getDriver().findElements(By.xpath(".//li[@class='messageStackSuccess']"))) {
            list.add(li.getText().trim());
        }
        return list;
    }

    public List<String> errorMessageStack() {
        List<String> list = new ArrayList<>();
        for (WebElement li : getDriver().findElements(By.xpath(".//li[@class='messageStackError']"))) {
            list.add(li.getText().trim());
        }
        return list;
    }

    public void viewOrChangeMyAccountDetails() {
        MyPersonalInformationPage.myAccountSection.getInstance().clickViewOrChangeMyAccountDetails();
    }

    public void navigateSelfCarePage() {
        loginPage.navigateToSelfCarePage();
    }

    public void verifyMyTariffDetailsPageIsDisplayed() {
        Assert.assertEquals("My tariff and credit agreement documents", MyPersonalInformationPage.getInstance().getHeader());
    }

    public void verifyAddOrChangeAFamilyPerkIsDisplayed() {
        Assert.assertEquals("Add or change a Family perk", MyPersonalInformationPage.getInstance().getHeader());
    }

    public void verifyMyPersonalInformationPageIsDisplayed() {
        Assert.assertEquals("My personal information", MyPersonalInformationPage.getInstance().getHeader());
    }

    public void clickChangeMyAccountPassword() {
        super.clickLinkByText("Change my account password");
    }

    public void clickLogOffLink() {
        super.clickLinkByText("Log off");
    }
    public void verifyForgotenPasswordPageDisplayed() {
        Assert.assertEquals("Forgotten password", MyPersonalInformationPage.getInstance().getHeader());
    }
    public void verifyMyAccountDetailPageIsDisplayed() {
        Assert.assertEquals("My account details", MyPersonalInformationPage.getInstance().getHeader());
    }
    public static String downloadGRGSMSFile() {
        String sql = "SELECT h.OrigTransactionIdent" +
                " FROM HITransactionDefinition d, HITransaction h" +
                " WHERE  d.TransactionKeyRef LIKE 'SMS%REQUEST' " +
                " AND    d.HITransactionDefinitionID = h.HITransactionDefinitionID" +
                " ORDER BY h.transactionDate DESC";
        List<String> GRGSMSFileName = OracleDB.SetToNonOEDatabase().executeQueryReturnListString(sql);
        String firstResult = GRGSMSFileName.get(0);
        String value = firstResult.substring(firstResult.indexOf("=") + 1).replace("}", "");
        String ftpFilePath = Config.getProp("cdrFolder");
        ftpFilePath = ftpFilePath.replace("Feed/a2aInterface/fileinbox", "ftp/tesgrg/fileoutbox");
        String localPath = Common.getFolderLogFilePath();
        FTPHelper.getInstance().downLoadFromDisk(ftpFilePath, value, localPath);
        Log.info("TM_HUB_SMSRQST file:" + localPath);
        return localPath + value;
    }
    public static void verifyGRGTemporaryPasswordIsNotRecorded(String fileName) {
        File file = new File("fileName");
        String expectedResult = fileName.split("_")[4];
        expectedResult=String.format("|HUB|GRG|%s|", expectedResult);
        String fileResult = Common.readFile(fileName);
        Assert.assertTrue(fileResult.contains(expectedResult));
        Assert.assertFalse(fileResult.contains("password1"));
    }


}

