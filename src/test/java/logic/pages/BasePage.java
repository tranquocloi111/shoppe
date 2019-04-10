package logic.pages;

import framework.wdm.WdManager;
import logic.utils.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;

public class BasePage {

    public BasePage() {
        PageFactory.initElements(WdManager.getAjaxEle(), this);
    }

    //region Useful actions

    public static String splitSignatureCode(String imgUrl) {
        return imgUrl.split("uniqueCode=")[1];
    }

    public static String stripNonDigits(final CharSequence input) {
        final StringBuilder sb = new StringBuilder(
                input.length() /* also inspired by seh's comment */);
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c > 47 && c < 58) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public  boolean switchWindow(String title, boolean isParent) {
        if (!isParent) {
            String currentWindow = driver().getWindowHandle();
            Set<String> availableWindows = driver().getWindowHandles();
            if (!availableWindows.isEmpty()) {
                for (String windowId : availableWindows) {
                    if (driver().switchTo().window(windowId).getTitle().equals(title)) {
                        return true;
                    } else {
                        driver().switchTo().window(currentWindow);
                    }
                }
            }
        } else {
            driver().switchTo().window(title);
        }
        return false;
    }

    /**
     * @param tbl table which contains labels and values
     * @param lbl label to fill value in
     * @param val value to be filled
     */
    protected void enterValueByLabel(WebElement tbl, String lbl, String val) {
        WebElement ele =
                tbl.findElement(By.xpath("//tr/td[@class='label' and contains(text(),'" + lbl + "')]/following-sibling::td/input"));
        //ele.click();
        // ele.clear();
        ele.sendKeys(val);
    }

    protected void enterValueByLabel(String lbl, String val) {
        WebElement ele =
                driver().findElement(By.xpath("//tr/td[@class='label' and contains(text(),'" + lbl + "')]/following-sibling::td/input"));
        ele.click();
        ele.clear();
        ele.sendKeys(val);
    }

    protected void enterValueByLabel(WebElement element, String val) {
        element.click();
        element.clear();
        element.sendKeys(val);
    }

    /**
     * @param tbl table which contains labels and values
     * @param lbl label to get value
     * @return
     */
    protected String getValueByLabel(WebElement tbl, String lbl) {
        return tbl.findElement(By.xpath("")).getText();
    }

    protected WebElement getCell(WebElement tbl, int row, int col) {
        return tbl.findElement(By.xpath("//tr[" + row + "]/td[" + col + "]"));
    }

    public void waitUntilElementVisible(WebElement ele) {
        WdManager.getWait().until(ExpectedConditions.visibilityOf(ele));
    }

    public void waitUntilElementNotVisible(WebElement ele) {
        WdManager.getWait().until(ExpectedConditions.invisibilityOf(ele));
    }

    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void clickLinkByText(String text) {
        click(driver().findElement(By.linkText(text)));
    }

    public void click(WebElement element) {
        element.click();
    }

    protected void clickByJs(WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver();
        executor.executeScript("arguments[0].click();", element);
    }

    protected void selectByVisibleText(WebElement element, String value) {
        Select drpCountry = new Select(element);
        drpCountry.selectByVisibleText(value);
    }

    protected void selectByIndex(WebElement element, int index) {
        Select drpCountry = new Select(element);
        drpCountry.selectByIndex(index);
    }

    protected void selectByValue(WebElement element, String value) {
        Select drpCountry = new Select(element);
        drpCountry.selectByValue(value);
    }

    protected WebElement findControlByLabel(String label) {
        WebElement ele = null;
        try {
            ele = driver().findElement(By.xpath("//tr/td[contains(@class, 'label') and contains(text(),'" + label + "')]/following-sibling::td//input"));
        } catch (Exception ex) {
            ele = driver().findElement(By.xpath("//tr/td[contains(@class, 'label') and contains(text(),'" + label + "')]/following-sibling::td"));
        }
        return ele;
    }

    protected void waitForPageLoadComplete(int specifiedTimeout) {
        Wait<WebDriver> wait = new WebDriverWait(driver(), specifiedTimeout);
        wait.until(driver -> String
                .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
                .equals("complete"));
    }

    protected void clickSaveButton() {
        click(driver().findElement(By.xpath(".//input[@value='Save']")));
        waitForPageLoadComplete(60);
    }

    protected void clickNextButton() {
        click(driver().findElement(By.xpath(".//input[@value='Next >']")));
        waitForPageLoadComplete(60);
    }

    protected void clickDeleteButton() {
        click(driver().findElement(By.xpath(".//input[@value='Delete']")));
    }

    protected void clickReturnToCustomer() {
        click(driver().findElement(By.xpath(".//input[@value='Return to Customer']")));
        waitForPageLoadComplete(60);
    }

    public int getColumnIndex(WebElement table, String columnName) {
        int columnIndex = 0;
        List<WebElement> header = table.findElements(By.tagName("tr"));
        for (WebElement row : header) {
            List<WebElement> cols = row.findElements(By.xpath("td"));
            for (WebElement col : cols) {
                String str = col.getText().toString();
                if (str.equals(columnName)) {
                    columnIndex = cols.indexOf(col);
                    break;
                }
            }
        }
        return columnIndex;
    }

    public List<WebElement> getColumnByName(WebElement table, String columnName) {
        int columnIndex = getColumnIndex(table, columnName);
        List<WebElement> column = new ArrayList<>();
        List<WebElement> body = getBody(table);
        for (WebElement el : body) {
            column.add(el.findElements(By.tagName("td")).get(columnIndex));
        }
        return column;
    }

    public List<WebElement> findRowsByColumns(WebElement table, HashMap<String,String> columns) {
        int columnIndex = 0;
        boolean flag = false;
        List<WebElement> column = new ArrayList<>();
        List<WebElement> body = getBody(table);
        for (WebElement el : body){
            for (Map.Entry mapElement : columns.entrySet()) {
                String columnName = (String)mapElement.getKey();
                String cellValue = (String) mapElement.getValue();
                columnIndex = getColumnIndex(table, columnName);
                if(el.findElements(By.tagName("td")).get(columnIndex).getText().equalsIgnoreCase(cellValue)) {
                    flag = true;
                }else{
                    flag = false;
                    break;
                }
            }
            if (flag){
                column.add(el);
                flag = false;
            }
        }
        return column;
    }

    public WebElement getRowByColumnAndCellValue(WebElement element, String columnName, String cellValue) {
        List<WebElement> body = getBody(element);
        int columnIndex = getColumnIndex(element, columnName);
        for (WebElement e : body) {
            if (e.findElements(By.tagName("td")).get(columnIndex).getText().equalsIgnoreCase(cellValue)) {
                return e;
            }
        }
        return null;
    }


    public List<WebElement> getBody(WebElement element) {
        return element.findElements(By.xpath(".//tr[not(contains(@class,'ridHeader'))]"));
    }

    public void navigate(String url){
        driver().get(url);
    }

    protected WebDriver driver(){
        return WdManager.get();
    }

    //endregion
}
