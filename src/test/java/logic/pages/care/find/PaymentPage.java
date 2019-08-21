package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;

public class PaymentPage extends BasePage {

    public static class ReceiptDetail extends PaymentPage {

        public static ReceiptDetail getInstance() {
            return new ReceiptDetail();
        }

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Receipt Details')]/../../..//following-sibling::div[1]//table")
        WebElement serviceOrdertable;
        TableControlBase tableControlBase = new TableControlBase(serviceOrdertable);

        public String getReceiptType() {
            return getTextOfElement(tableControlBase.getCellByLabel("Receipt Type"));
        }
        public String getReceiptStatus() {
            return getTextOfElement(tableControlBase.getCellByLabel("Receipt Status"));
        }
        public String getPaymentAmount() {
            return getTextOfElement(tableControlBase.getCellByLabel("Payment Amount"));
        }
        public String getPaymentCurrency() {
            return getTextOfElement(tableControlBase.getCellByLabel("Payment Currency"));
        }
        public String getCardType() {
            return getTextOfElement(tableControlBase.getCellByLabel("Card Type"));
        }
        public String getCardNumber() {
            return getTextOfElement(tableControlBase.getCellByLabel("Card Number"));
        }
    }

}
