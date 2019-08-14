package logic.pages.care.find;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;

public class SelfCareSettingContentPage extends BasePage {

    public static class SelfCareSettingSection extends SelfCareSettingContentPage {

        public static SelfCareSettingSection getInstance() {
            return new SelfCareSettingSection();
        }

        @FindBy(xpath = "//td[@class='informationBoxHeader' and contains(text(),'Self Care Settings')]/../../..//following-sibling::div[1]//table")
        WebElement selfCareSettingGridCotent;
        TableControlBase table = new TableControlBase(selfCareSettingGridCotent);

        public void clickFirstRow() {
            String xpath = String.format("//tr//a[@class='informationBoxRow1']");
            selfCareSettingGridCotent.findElement(By.xpath(xpath)).click();
        }


        @FindBy(xpath = "//input[@value ='Reset Password']")
        WebElement btnResetPassword;

        public void clickResetPasswordBtn() {
            btnResetPassword.click();
        }

        public String getUserStatusByUserName(String text) {
            String xpath = String.format("//a[contains(text(),'%s')]/ancestor::td[@class='desc']//following-sibling::td[2]", text);
            return getTextOfElement(selfCareSettingGridCotent.findElement(By.xpath(xpath)));

        }
        public String getUserName() {
            String xpath = String.format("//a[@class='informationBoxRow1']");
            return getTextOfElement(selfCareSettingGridCotent.findElement(By.xpath(xpath)));

        }
    }

}
