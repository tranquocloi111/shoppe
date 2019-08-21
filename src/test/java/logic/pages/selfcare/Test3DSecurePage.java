package logic.pages.selfcare;

import logic.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class Test3DSecurePage extends BasePage {
    public  static  WebDriver driver = null;
    public static Test3DSecurePage getInstance() {
        return new Test3DSecurePage();
    }


    public void enter3DPassword(String password) {
        enterValueByLabel(driver.findElement(By.xpath("//input[@id='issuerForm:password']")), password);
        waitForPageLoadComplete(50);
    }



    public void clickVerifyMe() {
        click(driver.findElement(By.xpath("//button[@id='issuerForm:verify']")));
    }
}
