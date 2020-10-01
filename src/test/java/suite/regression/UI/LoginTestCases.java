package suite.regression.UI;

import framework.config.Config;
import logic.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTestCases extends BaseTest {

    @Test(enabled = true, description = "Test login ")
    public void loginSucess() {
        test.get().info("Step 1: click the sign-in symbol");
        HomePage.getInstance().ClickSignInBtn();

        test.get().info("Step 2: Fill in the email and password");
        HomePage.getInstance().enterTheEmail(Config.getProp("email"));
        HomePage.getInstance().enterThePassword(Config.getProp("password"));
        HomePage.getInstance().clickLoginBtn();

        test.get().info("Step 3: verify login successfully");
        Assert.assertTrue(HomePage.getInstance().isLogOutDisplayed());


    }


}
