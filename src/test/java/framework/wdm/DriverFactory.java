package framework.wdm;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DriverFactory {
    private DriverFactory() {
        //Do-nothing..Do not allow to initialize this class from outside
    }

    private static DriverFactory instance = new DriverFactory();

    public static DriverFactory getInstance() {
        return instance;
    }

    //Initial value for webdriver
    ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>() // thread local driver object for webdriver
    {
        @Override
        protected WebDriver initialValue() {
            System.setProperty("webdriver.chrome.driver", "src/test/resources/Driver/chromedriver");
            ChromeOptions options =new ChromeOptions();

            return new ChromeDriver(); // can be replaced with other browser drivers
        }
    };

    //Initial value for webdriverwait
    private static ThreadLocal<WebDriverWait> waitManager = new ThreadLocal<WebDriverWait>() {
        @Override
        protected WebDriverWait initialValue() {
            return new WebDriverWait(getInstance().getDriver(), 30);
        }
    };

    public WebDriverWait getWaitDriver() {
        return waitManager.get();
    }


    public WebDriver getDriver() // call this method to get the driver object and launch the browser
    {
        return driver.get();
    }

    public void removeDriver() // Quits the driver and closes the browser
    {
        driver.get().quit();
        driver.remove();
    }
}
