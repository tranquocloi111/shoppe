package suite.regression.selfcarews.getaccountsummary;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 18/09/2019
 */
public class TC32114_Alternate_Path_1c_Active_Flexible_Cap_Subscription extends BaseTest {
    Date newStartDate = TimeStamp.TodayMinus20Days();
    List<String> subscriptionNumberList = new ArrayList<>();
    String masterSubscriptionNumber;
    String accountName;
    String clubcardNumber;

    @Test(enabled = true, description = "TC32114_Alternate_Path_1c_Active_Flexible_Cap_Subscription", groups = "SelfCareWS.GetAccountSummary")
    public void TC32114_Alternate_Path_1c_Active_Flexible_Cap_Subscription(){
        test.get().info("1. Create an onlines CC customer with 2 subscriptions and all with bundle");
        String TC32114_CREATE_ORDER = "src\\test\\resources\\xml\\ows\\TC32114_createOrder.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(TC32114_CREATE_ORDER);

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

        test.get().info("7. Get all subscriptions number");
        subscriptionNumberList = CareTestBase.getAllSubscription();

        test.get().info("8. Record master subscription for customer");
        recordMasterSubscriptionForCustomer();

        test.get().info("9. Submit Get Account Summary Request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountSummaryWithSubsRequest( masterSubscriptionNumber);

        test.get().info("10. Build Expected Account Summary Response Data");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getaccount\\TC32114_response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String expectedResponseFile = selfCareWSTestBase.buildResponseData(sampleResponseFile, newStartDate, TimeStamp.TodayPlus1Month(), customerNumber, subscriptionNumberList);

        test.get().info("11. Verify Get Account Summary Response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponseFile, response);

    }

    private void recordMasterSubscriptionForCustomer(){
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        masterSubscriptionNumber = DetailsContentPage.BillingInformationSectionPage.getInstance().getMasterMPN().split(" ")[0];
        accountName = CareTestBase.getCustomerName();
        clubcardNumber = CareTestBase.getClubCardNumber();
    }

}
