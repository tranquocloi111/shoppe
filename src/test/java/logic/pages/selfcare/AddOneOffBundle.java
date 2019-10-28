package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class AddOneOffBundle extends BasePage {
    public static AddOneOffBundle getInstance() {
        return new AddOneOffBundle();
    }


    public void selectBundleByName(String name) {
        click(findCheckBox(name));
    }

    @FindBy(xpath = "//label[normalize-space(text())='Mobile phone number']//ancestor::td[1]//following-sibling::td")
    WebElement mobilePhoneNumber;
    @FindBy(xpath = "//label[normalize-space(text())='Tariff']//ancestor::td[1]//following-sibling::td")
    WebElement tariff;
    @FindBy(xpath = "//label[normalize-space(text())='Monthly allowance']//ancestor::td[1]//following-sibling::td")
    WebElement monthlyAllowance;
    @FindBy(xpath = "//label[normalize-space(text())='This month’s allowance expiry date']//ancestor::td[1]//following-sibling::td")
    WebElement expiryDate;
    @FindBy(xpath = "//td[text()='Current bundles']//ancestor::table[1]")
    WebElement currentBundleGrid;
    @FindBy(xpath = "//td[contains(text(),'One off data bundles')]//ancestor::table[1]")
    WebElement availableOneOffDataBundles;
    @FindBy(xpath = "//td[contains(text(),'One off text bundles')]//ancestor::table[1]")
    WebElement availableOneOffTextBundles;
    @FindBy(xpath = "//td[contains(text(),'One off data bundles')]//ancestor::div[1]")
    WebElement note;
    @FindBy(xpath = "//td[contains(text(),'One off data bundles')]//ancestor::table[1]//following-sibling::a/span")
    WebElement link;
    @FindBy(xpath = "//a[@href='http://www.tescomobile.com/hfh']")
    WebElement readOurFairUsagePolicyLink;
    @FindBy(xpath = "//td[contains(text(),'One off data bundles')]//ancestor::table[1]//following-sibling::p")
    WebElement flexibleCapTariffWarningDiv;

    public String getMobilePhoneNumber() {
        return getTextOfElement(mobilePhoneNumber);
    }

    public String getTariff() {
        return getTextOfElement(tariff);
    }

    public String getMonthlyAllowance() {
        return getTextOfElement(monthlyAllowance);
    }

    public String getExpiryDate() {
        return getTextOfElement(expiryDate);
    }

    public String getNote() {
        String text = getTextOfElement(note);
        text = text.substring(text.indexOf("* Note"));
        return text;
    }

    public int getRowCountCurrentBundle() {
        TableControlBase tableControlBase = new TableControlBase(currentBundleGrid);
        return tableControlBase.getRowsCountWithOutBoxRow() - 3;
    }

    public int findRowInCurrentBundle(HashMap<String, String> enity) {
        TableControlBase tableControlBase = new TableControlBase(currentBundleGrid);
        return tableControlBase.findRowsByColumns(enity).size();
    }

    public String getCurrentBundleDescriptionByCellValue(String value, int index) {
        TableControlBase table = new TableControlBase(currentBundleGrid);
        WebElement row = table.getRowByCellValue(value);
        return row.findElement(By.xpath(".//following-sibling::td[" + index + "]")).getText().trim();
    }

    @FindBy(xpath = "//td[normalize-space(text())='Total:']//following-sibling::td")
    WebElement total;

    public String gettotal() {
        return getTextOfElement(total);
    }

    public int getRowCountAvailableOneOffDataBundleGrid() {
        TableControlBase tableControlBase = new TableControlBase(availableOneOffDataBundles);
        return tableControlBase.getRowsCountWithOutBoxRow() - 1;
    }

    public int findRowInAvailableOneOffDataBundleGrid(HashMap<String, String> enity) {
        TableControlBase tableControlBase = new TableControlBase(availableOneOffDataBundles);
        return tableControlBase.findRowsByColumns(enity).size();
    }

    public int findRowInAvailableOneOffTextBundleGrid(HashMap<String, String> enity) {
        TableControlBase tableControlBase = new TableControlBase(availableOneOffTextBundles);
        return tableControlBase.findRowsByColumns(enity).size();
    }

    public boolean isReadOurFairUsagePolicyLinkDisplayed() {
        return readOurFairUsagePolicyLink.isDisplayed();
    }

    @FindBy(xpath = "//td[normalize-space(text())='Tariff charge:']//following-sibling::td")
    WebElement tariffCharge;

    @FindBy(xpath = "//td[normalize-space(text())='Total monthly bundle charge:']//following-sibling::td")
    WebElement totalyMonthlyBundlecharge;

    @FindBy(xpath = "//td[normalize-space(text())='New one-off bundle charge:']//following-sibling::td")
    WebElement newOneOffBundleCharge;

    @FindBy(xpath = "//td[normalize-space(text())='Total one-off bundle charge:']//following-sibling::td")
    WebElement totalOneOffBundleCharge;

    public String getTariffCharge() {
        return getTextOfElement(tariffCharge);
    }

    public String getTotalyMonthlyBundlecharge() {
        return getTextOfElement(totalyMonthlyBundlecharge);
    }

    public String getNewOneOffBundleCharge() {
        return getTextOfElement(newOneOffBundleCharge);
    }

    public String getTotalOneOffBundleCharge() {
        return getTextOfElement(totalOneOffBundleCharge);
    }


}
