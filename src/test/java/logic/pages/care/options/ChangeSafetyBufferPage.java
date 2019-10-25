package logic.pages.care.options;

import logic.pages.care.main.ServiceOrdersPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * User: Nhi Dinh
 * Date: 6/09/2019
 */
public class ChangeSafetyBufferPage extends ServiceOrdersPage {

    public static class ChangeSafetyBuffer extends ChangeSafetyBufferPage {
        private static ChangeSafetyBuffer instance = new ChangeSafetyBuffer();

        public static ChangeSafetyBuffer getInstance() {
            return new ChangeSafetyBuffer();
        }

        @FindBy(xpath = "//td[normalize-space(text())='Available Safety Buffer(s)']//ancestor::form[1]")
        WebElement form;
        @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]/following-sibling::td//span")
        WebElement lblSubNumber;
        @FindBy(xpath = "//td[contains(text(),'Next Bill Date for this Account:')]/following-sibling::td//span")
        WebElement lblNextBillDateForThisAccount;
        @FindBy(xpath = "//td[contains(text(),'Current Tariff:')]/following-sibling::td//span")
        WebElement lblCurrentTariff;
        @FindBy(xpath = "//td[contains(text(),'Current Overage Cap Amount:')]/following-sibling::td//span")
        WebElement lblCurrentOverageCapAmount;
        @FindBy(xpath = "//td[contains(text(),'When to apply change?:')]/following-sibling::td//select")
        WebElement optWhenToApplyChangeText;

        public void selectWhenToApplyChangeText(String value) {
            selectByVisibleText(optWhenToApplyChangeText, value);
        }

        public void selectBundlesByName(String[] names, String value) {
            for (String name : names) {
                WebElement tdCell = form.findElement(By.xpath(String.format(".//td[normalize-space(text())='%s']", name)));
                WebElement checkbox = tdCell.findElement(By.xpath(".//input[@type='checkbox']"));
                if (name.equalsIgnoreCase(value)) {
                    click(checkbox);
                }
            }
        }

        public void unSelectBundlesByName(String[] names, String value) {
            for (String name : names) {
                WebElement tdCell = form.findElement(By.xpath(String.format(".//td[normalize-space(text())='%s']", name)));
                WebElement checkbox = tdCell.findElement(By.xpath(".//input[@type='checkbox']"));
                if (name.equalsIgnoreCase(value)) {
                    if (checkbox.getAttribute("checked").equals("true")) {
                        click(checkbox);
                    }
                }
            }
        }


        public void selectBundlesByName(String value) {
            WebElement tdCell = form.findElement(By.xpath(String.format(".//td[normalize-space(text())='%s']", value)));
            WebElement checkbox = tdCell.findElement(By.xpath(".//input[@type='checkbox']"));
            if (value.equalsIgnoreCase(value)) {
                click(checkbox);
            }
        }
        public String getlblCurrentOverageCapAmount()
        {
            return getTextOfElement(lblCurrentOverageCapAmount);
        }
    }

}
