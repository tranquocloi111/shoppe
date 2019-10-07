package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class LiveBillEstimateContentPage extends BasePage {

    public static class LiveBillEstimate extends LiveBillEstimateContentPage {

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

                public void expand() {
                    click(billEsHeaderRow.findElement(By.tagName("a")));
                }

                private void clickExpandBtnByName(String text) {
                    String xpath = String.format("//td[contains(text(),'%s')]//preceding::td[1]/a", text);
                    click(getDriver().findElement(By.xpath(xpath)));
                }

                public void clickBundleChargesExpandBtn() {
                    clickExpandBtnByName("Bundle Charges");
                }

                public class AdjustmentsChargesAndCredits {
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

                    public List<WebElement> getAdjustmentsChargesAndCredits(List<HashMap<String, String>> adjustments) {
                        return tableControlBase.findRowsByColumns(adjustments);
                    }

                    public int getNumberOfServiceOrders(List<HashMap<String, String>> adjustments) {
                        return tableControlBase.findRowsByColumns(adjustments).size();
                    }

                    public void expand() {
                        click(adjustmentHeaderRow.findElement(By.tagName("a")));
                    }

                    public List<List<String>> getAllValueAdjustmentsOrders() {
                        return tableControlBase.getAllCellValue();
                    }

                }

            }

        }

    }

    public static class BundleCharges extends LiveBillEstimate.ChargesToDate {
        public static BundleCharges getInstance() {
            return new BundleCharges();
        }

        @FindBy(xpath = "//td[contains(text(),'Bundle Charges')]//ancestor::tr[1]//following-sibling::tr/td[2]//table")
        WebElement bundleChargesTable;

        public int getRowInBundleCharge(HashMap<String, String> row) {
            TableControlBase tableControlBase = new TableControlBase(bundleChargesTable);
            return tableControlBase.findRowsByColumns(row).size();
        }
        public int getRowCount() {
            TableControlBase tableControlBase = new TableControlBase(bundleChargesTable);
            return tableControlBase.getAllRows().size()-1;
        }
    }

    public static class AccountPaymentsAndVouchers extends LiveBillEstimate.ChargesToDate {
        WebElement accountPaymentsAndVoucherHeaderRow;
        static WebElement accountPaymentAndVoucherDiv;
        WebElement accountPaymentsAndVoucherTable;
        TableControlBase tableControlBase;

        public AccountPaymentsAndVouchers() {
            WebElement td = getCdDiv().findElement(By.xpath(String.format(".//td[@class='GroupHeader' and contains(text(),'Account Payments and Vouchers')]" )));
            accountPaymentsAndVoucherHeaderRow = td.findElement(By.xpath(".//ancestor::tr[1]"));
            WebElement secondRow = accountPaymentsAndVoucherHeaderRow.findElement(By.xpath(".//following-sibling::tr[1]"));
            accountPaymentAndVoucherDiv = secondRow.findElement(By.xpath(".//div"));
            tableControlBase = new TableControlBase(accountPaymentsAndVoucherTable);
        }

        public void expand(){
            click(accountPaymentsAndVoucherHeaderRow.findElement(By.tagName("a")));
        }

        public String getReferenceByIndex(int index){
            return tableControlBase.getCellValueByColumnNameAndRowIndex(index, "Reference");
        }
    }
}
