package logic.pages.care.main;

import logic.pages.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HeaderContentPage extends BasePage {
    public static HeaderContentPage getInstance(){
        return new HeaderContentPage();
    }

    @FindBy(xpath = "//div[contains(@class,'breadCrumbsBox')]")
    WebElement lblMainContainBreadCrumbsBox;

    @FindBy(xpath = "//td[contains(@class,'mainHeader')]")
    WebElement lblMainHeaderText;

    public String getMainContainBreadCrumbsBox(){
        return getTextOfElement(lblMainContainBreadCrumbsBox);
    }

    public String getMainHeaderSummaryText(){
        return getTextOfElement(lblMainHeaderText);
    }
}
