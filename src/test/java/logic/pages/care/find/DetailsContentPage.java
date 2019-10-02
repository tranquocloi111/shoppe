package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DetailsContentPage extends BasePage {

    public static class BillingInformationSectionPage extends DetailsContentPage {
        private static BillingInformationSectionPage instance = new BillingInformationSectionPage();

        public static BillingInformationSectionPage getInstance() {
            return new BillingInformationSectionPage();
        }

        @FindBy(xpath = "//td[contains(text(),'Billing Information')]/ancestor::table[1]/following-sibling::div[1]")
        WebElement parent;

        public String getBillingGroup() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Billing Group:')]/following-sibling::td[1]")));
        }
        public void changeBillNotification(String text) {
            selectDropBoxByVisibelText(parent.findElement(By.xpath(".//td[contains(text(),'Bill Notification:')]/following-sibling::td[1]")), text);
        }
        public String getMasterMPN() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Master MPN:')]/following-sibling::td[1]"))).split(" ")[0];
        }
        public String getBillStyle() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Bill Style:')]/following-sibling::td[1]"))).split(" ")[0];
        }
        public String getBillNotification(){
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Bill Notification:')]/following-sibling::td[1]"))).split(" ")[0];
        }
        public void changeBillStyle(String text) {
            selectDropBoxByVisibelText(parent.findElement(By.xpath(".//td[contains(text(),'Bill Style:')]/following-sibling::td[1]")), text);
        }

    }

    public static class PaymentInformationPage extends DetailsContentPage {
        private static PaymentInformationPage instance = new PaymentInformationPage();

        public static PaymentInformationPage getInstance() {
            return new PaymentInformationPage();
        }

        @FindBy(xpath = "//td[contains(text(),'Payment Information')]/ancestor::table[1]/following-sibling::div[1]")
        WebElement parent;
        @FindBy(xpath = "//td[contains(text(),'Payment Information')]/ancestor::table[1]/following-sibling::div[1]//table")
        WebElement paymentInfotable;
        TableControlBase tableControlBase=new TableControlBase(parent);

        public String getCardType() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Card Type:"));
        }
        public String getPaymentMethod() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Payment Method:"));
        }
        public String getBankSortCode() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Bank Sort Code:"));
        }
        public String getBankAccountNumber() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Bank Account Number"));
        }
        public String getBankAccountHolderName() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Bank Account Holder Name:"));
        }
        public String getDDIReference() {
            return getTextOfElement(tableControlBase.findCellByLabelText("DDI Reference:"));
        }
        public String getDDIStatus() {
            return getTextOfElement(tableControlBase.findCellByLabelText("DDI Status:"));
        }
        public String getCreditCardNumber() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Credit Card Number:"));
        }
        public String getCardExpireYear() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Credit Card Expiry Year:"));
        }
        public String getCardExpireMonth() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Credit Card Expiry Month:"));
        }
        public String getCreditCardHolderName() {
            return getTextOfElement(tableControlBase.findCellByLabelText("Credit Card Holder Name:"));
        }
    }

    public static class AddressInformationPage extends DetailsContentPage {
        private static AddressInformationPage instance = new AddressInformationPage();

        public static AddressInformationPage getInstance() {
            return new AddressInformationPage();
        }

        @FindBy(xpath = "//td[contains(text(),'Address Information')]/ancestor::table[1]/following-sibling::div[1]")
        WebElement parent;
        @FindBy(xpath = "//input[@value='Apply']")
        WebElement applyBtn;
        TableControlBase tableControlBase = new TableControlBase(parent);

        public String getAddressee() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Addressee:')]/following-sibling::td[1]")));
        }

        public void clickApplyBtn() {
            click((applyBtn));
        }

        public String getEmail() {
            return getTextOfElement(tableControlBase.getCellByLabel("Email Address"));
        }

        public String getMobileNumber() {
            return getTextOfElement(tableControlBase.getCellByLabel("Mobile Number"));
        }

        public void changeEmail(String email) {
            enterValueByLabel(getDriver().findElement(By.xpath(".//td[contains(text(),'Email Address:')]/following-sibling::td[1]//input")), email);
        }
        public String getEveningTelephoneNumber() {
            return getTextOfElement(tableControlBase.getCellByLabel("Evening Telephone Number"));
        }
        public String getDayTimeTelephoneNumber() {
            return getTextOfElement(tableControlBase.getCellByLabel("Daytime Telephone Number"));
        }

    }

    public static class CreditInformationPage extends DetailsContentPage {
        private static CreditInformationPage instance = new CreditInformationPage();

        public static CreditInformationPage getInstance() {
            return new CreditInformationPage();
        }

        @FindBy(xpath = "//td[contains(text(),'Credit Information')]/ancestor::table[1]/following-sibling::div[1]")
        WebElement parent;

        public String getClubCardNumber() {
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Club Card Number:')]/following-sibling::td[1]")));
        }
    }

}
