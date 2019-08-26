package logic.pages.selfcare;

import logic.business.entities.CardDetailsEntity;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MakeAOneOffPaymentPage extends BasePage {


    public static class OutStandingSection extends MakeAOneOffPaymentPage {
        public static OutStandingSection getInstance() {
            return new OutStandingSection();
        }

        @FindBy(xpath = "//b[text()='Amount outstanding on my account']//ancestor::div[@id='outstandingAmountHeadDisplay']//following-sibling::div[@id='outstandingAmountBodyDisplay']")
        WebElement parent;


        public String getCurrentAmountValue() {
            return getTextOfElement(parent.findElement(By.xpath("//td[text()='Current total amount overdue on my account:']/following-sibling::td")));
        }

        public String getOldestOutstandingValue() {
            return getTextOfElement(parent.findElement(By.xpath("//td[text()='Oldest outstanding bill on my account:']/following-sibling::td")));
        }

        public String getOptionValue() {
            return getTextOfElement(parent.findElement(By.xpath(".//tr[3]")));
        }


    }

    public static class CardDetailSection extends MakeAOneOffPaymentPage {
        public static CardDetailSection getInstance() {
            return new CardDetailSection();
        }

        @FindBy(xpath = "//b[text()='Payment card details']//ancestor::div[@id='cardHeadDisplay']//following-sibling::div[@id='cardBodyDisplay']")
        WebElement parent;

        @FindBy(xpath = "//input[@name='paymentAmountAsString' and @type='text']")
        WebElement paymentAmountTextBox;

        public void enterPaymentAmount(String amount) {
            enterValueByLabel(paymentAmountTextBox, amount);
            paymentAmountTextBox.sendKeys(Keys.ENTER);
        }

        public void selectCardType(String type) {
            selectByVisibleText(parent.findElement(By.xpath(".//td[text()='Payment card type:']//following-sibling::td/select")), type);
        }

        public void enterCardNumber(String cardNumber) {
            enterValueByLabel(parent.findElement(By.xpath(".//td[text()='Card number:']//following-sibling::td/input")), cardNumber);
        }

        public void enterCardSecurityCode(String code) {
            enterValueByLabel(parent.findElement(By.xpath(".//td[text()='Card security code:']//following-sibling::td/input")), code);
        }

        public void selectCardExpiryDate(String month, String year) {
            selectByVisibleText(parent.findElement(By.xpath(".//td[text()='Card expiry date:']//following-sibling::td//select[@id='cardExpiryMonth']")), month);
            selectByVisibleText(parent.findElement(By.xpath(".//td[text()='Card expiry date:']//following-sibling::td//select[@id='cardExpiryYear']")), year);
        }

        public void inputCardDetail(CardDetailsEntity careDetail) {
            selectCardType(careDetail.getCardType());
            enterCardNumber(careDetail.getCardNumber());
            enterCardSecurityCode(careDetail.getCardSecurityCode());
            selectCardExpiryDate(careDetail.getCardExpiryMonth(), careDetail.getCardExpiryYear());
        }

        public String getMessageAfterSubmitPayment() {
            WebElement parent = getDriver().findElement(By.xpath("//label[text()='Confirmed payment amount']"));
            click(parent.findElement(By.xpath(".//ancestor::td[@class='fieldKey']//following-sibling::td//span//a//img")));
            waitUntilElementVisible(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
            return getTextOfElement(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
        }
        public String getMessageDesirePayment() {
            WebElement parent = getDriver().findElement(By.xpath("//label[text()='Desired payment amount']"));
            click(parent.findElement(By.xpath(".//ancestor::td[@class='fieldKey']//following-sibling::td//span//a//img")));
            waitUntilElementVisible(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
            return getTextOfElement(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
        }
        @FindBy(id="paymentAmtLessthanMsg")
        WebElement alertLessThanMssg;
        public String getAlertMessagePaymentLessThan() {
            return getTextOfElement(alertLessThanMssg);
        }
        @FindBy(id="paymentAmtGreaterMsg")
        WebElement alertGreaterThanMssg;
        public String getAlertGreaterThanMssg() {
            return getTextOfElement(alertGreaterThanMssg);
        }


    }

    public static class PesonalDetailsSection extends MakeAOneOffPaymentPage {
        public static PesonalDetailsSection getInstance() {
            return new PesonalDetailsSection();
        }

        @FindBy(xpath = "//b[text()='Personal details']//ancestor::div[@id='currrenttDetailsHeadDisplay']//following-sibling::div[@id='currrentDetailsBodyDisplay']/table")
        WebElement table;
        TableControlBase tableControlBase = new TableControlBase(table);

        public String getCardHolderName()
        {
            return  getTextOfElement(tableControlBase.findCellByLabelText("Cardholder name:"));
        }
        public String getStreetaddress()
        {
            return  getTextOfElement(tableControlBase.findCellByLabelText("Street address:"));
        }
        public String getTown()
        {
            return  getTextOfElement(tableControlBase.findCellByLabelText("Town:"));
        }
        public String getPostCode()
        {
            return  getTextOfElement(tableControlBase.findCellByLabelText("Postcode:"));
        }
    }

}
