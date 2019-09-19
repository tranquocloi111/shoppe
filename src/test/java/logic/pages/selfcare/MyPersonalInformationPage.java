package logic.pages.selfcare;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.RandomCharacter;
import logic.business.helper.MiscHelper;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyPersonalInformationPage extends BasePage {
    //private static MyPersonalInformationPage instance;
    public static MyPersonalInformationPage getInstance() {
        return new MyPersonalInformationPage();
    }

    @FindBy(id = "header")
    WebElement header;

    public String getHeader() {
        waitUntilElementVisible(header);
        return getTextOfElement(header);
    }

    public static class MyPreviousOrdersPage extends MyPersonalInformationPage {
        //private static MyPreviousOrdersPage instance;
        public static MyPreviousOrdersPage getInstance() {
            return new MyPreviousOrdersPage();
        }

        @FindBy(xpath = "//b[contains(text(),'My previous orders and contract')]/ancestor::table[1]/following-sibling::div//table")
        WebElement myPreviousOrdersContracttable;

        TableControlBase tableControlBase = new TableControlBase(myPreviousOrdersContracttable);

        public void clickViewByIndex(int index) {
            click(myPreviousOrdersContracttable.findElement(By.xpath(".//tr[" + index + "]")).findElement(By.linkText("View")));
        }
    }

    public static class MyTariffPage extends MyPersonalInformationPage {
        public static MyTariffPage getInstance() {
            return new MyTariffPage();
        }

        @FindBy(xpath = "//a[@href='/orderentry/ShowAllSubscriptions.do']")
        WebElement myTariffDetails;

        public void clickViewOrChangeMyTariffDetailsLink() {
            click(myTariffDetails);
        }

        public static class MyTariffDetailsPage extends MyTariffPage {
            static String serviceRefName;

            public static MyTariffDetailsPage getInstance(String name) {
                serviceRefName = name;
                return new MyTariffDetailsPage();
            }

            TableControlBase tableControlBase = new TableControlBase(myTariffTable());

            private WebElement myTariffTable() {
                return getDriver().findElement(By.xpath("//form//input[@value='" + serviceRefName + "']//ancestor::table[1]"));
            }

            public String getDescription() {
                return getValueOfElement(tableControlBase.findControlCellByLabel("Description", 1).findElement(By.tagName("input")));
            }

            public String getMobilePhoneNumber() {
                return getTextOfElement(tableControlBase.findControlCellByLabel("Mobile phone number", 1));
            }

            public boolean hasSaveButton() {
                return findLinkButtonText(tableControlBase.findControlCellByLabel("Description", 1), "Save") != null;
            }

            public String getTariff() {
                return getTextOfElement(tableControlBase.findControlCellByLabel("Tariff", 1));
            }

            public String getStatus() {
                return getTextOfElement(tableControlBase.findControlCellByLabel("Status", 1));
            }

            public String getSafetyBuffer() {
                try {
                    BufferedReader reader = new BufferedReader(new StringReader(getTextOfElement(tableControlBase.findControlCellByLabel("Safety buffer", 1))));
                    return reader.readLine();
                } catch (Exception ex) {
                    Log.error(ex.getMessage());
                }
                return null;
            }

            public boolean hasChangeMySafetyBufferButton() {
                return findLinkButtonText(tableControlBase.findControlCellByLabel("Safety buffer", 1), "Change my safety buffer") != null;
            }

            public boolean hasAddOrChangeABundleButton() {
                return findLinkButtonText(myTariffTable(), "Add or change a bundle") != null;
            }

            public boolean hasAddOrChangeAFamilyPerkButton() {
                return addOrChangeAFamilyPerkBtn() != null;
            }

            public boolean hasAddOrViewOneoffBundlesButton() {
                return findLinkButtonText(myTariffTable(), "Add or view one-off bundles") != null;
            }

            public boolean hasUpdateButton() {
                return findLinkButtonText(tableControlBase.findControlCellByLabel("Parental controls and favourite numbers", 1), "Update") != null;
            }

            private WebElement addOrChangeAFamilyPerkBtn() {
                return findLinkButtonText(myTariffTable(), "Add or change a Family perk");
            }

            public void clickAddOrChangeAFamilyPerkBtn() {
                click(addOrChangeAFamilyPerkBtn());
            }

            private WebElement addOrChangeABundleButton() {
                return findLinkButtonText(myTariffTable(), "Add or change a bundle");
            }

            private WebElement changeMySafetyBufferBtn() {
                return findLinkButtonText(myTariffTable(), "Change my safety buffer");
            }

            private WebElement addASafetyBufferBtn() {
                return findLinkButtonText(myTariffTable(), "Add a safety buffer");
            }

            public void clickAddOrChangeABundleButton() {
                click(addOrChangeABundleButton());
            }

            public void clickChangeMySafetyBufferBtn() {
                click(changeMySafetyBufferBtn());
            }

            public void clickAddASafetyBufferBtn() {
                click(addASafetyBufferBtn());
            }

            private WebElement findLinkButtonText(WebElement controlCell, String text) {
                List<WebElement> elements = controlCell.findElements(By.tagName("a"));
                for (WebElement line : elements) {
                    String lineText = line.getText();
                    if (!lineText.isEmpty()) {
                        if (lineText.trim().equalsIgnoreCase(text)) {
                            return line;
                        }
                    }
                }
                return null;
            }

            public List<String> familyPerkStack() {
                List<String> list = new ArrayList<>();
                WebElement monthlyBundlesLable = findLabelCell(myTariffTable(), "-  Monthly bundles");
                List<WebElement> allowances = monthlyBundlesLable.findElements(By.xpath(".//parent::tr[1]//following::tr"));
                for (WebElement familyPerk : allowances) {
                    if (familyPerk.getText().trim().contains("Family perk - ")) {
                        list.add(familyPerk.getText().trim());
                    }
                }
                return list;
            }

            public String getMonthlyAllowance() {
                return getTextOfElement(tableControlBase.findCellByLabelText("Monthly allowance"));
            }

            public String getMonthlyBundles() {
                return getTextOfElement(tableControlBase.findCellByLabelText("Monthly bundles"));
            }

            @FindBy(xpath = "//td[contains(text(),'Monthly bundles')]//ancestor::tr[1]//following-sibling::tr[1]//td[@class='fieldvalue']")
            WebElement secondMontlyBundle;

            public String getSecondMonthlyBundles() {
                return getTextOfElement(secondMontlyBundle);
            }

            @FindBy(xpath = "//label[text()='Roaming']//ancestor::td[1]//following-sibling::td")
            WebElement roamingCell;

            public String getRoaming() {
                return getTextOfElement(roamingCell);
            }

            public String getDataCapAbroad() {
                return getTextOfElement(tableControlBase.findCellByLabelText("£40 data cap abroad"));
            }

            public boolean isDataCapAbroadRed() {
                return tableControlBase.findCellByLabelText("£40 data cap abroad").findElement(By.tagName("a")).getAttribute("style").contains("red");
            }

            public String getHelpIntructionByIndex(int index) {
                clickHelpBtnByIndex(index);
                waitUntilElementVisible(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
                String toolTip = getTextOfElement(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
                click(getDriver().findElement(By.xpath("//span[@id='WzClOsE']")));
                return toolTip;
            }

            public String getHighUsage() {
                return getTextOfElement(tableControlBase.findCellByLabelText("High usage").findElement(By.tagName("span")));
            }

            public String getCustomer() {
                return getTextOfElement(tableControlBase.findCellByLabelText("Customer").findElement(By.tagName("span")));
            }

            public String getUnpaidBill() {
                return getTextOfElement(tableControlBase.findCellByLabelText("Unpaid bill").findElement(By.tagName("a")));
            }

            public String getFraud() {
                return getTextOfElement(tableControlBase.findCellByLabelText("Fraud").findElement(By.tagName("span")));
            }

            public void clickDataCapAbroad() {
                click(tableControlBase.findCellByLabelText("£40 data cap abroad").findElement(By.tagName("span")));
            }

            public void clickUnPaidLink() {
                click(tableControlBase.findCellByLabelText("Unpaid bill").findElement(By.tagName("a")));
            }

            public String getUnPaidToolTip() {
                clickHelpBtnByIndex(8);
                waitUntilElementVisible(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
                return getTextOfElement(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
            }

            public void updateDescription(String value) {
                enterValueByLabel(tableControlBase.findControlCellByLabel("Description", 1).findElement(By.tagName("input")), value);
            }

            @FindBy(xpath = "//a[@id='SaveBtn']")
            WebElement savePhoneUserNameBtn;

            public void clickSavePhoneUserNameBtn() {
                savePhoneUserNameBtn.click();
            }

            public void setCreditAgreementSelectByVisibleText(String text) {
                WebElement el = myTariffTable().findElement(By.xpath("..//label[contains(text(),'Credit Agreements')]//ancestor::td[1]//following-sibling::td/div/div[2]/select"));
                selectByVisibleText(el, text);
            }
            public void savePDFFile(String CCANo, String CreditAgreements, String customerNumber) {

                String ccaType= null;
                switch (CreditAgreements) {
                    case "Your Credit Agreement":
                        ccaType = "CCA_DOCUMENT";
                        break;
                    case "Your statement to date":
                        ccaType = "ACCOUNT_STATEMENT";
                        break;
                    case "What you've paid and your remaining balance":
                        ccaType = "CCD_STATEMENT";
                        break;
                    case "Your annual statement":
                        ccaType = "PERIODIC_STATEMENT";
                        break;
                }
                WebElement el = myTariffTable().findElement(By.xpath("//a[@id='viewAgreementButton']"));
                String fileName= String.format("%s_%s_%s_mobile1.pdf", customerNumber, RandomCharacter.getRandomNumericString(9), "31927");
                String url = Config.getProp("selfCareUrl") + String.format("/ShowAgreementDocument.do?agreementNumber=%s&documentType=%s", CCANo, ccaType);
                MiscHelper.saveFileFromWebRequest(el,url,fileName);
            }
        }

    }

    public static class myAlertSection extends MyPersonalInformationPage {
        private static myAlertSection instance;
        @FindBy(xpath = "//b[contains(text(),'My alerts')]/ancestor::p/following-sibling::div[1]/table")
        WebElement myAlertSection;

        public static myAlertSection getInstance() {
            if (instance == null)
                return new myAlertSection();
            return instance;
        }

        TableControlBase tableControlBase = new TableControlBase(myAlertSection);

        public String getAlertMessageByText(String text) {
            return (getTextOfElement(tableControlBase.getLinkByText(text)));
        }

        public List<String> getAllMessage() {
            List<WebElement> elementList = myAlertSection.findElements(By.xpath(".//tr"));
            List<String> allMessg = new ArrayList<>();
            for (int i = 0; i < elementList.size(); i++) {
                allMessg.add(getTextOfElement(elementList.get(i)));
            }
            return allMessg;
        }

        public boolean isMssgDisplayed(String mssg) {
            List<String> allMessg = getAllMessage();
            for (int i = 0; i < allMessg.size(); i++) {
                if (allMessg.get(i).equalsIgnoreCase(mssg)) {
                    return true;
                }
            }
            return false;
        }

        public void clickAlertMessageByText(String text) {
            click(tableControlBase.getLinkByText(text));
        }


        @FindBy(xpath = "//span[contains(text(),'overdue')]")
        WebElement overDueAlert;

        public String getAlertMessagebForOverDuePayment() {
            return (getTextOfElement(overDueAlert));
        }

    }

    public static class myAccountSection extends MyPersonalInformationPage {
        private static myAccountSection instance;
        @FindBy(xpath = "//b[contains(text(),'My account')]/ancestor::p/following-sibling::div[1]/table")
        WebElement myAccountTable;

        public static myAccountSection getInstance() {
            if (instance == null)
                return new myAccountSection();
            return instance;
        }

        TableControlBase tableControlBase = new TableControlBase(myAccountTable);


        public void clickViewOrChangeMyAccountDetails() {
            tableControlBase.clickLinkByText("View or change my account details");
        }

    }

    public static class MyBillsAndPaymentsSection extends MyPersonalInformationPage {
        private static MyBillsAndPaymentsSection instance;
        @FindBy(xpath = "//b[contains(text(),'My bills and payments')]/ancestor::p/following-sibling::div[1]/table")
        WebElement myBillsandPaymentstable;

        public static MyBillsAndPaymentsSection getInstance() {
            if (instance == null)
                return new MyBillsAndPaymentsSection();
            return instance;
        }

        TableControlBase tableControlBase = new TableControlBase(myBillsandPaymentstable);


        public void clickViewDetailsOfMyBillsAndPayments() {
            tableControlBase.clickLinkByText("View details of my bills and payments");
        }

        public void verifyTheMyBillsAndPaymentsPage() {
            Assert.assertEquals(MyPersonalInformationPage.getInstance().getHeader(), "My bills and payments");
        }

        public void clickViewDetailsOfMyCLubCardPoints() {
            tableControlBase.clickLinkByText("View details of my Clubcard points");
        }
    }
}
