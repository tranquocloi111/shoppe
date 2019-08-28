package logic.pages.selfcare;

import logic.business.entities.CardDetailsEntity;
import logic.business.entities.PaymentInfoEnity;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MyPaymentDetailsPage extends BasePage {


    public static class CurrentPaymentDetailsSection extends MyPaymentDetailsPage {
        public static CurrentPaymentDetailsSection getInstance() {
            return new CurrentPaymentDetailsSection();
        }

        @FindBy(xpath = "//b[text()='Current payment details']//ancestor::div[1]//following-sibling::div[@id='currrentPaymentBodyDisplay']")
        WebElement parent;
        TableControlBase tableControlBase = new TableControlBase(parent);

        public String getCardHolderName() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Cardholder name:"));
        }

        public String getAccountHolderName() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Name(s) of account holder:"));
        }

        public String getStreetaddress() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Street address:"));
        }

        public String getExpiryDate() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Card expiry date:"));
        }

        public String getCardNumber() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Card number:"));
        }

        public String getTown() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Town:"));
        }

        public String getPostCode() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Postcode:"));
        }

        public String getBankSortCode() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Bank sort code:"));
        }

        public String getBankAcountNumber() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Bank account number:"));
        }


    }

    public static class NewPaymentDetailsSection extends MyPaymentDetailsPage {
        public static NewPaymentDetailsSection getInstance() {
            return new NewPaymentDetailsSection();
        }

        @FindBy(xpath = "//b[text()='New payment details']//ancestor::div[1]//following-sibling::div[@id='cardBodyDisplay']")
        WebElement parent;

        @FindBy(xpath = "//select[@id='cardtype']")
        WebElement paymentMethod;
        @FindBy(id = "bankAccountNumber")
        WebElement bankAccountNumber;
        @FindBy(id = "accountName")
        WebElement accountName;
        @FindBy(id = "bankSortCodeSeg1")
        WebElement bankSortCode1;
        @FindBy(id = "bankSortCodeSeg2")
        WebElement bankSortCode2;
        @FindBy(id = "bankSortCodeSeg3")
        WebElement bankSortCode3;

        public void selectPaymentMethod(String type) {
            selectByVisibleText(paymentMethod, type);
        }


        public void enterCardHolder(String cardNumber) {
            enterValueByLabel(accountName, cardNumber);
        }


        public void enterBankAccountNumber(String number) {
            enterValueByLabel(bankAccountNumber, number);
        }

        public void enterBankBankSortCode(String code) {
            String[] codeList = code.split(" ");
            enterValueByLabel(bankSortCode1, codeList[0]);
            enterValueByLabel(bankSortCode2, codeList[1]);
            enterValueByLabel(bankSortCode3, codeList[2]);
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

        public void inputNewPaymentDetail(PaymentInfoEnity paymentInfoEnity) {
            selectPaymentMethod(paymentInfoEnity.getpaymentMethod());
            enterCardHolder(paymentInfoEnity.getbankAccountHolderName());
            enterBankAccountNumber(paymentInfoEnity.getbankAccountNumber());
            enterBankBankSortCode(paymentInfoEnity.getbankSortCode());

        }

        public void inputCardNewPaymentDetail(CardDetailsEntity cardDetailsEntity) {
            selectPaymentMethod(cardDetailsEntity.getCardType());
            enterCardNumber(cardDetailsEntity.getCardNumber());
            enterCardSecurityCode(cardDetailsEntity.getCardSecurityCode());
            selectCardExpiryDate(cardDetailsEntity.getCardExpiryMonth(),cardDetailsEntity.getCardExpiryYear());

        }


    }


}
