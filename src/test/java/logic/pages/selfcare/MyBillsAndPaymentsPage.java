package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;

public class MyBillsAndPaymentsPage extends BasePage {
    private static MyBillsAndPaymentsPage instance;
    @FindBy(xpath = "//div[@id='body-content']//table")
    WebElement myBillsAndPaymentTable;

    public static MyBillsAndPaymentsPage getInstance() {
        if (instance == null)
            return new MyBillsAndPaymentsPage();
        return instance;
    }

    TableControlBase tableControlBase = new TableControlBase(myBillsAndPaymentTable);


    public int getNumberPaymentByEnity(HashMap<String, String> enity) {
        return tableControlBase.findRowsByColumns(enity).size();
    }

    public int getTotalNumberPayment() {
        return tableControlBase.getAllRows().size()-1;
    }

}

