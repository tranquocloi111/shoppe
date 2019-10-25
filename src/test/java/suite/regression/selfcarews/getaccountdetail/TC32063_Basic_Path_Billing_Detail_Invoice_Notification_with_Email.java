package suite.regression.selfcarews.getaccountdetail;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

/**
 * User: Nhi Dinh
 * Date: 17/09/2019
 */
public class TC32063_Basic_Path_Billing_Detail_Invoice_Notification_with_Email extends BaseTest {
    String billingGroupName;
    String subscriptionNumber;
    String addressee;
    String email;

    @Test(enabled = true, description = "TC32058_Basic Path getAccountDetail Address contains Address Line 1", groups = "SelfCareWS.GetAccountDetail")
    public void TC32063_Basic_Path_Billing_Detail_Invoice_Notification_with_Email(){
        test.get().info("Create a customer with Email notification");
        OWSActions owsActions = new OWSActions();
        String request = "src\\test\\resources\\xml\\ows\\TC32063_createOrder.xml";
        owsActions.createGeneralCustomerOrder(request);
        String customerNumber = owsActions.customerNo;

        test.get().info("Create new billing group");
        createNewBillingGroup();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Verify the billing notification is email");
        verifyTheBillingNotificationIsEmail();

        test.get().info("Record billing related values from details content");
        recordBillingRelatedValuesFromDetailsContent();

        test.get().info("Submit get account detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountDetailsRequest(customerNumber);

        test.get().info("Build expected response data");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String tempFilePath = "src\\test\\resources\\xml\\sws\\getaccountdetails\\TC32063_response.xml";
        String expectedResponse = selfCareWSTestBase.buildCustomerDetailsResponseData(tempFilePath, customerNumber, addressee, subscriptionNumber, email, billingGroupName);

        test.get().info("Verify get account detail response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponse, response);

    }

    private void verifyTheBillingNotificationIsEmail(){
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("Email", DetailsContentPage.BillingInformationSectionPage.getInstance().getBillNotification());
    }

    private void recordBillingRelatedValuesFromDetailsContent(){
        billingGroupName = BillingActions.tempBillingGroupHeader.getValue();
        subscriptionNumber = DetailsContentPage.BillingInformationSectionPage.getInstance().getMasterMPN().split(" ")[0];
        addressee = DetailsContentPage.AddressInformationPage.getInstance().getAddressee();
        email = DetailsContentPage.AddressInformationPage.getInstance().getEmail();
    }
}
