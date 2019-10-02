package logic.pages.care.find;

import javafx.util.Pair;
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
    public static ServiceOrdersContentPage getInstance() {
        return new ServiceOrdersContentPage();
    }

    @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Services Orders')]/../../..//following-sibling::div[1]//table")
    WebElement serviceOrdertable;
    TableControlBase tableControlBase = new TableControlBase(serviceOrdertable);

    public String getServiceOrderIdByElementServiceOrders(List<WebElement> serviceOrder) {
        return serviceOrder.get(0).getText().split(" ")[0];
    }

    public String getServiceOrderIdByOrderServices(List<HashMap<String,String>> orderServices) {
        return tableControlBase.findRowsByColumns(orderServices).get(0).getText().split(" ")[0];
    }

    public String getSubscriptionNumber(List<WebElement> serviceOrder) {
        return serviceOrder.get(0).getText().split(" ")[8];
    }

    public List<WebElement> getServiceOrders(List<HashMap<String,String>> orderServices) {
        return tableControlBase.findRowsByColumns(orderServices);
    }

    public List<WebElement> getServiceOrders(HashMap<String,String> orderServices) {
        return tableControlBase.findRowsByColumns(orderServices);
    }

    public List<WebElement> getServiceOrder(Pair<String, String> orderService) {
        return tableControlBase.findRowsByColumns(orderService);
    }

    public int getNumberOfServiceOrders(List<HashMap<String, String>> orderService) {
        return tableControlBase.findRowsByColumns(orderService).size();
    }

    public int getNumberOfServiceOrdersByOrderService(HashMap<String, String> orderService) {
        return tableControlBase.findRowsByColumns(orderService).size();
    }
    public int getNumberOfServiceOrders(HashMap<String, String> orderService) {
        return tableControlBase.findRowsByColumns(orderService).size();
    }

    public void clickServiceOrderIdLink(String serviceOrderId) {
        tableControlBase.getRowByColumnNameAndCellValue(id, serviceOrderId).findElement(By.tagName("a")).click();
    }

    public void clickServiceOrderByType(String type) {
        tableControlBase.getRowByColumnNameAndCellValue(type, type).findElement(By.tagName("a")).click();
    }

    public String getServiceOrderIdByIndex(int index) {
        return tableControlBase.getCellValueByColumnNameAndRowIndex(index + 1, "Id");
    }
    public String getServiceOrderidByType(String type)
    {
        return getTextOfElement(tableControlBase.getRowByColumnNameAndCellValue(type, type).findElement(By.tagName("a")));
    }
    public void clickNextBtn()
    {
        super.clickNextBtn();
    }

    @FindBy(xpath = "//input[@value='...']")
    WebElement newTariffSearchBtn;
    public void clicknewTariffSearchBtn()
    {
        click(newTariffSearchBtn);
    }

    public List<List<String>> getAllValueOfServiceOrder(){
        return  tableControlBase.getAllCellValue();
    }
}
