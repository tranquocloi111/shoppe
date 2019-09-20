package suite.regression.selfcarews;

import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.options.ChangeSubscriptionNumberPage;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 18/09/2019
 */
public class TC32106_Active_account_with_MPN_change_Inactive_Subscription_Flag_BLANK extends BaseTest {
    Date newStartDate = TimeStamp.TodayMinus20Days();
    String subscriptionNumber;

    @Test(enabled = true, description = "TC32106_Active account with MPN change Inactive Subscription Flag BLANK", groups = "SelfCareWS")
    public void TC32106_Active_account_with_MPN_change_Inactive_Subscription_Flag_BLANK(){
        test.get().info("Create an onlines CC Customer with FC 1 bundle of SB and sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
        String customerNumber = owsActions.customerNo;

        test.get().info("Create new billing group");
        createNewBillingGroup();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Load customer in HUB Net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Start change subscription number wizard");

    }

    private void startChangeSubscriptionNumberWizard(){
        MenuPage.RightMenuPage.getInstance().clickChangeSubscriptionNumberLink();
        ChangeSubscriptionNumberPage.ChangeSubscriptionNumber content = ChangeSubscriptionNumberPage.ChangeSubscriptionNumber.getInstance();
        subscriptionNumber = content.getCurrentSubscriptionNumber().split(" ")[0];

    }
}
