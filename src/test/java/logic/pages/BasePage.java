package logic.pages;


import framework.wdm.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

import java.util.*;

public class BasePage {

    public BasePage() {
        PageFactory.initElements(DriverFactory.getInstance().getDriver(), this);
    }

    //region Useful actions
    public boolean switchWindow(String title, boolean isParent) {
        if (!isParent) {
            try {
                Set<String> availableWindows = getDriver().getWindowHandles();
                if (!availableWindows.isEmpty()) {
                    for (String windowId : availableWindows) {
                        if (getDriver().switchTo().window(windowId).getTitle().equals(title)) {
                            getDriver().manage().window().maximize();
                            Thread.sleep(2000);
                            return true;
                        }
                    }
                }
            } catch (Throwable ex) {
                getDriver().switchTo().window(title);

            }
        } else {
            getDriver().switchTo().window(title);
            getDriver().manage().window().maximize();
        }
        return false;
    }

    protected void enterValueByLabel(WebElement element, String val) {
        element.clear();
        element.sendKeys(val);
    }

    protected void enterValueByJs(WebElement element, String val) {
        setClearInputValue(element);
        ((JavascriptExecutor) getDriver()).executeAsyncScript("arguments[0].value='" + val + "'", element);
    }

    protected void setClearInputValue(WebElement element) {
        ((JavascriptExecutor) getDriver()).executeAsyncScript("arguments[0].value=''", element);
    }

    protected WebElement getCell(WebElement tbl, int row, int col) {
        return tbl.findElement(By.xpath("//tr[" + row + "]/td[" + col + "]"));
    }


    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", element);
    }


    public void click(WebElement element) {
        element.click();
        waitForPageLoadComplete(100);
    }
    public void submit(WebElement element) {
        element.submit();
        waitForPageLoadComplete(100);
    }

    protected void selectByVisibleText(WebElement element, String value) {
        Select drpCountry = new Select(element);
        drpCountry.selectByVisibleText(value);
    }

//    protected void waitForPageLoadComplete(int specifiedTimeout) {
//        Wait<WebDriver> wait = new WebDriverWait(getDriver(), specifiedTimeout);
//        wait.until(driver -> String
//                .valueOf(((JavascriptExecutor) driver).executeScript("return document.readyState"))
//                .equals("complete"));
//    }

    public void waitForPageLoadComplete(int specifiedTimeout) {
        ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        };
        WebDriverWait wait = new WebDriverWait(DriverFactory.getInstance().getDriver(), specifiedTimeout);
        wait.until(pageLoadCondition);
    }

    protected WebDriver getDriver() {
        return DriverFactory.getInstance().getDriver();
    }

    public String getTextOfElement(WebElement element) {
        return element.getText();
    }

    public void hover(WebElement element) {
        Actions action = new Actions(getDriver());
        scrollToElement(element);
        action.moveToElement(element).build().perform();
    }

    protected WebElement getSpanByText(String text){
        String xpath = String.format("//span[contains(text(),'%s')]",text);
        return getDriver().findElement(By.xpath(xpath));
    }

    protected WebElement getInputByType(String type){
        String xpath = String.format("//input[@type='%s']",type);
        return getDriver().findElement(By.xpath(xpath));
    }

    protected WebElement getInputById(String id){
        String xpath = String.format("//input[@id='%s']",id);
        return getDriver().findElement(By.xpath(xpath));
    }
    protected WebElement getInputByPlaceHolder(String placeHolder){
        String xpath = String.format("//input[@placeholder='%s']",placeHolder);
        return getDriver().findElement(By.xpath(xpath));
    }

    protected  WebElement getSelectById(String id){
        String xpath= String.format("//select[@id='%s']",id);
        return getDriver().findElement(By.xpath(xpath));
    }
    protected  WebElement getButtonByText(String text){
        String xpath= String.format("//button[text()='%s']",text);
        return getDriver().findElement(By.xpath(xpath));
    }



    //endregion
}

