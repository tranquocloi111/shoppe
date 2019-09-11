package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * User: Nhi Dinh
 * Date: 10/09/2019
 */
public class ChangeBillCyclePage extends ServiceOrdersPage {

    public static class ChangeBillCycle extends ChangeBillCyclePage{
        private static ChangeBillCycle instance = new ChangeBillCycle();
        public static ChangeBillCycle getInstance() {
            return new ChangeBillCycle();
        }

        @FindBy(xpath = "//td[contains(text(),'Current Bill Cycle:')]/following-sibling::td//span")
        WebElement lblCurrentBillCycle;
        @FindBy(xpath = "//td[contains(text(),'Next Bill Date for Current Bill Cycle:')]/following-sibling::td//span")
        WebElement lblNextBillDateForCurrentBillCycle;
        @FindBy(xpath = "//td[contains(text(),'New Bill Cycle:')]/following-sibling::td//select")
        WebElement ddlNewBillCycle;
        @FindBy(xpath = "//td[contains(text(),'Notes:')]/following-sibling::td///textarea")
        WebElement txtNotes;

        public void setNotes(String text) {
            txtNotes.sendKeys(text);
        }

        public void selectNewBillCycle(String newBillCycle) {
            selectByVisibleText(ddlNewBillCycle, newBillCycle);
        }

        public String getCurrentBillCycle(){
            return lblCurrentBillCycle.getText();
        }

        public String getNextBillDateForCurrentBillCycle(){
            return lblNextBillDateForCurrentBillCycle.getText();
        }
    }
}
