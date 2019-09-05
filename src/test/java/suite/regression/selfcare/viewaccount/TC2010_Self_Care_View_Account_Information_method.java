package suite.regression.selfcare.viewaccount;

import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

public class TC2010_Self_Care_View_Account_Information_method extends BaseTest {

    String masterMPN;
    String dateTimeTelephoneNumber;
    String eveningTelephoneNumber;
    String mobileNumber;
    String clubCardRegistered;
    String firstName;
    String lastName;
    String customer;
    String subNo;
    String email;
    String userName;

    @Test(enabled = true, description = "TC2010 Self Care view account infomation method ", groups = "SelfCare")
    public void TC2010_Self_Care_View_Account_Information_method() {
        test.get().info("Create a CC customer with no bundle and sim only");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_NC_no_bundle_and_sim_only";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile NC 1");
        String customerNumber = owsActions.customerNo;
        subNo = owsActions.serviceRef;
        firstName = owsActions.firstName;
        lastName = owsActions.lastName;
        email = owsActions.email;
        userName = owsActions.username;


        test.get().info("Load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("get expect result in hub");
        getExpectResultsInHub();

        test.get().info("Login to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Click view or change my account details in my account block");
        SelfCareTestBase.page().viewOrChangeMyAccountDetails();

        test.get().info("verify personal block is correct");
        verifyPersonalDetailsBlockIsCorrect();

        test.get().info("verify contact detail block is correct");
        verifyContacdetailsBlockIsCorrect();

        test.get().info("verify security block is correct");
        verifySecurityBlockIsCorrect();

        test.get().info("verify cub card block is correct");
        verifyClubCardBlockIsCorrect();

        test.get().info("verify address detail block is correct");
        verifyBillingAddressDetailBlockIsCorrect();


    }

    public void getExpectResultsInHub() {
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        masterMPN = DetailsContentPage.BillingInformationSectionPage.getInstance().getMasterMPN();
        dateTimeTelephoneNumber = DetailsContentPage.AddressInformationPage.getInstance().getDayTimeTelephoneNumber();
        eveningTelephoneNumber = DetailsContentPage.AddressInformationPage.getInstance().getEveningTelephoneNumber();
        mobileNumber = DetailsContentPage.AddressInformationPage.getInstance().getMobileNumber();
        clubCardRegistered = DetailsContentPage.CreditInformationPage.getInstance().getClubCardNumber();

    }

    public void verifyPersonalDetailsBlockIsCorrect() {
        Assert.assertEquals(MyAccountDetailsPage.personnalDetails.getInstance().getTitle(), "Mr");
        Assert.assertEquals(MyAccountDetailsPage.personnalDetails.getInstance().getFirstName(), firstName);
        Assert.assertEquals(MyAccountDetailsPage.personnalDetails.getInstance().getLastName(), lastName);
        Assert.assertEquals(MyAccountDetailsPage.personnalDetails.getInstance().getDOB(), "01/01/1985");
        Assert.assertEquals(MyAccountDetailsPage.personnalDetails.getInstance().getMainSubscription(), subNo);
        Assert.assertEquals(MyAccountDetailsPage.personnalDetails.getInstance().getBillFormat(), "Summary");
        Assert.assertEquals(MyAccountDetailsPage.personnalDetails.getInstance().getBillNotificationMethod(), "SMS");
    }

    public void verifyContacdetailsBlockIsCorrect() {
        Assert.assertEquals(MyAccountDetailsPage.ContactDetailSecton.getInstance().getDaytimetelephoneNumbe(), dateTimeTelephoneNumber);
        Assert.assertEquals(MyAccountDetailsPage.ContactDetailSecton.getInstance().getEveningTelephoneNumber(), eveningTelephoneNumber);
        Assert.assertEquals(MyAccountDetailsPage.ContactDetailSecton.getInstance().getMobilePhone(), mobileNumber);
        Assert.assertEquals(MyAccountDetailsPage.ContactDetailSecton.getInstance().getEmailAddress(), email);
        Assert.assertEquals(MyAccountDetailsPage.ContactDetailSecton.getInstance().getContact(), "Daytime Telephone Number");
        Assert.assertFalse(MyAccountDetailsPage.ContactDetailSecton.getInstance().isUseEmailAsUserNameSelected());
    }

    public void verifySecurityBlockIsCorrect() {
        Assert.assertEquals(MyAccountDetailsPage.SecurityDetailSecton.getInstance().getUsername(), userName);
        Assert.assertFalse(MyAccountDetailsPage.SecurityDetailSecton.getInstance().isUserNameReadOnly());
        Assert.assertEquals(MyAccountDetailsPage.SecurityDetailSecton.getInstance().getSecurityQuestion(), "Mothers maiden name?");
        Assert.assertEquals(MyAccountDetailsPage.SecurityDetailSecton.getInstance().getSecurityAnswer(), "smith");
    }


    public void verifyClubCardBlockIsCorrect() {
        Assert.assertEquals(MyAccountDetailsPage.clubCardDetailsSection.getInstance().getClubCard(), "**************2784");
    }

    public void verifyBillingAddressDetailBlockIsCorrect() {
        Assert.assertEquals(MyAccountDetailsPage.billingAddressSection.getInstance().getBuildingNameOrStreetNumber(), "6");
        Assert.assertEquals(MyAccountDetailsPage.billingAddressSection.getInstance().getPostCode(), "E10AA");
        Assert.assertEquals(MyAccountDetailsPage.billingAddressSection.getInstance().getAddressLine1(), "LUKIN STREET");
        Assert.assertEquals(MyAccountDetailsPage.billingAddressSection.getInstance().getAddressLine2(), "");
        Assert.assertEquals(MyAccountDetailsPage.billingAddressSection.getInstance().getTown(), "LONDON");
    }
}
