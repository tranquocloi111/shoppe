package logic.pages.care.sidebar;

import logic.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LeftMenuPage extends BasePage {

    private static LeftMenuPage leftMenuPage = new LeftMenuPage();
    @FindBy(xpath = ".//td[@class='functions']//table")
    WebElement leftMainDiv;

    public static LeftMenuPage page() {
        if (leftMenuPage == null)
            return new LeftMenuPage();
        return leftMenuPage;
    }

    public void clickSummaryLink() {
        clickLinkByName("Summary");
    }

    public void clickDetailsLink() {
        clickLinkByName("Details");
    }

    public void clickSubscriptionsLink() {
        clickLinkByName("Subscriptions");
    }

    public void clickOtherChargesCreditsLink() {
        clickLinkByName("Other Charges/Credits");
    }

    public void clickServiceOrdersLink() {
        clickLinkByName("Service Orders");
    }

    private void clickLinkByName(String name){
        WebElement element = leftMainDiv.findElement(By.linkText(name));
        click(element);
        waitForPageLoadComplete(60);
    }


}
