package logic.pages.selfcare;

import logic.pages.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MyTariffAndCreditAgreementDocumentsPage extends BasePage {
    @FindBy(id = "header")
    WebElement header;
    private static MyTariffAndCreditAgreementDocumentsPage instance;

    public static MyTariffAndCreditAgreementDocumentsPage getInstance() {
        if (instance == null)
            return new MyTariffAndCreditAgreementDocumentsPage();
        return instance;
    }
    public String getHeader() {
        waitUntilElementVisible(header);
        return getTextOfElement(header);
    }


}
