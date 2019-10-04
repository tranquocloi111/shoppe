package logic.pages.care.find;

import logic.business.helper.MiscHelper;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class UnbilledCallDetailsPage extends BasePage {
    public static UnbilledCallDetailsPage getInstance() {
        return new UnbilledCallDetailsPage();
    }

    @FindBy(id = "InventoryID")
    WebElement drSubscription;

    @FindBy(xpath = "//input[@value='Find Now']")
    WebElement findNowBtn;

    @FindBy(xpath = "//table[@class='instuctionalText']")
    WebElement instuctionalTextTable;
    TableControlBase instructionTableControl = new TableControlBase(instuctionalTextTable);

    public String get(int index){
        return getTextOfElement(instructionTableControl.getRecordByIndex(index));
    }

    public void selectSubscription(String sub){
        selectByVisibleText(drSubscription, sub);
    }

    public void clickFindNowBrn()
    {
        click(findNowBtn);
    }

}

