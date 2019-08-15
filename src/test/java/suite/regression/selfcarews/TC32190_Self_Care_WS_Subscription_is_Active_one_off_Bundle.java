package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 8/08/2019
 */
public class TC32190_Self_Care_WS_Subscription_is_Active_one_off_Bundle extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus20Days();
    private String subscriptionNumber;

    @Test(enabled = true, description = "TC32190 Self Care WS Subscription is Active one off Bundle", groups = "SelfCareWS")
    public void TC32190_Self_Care_WS_Subscription_is_Active_one_off_Bundle(){
        test.get().info("Step 1 : Create a Customer with one off bundle subscription active");
        OWSActions owsActions = new OWSActions();
        owsActions.createACustomerWithOneOffBundle();
        customerNumber = owsActions.customerNo;

        owsActions.getOrder(owsActions.orderIdNo);
        subscriptionNumber = owsActions.getOrderMpnByReference(1);

        test.get().info("Create new billing group");
        createNewBillingGroup();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //=============================================================================
        test.get().info("Login to HUBNet then search Customer by customer number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Record discount bundle monthly refill SO id");
        String orderId = CareTestBase.recordDiscountBundleMonthlyRefillSOId(subscriptionNumber);

        test.get().info("Submit get bundle request to selfcare WS");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetBundleRequest(customerNumber, subscriptionNumber);

        test.get().info("Verify get bundle response are correct");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getbundle\\TC32190_response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyGetBundleResponseAreCorrect(sampleResponseFile, response, customerNumber, subscriptionNumber, orderId, newStartDate);
    }
}
