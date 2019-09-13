package logic.pages.care.options;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;

/**
 * User: Nhi Dinh
 * Date: 13/09/2019
 */
public class TariffSearchPage extends BasePage {
    private static TariffSearchPage instance = new TariffSearchPage();
    public static TariffSearchPage getInstance(){
        return new TariffSearchPage();
    }

    @FindBy(xpath = "//select[@id='cboBillingType']")
    private WebElement cboBillingType;

    @FindBy(xpath = "//select[@id='cboTariffType']")
    private WebElement cboTariffType;

    @FindBy(xpath = "//select[@id='cboMonthlyRental']")
    private WebElement cboMonthlyRental;

    @FindBy(xpath = "//select[@id='cboSpecialTariff']")
    private WebElement cboSpecialTariff;

    @FindBy(xpath = "//select[@id='cboContractPeriod']")
    private WebElement cboContractPeriod;

    @FindBy(xpath = "//select[@id='cboEarlyTermChrg']")
    private WebElement cboEarlyTerminationCharge;

    @FindBy(xpath = "//select[@id='cboPastSaleDate']")
    private WebElement cboLastSaleDateExpired;

    @FindBy(xpath = "//select[@id='cboStaffTariff']")
    private WebElement cboStaffTariff;

    @FindBy(xpath = "//table[@id='tblSearchCriteria']//input[@name='btnfind']")
    private WebElement btnFind;

    @FindBy(xpath = "//table[@id='gridTariff']")
    private WebElement tableTariff;

    TableControlBase table = new TableControlBase(tableTariff);

    private int getNumberTariffRecord(HashMap<String,String> payment)
    {
        return table.findRowsByColumns(payment).size();
    }

    private void clickTariffByTariffCode(String tariffCode) {
        table.getRowByColumnNameAndCellValue("Tariff Code", tariffCode).findElement(By.tagName("a")).click();
    }
    private void clickFindButton(){
        btnFind.click();
    }

    public void selectTariffByCode(String tariffCode){
        String currentWindow = getDriver().getTitle();
        switchWindow("Tariff Search", false);
        clickTariffByTariffCode(tariffCode);
        switchWindow(currentWindow, false);
    }

}
