package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ChangeCustomerTypePage extends ServiceOrdersPage {

    public static ChangeCustomerTypePage getInstance(){
        return new ChangeCustomerTypePage();
    }

    @FindBy(xpath = "//input[@name='PropFld_BUNAME']")
    WebElement txtBusinessName;

    @FindBy(xpath = "//select[@name='PropFld_BSTY']")
    WebElement drBillStyle;


    public void ChangeCustomerTypeFromConsumerToBusinessType(String businessName, String billStyle){
        enterValueByLabel(txtBusinessName, businessName);
        selectByVisibleText(drBillStyle, billStyle);

        clickNextBtn();
        clickNextBtn();
        clickReturnToCustomer();
    }
}