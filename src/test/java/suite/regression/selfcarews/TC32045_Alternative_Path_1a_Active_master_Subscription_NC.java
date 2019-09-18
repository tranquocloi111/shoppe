package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

/**
 * User: Nhi Dinh
 * Date: 16/09/2019
 */
public class TC32045_Alternative_Path_1a_Active_master_Subscription_NC extends BaseTest {
    String billingGroupName;
    String subscriptionNumber;
    String addressee;
    String email;

    @Test(enabled = true, description = "TC32045_Alternative Path 1a Active master Subscription NC", groups = "SelfCareWS")
    public void TC32045_Alternative_Path_1a_Active_master_Subscription_NC(){
        test.get().info("Create a customer with master subscription NC");
        OWSActions owsActions = new OWSActions();
        String owsRequest = "src\\test\\resources\\xml\\ows\\TC32045_createOrder.xml";
        owsActions.createGeneralCustomerOrder(owsRequest);
        String customerNumber = owsActions.customerNo;

        test.get().info("Create new billing group");
        createNewBillingGroup();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Get subscription number");
        getSubscriptionNumber();

        test.get().info("Verify Master MPN is correct");
        verifyMasterMPNIsCorrect();

        test.get().info("Record billing related values from details content");
        recordBillingRelatedValuesFromDetailsContent();

        test.get().info("Submit get account detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountDetailsRequest(customerNumber);

        test.get().info("Build expected response data");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String tempFilePath = "src\\test\\resources\\xml\\sws\\getaccountdetails\\TC32045_response.xml";
        String expectedResponse = selfCareWSTestBase.buildCustomerDetailsResponseData(tempFilePath, customerNumber, addressee, subscriptionNumber, email, billingGroupName);

        test.get().info("Verify get account detail response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponse, response);
    }

    private void recordBillingRelatedValuesFromDetailsContent(){
        billingGroupName = BillingActions.tempBillingGroupHeader.getValue();
        subscriptionNumber = DetailsContentPage.BillingInformationSectionPage.getInstance().getMasterMPN().split(" ")[0];
        addressee = DetailsContentPage.AddressInformationPage.getInstance().getAddressee();
        email = DetailsContentPage.AddressInformationPage.getInstance().getEmail();
    }

    private void verifyMasterMPNIsCorrect(){
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(subscriptionNumber, DetailsContentPage.BillingInformationSectionPage.getInstance().getMasterMPN());
    }

    private void getSubscriptionNumber(){
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscriptionNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();
    }
}
