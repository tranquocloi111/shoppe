package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * User: Nhi Dinh
 * Date: 6/09/2019
 */
public class ChangeSafetyBufferPage extends ServiceOrdersPage {

    public static class ChangeSafetyBuffer extends ChangeSafetyBufferPage{
        private static ChangeSafetyBuffer instance = new ChangeSafetyBuffer();
        public static ChangeSafetyBuffer getInstance() {
            return new ChangeSafetyBuffer();
        }

        @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]/following-sibling::td//span")
        WebElement lblSubNumber;
        @FindBy(xpath = "//td[contains(text(),'Next Bill Date for this Account:')]/following-sibling::td//span")
        WebElement lblNextBillDateForThisAccount;
        @FindBy(xpath = "//td[contains(text(),'Current Tariff:')]/following-sibling::td//span")
        WebElement lblCurrentTariff;
        @FindBy(xpath = "//td[contains(text(),'Current Overage Cap Amount:')]/following-sibling::td//span")
        WebElement lblCurrentOverageCapAmount;



    }

}
