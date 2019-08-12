package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SelfCareSettingDetailPage extends BasePage {
    public static class SelfCareSettingDetailSection extends SelfCareSettingDetailPage {

        public static SelfCareSettingDetailSection getInstance() {
            return new SelfCareSettingDetailSection();
        }

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Self Care Settings')]/../../..//following-sibling::div[1]//table")
        WebElement selfCareSettingGridCotent;
        TableControlBase table = new TableControlBase(selfCareSettingGridCotent);

        public String getUserStatus() {
            return getTextOfElement(table.getCellByLabel("Status"));
        }

    }


}
