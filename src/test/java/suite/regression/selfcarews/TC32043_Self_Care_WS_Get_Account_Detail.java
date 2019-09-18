package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.main.TasksContentPage;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

/**
 * User: Nhi Dinh
 * Date: 16/09/2019
 */
public class TC32043_Self_Care_WS_Get_Account_Detail extends BaseTest {
    String billingGroupName;
    String subscriptionNumber;
    String addressee;
    String email;

    @Test(enabled = true, description = "TC32043_Self_Care WS Get Account Detail", groups = "SelfCareWS")
    public void TC32043_Self_Care_WS_Get_Account_Detail(){
        test.get().info("Create a customer with FC correct expiry date");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWithFCCorrectExpiryDate();
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
        String tempFilePath = "src\\test\\resources\\xml\\sws\\getaccountdetails\\TC32043_response.xml";
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
