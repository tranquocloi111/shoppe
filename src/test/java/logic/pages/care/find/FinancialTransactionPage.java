package logic.pages.care.find;

import logic.business.entities.FinancialTransactionEnity;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class FinancialTransactionPage extends BasePage {

    public static class FinancialTransactionGrid extends FinancialTransactionPage {
        public static FinancialTransactionGrid getInstance() {
            return new FinancialTransactionGrid();
        }

        private final String DETAILS = "Details";

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Financial Transactions')]/../../..//following-sibling::div[1]//table")
        WebElement financialTransactionTable;
        TableControlBase tableControlBase = new TableControlBase(financialTransactionTable);

        public int getNumberOfFinancialTransaction(HashMap<String, String> financialTransaction) {
            return tableControlBase.findRowsByColumns(financialTransaction).size();
        }

        public void clickFinancialTransactionByDetail(String detail) {
            tableControlBase.getRowByColumnNameAndCellValue(DETAILS, detail).findElement(By.tagName("a")).click();
        }

        public List<List<String>> getAllValueOfFinancialTransaction(){
            return tableControlBase.getAllCellValue();
        }

        public String getRefNumberByDetail(String detail){
            return getTextOfElement(tableControlBase.getRowByColumnNameAndCellValue(DETAILS, detail).findElement(By.tagName("a")));
        }
    }
}
