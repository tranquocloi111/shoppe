package logic.pages;

import framework.wdm.WdManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

import java.util.*;

public class BasePage {

    public BasePage() {
        PageFactory.initElements(WdManager.getAjaxEle(), this);
    }

    //region Useful actions
    public boolean switchWindow(String title, boolean isParent) {
        if (!isParent) {
            try {
                Set<String> availableWindows = getDriver().getWindowHandles();
                if (!availableWindows.isEmpty()) {
                    for (String windowId : availableWindows) {
                        if (getDriver().switchTo().window(windowId).getTitle().equals(title)) {
                            return true;
                        }
                    }
                }
            } catch (Throwable ex) {
                getDriver().switchTo().window(title);
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
        waitForPageLoadComplete(90);
    }

    public void click(WebElement element) {
        element.click();
        waitForPageLoadComplete(90);
    }

    protected void clickByJs(WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript("arguments[0].click();", element);
    }

    protected void clickByJs(String js, WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) getDriver();
        executor.executeScript(js, element);
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
        ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
            }
        };
        WebDriverWait wait = new WebDriverWait(getDriver(), specifiedTimeout);
        wait.until(pageLoadCondition);
    }

    protected void clickSaveButton() {
        click(getDriver().findElement(By.xpath(".//input[@value='Save']")));
        waitForPageLoadComplete(60);
    }

    protected void clickNextBtn() {
        click(getDriver().findElement(By.xpath(".//input[@value='Next >']")));
        waitForPageLoadComplete(60);
    }

    protected void clickDeleteButton() {
        click(getDriver().findElement(By.xpath(".//input[@value='Delete']")));
    }

    public void clickReturnToCustomer() {
        click(getDriver().findElement(By.xpath(".//input[@value='Return to Customer']")));
        waitForPageLoadComplete(90);
    }

    public void navigate(String url) {
        getDriver().get(url);
    }

    protected WebDriver getDriver() {
        return WdManager.get();
    }

    public WebElement findTdByLabelAndIndex(String lbl, int index) {
        return getDriver().findElement(By.xpath("//tr/td[contains(@class, 'label') and contains(text(),'" + lbl + "')]/following-sibling::td[" + index + "]"));
    }

    public String getTextOfElement(WebElement element) {
        return element.getText();
    }

    public String getValueOfElement(WebElement element) {
        return element.getAttribute("value");
    }


    public boolean isElementPresent(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (Throwable e) {
            return false;
        }
    }

    public String findValueByLabel(WebElement element, String label) {
        String xpath = ".//td[normalize-space(text())='%s']";
        WebElement td = element.findElement(By.xpath(String.format(xpath, label)));
        WebElement tr = td.findElement(By.xpath(".//ancestor::tr[1]"));
        return tr.findElement(By.xpath(".//td[4]")).getText();
    }

    public WebElement findLabelCell(WebElement element, String text) {
        List<WebElement> allLabels = element.findElements(By.xpath(".//td[@class='fieldKey']"));
        for (WebElement label : allLabels) {
            if (label.getText().trim().equalsIgnoreCase(text)) {
                return label;
            }
        }
        return null;
    }

    public WebElement findCheckBox(WebElement container, String name) {
        List<WebElement> labels = container.findElements(By.tagName("label"));
        for (WebElement label : labels) {
            if (label.getText().trim().equalsIgnoreCase(name)) {
                return label.findElement(By.tagName("input"));
            }
        }
        return null;
    }

    public void clickEditBtnByIndex(int index) {
        getDriver().findElements(By.xpath("//a[contains(text(),'Edit')]")).get(index).click();

    }

    public void clickEditBtnBySection(String sectionName) {
        String xpath = String.format("//td[contains(text(),'%s') and @class='informationBoxHeader' ]/ancestor::table[1]//a[@class='informationBoxHeader' and contains(text(),'Edit')]", sectionName);
        getDriver().findElement(By.xpath(xpath)).click();
    }

    @FindBy(xpath = "//input[@value='Apply']")
    WebElement applyBtn;

    public void clickApplyBtn() {
        click((applyBtn));
    }

    public void selectDropBoxByVisibelText(WebElement element, String text) {
        Select el = new Select(element.findElement(By.tagName("Select")));
        el.selectByVisibleText(text);
    }

    public void acceptComfirmDialog() {
        getDriver().switchTo().alert().accept();
    }

    public void dismissComfirmDialog() {
        getDriver().switchTo().alert().dismiss();
    }

    public void getTextComfirmDialog() {
        getDriver().switchTo().alert().getText();
    }

    public void clickSubmitBtn() {
        click(getDriver().findElement(By.xpath("//a[@id='SubmitBtn']")));
    }

    public void clickContinueBtn() {
        click(getDriver().findElement(By.xpath("//a[@id='ContinueBtn']")));
    }

    public WebDriver switchFrameByName(String name) {
        return getDriver().switchTo().frame(name);
    }

    public String getTextOfSelectedOption(WebElement element) {
        Select select = new Select(element.findElement(By.tagName("select")));
        return select.getFirstSelectedOption().getText();
    }

    public void clickHelpBtnByIndex(int index) {
        List<WebElement> helpList = getDriver().findElements(By.xpath("//img[@src='images/icons/qmark.jpg?v=2.60.0-SNAPSHOT']"));
        waitUntilElementClickable(helpList.get(index));
        click(helpList.get(index));
    }

    public void waitUntilElementClickable(WebElement ele) {
        WdManager.getWait().until(ExpectedConditions.elementToBeClickable(ele));
    }
    //endregion
}
