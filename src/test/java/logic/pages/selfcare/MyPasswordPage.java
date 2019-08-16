package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MyPasswordPage extends BasePage {
    private static MyPasswordPage instance;
    @FindBy(id = "header")
    WebElement header;
    @FindBy(xpath = "//b[contains(text(),'My password')]//ancestor::table[1]//following-sibling::div[1]/table")
    WebElement table;
    TableControlBase tableControlBase = new TableControlBase(table);


    public static MyPasswordPage getInstance() {
        if (instance == null)
            return new MyPasswordPage();
        return instance;
    }

    public String getHeader() {
        waitUntilElementVisible(header);
        return getTextOfElement(header);
    }

    public WebElement getCurrentPasswordTextBox() {
        return tableControlBase.getPasswordTextBoxByText("Current password");
    }

    public void enterValueForCurrentPasswordTextBox(String text) {
        enterValueByLabel(getCurrentPasswordTextBox(), text);
    }

    public WebElement getNewPasswordTextBox() {
        return tableControlBase.getPasswordTextBoxByText("New password");
    }

    public void enterValueForNewPasswordTextBox(String text) {
        enterValueByLabel(getNewPasswordTextBox(), text);
    }

    public WebElement getConfirmationOfNewPasswordTextBox() {
        return tableControlBase.getPasswordTextBoxByText("Confirmation of new password:");
    }

    public void enterValueForConfirmationOfNewPasswordTextBox(String text) {
        enterValueByLabel(getConfirmationOfNewPasswordTextBox(), text);
    }

    public void clickContinueBtn() {
        clickLinkByText("Continue");
        waitForPageLoadComplete(10);
    }

    public void updateNewPassword(String currentPass, String newPass) {
        enterValueForCurrentPasswordTextBox(currentPass);
        enterValueForNewPasswordTextBox(newPass);
        enterValueForConfirmationOfNewPasswordTextBox(newPass);
        clickContinueBtn();
    }

    public void inputUsername(String username) {
        waitUntilElementVisible(getDriver().findElement(By.xpath("//input[@type='text' and @name ='emailAddr']")));
        enterValueByLabel(getDriver().findElement(By.xpath("//input[@type='text' and @name ='emailAddr']")), username);
    }

    public void inputSecurityAnswer(String answer) {
        enterValueByLabel(getDriver().findElement(By.xpath("//input[@type='text' and @name ='answer']")), answer);
    }
}

