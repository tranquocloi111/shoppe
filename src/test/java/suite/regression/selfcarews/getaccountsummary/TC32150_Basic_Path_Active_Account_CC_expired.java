package suite.regression.selfcarews.getaccountsummary;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.find.CommonContentPage;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 1/10/2019
 */
public class TC32150_Basic_Path_Active_Account_CC_expired extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    private String subscriptionNumber;
    private String clubCardNumber;

    @Test(enabled = true, description = "TC32150_Basic_Path_Active_Account_CC_expired", groups = "SelfCareWS.GetAccountSummary")
    public void TC32150_Basic_Path_Active_Account_CC_expired(){
        test.get().info("Step 1 : Create a CC Customer with FC 1 bundle and NK2720");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC1BundleAndNK2720();
        customerNumber = owsActions.customerNo;

        test.get().info("2. Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("3. Update bill group payment collection date to 10 days later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("4. Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("5. Update the start date of customer");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("6. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("7. Get subscription number");
        subscriptionNumber = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberByIndex(1);

        test.get().info("8. Update Credit card expiry date");
        updateCreditCardExpiryDate();

        test.get().info("9. Submit Get Account Summary Request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountSummaryWithSubsRequest(subscriptionNumber);

        test.get().info("10. Build Expected Account Summary Response Data");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getaccount\\TC32150_response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String expectedResponseFile = selfCareWSTestBase.buildResponseData(sampleResponseFile, newStartDate, TimeStamp.TodayPlus1Month(), customerNumber, subscriptionNumber);

        test.get().info("11. Verify Get Account Summary Response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponseFile, response);

    }

    private void updateCreditCardExpiryDate() {
        CommonActions.updatePrpovaldate("1/01/2005", customerNumber);
        CommonActions.updateProvalNumberValue(customerNumber,"CCEY", 2005);
        CommonActions.updateProvalNumberValue(customerNumber,"CCEM", 1);
    }

}
