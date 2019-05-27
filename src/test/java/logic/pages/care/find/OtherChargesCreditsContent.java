package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class OtherChargesCreditsContent extends BasePage {

    private static OtherChargesCreditsContent instance = new OtherChargesCreditsContent();
    @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Other Charges/Credits')]/../../..//following-sibling::table")
    WebElement otherChargesCreditsContentTable;
    TableControlBase table = new TableControlBase(otherChargesCreditsContentTable);

    public static OtherChargesCreditsContent getInstance() {
        return instance;
    }

    public List<WebElement> getCharngeCredits(List<HashMap<String, String>> charngeCredit) {
        return table.findRowsByColumns(charngeCredit);
    }

    public int getRowNumberOfOtherChargesCreditsContentTable() {
        return table.getRowsCount();
    }
}
