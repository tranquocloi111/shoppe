package logic.pages.care.find;

//import javafx.util.Pair;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;

public class CommonContentPage extends BasePage {

    public static class SubscriptionsGridSectionPage extends CommonContentPage {
        private static final String subscriptionNumber = "Subscription Number";
        private static final String usageType = "Usage Type";
        private static final String startDate = "Start Date";
        private static final String endDate = "End Date";
        private static final String tariff = "Tariff";
        private static final String status = "Status";
        private static final String barring = "Barring";
        private static final String grg = "GRG";

        private static SubscriptionsGridSectionPage instance;

        public static SubscriptionsGridSectionPage getInstance() {
            if (instance == null)
                return new SubscriptionsGridSectionPage();
            return new SubscriptionsGridSectionPage();
        }

        @FindBy(xpath = "//td[(@class='informationBoxHeader' and contains(text(),'Subscriptions')) or (@class='informationBoxHeaderBusiness' and contains(text(),'Subscriptions'))]/../../..//following-sibling::div[1]//table")
        WebElement subscriptionsTable;

        TableControlBase table = new TableControlBase(subscriptionsTable);

        public String getValueOfSubscriptionsTable(int index, String value) {
            return table.getCellValueByColumnNameAndRowIndex(index + 1, value);
        }

        public int getRowNumberOfSubscriptionsTable() {
            return table.getRowsCount();
        }

        public List<WebElement> getSubscriptions(List<HashMap<String, String>> subscriptions) {
            return table.findRowsByColumns(subscriptions);
        }

        public String getSubscriptionNumberByIndex(int index) {
            return table.getElementByColumnNameAndRowIndex(index + 1, subscriptionNumber).getText().split(" ")[0];
        }

        public String getSubscriptionNumberAndNameByIndex(int index) {
            return table.getElementByColumnNameAndRowIndex(index + 1, subscriptionNumber).getText();
        }

        public String getSubscriptionNumberValue(String value) {
            return table.getRowByContainsColumnNameAndCellValue(subscriptionNumber, value).getText().split(" ")[0];
        }

        public String getStatusValue(String value) {
            String[] arrSubscriptions = table.getRowByContainsColumnNameAndCellValue(subscriptionNumber, value).getText().split(" ");
            return arrSubscriptions[arrSubscriptions.length - 1];
        }

        public String getStatusByIndex(int index) {
            return table.getElementByColumnNameAndRowIndex(index + 1, status).getText();
        }

        public void clickSubscriptionNumberLinkByIndex(int index) {
            click(table.getElementByColumnNameAndRowIndex(index + 1, subscriptionNumber).findElement(By.tagName("a")));
        }

        public void clickSubscriptionNumberLinkByCellValue(String cellValue) {
            clickByJs(table.getRowByCellValue(cellValue).findElement(By.tagName("a")));
        }

        public int getNumberOfSubscription(AbstractMap.SimpleEntry<String, String> subscriptions) {
            return table.findRowsByColumns(subscriptions).size();
        }

        public int getNumberOfSubscription(List<HashMap<String, String>> subscriptions) {
            return table.findRowsByColumns(subscriptions).size();
        }
        public int getNumberOfSubscription(HashMap<String, String> subscriptions) {
            return table.findRowsByColumns(subscriptions).size();
        }

        public void clickSubscriptionNumberByStatus(String status){
            try {
                click(getDriver().findElement(By.xpath("//td[a[.='"+status+"' and @class='informationBoxRow1']]//parent::tr//td[1]//a")));
            }catch (Exception ex){
                click(getDriver().findElement(By.xpath("//td[.='"+status+"']//parent::tr//td[1]//a")));
            }

        }

        public List<List<String>> getAllValueSubscription() {
            return table.getAllCellValue();
        }

        public String getEndDateByIndex(int index) {
            return table.getElementByColumnNameAndRowIndex(index + 1, endDate).getText();
        }
    }

    public static class CustomerSummarySectionPage extends CommonContentPage {
        private static CustomerSummarySectionPage instance = new CustomerSummarySectionPage();

        public static CustomerSummarySectionPage getInstance() {
            return new CustomerSummarySectionPage();
        }

        @FindBy(xpath = "//td[contains(text(),'Customer Summary')]/ancestor::table[1]/following-sibling::table[1]")
        WebElement parent;

        public String getCustomerSummaryEndDate() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'End Date:')]/following-sibling::td[1]")));
        }

        public String getBirthDay() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Birth Date:')]/following-sibling::td[1]")));
        }

        public String getAddress() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Address:')]/following-sibling::td[1]")));
        }

        public String getPostCode() {
            String postCode = getAddress();
           return postCode = postCode.split(",")[2];
        }

        public String getCustomerSummaryStartDate() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Start Date:')]/following-sibling::td[1]")));
        }

        public String getAmountBalance() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Account Balance:')]/following-sibling::td[1]")));
        }

        public String getCustomerType() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Customer Type:')]/following-sibling::td[1]")));
        }

        public String getStatus() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Status:')]/following-sibling::td[1]")));
        }

        public String getPassPhraseAnswer() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Pass Phrase Answer:')]/following-sibling::td[1]")));
        }
    }

    public static class RecentCustomersPage extends CommonContentPage{
        public static RecentCustomersPage getIntance(){
            return new RecentCustomersPage();
        }

        @FindBy(xpath = "//a[@class='RecentCust']")
        List<WebElement> recentCustomerList;

        public String getFirstRecentCustomer(){
            return   recentCustomerList.get(0).getAttribute("title");
        }
    }
}
