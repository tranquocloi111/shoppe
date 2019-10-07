package logic.pages.selfcare;

import logic.business.helper.MiscHelper;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;

public class MyUsageSinceMyLastBillPage extends BasePage {

    public static MyUsageSinceMyLastBillPage getInstance() {
        return new MyUsageSinceMyLastBillPage();
    }


    @FindBy(xpath = "//label[text()='Subscription']//ancestor::td[1]//following-sibling::td/select")
    WebElement subscriptionSelect;

    public void setSubscriptionSelect(String text)
    {
        selectByVisibleText(subscriptionSelect,text);
    }

    public void saveFileFromWebRequest( String imgFile){
        WebElement img= getDriver().findElement(By.xpath("//img[@id='imgUsage']"));
        String link=img.getAttribute("src");
        MiscHelper.saveFileFromWebRequest(link,imgFile);
    }
}
