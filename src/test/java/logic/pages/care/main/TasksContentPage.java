package logic.pages.care.main;

import javafx.util.Pair;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class TasksContentPage extends BasePage {

    public static class TaskSummarySectionPage extends TasksContentPage {
        private static TaskSummarySectionPage instance = new TaskSummarySectionPage();
        public static TaskSummarySectionPage getInstance() {
            if (instance == null)
                instance = new TaskSummarySectionPage();
            return  new TaskSummarySectionPage();
        }

        @FindBy(xpath = "//td[contains(text(),'Description:')]/following-sibling::td[1]")
        WebElement lblDescription;
        @FindBy(xpath = "//td[contains(text(),'Status:')]/following-sibling::td[1]")
        WebElement lblStatus;
        @FindBy(xpath = "//td[contains(text(),'SOID:')]/following-sibling::td[1]")
        WebElement lblSoID;

        public String getDescription() {
            return getTextOfElement(lblDescription);
        }

        public String getStatus() {
            return getTextOfElement(lblStatus);
        }

        public String getSoID() {
            return getTextOfElement(lblSoID);
        }

    }

    public static class TaskPage extends TasksContentPage {
        public static class EventsGridSectionPage extends TaskPage {
            private static EventsGridSectionPage instance = new EventsGridSectionPage();
            public static EventsGridSectionPage getInstance() {
                if (instance == null)
                    instance = new EventsGridSectionPage();
                return new EventsGridSectionPage();
            }

            @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Events')]/../../..//following-sibling::div[1]//td[@class='informationBox']//table")
            WebElement eventsGridPageTable;
            TableControlBase table = new TableControlBase(eventsGridPageTable);

            public List<WebElement> getEvents(Pair<String, String> events) {
                return table.findRowsByColumns(events);
            }

            public String getDescriptionByIndex(int index) {
                return table.getCellValueByColumnNameAndRowIndex(index, "Description");
            }

            public int getNumberOfEvents(Pair<String, String> events) {
                return table.findRowsByColumns(events).size();
            }

            public List<WebElement> getEvents(List<HashMap<String, String>> events) {
                return table.findRowsByColumns(events);
            }

            public int getNumberOfEvents(List<HashMap<String, String>> events) {
                return table.findRowsByColumns(events).size();
            }
            public int getNumberOfEventsByEvent(HashMap<String, String> event) {
                return table.getRowsByColumn(event).size();
            }

            public int getRowNumberOfEventGird(){
                return table.getRowsCount();
            }
        }

        public static class DetailsPage extends TaskPage {

            private static DetailsPage instance = new DetailsPage();
            public static DetailsPage getInstance() {
                if (instance == null)
                    instance = new DetailsPage();
                return new DetailsPage();
            }

            @FindBy(xpath = "//td[contains(text(),'Notification of low balance:')]/following-sibling::td[1]")
            WebElement lblNotificationOfLowBalance;

            @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]/following-sibling::td[1]")
            WebElement lblSubscriptionNumber;

            @FindBy(xpath = "//td[contains(text(),'Tariff:')]/following-sibling::td[1]")
            WebElement lblTariff;

            @FindBy(xpath = "//td[contains(text(),'Provisioning Date:')]/following-sibling::td[1]")
            WebElement lblProvisioningDate;

            @FindBy(xpath = "//td[contains(text(),'Bundles Added:')]/following-sibling::td[1]")
            WebElement lblBundlesAdded;

            @FindBy(xpath = "//td[contains(text(),'Bundles Removed:')]/following-sibling::td[1]")
            WebElement lblBundlesRemoved;

            @FindBy(xpath = "//td[contains(text(),'Payment Amount:')]/following-sibling::td[1]")
            WebElement lblPaymentAmount;

            @FindBy(xpath = "//td[contains(text(),'ReDS status authorisation:')]/following-sibling::td[1]")
            WebElement lblReDSStatusAuthorisation;

            @FindBy(xpath = "//td[contains(text(),'Order Type:')]/following-sibling::td[1]")
            WebElement lblOrderType;

            @FindBy(xpath = "//td[contains(text(),'Transaction Status:')]/following-sibling::td[1]")
            WebElement lblTransactionStatus;

            @FindBy(xpath = "//td[contains(text(),'Receipt Date:')]/following-sibling::td[1]")
            WebElement lblReceiptDate;

            @FindBy(xpath = "//td[contains(text(),'Sales Channel:')]/following-sibling::td[1]")
            WebElement lblSalesChannel;

            @FindBy(xpath = "//td[contains(text(),'End of Wizard Message:')]/following-sibling::td[1]")
            WebElement lblEndOfWizardMessage;

            @FindBy(xpath = "//td[contains(text(),'Temporary change flag:')]/following-sibling::td[1]")
            WebElement lblTemporaryChangeFlag;

            @FindBy(xpath = "//td[contains(text(),'EU Data Consent Flag:')]/following-sibling::td[1]")
            WebElement lblEUDataConsentFlag;

            public String getNotificationOfLowBalance() {
                return getTextOfElement(lblNotificationOfLowBalance);
            }

            public String getSubscriptionNumber() {
                return getTextOfElement(lblSubscriptionNumber);
            }

            public String getTariff() {
                return getTextOfElement(lblTariff);
            }

            public String getProvisioningDate() {
                return getTextOfElement(lblProvisioningDate);
            }

            public String getBundlesAdded() {
                return getTextOfElement(lblBundlesAdded);
            }

            public String getBundlesRemoved() {
                return getTextOfElement(lblBundlesRemoved);
            }

            public String getPaymentAmount(){
                return getTextOfElement(lblPaymentAmount);
            }

            public String getReDSStatusAuthorisation(){
                return getTextOfElement(lblReDSStatusAuthorisation);
            }


            public String getOrderType() {
                return getTextOfElement(lblOrderType);
            }

            public String getTransactionStatus(){
                return getTextOfElement(lblTransactionStatus);
            }

            public String getReceiptDate(){
                return getTextOfElement(lblReceiptDate);
            }

            public String getSalesChannel(){
                return getTextOfElement(lblSalesChannel);
            }

            public String getEndOfWizardMessage(){
                return getTextOfElement(lblEndOfWizardMessage);
            }

            public String getTemporaryChangeFlag(){
                return getTextOfElement(lblTemporaryChangeFlag);
            }

            public String getEUDataConsentFlag(){
                return getTextOfElement(lblEUDataConsentFlag);
            }
        }
    }

}
