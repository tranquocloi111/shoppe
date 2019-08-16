package suite.regression.selfcarews;

import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 13/08/2019
 */
public class TC32358_Self_Care_WS_Get_Subscription_Summary extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    private List<String> subscriptionNumberList = new ArrayList<>();

    @Test(enabled = true, description = "TC32358 Self Care WS Get Subscription Summary", groups = "SelfCareWS")
    public void TC32358_Self_Care_WS_Get_Subscription_Summary(){
        test.get().info("Step 1 : Create a CC Customer with 2 subscriptions order");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWith2SubscriptionOrder();
        customerNumber = owsActions.customerNo;

        test.get().info("Create new billing group start from today minus 15 days");
        createNewBillingGroupToMinus15days();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //=============================================================================

        test.get().info("Login to HUBNet then search Customer by customer number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Open Summary Content For Customer");
        //////NOT IMPLEMENTED YET

        test.get().info("Verify Customer Start Date and Billing Group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);
        //==============================================================================

        test.get().info("Submit Get Subscription Summary Request to SelfCare WS");
        SWSActions swsActions = new SWSActions();
        //////NOT IMPLEMENTED YET

        test.get().info("Get All Subscriptions Number");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        subscriptionNumberList = selfCareWSTestBase.getAllSubscription(2);

        test.get().info("Verify Subscription Summary Response of Mobile FC");
        //////NOT IMPLEMENTED YET

        test.get().info("Verify Subscription Summary Response of Mobile NC");
        //////NOT IMPLEMENTED YET

    }
}