package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderConfirmationPage  extends BasePage {

    public static class ProductSummary extends OrderConfirmationPage{
        private static ProductSummary instance;
        public static ProductSummary getInstance(){
            if (instance == null)
                return new ProductSummary();
            return instance;
        }

        @FindBy(xpath = "//b[contains(text(),'Product summary')]/following-sibling::div[1]//table")
        WebElement productSummarytable;
        TableControlBase tableControlBase = new TableControlBase(productSummarytable);

        public int getNumberOfProductSummary(String [] productSummary) {
            return tableControlBase.getRowsByColumnsWithIndex(productSummary).size();
        }
    }

    public static class OrderDetails extends OrderConfirmationPage{

        private static OrderDetails instance;
        public static OrderDetails getInstance(){
            if (instance == null)
                return new OrderDetails();
            return instance;
        }

        @FindBy(xpath = "//b[contains(text(),'Order Details')]/following-sibling::div[1]//table")
        WebElement orderDetailsTable;
        TableControlBase tableControlBase = new TableControlBase(orderDetailsTable);


        public List<String> getDetailItemsByFirstRow(String firstRowText)
        {
            List<WebElement> tds = orderDetailsTable.findElements(By.tagName("td"));
            WebElement td = null;
            List<String> list = new ArrayList<>();
            for(WebElement t : tds){
                if (t.getText().contains(firstRowText)){
                    td = t;
                    break;
                }
            }

            List<WebElement> trs = td.findElement(By.xpath(".//ancestor::table[1]")).findElements(By.tagName("tr"));

            for (int i = 1; i < trs.size(); i++){
                if (trs.get(i).getText().toLowerCase().contains(firstRowText.toLowerCase()))
                {
                    list.add(trs.get(i).getText().trim());
                }
            }

            return list;
        }

        public int countRowOfDetailItemsByFirstRow(String firstRowText){
            return getDetailItemsByFirstRow(firstRowText).size();
        }

        public int getItemsCountByRowText(String text){
            List<WebElement> tds = orderDetailsTable.findElements(By.xpath(String.format(".//td[normalize-space(text())='%s']", text)));
            return tds.size();
        }

    }


}
