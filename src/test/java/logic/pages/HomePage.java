package logic.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePage {


    private HomePage() {
    }

    public static HomePage getInstance() {
        return new HomePage();
    }

    public void ClickSignInBtn() {
        click(getSpanByText("Register/Login"));
    }

    public void enterTheEmail(String email) {
        enterValueByLabel(getInputById("loginEmail"), email);
    }

    public void enterThePassword(String password) {
        enterValueByLabel(getInputByPlaceHolder("Enter Password"), password);
    }

    public void clickLoginBtn() {
        submit(getButtonByText("Login"));
    }


    public boolean isLogOutDisplayed(){
        return getSpanByText("Logout").isDisplayed();
    }

}
