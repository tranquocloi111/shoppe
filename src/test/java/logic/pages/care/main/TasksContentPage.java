package logic.pages.care.main;

//import javafx.util.Pair;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;

public class TasksContentPage extends BasePage {

    public static class TaskSummarySectionPage extends TasksContentPage {
        private static TaskSummarySectionPage instance = new TaskSummarySectionPage();

        public static TaskSummarySectionPage getInstance() {
            if (instance == null)
                instance = new TaskSummarySectionPage();
            return new TaskSummarySectionPage();
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

            public List<WebElement> getEvents(AbstractMap.SimpleEntry<String, String> events) {
                return table.findRowsByColumns(events);
            }

            public String getDescriptionByIndex(int index) {
                return table.getCellValueByColumnNameAndRowIndex(index, "Description");
            }

            public int getNumberOfEvents(AbstractMap.SimpleEntry<String, String> events) {
                return table.findRowsByColumns(events).size();
            }

            public List<WebElement> getEvents(List<HashMap<String, String>> events) {
                return table.findRowsByColumns(events);
            }

            public int getNumberOfEvents(List<HashMap<String, String>> events) {
                return table.findRowsByColumns(events).size();
            }

            public int getNumberOfEventsByEvent(HashMap<String, String> event) {
                return table.findRowsByColumns(event).size();
            }

            public int getRowNumberOfEventGird() {
                return table.getRowsCount();
            }
            public String getDateTimeByIndex(int index) {
                return table.getCellValueByColumnNameAndRowIndex(index, "Date/Time");
            }

            public List<List<String>> getAllValueOfEvents(){
                return  table.getAllCellValue();
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

            @FindBy(xpath = "//td[contains(text(),'ReDS response Date:')]/following-sibling::td[1]")
            WebElement lblReDSRespondDate;

            @FindBy(xpath = "//td[contains(text(),'Customer Name:')]/following-sibling::td[1]")
            WebElement lblCustomerName;
            @FindBy(xpath = "//td[contains(text(),'HUB Customer ID:')]/following-sibling::td[1]")
            WebElement lblHUBCustomerID;

            @FindBy(xpath = "//td[contains(text(),'Order Type:')]/following-sibling::td[1]")
            WebElement lblOrderType;

            @FindBy(xpath = "//td[contains(text(),'Transaction Status:')]/following-sibling::td[1]")
            WebElement lblTransactionStatus;

            @FindBy(xpath = "//td[contains(text(),'Receipt Date:')]/following-sibling::td[1]")
            WebElement lblReceiptDate;

            @FindBy(xpath = "//td[contains(text(),'Receipt ID:')]/following-sibling::td[1]")
            WebElement lblReceiptID;

            @FindBy(xpath = "//td[contains(text(),'Sales Channel:')]/following-sibling::td[1]")
            WebElement lblSalesChannel;

            @FindBy(xpath = "//td[contains(text(),'End of Wizard Message:')]/following-sibling::td[1]")
            WebElement lblEndOfWizardMessage;

            @FindBy(xpath = "//td[contains(text(),'Temporary change flag:')]/following-sibling::td[1]")
            WebElement lblTemporaryChangeFlag;

            @FindBy(xpath = "//td[contains(text(),'EU Data Consent Flag:')]/following-sibling::td[1]")
            WebElement lblEUDataConsentFlag;

            @FindBy(xpath = "//td[contains(text(),'HUB Rejection Code:')]/following-sibling::td[1]")
            WebElement lblHubRejectionCode;

            @FindBy(xpath = "//td[contains(text(),'Contact Details:')]/following-sibling::td[1]")
            WebElement lblContactDetails;

            @FindBy(xpath = "//td[contains(text(),'Hierarchy Mbr:')]/following-sibling::td[1]")
            WebElement lblHierarchyMbr;

            @FindBy(xpath = "//td[contains(text(),'Invoice Number:')]/following-sibling::td[1]")
            WebElement lblInvoiceNumber;

            @FindBy(xpath = "//td[contains(text(),'Red Code:')]/following-sibling::td[1]")
            WebElement lblRedCode;

            @FindBy(xpath = "//td[contains(text(),'ReDS Authorisation Number:')]/following-sibling::td[1]")
            WebElement lblRedSAuthorisationNumber;

            @FindBy(xpath = "//td[contains(text(),'Tariff Product Code:')]/following-sibling::td[1]")
            WebElement lblTariffProductCode;

            @FindBy(xpath = "//td[contains(text(),'Service Order Start Date:')]/following-sibling::td[1]")
            WebElement lblServiceOrderStartDate;

            @FindBy(xpath = "//td[contains(text(),'ETC Override Amount:')]/following-sibling::td[1]")
            WebElement lblETCOverrideAmount;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'ETC Amount:')]/following-sibling::td[1]")
            WebElement lblETCAmount;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'Service No:')]/following-sibling::td[1]")
            WebElement lblServiceNo;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'Business Name:')]/following-sibling::td[1]")
            WebElement lblBusinessName;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'Current Customer Type:')]/following-sibling::td[1]")
            WebElement lblCurrentCustomerType;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'New Customer Type:')]/following-sibling::td[1]")
            WebElement lblNewCustomerType;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'Bill Style:')]/following-sibling::td[1]")
            WebElement lblBillStyle;

            @FindBy(xpath = "//td[contains(text(),'Subscription Number 2:')]/following-sibling::td[1]")
            WebElement lblSubscriptionNumber2;

            @FindBy(xpath = "//td[contains(text(),'Master MPN:')]/following-sibling::td[1]")
            WebElement lblMasterMpn;

            @FindBy(xpath = "//td[contains(text(),'Root BUID:')]/following-sibling::td[1]")
            WebElement lblRootBuid;

            @FindBy(xpath = "//td[contains(text(),'Deactivation Reason:')]/following-sibling::td[1]")
            WebElement lblDeactivationReason;

            @FindBy(xpath = "//td[contains(text(),'Barring Status - Outbound:')]/following-sibling::td[1]")
            WebElement lblBarringStatusOutbound;

            @FindBy(xpath = "//td[contains(text(),'Barring Status - Both-Way:')]/following-sibling::td[1]")
            WebElement lblBarringStatusBothWay;


            @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Details')]/../../..//following-sibling::div[1]//table")
            WebElement detailTable;
            TableControlBase tableControlBase = new TableControlBase(detailTable);

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

            public String getPaymentAmount() {
                return getTextOfElement(lblPaymentAmount);
            }

            public String getReDSStatusAuthorisation() {
                return getTextOfElement(lblReDSStatusAuthorisation);
            }


            public String getOrderType() {
                return getTextOfElement(lblOrderType);
            }

            public String getTransactionStatus() {
                return getTextOfElement(lblTransactionStatus);
            }

            public String getReceiptDate() {
                return getTextOfElement(lblReceiptDate);
            }

            public String getSalesChannel() {
                return getTextOfElement(lblSalesChannel);
            }

            public String getEndOfWizardMessage() {
                return getTextOfElement(lblEndOfWizardMessage);
            }

            public String getTemporaryChangeFlag() {
                return getTextOfElement(lblTemporaryChangeFlag);
            }

            public String getEUDataConsentFlag() {
                return getTextOfElement(lblEUDataConsentFlag);
            }

            public String getCreditCardExpiryYear() {
                return getTextOfElement(tableControlBase.getCellByLabel("Credit Card Expiry Year"));
            }

            public String getCardNumber() {
                return getTextOfElement(tableControlBase.getCellByLabel("Card Number:"));
            }

            public String getAmountToBeDebited() {
                return getTextOfElement(tableControlBase.getCellByLabel("Amount to be Debited:"));
            }

            public String getReDSResponseCode() {
                return getTextOfElement(tableControlBase.getCellByLabel("ReDS response code:"));
            }
            public String getReDSResponseDate() {
                return getTextOfElement(tableControlBase.getCellByLabel("ReDS response Date:"));
            }

            public String getCustomerName() {
                return getTextOfElement(lblCustomerName);
            }

            public String getCustomerID()
            {
                return  getTextOfElement(lblHUBCustomerID);
            }

            public String getHierarchyMbr()
            {
                return  getTextOfElement(lblHierarchyMbr);
            }

            public String getInvoiceNumber()
            {
                return  getTextOfElement(lblInvoiceNumber);
            }

            public String getContactDetail()
            {
                return  getTextOfElement(lblContactDetails);
            }

            public String getHubRejecttionCode()
            {
                return  getTextOfElement(lblHubRejectionCode);
            }

            public String getReceiptID()
            {
                return  getTextOfElement(lblReceiptID);
            }

            public String getRedCode()
            {
                return  getTextOfElement(lblRedCode);
            }

            public String getRedSAuthorisationNumber()
            {
                return  getTextOfElement(lblRedSAuthorisationNumber);
            }

            public String getServiceOrderStatus() {
                return getTextOfElement(tableControlBase.getCellByLabel("Service Order Status:"));
            }
            public String getDDIReference() {
                return getTextOfElement(tableControlBase.getCellByLabel("DDI Reference:"));
            }

            public String getCreditCardExpiryMonth() {
                return getTextOfElement(tableControlBase.getCellByLabel("Credit Card Expiry Month:"));
            }

            public String getCreditCardSecurityCode() {
                return getTextOfElement(tableControlBase.getCellByLabel("Credit Card Security Code:"));
            }
            public String getCreditCardHolderName() {
                return getTextOfElement(tableControlBase.getCellByLabel("Credit Card Holder Name:"));
            }

            public String getNewPaymentMethod() {
                return getTextOfElement(tableControlBase.getCellByLabel("New Payment Method:"));
            }

            public String getCardType() {
                return getTextOfElement(tableControlBase.getCellByLabel("Card Type:"));
            }

            public String getTariffProductCode(){
                return getTextOfElement(lblTariffProductCode);
            }

            public String getServiceOrderStartDate(){
                return getTextOfElement(lblServiceOrderStartDate);
            }

            public String getETCOverrideAmount(){
                return getTextOfElement(lblETCOverrideAmount);
            }

            public String getETCAmount(){
                return getTextOfElement(lblETCAmount);
            }

            public String getServiceNo(){
                return getTextOfElement(lblServiceNo);
            }

            public String getBusinessName(){
                return getTextOfElement(lblBusinessName);
            }

            public String getCurrentCustomerType(){
                return getTextOfElement(lblCurrentCustomerType);
            }

            public String getNewCustomerType(){
                return getTextOfElement(lblNewCustomerType);
            }

            public String getBillStyle(){
                return getTextOfElement(lblBillStyle);
            }

            public String getSubscriptionNumber2() {
                return getTextOfElement(lblSubscriptionNumber2);
            }

            public String getMasterMpn() {
                return getTextOfElement(lblMasterMpn);
            }

            public String getRootBuid() {
                return getTextOfElement(lblRootBuid);
            }

            public String getDeactivationReason() {
                return getTextOfElement(lblDeactivationReason);
            }

            public String getBarringStatusOutbound() {
                return getTextOfElement(lblBarringStatusOutbound);
            }

            public String getBarringStatusBothWay() {
                return getTextOfElement(lblBarringStatusBothWay);
            }
            public String getBankName() {
                return getTextOfElement(tableControlBase.getCellByLabel("Bank Name:"));
            }
            public String getBankSortCode() {
                return getTextOfElement(tableControlBase.getCellByLabel("Bank Sort Code:"));
            }
            public String getBankAccountNumber() {
                return getTextOfElement(tableControlBase.getCellByLabel("Bank Account Number:"));
            }
            public String getBankAccountHolderName() {
                return getTextOfElement(tableControlBase.getCellByLabel("Bank Account Holder Name:"));
            }
        }
    }

}
