package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ConfirmChangeBundlePage extends ServiceOrdersPage {
    public static ConfirmChangeBundlePage getInstance(){
        return new ConfirmChangeBundlePage();
    }

    @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]/following-sibling::td//span")
    WebElement subscriptionNumber;

    @FindBy(xpath = "//td[contains(text(),'Next Bill Date for this Account:')]/following-sibling::td//span")
    WebElement nextBillDateForThisAccount;

    @FindBy(xpath = "//td[contains(text(),'Current Tariff:')]/following-sibling::td//span")
    WebElement currentTariff;

    @FindBy(xpath = "//td[contains(text(),'Packaged Bundle:')]/following-sibling::td//span")
    WebElement packagedBundle;

    @FindBy(xpath = "//td[contains(text(),'Info:')]/following-sibling::td//span")
    WebElement infoBefore;

    @FindBy(xpath = "//td[contains(text(),'Total Recurring Bundle Charge:')]/following-sibling::td//span")
    WebElement totalRecurringBundleChargeBefore;

    @FindBy(xpath = "//td[contains(text(),'Total Recurring Bundle Charge:')]/following-sibling::td//span")
    WebElement totalRecurringBundleChargeAfter;

    @FindBy(xpath = "//td[contains(text(),'Total Recurring Bundle Charge:')]/following-sibling::td//span")
    List<WebElement> totalRecurringBundleCharge;

    @FindBy(xpath = "//td[contains(text(),'Recurring Bundles Charge Difference:')]/following-sibling::td//span")
    WebElement recurringBundlesChargeDifference;

    @FindBy(xpath = "//td[contains(text(),'Effective:')]/following-sibling::td//span")
    WebElement effective;

    @FindBy(xpath = "//td[@class='descError']")
    WebElement lblErrorEmssage;

    public String getSubscriptionNumber() {
        return getTextOfElement(subscriptionNumber);
    }

    public String getNextBillDateForThisAccount() {
        return getTextOfElement(nextBillDateForThisAccount);
    }

    public String getCurrentTariff() {
        return getTextOfElement(currentTariff);
    }

    public String getPackagedBundle() {
        return getTextOfElement(packagedBundle);
    }

    public String getInfoBefore() {
        return getTextOfElement(infoBefore);
    }

    public String getTotalRecurringBundleChargeBefore() {
        return getTextOfElement(totalRecurringBundleCharge.get(0));
    }

    public String getTotalRecurringBundleChargeAfter() {
        return getTextOfElement(totalRecurringBundleCharge.get(1));
    }

    public String getRecurringBundlesChargeDifference() {
        return getTextOfElement(recurringBundlesChargeDifference);
    }

    public String getEffective() {
        return getTextOfElement(effective);
    }

    public String getBundleInfo(String name){
        return getTextOfElement(getDriver().findElement(By.xpath("//td[contains(text(),'"+name+"')]/following-sibling::td//span")));
    }

    public String getErrorMessage(){
        return  getTextOfElement(lblErrorEmssage);
    }
}
