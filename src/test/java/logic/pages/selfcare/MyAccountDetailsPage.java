package logic.pages.selfcare;

import logic.pages.BasePage;
import logic.pages.TableControlBase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class MyAccountDetailsPage extends BasePage {
    private static class SingletonHelper {
        private static final MyAccountDetailsPage INSTANCE = new MyAccountDetailsPage();
    }

    //Variable
    private MyAccountDetailsPage() {
    }

    @FindBy(id = "UpdateBtn")
    WebElement updateBtn;

    public void clickUpdateBtn() {
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


        public static ContactDetailSecton getInstance() {
            return ContactDetailSecton.SingletonHelper.INSTANCE;
        }

        //Method

        public String getEmailAddress() {

            return table.getCellAttributeValueByColumnNameAndRowIndex(4, "Email Address");
        }

        public void changeEmailAddress(String email) {
            enterValueByLabel(table.getCellByFieldKey("Email address"), email);
        }

        public void clickUseEmailAsUserNameCheckBox() {
            click(myContactDetailTable.findElement(By.xpath("//input[@type='checkbox' and @name='useEmailAsUserName']")));
        }
        public String getDaytimetelephoneNumbe() {

            return getValueOfElement(table.getCellByFieldKey( "Daytime telephone number"));
        }
        public String getEveningTelephoneNumber() {

            return getValueOfElement(table.getCellByFieldKey( "Evening telephone number"));
        }
        public String getMobilePhone() {

            return getValueOfElement(table.getCellByFieldKey( "Mobile number"));
        }
        public String getContact() {

            return getTextOfSelectedOption(table.findCellByLabelText( "Contact"));
        }
        public boolean  isUseEmailAsUserNameSelected()
        {
            return myContactDetailTable.findElement(By.xpath("//input[@type='checkbox' and @name='useEmailAsUserName']")).isSelected();
        }
    }


    public static class SecurityDetailSecton extends MyAccountDetailsPage {
        private static class SingletonHelper {
            private static final SecurityDetailSecton INSTANCE = new SecurityDetailSecton();
        }

        public static SecurityDetailSecton getInstance() {
            return SecurityDetailSecton.SingletonHelper.INSTANCE;
        }

        //Control
        @FindBy(xpath = "//b[contains(text(),'Security details')]//ancestor::p//following-sibling::div[1]/table")
        WebElement mySecurityTable;
        TableControlBase table = new TableControlBase(mySecurityTable);


        //Method
        public void changeUsername(String username) {
            enterValueByLabel(table.getCellByFieldKey("Username"), username);
        }

        public String getUsername() {
            return getValueOfElement(table.getCellByFieldKey("Username"));
        }
        public boolean isUserNameReadOnly() {
            return table.getCellByFieldKey("Username").getAttribute("class").equalsIgnoreCase("readonly");
        }
        public String getSecurityQuestion() {
            return getTextOfSelectedOption(table.findCellByLabelText("Security question"));
        }

        public String getSecurityAnswer() {
            return getValueOfElement(table.getCellByFieldKey("Security answer"));
        }
    }

    public static class personnalDetails extends MyAccountDetailsPage {

        public static personnalDetails getInstance() {
            return new personnalDetails();
        }

        //Control
        @FindBy(xpath = "//b[contains(text(),'Personal details')]//ancestor::p//following-sibling::div[1]/table")
        WebElement myPersonalDetailTable;
        TableControlBase table = new TableControlBase(myPersonalDetailTable);


        public String getTitle() {
            return getTextOfElement(table.findCellByLabelText("Title:"));
        }

        public String getFirstName() {
            return getTextOfElement(table.findCellByLabelText("First name"));
        }

        public String getLastName() {
            return getTextOfElement(table.findCellByLabelText("Last name"));
        }

        public String getDOB() {
            return getTextOfElement(table.findCellByLabelText("Date of birth"));
        }

        public String getMainSubscription() {
            return getTextOfElement(table.findCellByLabelText("Main subscription"));
        }

        public String getBillFormat() {
            return getTextOfSelectedOption(table.findCellByLabelText("Bill format"));
        }

        public String getBillNotificationMethod() {
            return getTextOfSelectedOption(table.findCellByLabelText("Bill notification method"));
        }

        public String getBusinessName() {
            return getTextOfElement(table.findCellByLabelText("Business name:"));
        }

    }
    public static class clubCardDetailsSection extends MyAccountDetailsPage {
        private static class SingletonHelper {
            private static final clubCardDetailsSection INSTANCE = new clubCardDetailsSection();
        }

        public static clubCardDetailsSection getInstance() {
            return clubCardDetailsSection.SingletonHelper.INSTANCE;
        }

        //Control
        @FindBy(xpath = "//b[contains(text(),'Clubcard details')]//ancestor::div[@id='clubcardHeadDisplay']//following-sibling::div[@id='clubcardBodyDisplay']")
        WebElement myClubCardTable;
        TableControlBase table = new TableControlBase(myClubCardTable);


        public String getClubCard() {
            return getTextOfElement(table.findCellByLabelText("Clubcard Registered").findElement(By.tagName("span")));
        }
    }
    public static class billingAddressSection extends MyAccountDetailsPage {
        private static class SingletonHelper {
            private static final billingAddressSection INSTANCE = new billingAddressSection();
        }

        public static billingAddressSection getInstance() {
            return billingAddressSection.SingletonHelper.INSTANCE;
        }

        //Control
        @FindBy(xpath = "//b[contains(text(),'Billing address')]//ancestor::p//following-sibling::div[1]/table")
        WebElement myPersonalDetailTable;
        TableControlBase table = new TableControlBase(myPersonalDetailTable);


        public String getBuildingNameOrStreetNumber() {
            return getTextOfElement(table.findCellByLabelText("Building name or street number"));
        }

        public String getPostCode() {
            return getTextOfElement(table.findCellByLabelText("Postcode"));
        }
        public String getAddressLine1() {
            return getTextOfElement(table.findCellByLabelText("Address line 1"));
        }
        public String getAddressLine2() {
            return getTextOfElement(table.findCellByLabelText("Address line 2"));
        }
        public String getTown() {
            return getTextOfElement(table.findCellByLabelText("Town"));
        }
    }
}