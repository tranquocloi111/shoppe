package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class PaymentDetailPage extends BasePage {

    public static class ReceiptDetail extends PaymentDetailPage {

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
        public String getPaymentMethod() {
            return getTextOfElement(tableControlBase.getCellByLabel("Payment Method"));
        }
        public String getCardNumber() {
            return getTextOfElement(tableControlBase.getCellByLabel("Card Number"));
        }
        public String getPaymentReference() {
            return getTextOfElement(tableControlBase.getCellByLabel("Payment Reference"));
        }
        public String getVoucherSupplier() {
            return getTextOfElement(tableControlBase.getCellByLabel("Voucher Supplier"));
        }
        public String getVoucherCode() {
            return getTextOfElement(tableControlBase.getCellByLabel("Voucher Code"));
        }
        public String getVoucherProductCode() {
            return getTextOfElement(tableControlBase.getCellByLabel("Voucher Product Code"));
        }
        public String getOrderId() {
            return getTextOfElement(tableControlBase.getCellByLabel("Order Id"));
        }
    }

    public static class PaymentContentGrid extends PaymentDetailPage {

        public static PaymentContentGrid getInstance() {
            return new PaymentContentGrid();
        }

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Payments')]//ancestor::table[1]//following-sibling::div//table")
        WebElement paymentTable;
        TableControlBase table = new TableControlBase(paymentTable);
        public int getNumberPaymentRecord(HashMap<String,String> payment)
        {
            return table.findRowsByColumns(payment).size();
        }
        public void clickPaymentByType(String type) {
            table.getRowByColumnNameAndCellValue(type, type).findElement(By.tagName("a")).click();
        }
        public String getRefNoByType(String type) {
            return table.getRowByColumnNameAndCellValue(type, type).findElement(By.tagName("a")).getText();
        }
    }

    public static class ReceiptAllocation extends PaymentDetailPage {

        public static ReceiptAllocation getInstance() {
            return new ReceiptAllocation();
        }

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Receipt Allocations')]//ancestor::table[1]//following-sibling::div//table")
        WebElement receiptTable;
        TableControlBase table = new TableControlBase(receiptTable);

        public int getNumberReceiptRecord(HashMap<String, String> payment) {
            return table.findRowsByColumns(payment).size();
        }

        public List<List<String>> getAllValueOfReceiptAllocations(){
            return table.getAllCellValue();
        }

    }


}
