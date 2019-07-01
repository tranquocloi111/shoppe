package logic.pages.selfcare;

import framework.config.Config;
import logic.pages.BasePage;

public class SelfCareTestBase extends BasePage {
    LoginPage loginPage;
    String userName;
    String passWord;

    private SelfCareTestBase() {
        loginPage = new LoginPage();
    }

    public static SelfCareTestBase page() {
        return new SelfCareTestBase();
    }

    public void LoginIntoSelfCarePage(String userName, String passWord, String customerId) {
        loginPage.navigateToSelfCarePage();
        loginPage.login(userName, passWord, customerId);
        waitForPageLoadComplete(120);
    }


}
