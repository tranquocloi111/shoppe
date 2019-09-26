package logic.pages.care;

import framework.utils.Log;
import logic.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MenuPage extends BasePage {

    public static class HeaderMenuPage extends MenuPage {

        private static HeaderMenuPage instance = new HeaderMenuPage();

        public static HeaderMenuPage getInstance() {
            return instance;
        }

        public void clickCustomersTab() {
            clickLinkByText("Customers");
        }

        public void clickServiceOrdersTab() {
            clickLinkByText("Service Orders");
        }

        public void clickTasksTab() {
            clickLinkByText("Tasks");
        }

        public void clickAdminTab() {
            clickLinkByText("Admin");
        }
    }

    public static class LeftMenuPage extends MenuPage {

        private static LeftMenuPage instance = new LeftMenuPage();
        @FindBy(xpath = ".//td[@class='functions']//table")
        WebElement leftMainDiv;

        public static LeftMenuPage getInstance() {
            return new LeftMenuPage();
        }

        public void clickSummaryLink() {
            clickLinkByName("Summary");
        }

        public void clickDetailsLink() {
            clickLinkByName("Details");
        }

        public void clickSubscriptionsLink() {
            clickLinkByName("Subscriptions");
        }

        public void clickServiceOrdersLink() {
            clickLinkByName("Service Orders");
        }

        public void clickOtherChargesCreditsItem() {
            clickLinkByName("Other Charges/Credits");
        }

        public void clickInvoicesItem() {
            clickLinkByName("Invoices");
        }

        public void clickCreditAgreementsItem() {
            clickLinkByName("Credit Agreements");
        }

        public void clickLiveBillEstimateItem() {
            clickLinkByName("Live Bill Estimate");
        }

        public void clickSelfCareSetting() {
            clickLinkByName("Self Care Settings");
        }

        private void clickLinkByName(String name) {
            try {
                WebElement element = leftMainDiv.findElement(By.linkText(name));
                click(element);
                waitForPageLoadComplete(90);
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public boolean verifyLinkIsNotSelected(String linkName) {
            Boolean result = false;
            try {
                WebElement element = leftMainDiv.findElement(By.linkText(linkName));
                result = isElementPresent(element);
            } catch (Exception e) {
                Log.info("Link " + linkName +" is selected");
            }
            return result;
        }
        public void clickFinancialTransactionLink() {
            clickLinkByName("Financial Transactions");
        }

        public void clickPaymentsLink() {
            clickLinkByName("Payments");
        }
        public void clickUnBilledSummaryItem() {
            clickLinkByName("Unbilled Summary");
        }
    }

    public static class RightMenuPage extends MenuPage {

        private static RightMenuPage instance = new RightMenuPage();
        @FindBy(xpath = ".//div[@class='OptionsFrame']")
        WebElement rightMainDiv;

        public static RightMenuPage getInstance() {
            return new RightMenuPage();
        }

        public void clickDeactivateAccountLink() {
            clickLinkByName("Deactivate Account");
        }

        public void clickChangeTariffLink() {
            clickLinkByName("Change Tariff");
        }

        public void clickChangeSubscriptionNumberLink() {
            clickLinkByName("Change Subscription Number");
        }

        public void clickRefreshLink() {
            clickLinkByText("Refresh");
        }

        public void clickDeactivateSubscriptionLink() {
            clickLinkByName("Deactivate Subscription");
        }

        public void clickApplyFinancialTransactionLink() {
            clickLinkByName("Apply Financial Transaction");
        }

        public void clickChangeBundleLink() {
            clickLinkByName("Change Bundle");
        }

        private void clickLinkByName(String name) {
            WebElement element = rightMainDiv.findElement(By.linkText(name));
            click(element);
            waitForPageLoadComplete(60);
        }
        public void clickConfigureBarRoamingonLink() {
            clickLinkByName("Configure Bar/Roaming");
        }

        public void clickChangeBillCycle() {
            clickLinkByName("Change Bill Cycle");
        }

        public void clickChangeCustomerTypeLink() {
            clickLinkByName("Change Customer Type");
        }

    }

    public static class BreadCrumbPage extends MenuPage {
        private static BreadCrumbPage instance = new BreadCrumbPage();
        @FindBy(xpath = ".//div[@class='breadCrumbsBox ShadowOnGrey']")
        WebElement breadCrumbDiv;

        public static BreadCrumbPage getInstance() {
            return new BreadCrumbPage();
        }

        public void clickParentLink() {
            click(breadCrumbDiv.findElement(By.xpath(".//a[last()]")));
            waitForPageLoadComplete(60);
        }

        public void clickItemLink(String text) {
            click(breadCrumbDiv.findElement(By.partialLinkText(text)));
            waitForPageLoadComplete(60);
        }

    }

}
