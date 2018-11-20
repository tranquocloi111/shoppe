package logic.pages.care;

import javafx.util.Pair;
import logic.pages.BasePage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FindPage extends BasePage {

    @FindBy(xpath = "//*[@id='Validation']//following-sibling::table")
    WebElement tblFindDetail;

    @FindBy(xpath = "//input[@value='Find Now']")
    WebElement btnFindNow;

    @FindBy(xpath = "/html/body/div[2]/table/tbody/tr[1]/td[2]/div[2]/table/tbody/tr/td[1]/div/div[1]/table")
    WebElement tblResult;

    public void findCustomer(Pair<String, String>... pairs) {
        for (Pair<String, String> p : pairs) {
            enterValueByLabel(tblFindDetail, p.getKey(), p.getValue());
        }
        btnFindNow.click();
    }

    public void openCustomerByIndex(int index) {
        getCell(tblResult, index + 1, 2).click();
    }

}
