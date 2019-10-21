package logic.pages.care.find;

import logic.business.helper.MiscHelper;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


public class UnbilledSumaryPage extends BasePage {
    public static UnbilledSumaryPage getInstance() {
        return new UnbilledSumaryPage();
    }
    @FindBy(xpath = "//td[text()='Unbilled Summary Filter' and @class='informationBoxHeader']//ancestor::p//following-sibling::table")
    WebElement filter;

    public String getFirstFilter() {
        TableControlBase tableControlBase = new TableControlBase(filter);
        return getTextOfSelectedOption(tableControlBase.findCellByLabelText("Subscription:")).trim();
    }

    public void selectFilter(String text) {
        TableControlBase tableControlBase = new TableControlBase(filter);
        tableControlBase.selectDropBoxByVisibelText(tableControlBase.findCellByLabelText("Subscription:"), text);
    }

    @FindBy(xpath = "//input[@value='Find Now']")
    WebElement findNowBtn;
    public void clickFindNowBrn()
    {
        click(findNowBtn);
    }
    public void saveFileFromWebRequest( String imgFile){
        WebElement img= getDriver().findElement(By.xpath("//img[@id='imgUsage']"));
        String link=img.getAttribute("src");
        MiscHelper.saveFileFromWebRequest(link,imgFile);
    }
}

