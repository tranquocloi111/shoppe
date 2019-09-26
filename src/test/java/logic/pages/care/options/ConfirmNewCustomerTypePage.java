package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


public class ConfirmNewCustomerTypePage extends ServiceOrdersPage {

    public static ConfirmNewCustomerTypePage getInstance(){
        return new ConfirmNewCustomerTypePage();
    }

    @FindBy(xpath = "//td[@class='descError']")
    WebElement lblErrorMessage;

    @FindBy(xpath = "//td[contains(text(),'Current Customer Type:')]//following-sibling::td")
    WebElement lblCurrentCustomerType;

    @FindBy(xpath = "//td[contains(text(),'New Customer Type:')]//following-sibling::td")
    WebElement lblNewCustomerType;

    public String getErrorMessage(){
        return getTextOfElement(lblErrorMessage);
    }

    public String getCurrentCustomerType(){
        return getTextOfElement(lblCurrentCustomerType);
    }

    public String getNewCustomerType(){
        return getTextOfElement(lblNewCustomerType);
    }

}
