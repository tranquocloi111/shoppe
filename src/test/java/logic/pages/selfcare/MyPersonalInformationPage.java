package logic.pages.selfcare;

import framework.utils.Log;
import logic.business.helper.MiscHelper;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.testng.Assert;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class MyPersonalInformationPage extends BasePage {
    @FindBy(id = "header")
    WebElement header;

    //private static MyPersonalInformationPage instance;
    public static MyPersonalInformationPage getInstance() {
        return new MyPersonalInformationPage();
    }


    public String getHeader() {
        waitUntilElementVisible(header);
        return getTextOfElement(header);
    }

    public static class MyPreviousOrdersPage extends MyPersonalInformationPage {
        @FindBy(xpath = "//b[contains(text(),'My previous orders and contract')]/ancestor::table[1]/following-sibling::div//table")
        WebElement myPreviousOrdersContracttable;
        TableControlBase tableControlBase = new TableControlBase(myPreviousOrdersContracttable);

        //private static MyPreviousOrdersPage instance;
        public static MyPreviousOrdersPage getInstance() {
            return new MyPreviousOrdersPage();
        }

        public void clickViewByIndex(int index) {
            click(myPreviousOrdersContracttable.findElement(By.xpath(".//tr[" + index + "]")).findElement(By.linkText("View")));
        }

        public List<List<String>> getAllValueOfOrdersAndContractPage(){
            return tableControlBase.getAllCellValueWithoutColumnName();
        }
    }

    public static class MyTariffPage extends MyPersonalInformationPage {
        @FindBy(xpath = "//a[@href='/orderentry/ShowAllSubscriptions.do']")
        WebElement myTariffDetails;

        public static MyTariffPage getInstance() {
            return new MyTariffPage();
        }


        public void clickViewOrChangeMyTariffDetailsLink() {
            click(myTariffDetails);
        }

        public static class MyTariffDetailsPage extends MyTariffPage {
            static String serviceRefName;

            public static MyTariffDetailsPage getInstance() {
                return new MyTariffDetailsPage();
            }

            @FindBy(xpath = "//label[text()='Roaming']//ancestor::td[1]//following-sibling::td")
            WebElement roamingCell;

            @FindBy(xpath = "//a[@id='SaveBtn']")
            WebElement savePhoneUserNameBtn;

            @FindBy(xpath = "//td[contains(text(),'Monthly bundles')]//ancestor::tr[1]//following-sibling::tr[1]//td[@class='fieldvalue']")
            WebElement secondMonthlyBundle;

            @FindBy(xpath = "//td[contains(text(),'Monthly bundles')]//ancestor::tr[1]//following-sibling::tr[2]//td[@class='fieldvalue']")
            WebElement thirdMonthlyBundle;

            @FindBy(xpath = "//a[@id='viewAgreementButton']")
            WebElement viewAgreementButton;

            @FindBy(id = "plugin")
            WebElement embeddedPdfForm;

            public static MyTariffDetailsPage getInstance(String name) {
                serviceRefName = name;
                return new MyTariffDetailsPage();
            }


            private WebElement myTariffTable() {
                return getDriver().findElement(By.xpath("//form//input[@value='" + serviceRefName + "']//ancestor::table[1]"));
            }

            private WebElement myTariffDeactivateTable() {
                return getDriver().findElement(By.xpath("//form//td[contains(.,'" + serviceRefName + "')]//ancestor::table[1]"));
            }


            public String getDescription() {
                return getValueOfElement(validTableControlBase().findControlCellByLabel("Description", 1).findElement(By.tagName("input")));
            }

            public String getMobilePhoneNumber() {
                return getTextOfElement(validTableControlBase().findControlCellByLabel("Mobile phone number", 1));
            }

            public boolean hasSaveButton() {
                return findLinkButtonText(validTableControlBase().findControlCellByLabel("Description", 1), "Save") != null;
            }

            public String getTariff() {
                return getTextOfElement(validTableControlBase().findControlCellByLabel("Tariff", 1));
            }

            public String getStatus() {
                return getTextOfElement(validTableControlBase().findControlCellByLabel("Status", 1));
            }

            public String getSafetyBuffer() {
                try {
                    BufferedReader reader = new BufferedReader(new StringReader(getTextOfElement(validTableControlBase().findControlCellByLabel("Safety buffer", 1))));
                    return reader.readLine();
                } catch (Exception ex) {
                    Log.error(ex.getMessage());
                }
                return null;
            }

            public boolean hasChangeMySafetyBufferButton() {
                return findLinkButtonText(validTableControlBase().findControlCellByLabel("Safety buffer", 1), "Change my safety buffer") != null;
            }

            public boolean hasAddOrChangeABundleButton() {
                return findLinkButtonText(validTable(), "Add or change a bundle") != null;
            }

            public boolean hasAddOrChangeAFamilyPerkButton() {
                return addOrChangeAFamilyPerkBtn() != null;
            }

            public boolean hasAddOrViewOneoffBundlesButton() {
                return findLinkButtonText(validTable(), "Add or view one-off bundles") != null;
            }

            public boolean hasUpdateButton() {
                return findLinkButtonText(validTableControlBase().findControlCellByLabel("Parental controls and favourite numbers", 1), "Update") != null;
            }

            private WebElement addOrChangeAFamilyPerkBtn() {
                return findLinkButtonText(validTable(), "Add or change a Family Perk");
            }

            public void clickAddOrChangeAFamilyPerkBtn() {
                clickWithOutWait(addOrChangeAFamilyPerkBtn());
            }

            private WebElement addOrChangeABundleButton() {
                return findLinkButtonText(validTable(), "Add or change a bundle");
            }

            private WebElement changeMySafetyBufferBtn() {
                return findLinkButtonText(validTable(), "Change my safety buffer");
            }

            private WebElement addASafetyBufferBtn() {
                return findLinkButtonText(validTable(), "Add a safety buffer");
            }

            private WebElement addOrChangeAPerkBtn() {
                return findLinkButtonText(validTable(), "Add or change a Perk");
            }

            private WebElement addOrViewOneOffBundles() {
                return findLinkButtonText(validTable(), "Add or view one-off bundles");
            }

            public void clickAddOrChangeABundleButton() {
                click(addOrChangeABundleButton());
            }

            public void clickAddOrViewOneOffBundlesButton() {
                click(addOrViewOneOffBundles());
            }

            public void clickChangeMySafetyBufferBtn() {
                click(changeMySafetyBufferBtn());
            }

            public void clickAddASafetyBufferBtn() {
                click(addASafetyBufferBtn());
            }

            public void clickAddOrChangeAPerkBtn() {
                clickWithOutWait(addOrChangeAPerkBtn());
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
                WebElement monthlyBundlesLable = findLabelCell(validTable(), "-  Monthly bundles");
                List<WebElement> allowances = monthlyBundlesLable.findElements(By.xpath(".//parent::tr[1]//following::tr"));
                for (WebElement familyPerk : allowances) {
                    if (familyPerk.getText().trim().contains("Family perk - ")) {
                        list.add(familyPerk.getText().trim());
                    }
                }
                return list;
            }

            public String getMonthlyAllowance() {
                return getTextOfElement(validTableControlBase().findCellByLabelText("Monthly allowance"));
            }

            public String getMonthlyBundles() {
                return getTextOfElement(validTableControlBase().findCellByLabelText("Monthly bundles"));
            }
            public String getOneOffBundles() {
                return getTextOfElement(validTableControlBase().findCellByLabelText("One-off bundles"));
            }

            public String getSecondMonthlyBundles() {
                return getTextOfElement(secondMonthlyBundle);
            }
            public String getThirdMonthlyBundles() {
                return getTextOfElement(thirdMonthlyBundle);
            }

            public String getRoaming() {
                return getTextOfElement(roamingCell);
            }

            public String getDataCapAbroad() {
                return getTextOfElement(validTableControlBase().findCellByLabelText("£40 data cap abroad"));
            }

            public boolean isDataCapAbroadRed() {
                return validTableControlBase().findCellByLabelText("£40 data cap abroad").findElement(By.tagName("a")).getAttribute("style").contains("red");
            }

            public String getHelpIntructionByIndex(int index) {
                clickHelpBtnByIndex(index);
                waitUntilElementVisible(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
                String toolTip = getTextOfElement(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
                click(getDriver().findElement(By.xpath("//span[@id='WzClOsE']")));
                return toolTip;
            }

            public String getHighUsage() {
                return getTextOfElement(validTableControlBase().findCellByLabelText("High usage").findElement(By.tagName("span")));
            }

            public String getCustomer() {
                return getTextOfElement(validTableControlBase().findCellByLabelText("Customer").findElement(By.tagName("span")));
            }

            public String getUnpaidBill() {
                return getTextOfElement(validTableControlBase().findCellByLabelText("Unpaid bill").findElement(By.tagName("a")));
            }

            public String getFraud() {
                return getTextOfElement(validTableControlBase().findCellByLabelText("Fraud").findElement(By.tagName("span")));
            }

            public void clickDataCapAbroad() {
                click(validTableControlBase().findCellByLabelText("£40 data cap abroad").findElement(By.tagName("span")));
            }

            public void clickUnPaidLink() {
                click(validTableControlBase().findCellByLabelText("Unpaid bill").findElement(By.tagName("a")));
            }

            public String getUnPaidToolTip() {
                clickHelpBtnByIndex(8);
                waitUntilElementVisible(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
                return getTextOfElement(getDriver().findElement(By.xpath("//td[@id='WzBoDyI']")));
            }

            public void updateDescription(String value) {
                enterValueByLabel(validTableControlBase().findControlCellByLabel("Description", 1).findElement(By.tagName("input")), value);
            }

            public void clickSavePhoneUserNameBtn() {
                savePhoneUserNameBtn.click();
            }


            public void setCreditAgreementSelectByVisibleText(String text) {
                WebElement el = validTable().findElement(By.xpath("..//label[contains(text(),'Credit Agreements')]//ancestor::td[1]//following-sibling::td/div/div[2]/select"));
                selectByVisibleText(el, text);
            }

            public List<WebElement> getListViewAgreementBtn() {
                return getDriver().findElements(By.xpath("//a[@id='viewAgreementButton']"));
            }

            public void clickViewAgreementButton(int index) {
                click(getListViewAgreementBtn().get(index));
            }

            public void savePDFFile(String fileName) {
                String parent = getTitle();
                switchWindow("Your Agreement", false);
                String url = embeddedPdfForm.getAttribute("src");
                MiscHelper.saveFileFromWebRequest(url, fileName);

                switchWindow(parent, false);
            }

            public List<WebElement> getFamilyPerkStack() {
                List<WebElement> list = new ArrayList<>();
                WebElement monthlyBundlesLable = findLabelCell(validTable(), "-  Monthly bundles");
                List<WebElement> allowances = monthlyBundlesLable.findElements(By.xpath(".//parent::tr[1]//following::tr"));
                for (WebElement familyPerk : allowances) {
                    if (familyPerk.getText().trim().contains("Family perk - ")) {
                        list.add(familyPerk);
                    }
                }
                return list;
            }

            public String getFamilyPerkHelpIconText() {
                try {
                    WebElement icon = getFamilyPerkStack().get(0).findElement(By.tagName("a"));
                    WebElement image = icon.findElement(By.tagName("img"));
                    String js = image.getAttribute("onmouseover");
                    //hover(image);
                    executeJs(js, image);
                    Thread.sleep(1000);
                    WebElement div = getDriver().findElement(By.cssSelector("div[id='WzBoDy']"));
                    return div.getText().trim();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                return null;
            }

            public String getPopupMessageOfPerk() {
                return getTextComfirmDialog();
            }

            private WebElement validTable() {
                WebElement table = null;
                try {
                    if (isElementPresent(myTariffTable()))
                        table = myTariffTable();
                } catch (Exception ex) {
                    table = myTariffDeactivateTable();
                }

                return table;
            }

            private TableControlBase validTableControlBase() {
                WebElement table = null;
                try {
                    if (isElementPresent(myTariffTable()))
                        table = myTariffTable();
                } catch (Exception ex) {
                    table = myTariffDeactivateTable();
                }
                return new TableControlBase(table);
            }

            public List<WebElement> getLoyaltyStack() {
                List<WebElement> list = new ArrayList<>();
                WebElement monthlyBundlesLable = findLabelCell(validTable(), "-  Monthly bundles");
                List<WebElement> allowances = monthlyBundlesLable.findElements(By.xpath(".//parent::tr[1]//following::tr"));
                for (WebElement familyPerk : allowances) {
                    if (familyPerk.getText().trim().contains("Loyalty Bundle - ")) {
                        list.add(familyPerk);
                    }
                }
                return list;
            }

            public String getLoyaltyHelpIconText(int index) {
                try {
                    WebElement icon = getLoyaltyStack().get(index).findElement(By.tagName("a"));
                    WebElement image = icon.findElement(By.tagName("img"));
                    String js = image.getAttribute("onmouseover");
                    //hover(image);
                    executeJs(js, image);
                    Thread.sleep(1000);
                    WebElement div = getDriver().findElement(By.cssSelector("div[id='WzBoDy']"));
                    return div.getText().trim();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                return null;
            }
        }

        public String getErrorMssgDialog() {
            return super.getTextComfirmDialog();
        }

        @FindBy(xpath = "//span[contains(text(),'INACTIVE')]//ancestor::tr[1]//following-sibling::tr[4]//a[@id='addBundleBtn']")
        WebElement addOrChangeAFamilyPerkOfInacitveSubscriptionBtn;

        public void clickAddOrChangeAFamilyPerkOfInacitveSubscription() {
            clickWithOutWait(addOrChangeAFamilyPerkOfInacitveSubscriptionBtn);
        }

        public WebElement getInactiveSubscriptionTable(String reference) {
            String xpath = String.format("//td[contains(text(),'%s')]//ancestor::table[1]", reference);
            return getDriver().findElement(By.xpath(xpath));
        }

        public List<WebElement> getListCreditAgreementSelect() {
            return getDriver().findElements(By.xpath("//label[contains(text(),'Credit Agreements')]//ancestor::td[1]//following-sibling::td/div/div[2]/select"));
        }

        public void setCreditAgreementSelectByVisibleTextForInactiveSubscription(String text, int index) {
            selectByVisibleText(getListCreditAgreementSelect().get(index), text);
        }

        public void clickViewMyUsageDetailsSinceMyLastBillLink() {
            clickLinkByText("View my usage details since my last bill");
        }

        public void clickViewMyUsageSinceMyLastBillLink() {
            clickLinkByText("View my usage since my last bill");
        }

        @FindBy(xpath = "//label[normalize-space(text())='Safety buffer']//ancestor::tr[1]//following-sibling::tr[1]")
        WebElement secondMonthlyBundle;
        public String getSecondSafetyBuffer()
        {
            return getTextOfElement(secondMonthlyBundle).substring(0,getTextOfElement(secondMonthlyBundle).indexOf("Change my"));
        }

    }

    public static class myAlertSection extends MyPersonalInformationPage {
        private static myAlertSection instance;
        @FindBy(xpath = "//b[contains(text(),'My alerts')]/ancestor::p/following-sibling::div[1]/table")
        WebElement myAlertSection;
        TableControlBase tableControlBase = new TableControlBase(myAlertSection);
        @FindBy(xpath = "//span[contains(text(),'overdue')]")
        WebElement overDueAlert;

        public static myAlertSection getInstance() {
            if (instance == null)
                return new myAlertSection();
            return instance;
        }

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



        public String getAlertMessagebForOverDuePayment() {
            return (getTextOfElement(overDueAlert));
        }

    }

    public static class myAccountSection extends MyPersonalInformationPage {
        private static myAccountSection instance;
        @FindBy(xpath = "//b[contains(text(),'My account')]/ancestor::p/following-sibling::div[1]/table")
        WebElement myAccountTable;
        TableControlBase tableControlBase = new TableControlBase(myAccountTable);

        public static myAccountSection getInstance() {
            if (instance == null)
                return new myAccountSection();
            return instance;
        }


        public void clickViewOrChangeMyAccountDetails() {
            tableControlBase.clickLinkByText("View or change my account details");
        }

    }

    public static class MyBillsAndPaymentsSection extends MyPersonalInformationPage {
        private static MyBillsAndPaymentsSection instance;
        @FindBy(xpath = "//b[contains(text(),'My bills and payments')]/ancestor::p/following-sibling::div[1]/table")
        WebElement myBillsandPaymentstable;
        TableControlBase tableControlBase = new TableControlBase(myBillsandPaymentstable);

        public static MyBillsAndPaymentsSection getInstance() {
            if (instance == null)
                return new MyBillsAndPaymentsSection();
            return instance;
        }

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
