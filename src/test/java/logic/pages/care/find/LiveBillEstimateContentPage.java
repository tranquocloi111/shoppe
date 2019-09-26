package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.HashMap;
import java.util.List;

public class LiveBillEstimateContentPage extends BasePage {

    public static class LiveBillEstimate extends LiveBillEstimateContentPage{

        public WebElement getParent() {
            return getDriver().findElement(By.xpath("//td[contains(text(),'Live Bill Estimate')]/../../..//following-sibling::div[1]"));
        }

        public static class ChargesToDate extends LiveBillEstimate {
            WebElement sectionHeader = getParent().findElement(By.xpath(".//td[@class='SectionHeader' and text()='Charges to Date']"));
            WebElement cdHeaderRow;
            WebElement cdDiv;
            WebElement cdContentTable;

            public WebElement getCdHeaderRow() {
                return sectionHeader.findElement(By.xpath(".//ancestor::tr[1]"));
            }

            public WebElement getCdDiv() {
                return getCdHeaderRow().findElement(By.xpath(".//following-sibling::tr[1]")).findElement(By.tagName("div"));
            }

            public WebElement getCdContentTable() {
                return getCdDiv().findElement(By.xpath("table[@class='ContentTable']"));
            }

            public static class BillEstimatePerSubscription extends ChargesToDate {
                WebElement billEsHeaderRow;
                static WebElement billEsDiv;
                public BillEstimatePerSubscription(String title) {
                    WebElement td = getCdDiv().findElement(By.xpath(String.format(".//td[@class='GroupHeader' and contains(text(),'%s')]", title)));
                    billEsHeaderRow = td.findElement(By.xpath(".//ancestor::tr[1]"));
                    WebElement secondRow = billEsHeaderRow.findElement(By.xpath(".//following-sibling::tr[1]"));
                    billEsDiv = secondRow.findElement(By.xpath(".//div"));
                }

                public void expand(){
                   click(billEsHeaderRow.findElement(By.tagName("a")));
                }

                public class AdjustmentsChargesAndCredits{
                    WebElement adjustmentHeaderRow;
                    WebElement adjustmentTable;
                    TableControlBase tableControlBase;

                    public AdjustmentsChargesAndCredits() {
                        WebElement td = billEsDiv.findElement(By.xpath(".//table/tbody/tr/td[text()='Adjustments, Charges and Credits']"));
                        adjustmentHeaderRow = td.findElement(By.xpath(".//ancestor::tr[1]"));
                        WebElement secondRow = adjustmentHeaderRow.findElement(By.xpath(".//following-sibling::tr[1]"));
                        adjustmentTable = secondRow.findElement(By.tagName("table"));
                        tableControlBase = new TableControlBase(adjustmentTable);
                    }

                    public List<WebElement> getAdjustmentsChargesAndCredits(List<HashMap<String,String>> adjustments) {
                        return tableControlBase.findRowsByColumns(adjustments);
                    }

                    public int getNumberOfServiceOrders(List<HashMap<String, String>> adjustments) {
                        return tableControlBase.findRowsByColumns(adjustments).size();
                    }

                    public void expand(){
                        click(adjustmentHeaderRow.findElement(By.tagName("a")));
                    }

                    public List<List<String>> getAllValueAdjustmentsOrders() {
                        return tableControlBase.getAllCellValue();
                    }
                }

            }

        }

    }
}
