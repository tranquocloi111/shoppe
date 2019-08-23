package logic.pages.selfcare;

import logic.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Test3DSecurePage extends BasePage {

    public static Test3DSecurePage getInstance() {
        return new Test3DSecurePage();
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
    }
}
