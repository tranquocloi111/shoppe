package suite.performance;

import com.aventstack.extentreports.Status;
//import javafx.util.Pair;
import logic.pages.care.find.FindPage;
import logic.pages.care.main.LoginPage;
import framework.utils.Timer;
import org.testng.annotations.Test;
import suite.BaseTest;

import java.util.AbstractMap;

public class CareScreenPerfTest extends BaseTest {


    private void testLoginScreen() {
        LoginPage loginPage = new LoginPage();
        Timer.start();
        loginPage.login("admin", "ADMIN1");
        test.get().log(Status.PASS, "Login loading time: " + Timer.stop() + " ms");

        FindPage findPage = new FindPage();
        Timer.start();
        findPage.findCustomer(new AbstractMap.SimpleEntry<>("First Name", "first*"));
        test.get().log(Status.PASS, "Find customer loading time: " + Timer.stop() + " ms");

        Timer.start();
        findPage.openCustomerByIndex(1);
        test.get().log(Status.PASS, "Open customer loading time: " + Timer.stop() + " ms");

    }

    @Test
    public void testLoginScreen1() {
        testLoginScreen();
    }

    @Test
    public void testLoginScreen2() {
        testLoginScreen();
    }

    @Test
    public void testLoginScreen3() {
        testLoginScreen();
    }
}
