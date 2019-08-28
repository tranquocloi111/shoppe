package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;

public class PaymentPage extends BasePage {


    public static class paymentContentGrid extends PaymentPage {

        public static paymentContentGrid getInstance() {
            return new paymentContentGrid();
        }

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Payments')]//ancestor::table[1]//following-sibling::div//table")
        WebElement paymentTable;
        TableControlBase table = new TableControlBase(paymentTable);

        public int getNumberPaymentRecord(HashMap<String, String> payment) {
            return table.findRowsByColumns(payment).size();
        }

        public void clickPaymentByType(String type) {
            table.getRowByColumnNameAndCellValue(type, type).findElement(By.tagName("a")).click();
        }

        public String getRefNoByType(String type) {
            return table.getRowByColumnNameAndCellValue(type, type).findElement(By.tagName("a")).getText();
        }
    }

}

