package logic.pages.care;

import framework.config.Config;
import javafx.util.Pair;
import logic.pages.BasePage;
import logic.pages.care.find.FindPage;
import logic.pages.care.main.LoginPage;

public class CareTestBase extends BasePage {
    LoginPage loginPage;
    FindPage findPage;
    String userName;
    String passWord;

    private CareTestBase() {
        loginPage = new LoginPage();
        findPage = new FindPage();
        userName = Config.getProp("careUserName");
        passWord = Config.getProp("carePassword");
    }

    public static CareTestBase page() {
        return new CareTestBase();
    }

    public void loadCustomerInHubNet(String customerId) {
        loginPage.navigateToLoginPage();
        loginPage.login(userName, passWord);

        findPage.findCustomer(new Pair<String, String>("Customer Number", customerId));
        findPage.openCustomerByIndex(1);
    }


}
