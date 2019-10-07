package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;

public class MyUsageDetailsSinceMyLastBillPage extends BasePage {

    public static MyUsageDetailsSinceMyLastBillPage getInstance() {
        return new MyUsageDetailsSinceMyLastBillPage();
    }


    public WebElement getDivPanelBox(String text) {
        String xpath = String.format("//td[text()[normalize-space()='%s']]//ancestor::div[@class='panelBox_']", text);
        return getDriver().findElement(By.xpath(xpath));
    }

    @FindBy(xpath = "//label[contains(text(),'My usage for')]//ancestor::td[1]//following-sibling::td/select")
    WebElement myUsageForSelect;

    @FindBy(xpath = "//label[contains(text(),'My usage for')]//ancestor::td[1]//following-sibling::td/a")
    WebElement myUsageForViewBtn;

    public void selectSubscriptionForUsage(String text) {
        selectByVisibleText(myUsageForSelect, text);
    }

    public void clickUsageViewBtn() {
        click(myUsageForViewBtn);
    }

    private void clickExpandBtnByName(String text) {
        String xpath = String.format("//td[text()[normalize-space()='%s']]//preceding::td[1]/a", text);
        click(getDriver().findElement(By.xpath(xpath)));
    }

    public void clickMonthlyChargeExpandBtn() {
        clickExpandBtnByName("Monthly Charges");
    }

    public void clickBundleChargeExpandBtn() {
        clickExpandBtnByName("Bundle Charges");
    }

    public void clickUsageChargeExpandBtn() {
        clickExpandBtnByName("Usage Charges");
    }

    public void clickAdjustmentChargesAndCreditsChargeExpandBtn() {
        clickExpandBtnByName("Adjustments, Charges and Credits");
    }

    public void clickSubscriptionPaymentsExpandBtn() {
        clickExpandBtnByName("Subscription Payments");
    }

    public WebElement getDropDownTable(String text) {
        String xpath = String.format("//td[text()[normalize-space()='%s']]//ancestor::tr[1]//following-sibling::tr//table", text);
        return getDriver().findElement(By.xpath(xpath));
    }

    public int getRowInDropDown(String text, HashMap<String, String> enity) {
        TableControlBase tableControlBase = new TableControlBase(getDropDownTable(text));
        return tableControlBase.findRowsByColumns(enity).size();
    }

    @FindBy(xpath = "//p[text()='Key:']//following-sibling::p")
    WebElement key;

    public String getKey() {
        return getTextOfElement(key);
    }

    @FindBy(xpath = "//label[text()='Usage type']//ancestor::td[1]//following-sibling::td/select[@name='searchCallType']")
    WebElement usageTypeSelect;
    @FindBy(xpath = "//label[text()='Start date']//ancestor::td[1]//following-sibling::td/input[@name='searchStartDate']")
    WebElement startDateTextBox;
    @FindBy(xpath = "//label[text()='Start date']//ancestor::td[1]//following-sibling::td/input[@name='searchEndDate']")
    WebElement endDateTextBox;
    @FindBy(xpath = "//a[@id='SearchBtn']")
    WebElement searchBtn;
    @FindBy(xpath = "//a[text()[normalize-space()='Next']]")
    WebElement nextLink;
    @FindBy(xpath = "//span[@class='pagebanner']")
    WebElement totalCountText;
    @FindBy(xpath = "//b[text()='Total duration / volume:']//ancestor::td[1]//following-sibling::td/b")
    WebElement totalDurationVolume;
    @FindBy(xpath = "//b[text()='Total cost:']//ancestor::td[1]//following-sibling::td/b")
    WebElement totalCost;
    @FindBy(xpath = "//b[text()='Total charge:']//ancestor::td[1]//following-sibling::td//b")
    WebElement totalCharge;
    @FindBy(xpath = "//b[text()='Call details']//ancestor::div[1]//following::div[1]/table[@class='DisplayTable']")
    WebElement callDetailTable;

    public String getStartDate()
    {
        return getValueOfElement(startDateTextBox);
    }
    public String getEndDate()
    {
        return getValueOfElement(endDateTextBox);
    }
    public void selectUsageType(String text)
    {
         selectByVisibleText(usageTypeSelect,text);
    }
    public void clickNextLink()
    {
        click(nextLink);
    }
    public String getToTalCountText()
    {
        return getTextOfElement(totalCountText);
    }
    public String getTotalCost()
    {
        return getTextOfElement(totalCost).trim();
    }
    public String getTotalCharge()
    {
        return getTextOfElement(totalCharge).trim();
    }
    public String getTotalDurationValue()
    {
        return getTextOfElement(totalDurationVolume);
    }
    public void clickSearchButton()
    {
        click(searchBtn);
    }
    public int getCountRowInCallDetailTable()
    {
        TableControlBase tableControlBase = new TableControlBase(callDetailTable);
        return tableControlBase.countTrElements()-1;
    }
}
