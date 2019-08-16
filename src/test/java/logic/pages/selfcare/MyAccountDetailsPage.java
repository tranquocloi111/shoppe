package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MyAccountDetailsPage extends BasePage {
    private static class SingletonHelper {
        private static final MyAccountDetailsPage INSTANCE = new MyAccountDetailsPage();
    }

    //Variable
    private MyAccountDetailsPage() {
    }
    @FindBy(id ="UpdateBtn")
    WebElement updateBtn;
    public void clickUpdateBtn()
    {
        click(updateBtn);
    }

    public static MyAccountDetailsPage getInstance() {
        return SingletonHelper.INSTANCE;
    }


    public static class ContactDetailSecton extends MyAccountDetailsPage {
        private static class SingletonHelper {
            private static final ContactDetailSecton INSTANCE = new ContactDetailSecton();
        }

        //Control
        @FindBy(xpath = "//b[contains(text(),'Contact details')]//ancestor::p//following-sibling::div[1]/table")
        WebElement myContactDetailTable;
        TableControlBase table = new TableControlBase(myContactDetailTable);

        //Variable
        private ContactDetailSecton() {
        }

        public static ContactDetailSecton getInstance() {
            return ContactDetailSecton.SingletonHelper.INSTANCE;
        }

        //Method

        public String getEmailAddress() {

            return table.getCellAttributeValueByColumnNameAndRowIndex(4, "Email Address");
        }

        public void changeEmailAddress(String email) {
            enterValueByLabel(table.getCellByFieldKey("Email address"),email);
        }
        public void clickUseEmailAsUserNameCheckBox() {
            click(myContactDetailTable.findElement(By.xpath("//input[@type='checkbox' and @name='useEmailAsUserName']")));
        }

    }


    public static class SecurityDetailSecton extends MyAccountDetailsPage {
        private static class SingletonHelper {
            private static final SecurityDetailSecton INSTANCE = new SecurityDetailSecton();
        }

        //Variable
        private SecurityDetailSecton() {
        }

        public static SecurityDetailSecton getInstance() {
            return SecurityDetailSecton.SingletonHelper.INSTANCE;
        }

        //Control
        @FindBy(xpath = "//b[contains(text(),'Security details')]//ancestor::p//following-sibling::div[1]/table")
        WebElement myContactDetailTable;
        TableControlBase table = new TableControlBase(myContactDetailTable);


        //Method
        public void changeUsername(String username) {
            enterValueByLabel(table.getCellByFieldKey("Username"), username);
        }
        public String getUsername() {
           return getValueOfElement(table.getCellByFieldKey("Username"));
        }
    }
}