package logic.pages.care;

import logic.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MenuPage  extends BasePage {

    private static MenuPage instance = new MenuPage();
    public static MenuPage getInstance() {
        if (instance == null)
            return new MenuPage();
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


    public static class LeftMenuPage extends MenuPage {

        private static LeftMenuPage instance = new LeftMenuPage();
        @FindBy(xpath = ".//td[@class='functions']//table")
        WebElement leftMainDiv;

        public static LeftMenuPage getInstance() {
            if (instance == null)
                return new LeftMenuPage();
            return instance;
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

        public void clickOtherChargesCreditsLink() {
            clickLinkByName("Other Charges/Credits");
        }

        public void clickServiceOrdersLink() {
            clickLinkByName("Service Orders");
        }

        public void clickOtheChargesCreditsItem() {
            clickLinkByName("Other Charges/Credits");
        }

        public void clickInvoicesItem() {
            clickLinkByName("Invoices");
        }

        private void clickLinkByName(String name) {
            try {
                WebElement element = leftMainDiv.findElement(By.linkText(name));
                click(element);
                waitForPageLoadComplete(60);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class RightMenuPage extends MenuPage {

        private static RightMenuPage instance = new RightMenuPage();
        @FindBy(xpath = ".//div[@class='OptionsFrame']")
        WebElement rightMainDiv;

        public static RightMenuPage getInstance() {
            if (instance == null)
                return new RightMenuPage();
            return instance;
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

        private void clickLinkByName(String name) {
            WebElement element = rightMainDiv.findElement(By.linkText(name));
            click(element);
            waitForPageLoadComplete(60);
        }
    }
}
