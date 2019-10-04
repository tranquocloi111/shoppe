package logic.pages.care.find;

import javafx.util.Pair;
import logic.pages.BasePage;
import logic.pages.care.MenuPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FindPage extends BasePage {

    @FindBy(xpath = "//*[@id='Validation']//following-sibling::table")
    WebElement tblFindDetail;

    @FindBy(xpath = "//input[@value='Find Now']")
    WebElement btnFindNow;

    @FindBy(xpath = "//td[contains(.,'Results')]//ancestor::table/following-sibling::div[@class='box-shadow']//table")
    WebElement tblResult;

    @FindBy(xpath = "//a[@class='informationBoxRow1']")
    WebElement lblName;

    public static FindPage getInstance(){
        return  new FindPage();
    }

    public void findCustomer(Pair<String, String>... pairs) {
        for (Pair<String, String> p : pairs) {
            enterValueByLabel(tblFindDetail, p.getKey(), p.getValue());
        }
        btnFindNow.click();
        waitForPageLoadComplete(60);
    }

    public void openCustomerByIndex(int index) {
        click(getCell(tblResult, index + 1, 2).findElement(By.xpath("//a[@class='informationBoxRow1']")));
        waitForPageLoadComplete(60);
    }
    public boolean IsCustomerDiplayedByIndex(int index) {
        return isElementPresent(getCell(tblResult, index + 1, 2).findElement(By.xpath("//a[@class='informationBoxRow1']")));
    }

    public void navigateToCustomerDetailPage(int index, String customerNumber){
        MenuPage.HeaderMenuPage.getInstance().clickCustomersTab();
        findCustomer(new Pair<String, String>("Customer Number", customerNumber));
        openCustomerByIndex(index);
    }

    public String getUnderGoValue(){
        return lblName.getCssValue("text-decoration");
    }

    public boolean isUnderGoPresent(){
        return isElementPresent(lblName);
    }

    public String getNameOfResult(int index){
       return getTextOfElement(getCell(tblResult, index + 1, 2).findElement(By.xpath("//a[@class='informationBoxRow1']")));
    }

}
