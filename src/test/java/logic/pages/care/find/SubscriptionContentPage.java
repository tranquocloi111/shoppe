package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class SubscriptionContentPage extends BasePage {

    public static class SubscriptionDetailsPage extends SubscriptionContentPage {

        public static class GeneralSectionPage extends SubscriptionDetailsPage {

            private static GeneralSectionPage instance = new GeneralSectionPage();

            public static GeneralSectionPage getInstance() {
                return new GeneralSectionPage();
            }

            @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]//following-sibling::td")
            WebElement lblSubscriptionNumber;

            @FindBy(xpath = "//td[contains(text(),'Discount Group Code:')]//following-sibling::td")
            WebElement lblDiscountGroupCode;

            @FindBy(xpath = "//td[contains(text(),'HUB Internal ID:')]//following-sibling::td")
            WebElement lblHUBInternalID;

            @FindBy(xpath = "//td[contains(text(),'IMEI:')]//following-sibling::td")
            WebElement lblIMEI;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'Reference:')]//following-sibling::td[1]")
            WebElement lblReference;

            @FindBy(xpath = "//td[contains(text(),'Initial Purchase Price:')]//following-sibling::td")
            WebElement lblInitialPurchasePrice;

            @FindBy(xpath = "//td[contains(text(),'Make:')]//following-sibling::td")
            WebElement lblMake;

            @FindBy(xpath = "//td[contains(text(),'Agreement number:')]//following-sibling::td")
            WebElement lblAgreementNumber;

            @FindBy(xpath = "//td[contains(text(),'Start Date:')]//following-sibling::td")
            WebElement lblStartDate;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'Order Reference:')]//following-sibling::td[1]")
            WebElement lblOrderReference;

            @FindBy(xpath = "//td[contains(text(),'Warranty End Date:')]//following-sibling::td")
            WebElement lblWarrantyEndDate;

            @FindBy(xpath = "//td[contains(text(),'Quantity:')]//following-sibling::td")
            WebElement lblQuantity;

            @FindBy(xpath = "//td[contains(text(),'Amount Paid:')]//following-sibling::td")
            WebElement lblAmountPaid;

            @FindBy(xpath = "//td[contains(text(),'Model:')]//following-sibling::td")
            WebElement lblModel;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'Provisioning System:')]//following-sibling::td[1]")
            WebElement lblProvisioningSystem;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'OCS Subscriber Key :')]//following-sibling::td[1]")
            WebElement lblOCSSubscriberKey;

            @FindBy(xpath = "//td[starts-with(normalize-space(text()),'OCS Subscriber Account Key :')]//following-sibling::td[1]")
            WebElement lblOCSSubscriberAccountKey;

            @FindBy(xpath = "//td[contains(text(),'Upgrade Order Reference:')]//following-sibling::td[1]")
            WebElement lblUpgradeOrderReference;
            @FindBy(xpath = "//td[contains(text(),'Flexible Cap Amount:')]//following-sibling::td[1]")
            WebElement lblFlexibleCapAmount;

            public String getSubscriptionNumber() {
                return getTextOfElement(lblSubscriptionNumber);
            }

            public String getDiscountGroupCode() {
                return getTextOfElement(lblDiscountGroupCode);
            }

            public String getHUbInternalId() {
                return getTextOfElement(lblHUBInternalID);
            }

            public String getIMEI() {
                return getTextOfElement(lblIMEI);
            }

            public String getReference() {
                return getTextOfElement(lblReference);
            }

            public String getInitialPurchasePrice() {
                return getTextOfElement(lblInitialPurchasePrice);
            }

            public String getMake() {
                return getTextOfElement(lblMake);
            }

            public String getAgreementNumber() {
                return getTextOfElement(lblAgreementNumber);
            }

            public String getStartDate() {
                return getTextOfElement(lblStartDate);
            }

            public String getOrderReference() {
                return getTextOfElement(lblOrderReference);
            }

            public String getWarrantyEndDate() {
                return getTextOfElement(lblWarrantyEndDate);
            }

            public String getQuantity() {
                return getTextOfElement(lblQuantity);
            }

            public String getAmountPaid() {
                return getTextOfElement(lblAmountPaid);
            }

            public String getModel() {
                return getTextOfElement(lblModel);
            }

            public String getUpgradeOrderReference() {
                return getTextOfElement(lblUpgradeOrderReference);
            }

            public String getFlexibleCapAmount() {
                return getTextOfElement(lblFlexibleCapAmount);
            }

            @FindBy(xpath = "//td[contains(text(),'Flexible Cap Amount:')]//following-sibling::td//img")
            WebElement flexibleCapAmountToolTip;

            public String getFlexibleCapToolTip() {
                hover(flexibleCapAmountToolTip);
                return getDriver().findElement(By.xpath("//body/div[last()]")).getText();
            }

            public String getProvisioningSystem() {
                return getTextOfElement(lblProvisioningSystem);
            }

            public String getOCSSubscriberKey() {
                return getTextOfElement(lblOCSSubscriberKey);
            }

            public String getOCSSubscriberAccountKey() {
                return getTextOfElement(lblOCSSubscriberAccountKey);
            }
        }

        public static class OtherProductsGridSectionPage extends SubscriptionDetailsPage {
            private static final String productCode = "Product Code";
            private static final String type = "Type";
            private static final String description = "Description";
            private static final String startDate = "Start Date";
            private static final String endDate = "End Date";
            private static final String charge = "Charge";

            private static OtherProductsGridSectionPage instance = new OtherProductsGridSectionPage();

            public static OtherProductsGridSectionPage getInstance() {
                return new OtherProductsGridSectionPage();
            }

            @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Other Products')]/../../..//following-sibling::div[1]//table")
            WebElement otherProductsGridTable;
            TableControlBase table = new TableControlBase(otherProductsGridTable);

            public int getRowNumberOfOtherProductsGridTable() {
                return table.getRowsCount();
            }

            public List<WebElement> getOtherProducts(List<HashMap<String, String>> otherProduct) {
                return table.findRowsByColumns(otherProduct);
            }

            public List<WebElement> getOtherProduct(HashMap<String, String> otherProduct) {
                return table.findRowsByColumns(otherProduct);
            }

            public int getNumberOfOtherProducts(List<HashMap<String, String>> otherProduct) {
                return table.findRowsByColumns(otherProduct).size();
            }

            public int getNumberOfOtherProductsByProduct(HashMap<String, String> otherProduct) {
                return table.findRowsByColumns(otherProduct).size();
            }

            public int getNumberOfOtherProduct(HashMap<String, String> otherProduct) {
                return table.findRowsByColumns(otherProduct).size();
            }


            public void clickProductCodeByProductCode(String productCode) {
                List<WebElement> aList = otherProductsGridTable.findElements(By.tagName("a"));
                for (WebElement el : aList) {
                    if (el.getText().replace(" ", "").equalsIgnoreCase(productCode.replace(" ", ""))) {
                        el.click();
                        break;
                    }
                }
            }
        }


        public static class SubscriptionFeatureSectionPage extends SubscriptionDetailsPage {
            private static SubscriptionFeatureSectionPage instance = new SubscriptionFeatureSectionPage();

            public static SubscriptionFeatureSectionPage getInstance() {
                return new SubscriptionFeatureSectionPage();
            }

            @FindBy(xpath = "//td[contains(text(),'Service Feature:')]//following-sibling::td[1]")
            WebElement lblServiceFeature;
            @FindBy(xpath = "//td[contains(text(),'Barring:')]//following-sibling::td[1]")
            WebElement lblBarring;
            @FindBy(xpath = "//td[contains(text(),'Barring Status:')]//following-sibling::td[1]")
            WebElement lblBarringStatus;

            public String getServiceFeature() {
                return getTextOfElement(lblServiceFeature);
            }

            public String getBarring() {
                return getTextOfElement(lblBarring);
            }

            public String getBarringStatus() {
                return getTextOfElement(lblBarringStatus);
            }
        }

    }

    public static class TariffComponentsGridPage extends SubscriptionContentPage {
        private static TariffComponentsGridPage instance = new TariffComponentsGridPage();

        public static TariffComponentsGridPage getInstance() {
            return new TariffComponentsGridPage();
        }

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Tariff Components')]")
        WebElement headerText;

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Tariff Components')]/../../..//following-sibling::div[1]//table")
        WebElement TariffComponentsGridTable;
        TableControlBase tableControlBase = new TableControlBase(TariffComponentsGridTable);

        public List<WebElement> getTariffComponents(List<HashMap<String, String>> tariffComponents) {
            return tableControlBase.findRowsByColumns(tariffComponents);
        }

        public List<WebElement> getTariffComponents(HashMap<String, String> tariffComponent) {
            return tableControlBase.findRowsByColumns(tariffComponent);
        }

        public String getHeaderText() {
            return getTextOfElement(headerText);
        }

        public int rowOfTariffComponents() {
            return tableControlBase.getRowsCount();
        }

    }
}

