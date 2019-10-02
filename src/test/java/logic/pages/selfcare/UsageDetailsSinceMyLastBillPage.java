package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;

public class UsageDetailsSinceMyLastBillPage extends BasePage {

        public static UsageDetailsSinceMyLastBillPage getInstance (){return new UsageDetailsSinceMyLastBillPage();}


    public WebElement getDivPanelBox(String text)
    {
        String xpath=String.format("//td[text()[normalize-space()='%s']]//ancestor::div[@class='panelBox_']",text);
       return getDriver().findElement(By.xpath(xpath));
    }

    @FindBy(xpath ="//label[contains(text(),'My usage for')]//ancestor::td[1]//following-sibling::td/select" )
    WebElement myUsageForSelect;

    @FindBy(xpath ="//label[contains(text(),'My usage for')]//ancestor::td[1]//following-sibling::td/a" )
    WebElement myUsageForViewBtn;

    public void selectSubscriptionForUsage(String text)
    {
        selectByVisibleText(myUsageForSelect,text);
    }
    public void clickUsageViewBtn()
    {
        click(myUsageForViewBtn);
    }
    private void clickExpandBtnByName(String text) {
        String xpath = String.format("//td[text()[normalize-space()='%s']]//preceding::td[1]/a", text);
        click(getDriver().findElement(By.xpath(xpath)));
    }
    public void clickMonthlyChargeExpandBtn()
    {
        clickExpandBtnByName("Monthly Charges");
    }
    public void clickBundleChargeExpandBtn()
    {
        clickExpandBtnByName("Bundle Charges");
    }
    public void clickUsageChargeExpandBtn()
    {
        clickExpandBtnByName("Usage Charges");
    }
    public void clickAdjustmentChargesAndCreditsChargeExpandBtn()
    {
        clickExpandBtnByName("Adjustments, Charges and Credits");
    }
    public void clickSubscriptionPaymentsExpandBtn()
    {
        clickExpandBtnByName("Subscription Payments");
    }
    public WebElement getDropDownTable(String text)
    {
        String xpath = String.format("//td[text()[normalize-space()='%s']]//ancestor::tr[1]//following-sibling::tr//table", text);
        return getDriver().findElement(By.xpath(xpath));
    }

    public int getRowInDropDown(String text, HashMap<String,String> enity)
    {
        TableControlBase tableControlBase = new TableControlBase(getDropDownTable(text));
        return tableControlBase.findRowsByColumns(enity).size();
    }
    @FindBy(xpath = "//p[text()='Key:']//following-sibling::p")
    WebElement key;
    public String getKey()
    {
        return getTextOfElement(key);
    }
}
