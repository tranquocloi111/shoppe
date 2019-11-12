package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TransferExistingFundsPage extends ServiceOrdersPage {

    public static TransferExistingFundsPage getInstance(){
        return  new TransferExistingFundsPage();
    }

    @FindBy(name = "PropFld_DBAMT")
    WebElement txtAmountToRefund;

    @FindBy(name = "PropFld_NOTES")
    WebElement notes;

    public void inputAmountToRefund(String amount){
        enterValueByLabel(txtAmountToRefund, amount);
        enterValueByLabel(notes, "Regression testing");
        clickNextButton();
    }
}
