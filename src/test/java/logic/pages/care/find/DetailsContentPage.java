package logic.pages.care.find;

import logic.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DetailsContentPage extends BasePage {

    public static class BillingInformationSectionPage extends DetailsContentPage{
        private static BillingInformationSectionPage instance = new BillingInformationSectionPage();
        public static BillingInformationSectionPage getInstance(){
            return new BillingInformationSectionPage();
        }

        @FindBy(xpath = "//td[contains(text(),'Billing Information')]/ancestor::table[1]/following-sibling::div[1]")
        WebElement parent;

        public  String getBillingGroup(){
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Billing Group:')]/following-sibling::td[1]")));
        }
    }

    public static class PaymentInformationPage extends DetailsContentPage{
        private static PaymentInformationPage instance = new PaymentInformationPage();
        public static PaymentInformationPage getInstance(){
            return new PaymentInformationPage();
        }

        @FindBy(xpath = "//td[contains(text(),'Payment Information')]/ancestor::table[1]/following-sibling::div[1]")
        WebElement parent;

        public  String getCardType(){
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Card Type:')]/following-sibling::td[1]")));
        }

    }

    public static class AddressInformationPage extends DetailsContentPage{
        private static AddressInformationPage instance = new AddressInformationPage();
        public static AddressInformationPage getInstance(){
            return new AddressInformationPage();
        }

        @FindBy(xpath = "//td[contains(text(),'Address Information')]/ancestor::table[1]/following-sibling::div[1]")
        WebElement parent;

        public String getAddressee(){
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Addressee:')]/following-sibling::td[1]")));
        }
    }

    public static class CreditInformationPage extends DetailsContentPage{
        private static CreditInformationPage instance = new CreditInformationPage();
        public static CreditInformationPage getInstance(){
            return new CreditInformationPage();
        }

        @FindBy(xpath = "//td[contains(text(),'Credit Information')]/ancestor::table[1]/following-sibling::div[1]")
        WebElement parent;

        public String getClubCardNumber(){
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Club Card Number:')]/following-sibling::td[1]")));
        }
    }


}
