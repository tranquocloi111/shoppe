package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * User: Nhi Dinh
 * Date: 24/07/2019
 */
public class ChangeSubscriptionNumberPage extends ServiceOrdersPage {
    public static class ChangeSubscriptionNumber extends ChangeSubscriptionNumberPage {
        private static ChangeSubscriptionNumber instance = new ChangeSubscriptionNumber();
        public static ChangeSubscriptionNumber getInstance() {
            return new ChangeSubscriptionNumber();
        }

        @FindBy(xpath = "//td[contains(text(),'Current Subscription Number:')]//following-sibling::td//option")
        WebElement ctlCurrentSubscriptionNumber;
        @FindBy(xpath = "//td[contains(text(),'New Subscription Number:')]//following-sibling::td//input")
        WebElement ctlNewSubscriptionNumber;
        @FindBy(xpath = "//td[contains(text(),'Next Bill Date:')]//following-sibling::td")
        WebElement lblNextBillDate;
        @FindBy(xpath = "//td[contains(text(),'Change Date:')]//following-sibling::td//input")
        WebElement ctlChangeDate;
        @FindBy(xpath = "//td[contains(text(),'Change Time:')]//following-sibling::td//input")
        WebElement ctlChangeTime;
        @FindBy(xpath = "//td[contains(text(),'Notes')]//following-sibling::td//textarea")
        WebElement ctlNotes;


        public String getCurrentSubscriptionNumber() {
            return getTextOfElement(ctlCurrentSubscriptionNumber);
        }

        public String getNewSubscriptionNumber() {
            return getTextOfElement(ctlNewSubscriptionNumber);
        }

        public void setNewSubscriptionNumber(String text) {
            ctlNewSubscriptionNumber.sendKeys(text);
        }

        public String getNextBillDate() {
            return getTextOfElement(lblNextBillDate);
        }

        public String getChangeDate() {
            return getValueOfElement(ctlChangeDate);
        }

        public String getNotes() {
            return getTextOfElement(ctlNotes);
        }

        public void setNotes(String text) {
            ctlNotes.sendKeys(text);
        }
    }

    public static class ConfirmChangingSubscriptionNumber extends ChangeSubscriptionNumberPage{
        private static ConfirmChangingSubscriptionNumber instance = new ConfirmChangingSubscriptionNumber();
        public static ConfirmChangingSubscriptionNumber getInstance() {
            return new ConfirmChangingSubscriptionNumber();
        }

        @FindBy(xpath = ".//td[@class='descError']")
        WebElement lblMessage;

        public String getConfirmMessage(){
            return getTextOfElement(lblMessage);
        }
    }
}
