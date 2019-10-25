package suite.regression.selfcarews.getaccountdetail;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

/**
 * User: Nhi Dinh
 * Date: 16/09/2019
 */
public class TC32044_Self_Care_WS_Active_Subscription_Flexible_Cap extends BaseTest {
    String billingGroupName;
    String subscriptionNumber;
    String addressee;
    String email;

    @Test(enabled = true, description = "TC32044_Self Care WS Active Subscription Flexible Cap", groups = "SelfCareWS.GetAccountDetail")
    public void TC32044_Self_Care_WS_Active_Subscription_Flexible_Cap(){
        test.get().info("Create a customer with 1 FC");
        OWSActions owsActions = new OWSActions();
        String owsRequest = "src\\test\\resources\\xml\\ows\\TC32044_createOrder.xml";
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

        test.get().info("Record billing related values from details content");
        recordBillingRelatedValuesFromDetailsContent();
        //=============================================================================
        test.get().info("Submit get account detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountDetailsRequest(customerNumber);

        test.get().info("Build expected response data");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String tempFilePath = "src\\test\\resources\\xml\\sws\\getaccountdetails\\TC32044_response.xml";
        String expectedResponse = selfCareWSTestBase.buildCustomerDetailsResponseData(tempFilePath, customerNumber, addressee, subscriptionNumber, email, billingGroupName);

        test.get().info("Verify get account detail response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponse, response);
    }

    private void recordBillingRelatedValuesFromDetailsContent(){
        billingGroupName = BillingActions.tempBillingGroupHeader.getValue();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        subscriptionNumber = DetailsContentPage.BillingInformationSectionPage.getInstance().getMasterMPN();
        addressee = DetailsContentPage.AddressInformationPage.getInstance().getAddressee();
        email = DetailsContentPage.AddressInformationPage.getInstance().getEmail();
    }
}
