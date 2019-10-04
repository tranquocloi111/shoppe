package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ApplyCreditPage extends ServiceOrdersPage {
    public static ApplyCreditPage getInstance(){
        return  new ApplyCreditPage();
    }

    @FindBy(name = "PropFld_CRAMT")
    WebElement txtCreditAmount;

    @FindBy(name = "PropFld_NOTES")
    WebElement notes;

    public void inputCreditAmount(String amount){
        enterValueByLabel(txtCreditAmount, amount);
        enterValueByLabel(notes, "Regression testing");
        clickNextButton();
    }
}
