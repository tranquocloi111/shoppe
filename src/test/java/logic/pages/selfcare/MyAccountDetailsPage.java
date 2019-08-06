package logic.pages.selfcare;

import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MyAccountDetailsPage extends MyPersonalInformationPage {
    private static class SingletonHelper {
        private static final MyAccountDetailsPage INSTANCE = new MyAccountDetailsPage();
    }
    //Variable
    private MyAccountDetailsPage() {}

    public static MyAccountDetailsPage getInstance() {
        return SingletonHelper.INSTANCE;
    }

    //Control
    @FindBy(xpath = "//b[contains(text(),'Contact details')]//ancestor::p//following-sibling::div[1]/table")
    WebElement myContactDetailTable;
    TableControlBase table = new TableControlBase(myContactDetailTable);



    //Method

    public String getEmailAddress() {

       return   table.getCellAttributeValueByColumnNameAndRowIndex(4,"Email Address");
    }

}