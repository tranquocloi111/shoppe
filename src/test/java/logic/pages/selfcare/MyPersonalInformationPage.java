package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MyPersonalInformationPage extends BasePage {

    public static class MyPreviousOrdersPage extends MyPersonalInformationPage {
        private static MyPreviousOrdersPage instance;
        public static MyPreviousOrdersPage getInstance(){
            if (instance == null)
                return new MyPreviousOrdersPage();
            return instance;
        }


        @FindBy(xpath = "//b[contains(text(),'My previous orders and contract')]/ancestor::table[1]/following-sibling::div//table")
        WebElement myPreviousOrdersContracttable;
        TableControlBase tableControlBase = new TableControlBase(myPreviousOrdersContracttable);

        public void clickViewByIndex(int index){
            click(myPreviousOrdersContracttable.findElement(By.xpath(".//tr["+index+"]")).findElement(By.linkText("View")));
        }
    }
}
