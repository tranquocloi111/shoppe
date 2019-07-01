package logic.pages.care.find;

import logic.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DetailsContentPage extends BasePage {

    public static class BillingInformationSectionPage extends DetailsContentPage{
        private static BillingInformationSectionPage instance;
        public static BillingInformationSectionPage getInstance(){
            if (instance == null)
                instance = new BillingInformationSectionPage();
            return instance;
        }

        @FindBy(xpath = "//td[contains(text(),'Billing Information')]/ancestor::table[1]/following-sibling::div[1]")
        WebElement parent;

        public  String getBillingGroup(){
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Billing Group:')]/following-sibling::td[1]")));
        }
    }

    public static class PaymentInformationPage extends DetailsContentPage{
        private static PaymentInformationPage instance;
        public static PaymentInformationPage getInstance(){
            if (instance == null)
                instance = new PaymentInformationPage();
            return instance;
        }

        @FindBy(xpath = "//td[contains(text(),'Payment Information')]/ancestor::table[1]/following-sibling::div[1]")
        WebElement parent;

        public  String getCardType(){
            return getTextOfElement(parent.findElement(By.xpath(".//td[contains(text(),'Card Type:')]/following-sibling::td[1]")));
        }

    }


}
