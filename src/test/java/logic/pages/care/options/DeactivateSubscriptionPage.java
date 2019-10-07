package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 9/08/2019
 */
public class DeactivateSubscriptionPage extends ServiceOrdersPage {
    public static class DeactivateSubscription extends DeactivateSubscriptionPage {
        private static DeactivateSubscription instance = new DeactivateSubscription();

        public static DeactivateSubscription getInstance() {
            return new DeactivateSubscription();
        }

        @FindBy(xpath = "//td[normalize-space(text())='Deactivate Subscription']//ancestor::form[1]")
        WebElement form;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Next Bill Date:')]/following-sibling::td//input")
        WebElement nextBillDate;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Customers email address:')]/following-sibling::td//input")
        WebElement customersEmailAddress;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Deactivation Date:')]/following-sibling::td//input")
        WebElement deactivationDate;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Deactivation Time:')]/following-sibling::td//input")
        WebElement deactivationTime;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Deactivation Reason:')]/following-sibling::td//select")
        WebElement deactivationReason;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Notes:')]/following-sibling::td//textarea")
        WebElement deactivationNotes;
        @FindBy(xpath = "//input[@type='checkbox' and not (@disabled)]")
        WebElement ckSubscription;

        public void setDeactivationDate(Date deactivateDate ) {
            String sDeactivationDate = Parser.parseDateFormate(deactivateDate, TimeStamp.DATE_FORMAT4);
            enterValueByLabel(deactivationDate, sDeactivationDate);
        }

        public void selectDeactivateBySubscription(String subNo) {
            String xpath = String.format("//td[contains(text(),'%s')]//input[@type='checkbox']", subNo);
            waitUntilElementClickable(getDriver().findElement(By.xpath(xpath)));
            click(getDriver().findElement(By.xpath(xpath)));
        }

        public void setNotes(String text) {
            deactivationNotes.sendKeys(text);
        }

        public void deactivateLastActiveSubscription() {
            Date deactivateDate = TimeStamp.TodayPlus1Day();
            setDeactivationDate(deactivateDate);
            setNotes("Deactivate Last Active Subscription on the Account");
            clickNextButton();
            ConfirmDeactivatingSubscription.instance.verifyConfirmDeactivatingSubscriptionIsDisplay();
            clickNextButton();
            clickReturnToCustomer();
        }

        public void enterDeactivateReason(String text) {
            selectByVisibleText(deactivationReason,text);
        }
    }

    public static class ConfirmDeactivatingSubscription extends DeactivateSubscriptionPage {
        private static ConfirmDeactivatingSubscription instance = new ConfirmDeactivatingSubscription();

        public static ConfirmDeactivatingSubscription getInstance() {
            if (instance == null) {
                return new ConfirmDeactivatingSubscription();
            }
            return instance;
        }

        @FindBy(xpath = "//form[@id='wizard']//td[contains(text(),'Confirm Deactivating Subscription')]")
        WebElement lblConfirmDeactivatingSubscription;

        @FindBy(xpath = "//td[@class='descError']")
        WebElement lblConfirmMessage;

        public boolean verifyConfirmDeactivatingSubscriptionIsDisplay() {
            return isElementPresent(lblConfirmDeactivatingSubscription);
        }

        public String getConfirmDeactivatingSubscription() {
            return getTextOfElement(lblConfirmMessage);
        }


    }

    public static class DeactivateSubscriptionRefundDetails extends DeactivateSubscriptionPage {
        private static DeactivateSubscriptionRefundDetails instance = new DeactivateSubscriptionRefundDetails();
        @FindBy(xpath = "//input[@value='New Card Details']")
        WebElement btnNewCardDetails;
        @FindBy(xpath = "//input[@name='PostCmdBtn_NEXT']")
        WebElement btnNextCardDetails;
        @FindBy(xpath = "//form[@id='wizard']//td[contains(text(),'Deactivate Subscription Refund Details')]")
        WebElement lblDeactivateSubscriptionRefundDetails;

        public static DeactivateSubscriptionRefundDetails getInstance() {
            if (instance == null)
                return new DeactivateSubscriptionRefundDetails();
            return instance;
        }

        public void clickNewCardDetailsButton() {
            clickByJs(btnNewCardDetails);
        }

        public void clickDeactivateSubscriptionNextButton() {
            click(btnNextCardDetails);
        }

        public boolean verifyDeactivateSubscriptionRefundDetailsSectionIsDisplay() {
            return isElementPresent(lblDeactivateSubscriptionRefundDetails);
        }
    }
}
