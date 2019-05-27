package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class ServiceOrdersContentPage extends BasePage {

    private static final String id = "Id";
    private static final String date = "Date";
    private static final String status = "Status";
    private static final String type = "Type";
    private static final String subscription = "Subscription";
    private static final String parentId = "Parent Id";


    private static ServiceOrdersContentPage instance = new ServiceOrdersContentPage();
    public static ServiceOrdersContentPage getInstance(){
        return instance;
    }

    @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Services Orders')]/../../..//following-sibling::div[1]//table")
    WebElement serviceOrdertable;

    TableControlBase tableControlBase = new TableControlBase(serviceOrdertable);
    public List<WebElement> getServiceOrder(List<HashMap<String,String>> orderService){
        return tableControlBase.findRowsByColumns(orderService);
    }

    public static int getServiceOrderId(List<WebElement> serviceOrder){
        return Integer.parseInt(serviceOrder.get(0).getText().split(" ")[0]);
    }

    public void clickServiceOrderIdLink(int serviceOrderId){
        tableControlBase.getRowByColumnNameAndCellValue(id, String.valueOf(serviceOrderId)).findElement(By.tagName("a")).click();
    }
}
