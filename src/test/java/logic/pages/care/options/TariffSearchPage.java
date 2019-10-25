package logic.pages.care.options;

import logic.business.entities.TariffSearchCriteriaEnity;
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
    public static TariffSearchPage getInstance() {
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
    @FindBy(xpath = "//table[@id='tblSearchCriteria']")
    private WebElement criteriaFilterTable;

    @FindBy(xpath = "//table[@id='gridTariff']")
    private WebElement tableTariff;

    TableControlBase table = new TableControlBase(tableTariff);

    private int getNumberTariffRecord(HashMap<String, String> payment) {
        return table.findRowsByColumns(payment).size();
    }

    private void clickTariffByTariffCode(String tariffCode) {
        table.getRowByColumnNameAndCellValue("Tariff Code", tariffCode).findElement(By.tagName("a")).click();
    }

    private void clickFindButton() {
        btnFind.click();
    }

    public void selectTariffByCode(String tariffCode) {
        String currentWindow = getDriver().getTitle();
        switchWindow("Tariff Search", false);
        clickTariffByTariffCode(tariffCode);
        switchWindow(currentWindow, false);
    }

    public void searchAndSelectTariffByCode(TariffSearchCriteriaEnity tariffSearchEntity, String tariffCode) {
        String currentWindow = getDriver().getTitle();
        switchWindow("Tariff Search", false);
        searchTariffByCriteria(tariffSearchEntity);
        clickTariffByTariffCode(tariffCode);
        switchWindow(currentWindow, false);
    }

    public void selectTheFilterCriteria(String criteria, String value) {
        TableControlBase tableControlBase = new TableControlBase(criteriaFilterTable);
        if (value == null) {
            selectByVisibleText(tableControlBase.findCellByLabelText(criteria).findElement(By.tagName("select")), "");
        } else {
            selectByVisibleText(tableControlBase.getCellByLabel(criteria).findElement(By.tagName("select")), value);
        }
    }

    private void selectBillingType(String text) {
        if (text == null) {
            text = "";
        }
        selectByVisibleText(cboBillingType, text);
    }

    private void selectTariffType(String text) {
        if (text == null) {
            text = "";
        }
        selectByVisibleText(cboTariffType, text);
    }

    private void selectMonthlyRental(String text) {
        if (text == null) {
            text = "";
        }
        selectByVisibleText(cboMonthlyRental, text);
    }

    private void selectSpecialTariff(String text) {
        if (text == null) {
            text = "";
        }
        selectByVisibleText(cboSpecialTariff, text);
    }

    private void selectContactPeriod(String text) {
        if (text == null) {
            text = "";
        }
        selectByVisibleText(cboContractPeriod, text);
    }

    private void selectETC(String text) {
        if (text == null) {
            text = "";
        }
        selectByVisibleText(cboEarlyTerminationCharge, text);
    }

    private void selectLastDateExpired(String text) {
        if (text == null) {
            text = "";
        }
        selectByVisibleText(cboLastSaleDateExpired, text);
    }

    private void selectStaffTariff(String text) {
        if (text == null) {
            text = "";
        }
        selectByVisibleText(cboStaffTariff, text);
    }

    public void searchTariffByCriteria(TariffSearchCriteriaEnity entity) {
        switchWindow("Tariff Search", false);
        selectBillingType(entity.getBillingType());
        selectTariffType(entity.getTariffType());
        selectMonthlyRental(entity.getMonthlyRental());
       // selectSpecialTariff(enity.getSpecialTariff());
        selectContactPeriod(entity.getContractPeriod());
        selectETC(entity.getEarlyTerminationCharge());
        selectLastDateExpired(entity.getLastSaleDateExpired());
        selectStaffTariff(entity.getStaffTariff());
        clickFindButton();
    }
    public void clickNextBtn()
    {
        super.clickNextBtn();
    }

}
