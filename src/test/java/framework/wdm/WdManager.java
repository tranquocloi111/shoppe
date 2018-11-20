package framework.wdm;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Manage WD instances
 */
public class WdManager {

    /**
     * ThreadLocal helps manage WD instances among threads
     */
    private static ThreadLocal<WebDriver> wdm = new ThreadLocal<>();

    /**
     * ThreadLocal helps mânge WD wait instances among threads
     */
    private static ThreadLocal<WebDriverWait> waitManager = new ThreadLocal<>();

    /**
     * ThreadLocal helps mânge WD wait instances among threads
     */
    private static ThreadLocal<AjaxElementLocatorFactory> ajaxEle = new ThreadLocal<>();

    /**
     * Get WD in current thread
     *
     * @return WD in current thread
     */
    public static WebDriver get() {
        return wdm.get();
    }

    /**
     * Get WD wait
     *
     * @return WD wait in current thread
     */
    public static WebDriverWait getWait() {
        return waitManager.get();
    }

    /**
     * Get WD wait
     *
     * @return WD wait in current thread
     */
    public static AjaxElementLocatorFactory getAjaxEle() {
        return ajaxEle.get();
    }

    /**
     * Set WD to current thread
     *
     * @param driver
     */
    public static void set(final WebDriver driver) {
        if (wdm.get() != null) {
            dismissWD();
        }
        wdm.set(driver);
        waitManager.set(new WebDriverWait(wdm.get(), 20));
        ajaxEle.set(new AjaxElementLocatorFactory(wdm.get(), 20));
    }

    /**
     * Close and remove WD from current thread
     */
    public static void dismissWD() {
        if (get() != null) {
            get().quit();
        }
        wdm.remove();
    }
}
