package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.List;

public class MonthlyBundlesAddChangeOrRemovePage extends BasePage {
    public static MonthlyBundlesAddChangeOrRemovePage getInstance() {
        return new MonthlyBundlesAddChangeOrRemovePage();
    }

    TableControlBase table;
    @FindBy(xpath = "//a[@title='Back']")
    WebElement btnBack;

    @FindBy(xpath = ".//div[@class='msg-box']/table[.//td[contains(.,'Current bundles')]]")
    WebElement currentBundlesTable;

    @FindBy(xpath = ".//b[.='Monthly bundles']//../following-sibling::div[.//table[.//div[@id='BundleGrp0_ErrorPanel']]]")
    WebElement msgBoxDivMonthlyBundles;

    private List<WebElement> msgBoxList() {
        return getDriver().findElements(By.xpath(".//div[@class='msg-box']"));
    }

    private WebElement totalLabel() {
        for (WebElement label : msgBoxDivMonthlyBundles.findElements(By.tagName("label"))) {
            if (label.getText().trim().equalsIgnoreCase("Total:")) {
                return label;
            }
        }
        return null;
    }

    private WebElement messageTable() {
        for (int i = 2; i < msgBoxList().size(); i++) {
            if (msgBoxList().get(i).getText().contains("Your bundle will be available for you to use")) {
                return msgBoxList().get(i);
            }
        }
        return null;
    }

    private WebElement noteSavingMessageTr() {
        return messageTable().findElement(By.xpath(".//tr[2]"));
    }

    private WebElement fairUsagePolicyMessageTr() {
        return messageTable().findElement(By.xpath(".//tr[3]"));
    }

    private WebElement readOurFairUsagePolicyLink() {
        return fairUsagePolicyMessageTr().findElement(By.linkText("Read our fair usage policy"));
    }

    private WebElement underneathLink() {
        return getDriver().findElement(By.tagName("body")).findElement(By.cssSelector("a[id='addBundleLink']"));
    }

    public String monthly500DataAllowancePrice() {
        table = new TableControlBase(msgBoxDivMonthlyBundles);
        WebElement row = table.findRowByLabel("Monthly 500MB data allowance");
        return row.findElement(By.xpath(".//td[3]")).getText().trim();
    }

    public String monthly1GBDataAllowancePrice() {
        table = new TableControlBase(msgBoxDivMonthlyBundles);
        WebElement row = table.findRowByLabel("Monthly 1GB data allowance");
        return row.findElement(By.xpath(".//td[3]")).getText().trim();
    }

    public String getMonthlyDataBundleByValue(String value) {
        table = new TableControlBase(msgBoxDivMonthlyBundles);
        WebElement row = table.findRowByLabel(value);
        return row.findElement(By.xpath(".//td[3]")).getText().trim();
    }

    public String totalPrice() {
        return totalLabel().findElement(By.xpath(".//following-sibling::div")).getText().trim();
    }

    public String noteSavingMessage() {
        return noteSavingMessageTr().getText().trim();
    }

    public boolean isNoteSavingMessageRed() {
        WebElement td = noteSavingMessageTr().findElement(By.xpath("child::td"));
        return td.getAttribute("style").toLowerCase().contains("color: red");
    }

    public boolean readOurFairUsagePolicyLinkDisplayed() {
        String text = readOurFairUsagePolicyLink().getAttribute("href");
        String href = "http://www.tescomobile.com/hfh";
        return text.equalsIgnoreCase(href);
    }

    public String fairUsagePolicyMessage() {
        return fairUsagePolicyMessageTr().getText();
    }

    public String underneathLinkText() {
        return underneathLink().getText().trim();
    }

    public boolean underneathLinkDisplayed() {
        String href = underneathLink().getAttribute("href");
        return !href.isEmpty();
    }

    public String bundleAvailableDateMessage() {
        return messageTable().findElement(By.xpath(".//tr[1]")).getText().trim();
    }

    public String tariffCharge() {
        return getDriver().findElement(By.tagName("body")).findElement(By.id("TariffCharges")).getText().trim();
    }

    public String totalMonthlyCharge() {
        return getDriver().findElement(By.tagName("body")).findElement(By.id("MonthlyCharge")).getText().trim();
    }

    public String getOffers() {
        table = new TableControlBase(currentBundlesTable);
        WebElement row = table.getCellByLabel("Offers");
        return getTextOfElement(row.findElement(By.xpath(".//following-sibling::td[1]")));
    }

    public String getAllowance() {
        table = new TableControlBase(currentBundlesTable);
        WebElement row = table.getCellByLabel("Offers");
        return getTextOfElement(row.findElement(By.xpath(".//following-sibling::td[2]")));
    }

    public void selectBundlesByName(String... value) {
        SelfCareTestBase.page().selectBundlesByName(msgBoxDivMonthlyBundles, value);
    }

    public void unSelectBundlesByName(String... value) {
        SelfCareTestBase.page().unSelectBundlesByName(msgBoxDivMonthlyBundles, value);
    }

    public void clickBackButton() {
        click(btnBack);
    }

}

