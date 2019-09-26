package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;


public class ChangeBundlePage extends ServiceOrdersPage {

    @FindBy(xpath = "//td[normalize-space(text())='Available Bundle(s)']//ancestor::form[1]")
    WebElement form;
    @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]/following-sibling::td//span")
    WebElement lblSubNumber;
    @FindBy(xpath = "//td[contains(text(),'Next Bill Date for this Account:')]/following-sibling::td//span")
    WebElement lblNextBillDateForThisAccount;
    @FindBy(xpath = "//td[contains(text(),'Current Tariff:')]/following-sibling::td//span")
    WebElement lblCurrentTariff;
    @FindBy(xpath = "//td[contains(text(),'Packaged Bundle:')]/following-sibling::td//span")
    WebElement lblPackagedBundle;
    @FindBy(xpath = "//td[contains(text(),'Info:')]/following-sibling::td//span")
    WebElement lblInfo;
    @FindBy(xpath = "//td[contains(text(),'When to apply change?:')]/following-sibling::td//span")
    WebElement lblWhenToApplyChangeText;

    @FindBy(xpath = "//td[contains(.,'Double Data')]")
    WebElement lblBonusBundle;

    public static ChangeBundlePage getInstance() {
        return  new ChangeBundlePage();
    }

    public String getSubscriptionNumber() {
        return getTextOfElement(lblSubNumber);
    }

    public String getNextBillDateForThisAccount() {
        return getTextOfElement(lblNextBillDateForThisAccount);
    }

    public String getCurrentTariff() {
        return getTextOfElement(lblCurrentTariff);
    }

    public String getPackagedBundle() {
        return getTextOfElement(lblPackagedBundle);
    }

    public String getWhenToApplyChangeText() {
        return getTextOfElement(lblWhenToApplyChangeText);
    }

    public String getInfo() {
        return getTextOfElement(lblInfo);
    }

    public Boolean bundleExists(String[] bundles) {
        int matchCount = 0;
        for (String bundle : bundles) {
            List<WebElement> tdCells = form.findElements(By.xpath(".//td"));
            for (WebElement cell : tdCells) {
                if (cell.getText().equalsIgnoreCase(bundle))
                    System.out.println("cell.getText().trim() : " + cell.getText().trim());
                matchCount++;
                break;
            }
        }
        if (bundles.length == matchCount)
            return true;
        else
            return false;
    }

    public String bundleToolTip(String bundle) {
        List<WebElement> tds = form.findElements((By.xpath(".//td")));
        try {
            for (WebElement td : tds) {
                if (td.getText().equalsIgnoreCase(bundle)) {
                    WebElement image = td.findElement(By.tagName("img"));
                    String js = image.getAttribute("onmouseover");
                    Actions a = new Actions(getDriver());
                    a.moveToElement(image).build().perform();
                    Thread.sleep(1000);
                    WebElement div = getDriver().findElement(By.xpath(".//body/div[last()]"));
                    return div.getText();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clickNextButton(){
        clickNextBtn();
    }

    public void selectBundlesByName(String[] names, String value){
        for (String name : names){
            WebElement tdCell = form.findElement(By.xpath(String.format(".//td[normalize-space(text())='%s']", name)));
            WebElement checkbox = tdCell.findElement(By.xpath(".//input[@type='checkbox']"));
            if (name.equalsIgnoreCase(value)) {
                if (checkbox.getAttribute("checked") != "true") {
                    click(checkbox);
                }
            }
        }
    }

    public boolean isBonusBundle(){
        return isElementPresent(lblBonusBundle);
    }

}
