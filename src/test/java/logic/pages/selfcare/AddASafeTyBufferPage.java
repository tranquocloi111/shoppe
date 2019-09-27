package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class AddASafeTyBufferPage extends BasePage {
    public static AddASafeTyBufferPage getInstance() {
        return new AddASafeTyBufferPage();
    }

    WebElement infoBox = super.getMssgBoxByIndex(1);

    @FindBy(xpath = "//div[contains(text(),'Choose your new safety buffer')]/table")
    WebElement msgBoxWhenWouldYouWantToChangeBuffer;

    @FindBy(xpath = "//label[contains(text(),'Mobile phone number')]//ancestor::td[1]//following-sibling::td")
    WebElement infoPhoneNumber;
    @FindBy(xpath = "//label[contains(text(),'Next allowance date')]//ancestor::td[1]//following-sibling::td")
    WebElement infoNextAllowanceDate;
    @FindBy(xpath = "//label[contains(text(),'Monthly safety buffer')]//ancestor::td[1]//following-sibling::td")
    WebElement infoMonthlySafetyBuffer;

    @FindBy(xpath = "//b[contains(text(),'Add a safety buffer')]//ancestor::p//following-sibling::div[1]")
    WebElement addASafetyBufferMsgBox;

    @FindBy(xpath = "//b[contains(text(),'Confirming your changes')]//ancestor::div[1]/table[1]")
    WebElement comfirmingYourChangesBox;

    public List<WebElement> getGreyPanelList() {
        return getDriver().findElements(By.xpath("//div[@class='grey-panel']"));
    }

    public String getMobilePhoneNumber() {
        return getTextOfElement(infoPhoneNumber);
    }

    public String getInfoNextAllowanceDate() {
        return getTextOfElement(infoNextAllowanceDate);
    }

    public String getInfoMonthlySafetyBuffer() {
        return getTextOfElement(infoNextAllowanceDate);
    }

    public String getThirdPartyMssg() {
        List<WebElement> greyPanelList = getGreyPanelList();
        return getTextOfElement(greyPanelList.get(0).findElement(By.tagName("a")));
    }

    public boolean isLinkHasAThirdPartyTermAndConditionPage() {
        List<WebElement> greyPanelList = getGreyPanelList();
        return greyPanelList.get(0).findElement(By.tagName("a")).getAttribute("href").equalsIgnoreCase("http://www.tescomobile.com/safetybuffer");
    }

    public boolean areAllItemsSafetyBuffer() {
        List<WebElement> labelList = addASafetyBufferMsgBox.findElements(By.tagName("label"));
        boolean flag = true;
        for (WebElement el : labelList) {
            if (!el.getText().contains("safety buffer")) {
                flag = false;
            }
        }
        return flag;
    }

    public String getSelectSafetyBufferMessage() {
        return getTextOfElement(addASafetyBufferMsgBox.findElement(By.tagName("Strong")));
    }
    public String getPreviousSafetyBuffer() {
        TableControlBase tableControlBase= new TableControlBase(comfirmingYourChangesBox);
       return getTextOfElement(tableControlBase.getCellByTagLabel("Previous safety buffer"));
    }
    public String getNewSafetyBuffer() {
        TableControlBase tableControlBase= new TableControlBase(comfirmingYourChangesBox);
        return getTextOfElement(tableControlBase.getCellByTagLabel("New safety buffer"));
    }

    public void selectSafetyBuffer(String name) {
        WebElement checkbox = findCheckBox(addASafetyBufferMsgBox, name);
        checkbox.click();
    }
    public String calculateNextAllowanceDate()
    {
        return super.getNextAllowanceDate();
    }
}
