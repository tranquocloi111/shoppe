package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class SummaryContentsPage extends BasePage {

    private static final String subscriptionNumber = "Subscription Number";
    private static final  String usageType = "Usage Type";
    private static final  String startDate = "Start Date";
    private static final  String endDate = "End Date";
    private static final  String tariff = "Tariff";
    private static final  String status = "Status";
    private static final  String barring = "Barring";
    private static final  String grg = "GRG";

    private static SummaryContentsPage instance = new SummaryContentsPage();
    public static SummaryContentsPage getInstance(){
        return instance;
    }

    @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Subscriptions')]/../../..//following-sibling::div[1]//table")
    WebElement subscriptionstable;

    @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'End Date:')]/following-sibling::td[1]")
    WebElement lblEndDate;

    TableControlBase table = new TableControlBase(subscriptionstable);
    public String getValueInSubscriptionsTable(int index, String value){
        return table.getCellValueByColumnNameAndRowIndex(index, value);
    }

    public String getSummaryEndDate(){
        return getTextOfElement(lblEndDate);
    }

    public int getRownumberOfSubscriptionsTable(){
        return table.getRowsCount();
    }

    public List<WebElement> getSubscriptions(List<HashMap<String,String>> subscriptions){
        return table.findRowsByColumns(subscriptions);
    }

    public void clickSubscriptionNumberLinkByIndex(int index){
        table.getElementByColumnNameAndRowIndex(index,subscriptionNumber).findElement(By.tagName("a")).click();
    }
}
