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
    WebElement msgBoxChangeOrRemoveYourSafetyBuffer;
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

    public List<WebElement> getGreyPanelByIndex() {
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

    public String getThirdPartyMssg()
    {
        List<WebElement> greyPanelList = getGreyPanelByIndex();
        return getTextOfElement(greyPanelList.get(1).findElement(By.tagName("a")));
    }

    public boolean isLinkHasAThirdPartyTermAndConditionPage()
    {
        List<WebElement> greyPanelList = getGreyPanelByIndex();
        return greyPanelList.get(1).findElement(By.tagName("a")).getAttribute("href").equalsIgnoreCase("http://www.tescomobile.com/safetybuffer");
    }




}
