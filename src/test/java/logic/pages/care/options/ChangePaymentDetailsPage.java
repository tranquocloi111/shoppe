package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.security.PublicKey;

public class ChangePaymentDetailsPage extends ServiceOrdersPage {
    public static ChangePaymentDetailsPage getInstance(){
        return new ChangePaymentDetailsPage();
    }

    @FindBy(name = "PropFld_NEWPAYMT")
    WebElement drPaymentMethod;

    @FindBy(xpath = "//td[contains(text(),'Bank Name:')]//following-sibling::td//input")
    WebElement txtBankName;

    @FindBy(xpath = "//td[contains(text(),'Bank Account Holder Name:')]//following-sibling::td//input")
    WebElement txtBankAccountHolderName;

    @FindBy(xpath = "//td[contains(text(),'Bank Account Number:')]//following-sibling::td//input")
    WebElement txtBankAccountNumber;

    public void enterNewPaymentDetailsForDD(String ... paymentInformation){
        selectByVisibleText(drPaymentMethod, paymentInformation[0]);
        enterValueByLabel(txtBankName, paymentInformation[1]);
        enterValueByLabel(txtBankAccountHolderName, paymentInformation[2]);
        enterValueByLabel(txtBankAccountNumber, paymentInformation[3]);

        clickNextBtn();
        clickReturnToCustomer();
    }

}
