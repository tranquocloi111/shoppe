package logic.pages.care.sidebar;

import logic.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class OptionsMenuPage extends BasePage {

    private static OptionsMenuPage optionMenuPage = new OptionsMenuPage();
    @FindBy(xpath = ".//div[@class='OptionsFrame']")
    WebElement rightMainDiv;

    public static OptionsMenuPage page() {
        if (optionMenuPage == null)
            return new OptionsMenuPage();
        return optionMenuPage;
    }

    public void clickDeactivateAccountLink() {
        clickLinkByName("Deactivate Account");
    }

    public void clickChangeTariffLink() {
        clickLinkByName("Change Tariff");
    }

    public void clickChangeSubscriptionNumberLink() {
        clickLinkByName("Change Subscription Number");
    }

    private void clickLinkByName(String name) {
        WebElement element = rightMainDiv.findElement(By.linkText(name));
        click(element);
        waitForPageLoadComplete(60);
    }
}
