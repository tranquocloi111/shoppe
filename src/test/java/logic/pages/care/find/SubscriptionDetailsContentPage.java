package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class SubscriptionDetailsContentPage extends BasePage {

    public static class General extends SubscriptionDetailsContentPage {

        private static General instance = new General();
        @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]//following-sibling::td")
        WebElement lblSubscriptionNumber;
        @FindBy(xpath = "//td[contains(text(),'Discount Group Code:')]//following-sibling::td")
        WebElement lblDiscountGroupCode;

        public static General getInstance() {
            if (instance == null)
                instance = new General();
            return instance;
        }

        public String getSubscriptionNumber() {
            return getTextOfElement(lblSubscriptionNumber);
        }

        public String getDiscountGroupCode() {
            return getTextOfElement(lblDiscountGroupCode);
        }
    }


    public static class OtherProductsGridClass extends SubscriptionDetailsContentPage {
        private static final String productCode = "Product Code";
        private static final String type = "Type";
        private static final String description = "Description";
        private static final String startDate = "Start Date";
        private static final String endDate = "End Date";
        private static final String charge = "Charge";

        private static OtherProductsGridClass instance = new OtherProductsGridClass();
        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Other Products')]/../../..//following-sibling::div[1]//table")
        WebElement otherProductsGridTable;
        TableControlBase table = new TableControlBase(otherProductsGridTable);

        public static OtherProductsGridClass getInstance() {
            if (instance == null)
                return new OtherProductsGridClass();
            return instance;
        }

        public int getRowNumberOfOtherProductsGridTable() {
            return table.getRowsCount();
        }

        public List<WebElement> getSubscriptions(List<HashMap<String, String>> otherProduct) {
            return table.findRowsByColumns(otherProduct);
        }
    }
}
