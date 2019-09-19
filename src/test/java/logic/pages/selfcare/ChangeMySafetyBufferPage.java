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

    public List<WebElement> getGreyPanelByIndex() {
        return getDriver().findElements(By.xpath("//div[@class='grey-panel']"));
    }


    public void selectASafetyBufferByCode(String text) {
        selectRadioButtonByText(changeOrRemoveYourSafetyBuffer, text);
    }

    @FindBy(xpath = "//label[@id='flexibleCapWarning']")
    WebElement descreaseWarningMessage;
    @FindBy(xpath = "//div[@id='confirmBillCapChanges']//label[contains(text(),'Previous safety buffer')]//ancestor::td[1]//following::td[1]")
    WebElement previousSafetyBuffer;
    @FindBy(xpath = "//div[@id='confirmBillCapChanges']//label[contains(text(),'New safety buffer')]//ancestor::td[1]//following::td[1]")
    WebElement newSafetyBuffer;

    public String getDescreaseWarningMessage() {
        return getTextOfElement(descreaseWarningMessage);
    }

    public String getPreviousSafetyBuffer() {
        return getTextOfElement(previousSafetyBuffer);
    }

    public String getNewSafetyBuffer() {
        return getTextOfElement(newSafetyBuffer);
    }


}
