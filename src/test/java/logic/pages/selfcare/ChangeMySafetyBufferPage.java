package logic.pages.selfcare;

import logic.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ChangeMySafetyBufferPage extends BasePage {
    public static ChangeMySafetyBufferPage getInstance() {
        return new ChangeMySafetyBufferPage();
    }


    @FindBy(xpath = "//b[contains(text(),'Change or remove your safety buffer')]//ancestor::p//following-sibling::div[1]")
    WebElement changeOrRemoveYourSafetyBuffer;

    @FindBy(xpath = "//b[contains(text(),'Confirming your changes')]//ancestor::div[@id='confirmBillCapChanges']")
    WebElement comfirmYourChangesMssgBox;
    @FindBy(xpath = "//b[contains(text(),'When would you like your safety buffer to change?')]//following-sibling::div")
    WebElement whenWouldYouLikeToChangeMssgBox;

    public List<WebElement> getGreyPanelByIndex() {
        return getDriver().findElements(By.xpath("//div[@class='grey-panel']"));
    }

    @FindBy(xpath = "//div[@class='grey-panel']//a[@href='http://www.tescomobile.com/safetybuffer']")
    WebElement greyPanelLinkText;


    public void selectASafetyBufferByCode(String text) {
        selectRadioButtonByText(changeOrRemoveYourSafetyBuffer, text);
    }

    @FindBy(xpath = "//label[@id='flexibleCapWarning']")
    WebElement descreaseWarningMessage;

    @FindBy(xpath = "//label[@id='flexibleCapMessage']")
    WebElement flexibleCapMessage;
    @FindBy(xpath = "//div[@id='confirmBillCapChanges']//label[contains(text(),'Previous safety buffer')]//ancestor::td[1]//following::td[1]")
    WebElement previousSafetyBuffer;
    @FindBy(xpath = "//div[@id='confirmBillCapChanges']//label[contains(text(),'New safety buffer')]//ancestor::td[1]//following::td[1]")
    WebElement newSafetyBuffer;

    public String getDescreaseWarningMessage() {
        return getTextOfElement(descreaseWarningMessage);
    }

    public String getFlexibleCapMsg() {
        return getTextOfElement(flexibleCapMessage);
    }

    public String getPreviousSafetyBuffer() {
        return getTextOfElement(previousSafetyBuffer);
    }

    public String getNewSafetyBuffer() {
        return getTextOfElement(newSafetyBuffer);
    }

    @FindBy(xpath = "//label[contains(text(),'Mobile phone number')]//ancestor::td[1]//following-sibling::td")
    WebElement mobilePhoneNumber;
    @FindBy(xpath = "//label[contains(text(),'Next allowance date')]//ancestor::td[1]//following-sibling::td")
    WebElement nextAllowanceDate;
    @FindBy(xpath = "//label[contains(text(),'Monthly safety buffer')]//ancestor::td[1]//following-sibling::td")
    WebElement monthlySafetyBuffer;

    public String getMobilePhone() {
        return getTextOfElement(mobilePhoneNumber);
    }

    public String getnextAllowanceDate() {
        return getTextOfElement(nextAllowanceDate);
    }

    public String getMonthlySafetyBuffer() {
        return getTextOfElement(monthlySafetyBuffer);
    }

    public String getLinkText() {
        return getTextOfElement(greyPanelLinkText);
    }

    public boolean IsSaftyBufferSelected(String name) {
        WebElement checkbox = findCheckBox(changeOrRemoveYourSafetyBuffer, name);
        return checkbox.isSelected();
    }

    public boolean IsSaftyBufferExisted(String name) {
        WebElement checkbox = findCheckBox(changeOrRemoveYourSafetyBuffer, name);
        return checkbox.isDisplayed();
    }

    public boolean IsSaftyBufferEnable(String name) {
        WebElement checkbox = findCheckBox(changeOrRemoveYourSafetyBuffer, name);
        return checkbox.isEnabled();
    }

    public void selectWhenWouldLikeToChangeMethod(String name) {
        WebElement checkbox = findCheckBox(whenWouldYouLikeToChangeMssgBox, name);
        checkbox.click();
    }

    public void selectSafetyBuffer(String name) {
        WebElement checkbox = findCheckBox(changeOrRemoveYourSafetyBuffer, name);
        checkbox.click();
    }

    public boolean isWhenWouldLikeToChangeMethodEnable(String name) {
        WebElement checkbox = findCheckBox(changeOrRemoveYourSafetyBuffer, name);
        return checkbox.isEnabled();
    }

    public boolean isWhenWouldLikeToChangeMethodExist(String name) {
        WebElement checkbox = findCheckBox(changeOrRemoveYourSafetyBuffer, name);
        return checkbox.isDisplayed();
    }

    @FindBy(xpath = "//span[@id='waitLabelabel']")
    WebElement waitLabelLabel;

    @FindBy(xpath = "//label[@id='confirmWhenLabel']")
    WebElement comfirmWhenLabel;

    public String getComfirmMessage() {
        return getTextOfElement(comfirmWhenLabel) + "\r\n" + getTextOfElement(waitLabelLabel);
    }

    public String calculateNextAllowanceDate() {
        return super.getNextAllowanceDate();
    }

    public boolean checkRadioBoxExists(String name) {
        return findCheckBox(changeOrRemoveYourSafetyBuffer, name).isDisplayed();
    }

    @FindBy(xpath = "//b[normalize-space()='Change it now but only until my next bill date']//ancestor::td[1]//following-sibling::td")
    WebElement changeItNowButOnlyUntillMssg;
    @FindBy(xpath = "//b[normalize-space()='Change it now and keep it at this amount']//ancestor::td[1]//following-sibling::td")
    WebElement changeItNowAndKeepItAtThisAmount;
    @FindBy(xpath = "//b[normalize-space()='Change it from my next bill date']//ancestor::td[1]//following-sibling::td")
    WebElement changeItFromMyTextBillDate;

    public String getChangeItNowButOnlyUntillMyNextBillDate() {
        return getTextOfElement(changeItNowButOnlyUntillMssg);
    }

    public String getChangeITNowAndKeepAtThisAmount() {
        return getTextOfElement(changeItNowAndKeepItAtThisAmount);
    }

    public String getChangeItFromMyTextBillDate() {
        return getTextOfElement(changeItFromMyTextBillDate);
    }

    public boolean IsWhenWouldYouLikeYourSafetyBufferToChangeBlockDisplayed() {
        return whenWouldYouLikeToChangeMssgBox.isDisplayed();
    }
    public boolean IsComfirmingYourChangesTableDisplayed() {
        return comfirmYourChangesMssgBox.isDisplayed();
    }


    public static class ComfirmationYourChanges extends ChangeMySafetyBufferPage {
        public static ComfirmationYourChanges getInstance() {
            return new ComfirmationYourChanges();
        }

        @FindBy(xpath = "//b[contains(text(),'Confirming your changes')]//ancestor::div[@id='confirmBillCapChanges']")
        WebElement comfirmYourChangesMssgBox;

        @FindBy(xpath = "//label[normalize-space(text())='I’m ok with this.']//preceding::input[@type='checkbox']")
        WebElement agreeCheckBox;

        public void tickAgreeCheckBox() {
            waitUntilVisible(agreeCheckBox);
            if (agreeCheckBox.getAttribute("value").equalsIgnoreCase("unchecked"))
                click(agreeCheckBox);
        }
    }
}