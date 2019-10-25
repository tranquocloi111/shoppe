package suite.regression.selfcarews.getsubscriptionsummary;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 10/10/2019
 */
public class TC32328_Self_Care_WS_Subscription_Summary_deactivated_account_Inactive_Subscription_Flag_as_TRUE extends BaseTest {
    private Date newStartDate = TimeStamp.TodayMinus20Days();
    private String latestSubscriptionNumber;


    @Test(enabled = true, description = "TC32328_Self_Care_WS_Subscription_Summary_deactivated_account_Inactive_Subscription_Flag_as_TRUE", groups = "SelfCareWS.GetSubscriptionSummary")
    public void TC32328_Self_Care_WS_Subscription_Summary_deactivated_account_Inactive_Subscription_Flag_as_TRUE() {
        test.get().info("1. Create an online CC customer with FC 1 bundle of SB and simonly");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
        String customerNumber = owsActions.customerNo;

        test.get().info("2. Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("3. Update bill group payment collection date to 10 days later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("4. Set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("5. Update the start date of customer");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //=============================================================================
        test.get().info("6. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("7. Record latest subscription number for customer");
        latestSubscriptionNumber = CareTestBase.page().recordLatestSubscriptionNumberForCustomer();

        test.get().info("8. Deactivate account and return to customer");
        CareTestBase.deactivateAccountAndReturnToCustomer();

        test.get().info("9. Verify account status is inactive");
        CareTestBase.verifyAccountStatus("Inactive");

        test.get().info("10. Submit get subscription summary request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetSubscriptionSummaryRequestByCusNumber(customerNumber, true);

        test.get().info("12. Verify get summary response response");

    }
}
