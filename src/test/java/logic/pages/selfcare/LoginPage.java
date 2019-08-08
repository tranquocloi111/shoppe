package logic.pages.selfcare;

import framework.config.Config;
import framework.utils.Log;
import logic.business.db.billing.CommonActions;
import logic.pages.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage  extends BasePage {
    @FindBy(name = "user")
    WebElement txtUsername;

    @FindBy(name = "password")
    WebElement txtPassword;

    @FindBy(id = "LoginBtn")
    WebElement btnOk;

    @FindBy(xpath = ".//div[@role='dialog']")
    WebElement pinDiapog;

    @FindBy(xpath = ".//div[@role='dialog']//input")
    WebElement txtPinText;

    @FindBy(xpath = ".//div[@role='dialog']//table//a")
    WebElement btnDialogLogin;


    public void login(String username, String password, String customerId) {
        if (isElementPresent(txtUsername)) {
            txtUsername.sendKeys(username);
            txtPassword.sendKeys(password);
            click(btnOk);
            if (isElementPresent(pinDiapog)) {
                String pinCode = CommonActions.getPinCode(customerId);
                Log.info("Pin Code : " + pinCode);
                enterValueByLabel(txtPinText, pinCode);
                click(btnDialogLogin);
            }
            waitForPageLoadComplete(10);
        }
    }
    public void relogin(String username, String password, String customerId) {
        if (isElementPresent(txtUsername)) {
            enterValueByLabel(txtUsername,username);
            enterValueByLabel(txtPassword,password);
            click(btnOk);
            waitForPageLoadComplete(10);
        }
    }


    public void navigateToSelfCarePage(){
        navigate(Config.getProp("selfCareUrl"));
    }
}

