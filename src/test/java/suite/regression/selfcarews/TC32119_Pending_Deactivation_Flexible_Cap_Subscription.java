package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.DeactivateSubscriptionPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 30/09/2019
 */
public class TC32119_Pending_Deactivation_Flexible_Cap_Subscription extends BaseTest {
    Date newStartDate = TimeStamp.TodayMinus20Days();
    List<String> subscriptionNumberList = new ArrayList<>();
    String accountName;
    String clubcardNumber;
    String FC2Subscription;
    String FCSubscription;
    String NCSubscription;

    @Test(enabled = true, description = "TC32114_Alternate_Path_1c_Active_Flexible_Cap_Subscription", groups = "SelfCareWS")
    public void TC32119_Pending_Deactivation_Flexible_Cap_Subscription(){
        test.get().info("1. Create a customer with FC and master subscription NC");
        String TC32114_CREATE_ORDER = "src\\test\\resources\\xml\\ows\\TC32119_createOrder.xml";
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
        getAllSubscriptionNumber();

        test.get().info("8. Record account name and club card number");
        recordAccountNameAndClubCardNumber();

        test.get().info("9. Deactivate subscription FC 3 days later and return to customer");
        String deactivationReason = "Customer Cancelled";
        deactivateSubscriptionFC3DaysLaterAndReturnToCustomer(FCSubscription + "  Mobile FC", deactivationReason);

        test.get().info("10. Verify provisioning wait SO of deactivated account created");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        HashMap<String, String> expectedServiceOrder = ServiceOrderEntity.dataServiceOrderForChangePassword("Deactivate Subscription", "Provision Wait");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(expectedServiceOrder), 1);

        test.get().info("11. Submit get account summary request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountSummaryWithSubsRequest(FCSubscription);

        test.get().info("12. Build Expected Account Summary Response Data");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getaccount\\TC32119_response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String expectedResponseFile = selfCareWSTestBase.buildResponseData(sampleResponseFile, newStartDate, TimeStamp.TodayPlus1Month(), customerNumber, subscriptionNumberList);

        test.get().info("13.  Verify Get Account Summary Response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponseFile, response);

    }

    private void deactivateSubscriptionFC3DaysLaterAndReturnToCustomer(String subscription, String reason){
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        DeactivateSubscriptionPage.DeactivateSubscription deactivationForm = DeactivateSubscriptionPage.DeactivateSubscription.getInstance();

        deactivationForm.selectDeactivateBySubscription(subscription);
        deactivationForm.enterDeactivateReason(reason);
        deactivationForm.setDeactivationDate(TimeStamp.TodayPlusDayAndMonth(3,0));
        deactivationForm.clickNextButton();

        String warningMessage = String.format("Subscription %s will be deactivated and cannot be undone. Are you sure? Select 'Next' to confirm.", subscription.split(" ")[0]);
        Assert.assertEquals(warningMessage, DeactivateSubscriptionPage.ConfirmDeactivatingSubscription.getInstance().getConfirmDeactivatingSubscription());
        DeactivateSubscriptionPage.ConfirmDeactivatingSubscription.getInstance().clickNextButton();

        ServiceOrdersPage.ServiceOrderComplete.getInstance().clickReturnToCustomer();
    }

    private void recordAccountNameAndClubCardNumber(){
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        accountName = CareTestBase.getCustomerName();
        clubcardNumber = CareTestBase.getClubCardNumber();
    }

    private void getAllSubscriptionNumber(){
        subscriptionNumberList = CareTestBase.getAllSubscription();
        for (String subscription : subscriptionNumberList) {
            if (subscription.endsWith("Mobile FC")) {
                FCSubscription = subscription.split(" ")[0];
            } else if (subscription.endsWith("Mobile NC")) {
                NCSubscription = subscription.split(" ")[0];
            }else {
                FC2Subscription = subscription.split(" ")[0];
            }
        }
    }

}
