package logic.pages.selfcare;

import framework.utils.RandomCharacter;
import logic.business.helper.MiscHelper;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class OrderConfirmationPage  extends BasePage {

    public static OrderConfirmationPage getInstance(){
        return  new OrderConfirmationPage();
    }

    @FindBy(xpath = "//b[contains(.,'Order #')]")
    WebElement lblOrderIdConfirmation;

    @FindBy(xpath = "//a[span[contains(.,'View and print contract')]]")
    WebElement btnViewAndPrintContract;

    @FindBy(id = "plugin")
    WebElement embeddedPdfForm;

    public String getOrderIdConfirmation(){
        return getTextOfElement(lblOrderIdConfirmation);
    }

    public void clickViewAndPrintContrat(){
        click(btnViewAndPrintContract);
    }

    public String saveContractPdfFile(String customerNumber){
        clickViewAndPrintContrat();
        String fileName = String.format("%s_%s_Contract.pdf", RandomCharacter.getRandomNumericString(9), customerNumber);
        savePDFFile(embeddedPdfForm, fileName, "Your Contract");

        return fileName;
    }

    public static class ProductSummary extends OrderConfirmationPage{
        public static ProductSummary getInstance(){
            return new ProductSummary();
        }

        @FindBy(xpath = "//b[contains(text(),'Product summary')]/following-sibling::div[1]//table")
        WebElement productSummarytable;
        TableControlBase tableControlBase = new TableControlBase(productSummarytable);

        public int getNumberOfProductSummary(String [] productSummary) {
            return tableControlBase.getRowsByColumnsWithIndex(productSummary).size();
        }
    }

    public static class OrderDetails extends OrderConfirmationPage{
        public static OrderDetails getInstance(){
            return new OrderDetails();
        }

        @FindBy(xpath = "//b[contains(text(),'Order Details')]/following-sibling::div[1]//table")
        WebElement orderDetailsTable;
        TableControlBase tableControlBase = new TableControlBase(orderDetailsTable);


        public List<String> getDetailItemsByFirstRow(String firstRowText){
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

        public String getMessageOfOrderConfirmation(){
            WebElement element = orderDetailsTable.findElement(By.xpath(".//following-sibling::div//td"));
            return getTextOfElement(element);
        }
    }


}
