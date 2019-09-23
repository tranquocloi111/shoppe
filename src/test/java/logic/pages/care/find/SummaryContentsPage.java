package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SummaryContentsPage extends BasePage {

    public static SummaryContentsPage getInstance(){
        return new SummaryContentsPage();
    }


    @FindBy(xpath = "//td[@class='informationBoxHeaderBusiness']")
    List<WebElement> listHeaderBusiness;

    public List<String> getBackGroundColorOfHeader(){
        List<String> colors = new ArrayList<>();
        for (WebElement elment: listHeaderBusiness) {
            colors.add(elment.getCssValue("background-color"));
        }
        return colors;
    }

    public static class BusinessInformationPage extends SummaryContentsPage{

        public static BusinessInformationPage getInstance(){
            return new BusinessInformationPage();
        }

        @FindBy(xpath = ".//td[contains(text(),'Business Name:')]/following-sibling::td[1]")
        WebElement businessName;

        @FindBy(xpath = "//td[.='Business Information']")
        WebElement businessHeaderBox;

        public String getBusinessName(){
            return getTextOfElement(businessName);
        }

        public boolean isBusinessPresent(){
            return isElementPresent(businessHeaderBox);
        }

    }
}
