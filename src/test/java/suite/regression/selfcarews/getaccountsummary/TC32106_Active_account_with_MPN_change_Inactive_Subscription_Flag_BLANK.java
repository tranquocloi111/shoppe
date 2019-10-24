package suite.regression.selfcarews.getaccountsummary;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.SubscriptionEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ChangeSubscriptionNumberPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
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
    String newSubscriptionNumber;

    @Test(enabled = true, description = "TC32106_Active account with MPN change Inactive Subscription Flag BLANK", groups = "SelfCareWS.GetAccountSummary")
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
        startChangeSubscriptionNumberWizard();

        test.get().info("Verify subscriptions in summary");
        verifySubscriptionsInSummary();

        test.get().info("Submit Get Account Summary Request to with flag 'InactiveSubscription' is BLANK");
        SWSActions swsActions = new SWSActions();
        String requestFilePath = "src\\test\\resources\\xml\\sws\\getaccount\\Get_Account_Summary_Admin_Request.xml";
        Xml response = swsActions.submitAccountSummaryWithFlagRequest(requestFilePath, customerNumber, "");

        test.get().info("Build Expected Account Summary Response Data");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getaccount\\TC32106_response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String expectedResponseFile = selfCareWSTestBase.buildResponseData(sampleResponseFile, newStartDate, TimeStamp.TodayPlus1Month(), customerNumber, newSubscriptionNumber);

        test.get().info("Verify Get Account Summary Response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponseFile, response);
    }

    private void verifySubscriptionsInSummary(){
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGridSectionPage.getInstance().getNumberOfSubscription(
                SubscriptionEntity.dataForActiveSubscriptions((newSubscriptionNumber + " Mobile Ref 1"))));
    }

    private void startChangeSubscriptionNumberWizard(){
        MenuPage.RightMenuPage.getInstance().clickChangeSubscriptionNumberLink();
        ChangeSubscriptionNumberPage.ChangeSubscriptionNumber content = ChangeSubscriptionNumberPage.ChangeSubscriptionNumber.getInstance();
        subscriptionNumber = content.getCurrentSubscriptionNumber().split(" ")[0];
        newSubscriptionNumber = CareTestBase.page().updateTheSubscriptionNumberAndClickNextButton();

        ChangeSubscriptionNumberPage.ConfirmChangingSubscriptionNumber.getInstance().clickNextButton();
        ServiceOrdersPage.ServiceOrderComplete.getInstance().clickReturnToCustomer();
    }
}
