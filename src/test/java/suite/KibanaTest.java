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
        WDFactory.getConfig().setDriverVersion("74");
        WdManager.set(WDFactory.initBrowser(Browser.CHROME));
        WdManager.get().get("https://dev-platform.theapipractice.com");
        WdManager.get().manage().window().maximize();
        System.out.println(WdManager.get().getTitle());
        type(By.name("user_name"), "dinhquyenonline@gmail.com");
        type(By.name("password"),"0906879564@Qt");
        click(By.xpath("//button[@type='submit']"));
        Thread.sleep(10000);

        click(By.xpath("//a[@href='/userflows']"));
        click(By.xpath("//a[@href='/userflows/08d6f408-7218-fe1e-97e3-995037e58882']"));
        //click(By.xpath("//button[contains(@class,'ant-btn-primary')]"));
        Point browserLoc =  WdManager.get().manage().window().getPosition();
        Point elementLoc = WdManager.get().findElement(By.xpath("//button[contains(text(),'Flow Step')]")).getLocation();
        Dimension elementSize = WdManager.get().findElement(By.xpath("//button[contains(text(),'Flow Step')]")).getSize();

        int x = elementLoc.getX();// + browserLoc.getX() + elementSize.getWidth()/2;
        int y = elementLoc.getY() ;// + browserLoc.getY() + elementSize.getHeight()/2;

        elementLoc = WdManager.get().findElement(By.xpath("//div[@title='deom']")).getLocation();
        elementSize = WdManager.get().findElement(By.xpath("//div[@title='deom']")).getSize();

        int x1 = elementLoc.getX() ;// + browserLoc.getX() + elementSize.getWidth()/2;
        int y1 = elementLoc.getY() ;// + browserLoc.getY() + elementSize.getHeight()/2;

        //click(x + 800, y + 200 , x1, y1);

        dragAndDropElement(WdManager.get().findElement(By.xpath("//button[contains(text(),'Flow Step')]")), WdManager.get().findElement(By.xpath("//div[@title='deom']")));

        System.out.println("asas");
    }

    public static void dragAndDropElement(WebElement dragFrom, WebElement dragTo) throws Exception {
        // Setup robot
        Robot robot = new Robot();
        robot.setAutoDelay(500);
        // Get size of elements
        Dimension fromSize = dragFrom.getSize();
        Dimension toSize = dragTo.getSize();
        Point toLocation = dragTo.getLocation();
        Point fromLocation = dragFrom.getLocation();
        //Make Mouse coordinate centre of element
        toLocation.x += toSize.width/2;
        toLocation.y += toSize.height/2 + 50 ;
        fromLocation.x += fromSize.width/2;
        fromLocation.y += fromSize.height/2 + 50;

        //Move mouse to drag from location
        robot.mouseMove(fromLocation.x + 16, fromLocation.y +55);
        //Click and drag
        robot.mousePress(InputEvent.BUTTON1_MASK);

        //Drag events require more than one movement to register
        //Just appearing at destination doesn't work so move halfway first
        //Have 1 flow step
        //robot.mouseMove(((toLocation.x - fromLocation.x) / 2) + fromLocation.x , ((toLocation.y - fromLocation.y) / 2) + fromLocation.y +50);
        robot.mouseMove(((toLocation.x - fromLocation.x) / 2) + fromLocation.x , ((toLocation.y - fromLocation.y) / 2) + fromLocation.y +55);


        //Move to final position
        //Have 1 flow step
        //robot.mouseMove(toLocation.x, toLocation.y + 50);
        robot.mouseMove(toLocation.x, toLocation.y + 95);
        //Drop
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(10000);
    }

    public static void click(int x , int y,int x2, int y2) throws AWTException, InterruptedException{
        Robot b11 = new Robot();

        b11.mouseMove(x, y);

        b11.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(1000);//There is pause in miliseconds
        b11.mouseMove(x2, y2);
        b11.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

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
