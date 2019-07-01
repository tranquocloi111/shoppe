package logic.pages;

import framework.wdm.WdManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

import java.util.*;

public class BasePage {

    public BasePage() {
        PageFactory.initElements(WdManager.getAjaxEle(), this);
    }

    //region Useful actions
    public  boolean switchWindow(String title, boolean isParent) {
        if (!isParent) {
            String currentWindow = getDriver().getWindowHandle();
            Set<String> availableWindows = getDriver().getWindowHandles();
            if (!availableWindows.isEmpty()) {
                for (String windowId : availableWindows) {
                    if (getDriver().switchTo().window(windowId).getTitle().equals(title)) {
                        return true;
                    } else {
                        getDriver().switchTo().window(currentWindow);
                    }
                }
            }
        } else {
            getDriver().switchTo().window(title);
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
                getDriver().findElement(By.xpath("//tr/td[@class='label' and contains(text(),'" + lbl + "')]/following-sibling::td/input"));
        ele.click();
        ele.clear();
        ele.sendKeys(val);
    }

    protected void enterValueByLabel(WebElement element, String val) {
        element.click();
        element.clear();
        element.sendKeys(val);
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
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void clickLinkByText(String text) {
        click(getDriver().findElement(By.linkText(text)));
        waitForPageLoadComplete(60);
    }

    public void click(WebElement element) {
        element.click();
        waitForPageLoadComplete(60);
    }

    protected void clickByJs(WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript("arguments[0].click();", element);
    }

    protected void clickByJs(String js) {
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript(js);
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
            ele = getDriver().findElement(By.xpath("//tr/td[contains(@class, 'label') and contains(text(),'" + label + "')]/following-sibling::td//input"));
        } catch (Exception ex) {
            ele = getDriver().findElement(By.xpath("//tr/td[contains(@class, 'label') and contains(text(),'" + label + "')]/following-sibling::td"));
        }
        return ele;
    }

//    protected void waitForPageLoadComplete(int specifiedTimeout) {
//        Wait<WebDriver> wait = new WebDriverWait(getDriver(), specifiedTimeout);
//        wait.until(driver -> String
//                .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
//                .equals("complete"));
//    }

    public void waitForPageLoadComplete(int specifiedTimeout) {
        ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(getDriver(), specifiedTimeout);
        wait.until(pageLoadCondition);
    }

    protected void clickSaveButton() {
        click(getDriver().findElement(By.xpath(".//input[@value='Save']")));
        waitForPageLoadComplete(60);
    }

    protected void clickNextButton() {
        click(getDriver().findElement(By.xpath(".//input[@value='Next >']")));
        waitForPageLoadComplete(60);
    }

    protected void clickDeleteButton() {
        click(getDriver().findElement(By.xpath(".//input[@value='Delete']")));
    }

    protected void clickReturnToCustomer() {
        click(getDriver().findElement(By.xpath(".//input[@value='Return to Customer']")));
        waitForPageLoadComplete(90);
    }

    public void navigate(String url){
        getDriver().get(url);
    }

    protected WebDriver getDriver(){
        return WdManager.get();
    }

    public WebElement findTdByLabelAndIndex(String lbl, int index){
        return getDriver().findElement(By.xpath("//tr/td[contains(@class, 'label') and contains(text(),'" + lbl + "')]/following-sibling::td["+index+"]"));
    }

    public String getTextOfElement(WebElement element){
        return element.getText();
    }

    public boolean isElementPresent(WebElement element) {
        try {
            return element.isDisplayed();
        }
        catch (Throwable e) {
            return false;
        }
    }

    public String findValueByLabel(WebElement element,  String label){
        String xpath = xpath = ".//td[normalize-space(text())='%s']";;
        WebElement td = element.findElement(By.xpath(String.format(xpath, label)));
        WebElement tr = td.findElement(By.xpath(".//ancestor::tr[1]"));
        return tr.findElement(By.xpath(".//td[4]")).getText();
    }

    //endregion
}
