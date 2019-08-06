package logic.pages.selfcare;

import framework.utils.Log;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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

            public void clickAddOrChangeABundleButton() {
                click(addOrChangeABundleButton());
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
        public String getAlertMessageByText(String text){
            return (getTextOfElement(tableControlBase.getLinkByText(text)));
        }
        public void clickAlertMessageByText(String text){
            click(tableControlBase.getLinkByText(text));
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


        public void clickViewOrChangeMyAccountDetails(){
            tableControlBase.clickLinkByText("View or change my account details");
        }


    }
}
