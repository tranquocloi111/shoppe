package logic.pages;

import logic.pages.selfcare.Test3DSecurePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Hansen3DSecurePage extends BasePage {

    public static Hansen3DSecurePage getInstance() {
        return new Hansen3DSecurePage();
    }
    @FindBy(id = "threeDurlFailForm:pageFailTitle")
    WebElement failMessage;

    public String getFailMessage() {
        return getTextOfElement(failMessage);
    }

    public String getFailState() {
        return getTextOfElement(failState);
    }

    @FindBy(id = "threeDurlFailForm:failState")
    WebElement failState;

    @FindBy(id = "threeDurlForm:hubThreeDurl")
    WebElement threeDSecureUrl;
    public void inputSecureUrl(String url)
    {
        enterValueByLabel(threeDSecureUrl,url);
    }
    @FindBy(id = "threeDurlForm:submit_button")
    WebElement submitBtn;
    public void clickSubmitBtn()
    {
        click(submitBtn);
    }

    @FindBy(xpath = "//input[@id='issuerForm:password']")
    WebElement PasswordInput;
    @FindBy(xpath = "//button[@id='issuerForm:verify']")
    WebElement verifyBtn;

    public void enter3DPassword(String password) {
        enterValueByLabel(PasswordInput, password);
        waitForPageLoadComplete(50);
    }


    public void clickVerifyMe() {
        click(verifyBtn);
        waitUntilSpecificTime(10);
    }
}
