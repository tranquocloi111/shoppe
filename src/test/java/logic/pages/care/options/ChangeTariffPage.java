package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * User: Nhi Dinh
 * Date: 13/09/2019
 */
public class ChangeTariffPage extends ServiceOrdersPage {
    public static class ChangeTariff extends ChangeTariffPage{
        private static ChangeTariff instance = new ChangeTariff();
        public static ChangeTariff getInstance(){
            return new ChangeTariff();
        }

        @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]/following-sibling::td//span")
        WebElement lblSubNumber;
        @FindBy(xpath = "//td[contains(text(),'Next Bill Date for this Account:')]/following-sibling::td//span")
        WebElement lblNextBillDateForThisAccount;
        @FindBy(xpath = "//td[contains(text(),'Current Tariff:')]/following-sibling::td//span")
        WebElement lblCurrentTariff;
        @FindBy(xpath = "//td[contains(text(),'Packaged Bundle:')]/following-sibling::td//span")
        WebElement lblPackagedBundle;
        @FindBy(xpath = "//td[contains(text(),'New Tariff:')]/following-sibling::td//input[@type='Button']")
        WebElement btnNewTariff;
        @FindBy(xpath = "//td[contains(text(),'Notes:')]/following-sibling::td///textarea")
        WebElement txtNotes;

        public void setTextForNotes(String text){
            txtNotes.sendKeys(text);
        }
        public void clickButtonNewTariff(){
            btnNewTariff.click();
            waitForPageLoadComplete(60);
        }
        public String getPackagedBundle(){
            return lblPackagedBundle.getText();
        }
        public String getCurrentTariff(){
            return lblCurrentTariff.getText();
        }
        public String getNextBillDateForThisAccount(){
            return lblNextBillDateForThisAccount.getText();
        }
        public String getSubscriptionNumber(){
            return lblSubNumber.getText();
        }
    }
}
