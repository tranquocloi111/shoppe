package suite;

import framework.report.elasticsearch.ExecutionListener;
import framework.wdm.Browser;
import framework.wdm.WDFactory;
import framework.wdm.WdManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import java.awt.*;
import java.awt.event.InputEvent;


@Listeners(ExecutionListener.class)
public class KibanaTest {

    public void waitForElement(By by) {
        WdManager.getWait().until(ExpectedConditions.elementToBeClickable(by));
    }

    public void type(By by, String txt) {
        waitForElement(by);
        WdManager.get().findElement(by).sendKeys(txt);
    }

    public void click(By by) {
        waitForElement(by);
        WdManager.get().findElement(by).click();
    }

    @Test(description = "Test Google")
    public void testGoogle() throws Exception {
        WDFactory.getConfig().setDriverVersion("77");
        WdManager.set(WDFactory.initBrowser(Browser.CHROME));
        WdManager.get().get("https://google.com");

        System.out.println("google.com");
    }


//    @Test(description = "Test Facebook")
//    public void testFacebook() {
//        WdManager.set(WDFactory.initBrowser(Browser.CHROME));
//        WdManager.get().get("https://facebook.com");
//        System.out.println(WdManager.get().getTitle());
//        click(By.xpath("//div[@id='reg_pages_msg']/a"));
//        click(By.xpath("//button"));
//        type(By.xpath("//input[@type='text']"), "aaaaaaa");
//        type(By.xpath("//input[@type='password']"), "111111111111");
//        click(By.xpath("//button"));
//
//    }
//
//    @Test(description = "Test Youtube")
//    public void testYoutube() {
//        WdManager.set(WDFactory.initBrowser(Browser.FIREFOX));
//        WdManager.get().get("https://youtube.com");
//        System.out.println(WdManager.get().getTitle());
//        type(By.cssSelector("input#search"), "aaaaaa");
//        click(By.cssSelector("button#search-icon-legacy"));
//    }
//
//    @Test(description = "Test remote 1")
//    public void testRemote() throws MalformedURLException {
//        WdManager.set(WDFactory.remote(new URL("http://localhost:4444/wd/hub"), DesiredCapabilities.chrome()));
//        WdManager.get().get("https://google.com");
//        System.out.println(WdManager.get().getTitle());
//        type(By.id("lst-ib"), "aaaaaaaa");
//        click(By.name("btnK"));
//        click(By.xpath("//a[text() = 'Video']"));
//    }
//
//    @Test(description = "Test remote 2")
//    public void testRemote2() throws MalformedURLException {
//        WdManager.set(WDFactory.remote(new URL("http://localhost:4444/wd/hub"), DesiredCapabilities.chrome()));
//        WdManager.get().get("https://facebook.com");
//        System.out.println(WdManager.get().getTitle());
//        click(By.xpath("//div[@id='reg_pages_msg']/a"));
//        click(By.xpath("//button"));
//        type(By.xpath("//input[@type='text']"), "aaaaaaa");
//        type(By.xpath("//input[@type='password']"), "111111111111");
//        click(By.xpath("//button"));
//    }

    @AfterMethod
    public void after() {
        WdManager.dismissWD();
    }
}
