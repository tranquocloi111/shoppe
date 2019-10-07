package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;

public class MyClubCardPointPage extends BasePage {

    public static MyClubCardPointPage getInstance() {
        return new MyClubCardPointPage();
    }

    @FindBy(xpath = "//div[contains(text(),'Clubcard number:')]")
    WebElement clubCardNumber;
    @FindBy(xpath = "//*[@id='body-content']")
    WebElement clubCardNote;
    @FindBy(xpath = "//a[@href='http://tescomobile.com/clubcard']")
    WebElement clubCardNote1Link;

    @FindBy(xpath = "//table[@id='clubcardTransaction']")
    WebElement clubCardTransactionTable;
    @FindBy(xpath = "//*[@class='pagelinks']")
    WebElement pageLink;
    @FindBy(xpath = "//*[@class='pagebanner']")
    WebElement pageBanner;
    @FindBy(xpath = "//td[text()='Invoice']//following-sibling::td[1]")
    WebElement lastReferenceNumber;

    public String getClubCardNumber() {
        return getTextOfElement(clubCardNumber);
    }
    public String getLastReferenceNumber() {
        return getTextOfElement(lastReferenceNumber);
    }

    public String getClubCardLinkText() {
        return getTextOfElement(clubCardNote1Link);
    }

    public String getClubCardNumberTagert() {
        return clubCardNote1Link.getAttribute("target");
    }

    public String getClubCardNumberLinkHref() {
        return clubCardNote1Link.getAttribute("href");
    }

    public String getPageBanner() {
        return getTextOfElement(pageBanner);
    }

    public String getClubCardNote() {
        String str = getTextOfElement(clubCardNote);
        return str.substring(str.indexOf("Please"),str.length());
    }

    public int getNumberRowExceptHeader() {
        TableControlBase tableControlBase = new TableControlBase(clubCardTransactionTable);
        return tableControlBase.countTrElements()-1;
    }

    public int findRowInTable(HashMap<String, String> entity) {
        TableControlBase tableControlBase = new TableControlBase(clubCardTransactionTable);
        return tableControlBase.findRowsByColumnsWithTagTh(entity).size();
    }

    public String getPageLinkText() {
        return getTextOfElement(pageLink);
    }
}
