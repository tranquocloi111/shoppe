package logic.pages.care;

import framework.wdm.WdManager;
import logic.pages.BasePage;
import logic.pages.care.sidebar.LeftMenuPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import java.util.HashMap;

public class ServiceOrdersContentPage extends BasePage {

    @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Services Orders')]/../../..//following-sibling::div[1]//table")
    WebElement serviceOrdertable;


    public void verifyDeactivateAccountSOIsInProvisionWait(){
        LeftMenuPage.page().clickServiceOrdersLink();
        Assert.assertEquals(1, findRowsByColumns(serviceOrdertable, serviceOrderParam()).size());
    }

    public String getServiceOrderId(){
        return findRowsByColumns(serviceOrdertable, serviceOrderParam()).get(0).getText().split(" ")[0];
    }

    private HashMap<String,String> serviceOrderParam(){
        HashMap<String,String> so = new HashMap<String,String>();
        so.put("Date", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT));
        so.put("Status","Deactivate Account");
        so.put("Type","Send DDI to BACS");
        return null;
    }

}
