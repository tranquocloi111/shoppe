package suite.regression.selfcare;

import logic.pages.BasePage;
import logic.pages.selfcare.LoginPage;
import logic.pages.selfcare.MyPersonalInformationPage;
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
        waitForPageLoadComplete(120);
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

    public void navigateSelfCarePage(){
        loginPage.navigateToSelfCarePage();
    }

    public void verifyMyTariffDetailsPageIsDisplayed(){
        Assert.assertEquals("My tariff and credit agreement documents", MyPersonalInformationPage.getInstance().getHeader());
    }

    public void verifyMyPersonalInformationPageIsDisplayed(){
        Assert.assertEquals("My personal information", MyPersonalInformationPage.getInstance().getHeader());
    }

}
