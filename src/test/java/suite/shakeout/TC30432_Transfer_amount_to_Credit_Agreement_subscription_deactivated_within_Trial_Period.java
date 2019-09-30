package suite.shakeout;

import logic.business.db.billing.CommonActions;
import logic.business.entities.AdjustmentsChargesAndCreditsEntity;
import logic.business.entities.CreditAgreementPaymentsEntiy;
import logic.business.entities.OtherChargeCreditsEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

public class TC30432_Transfer_amount_to_Credit_Agreement_subscription_deactivated_within_Trial_Period  extends BaseTest {

    @Test(enabled = true, description = "TC30432_Transfer_amount_to_Credit_Agreement_subscription_deactivated_within_Trial_Period", groups = "Smoke")
    public void TC30432_Transfer_amount_to_Credit_Agreement_subscription_deactivated_within_Trial_Period(){
        test.get().info("Step 1 : Create a customer with FC and device");
        OWSActions owsActions = new OWSActions();
        owsActions.createCustomerWithFCAndDevice();

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 6 : Get subscription number and agreement number");
        String subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        String subNo2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");
        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();
        CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().clickExpandButtonOfCABySubscription(subNo1);
        String agreementNo1 =  CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(subNo1).agreementNumber();
        CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().clickExpandButtonOfCABySubscription(subNo2);
        String agreementNo2 =  CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(subNo2).agreementNumber();

        test.get().info("Step 7 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateSubscription();

        test.get().info("Step 8 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo2));

        test.get().info("Step 9 : Take a payment to master subscription");
        MenuPage.RightMenuPage.getInstance().clickApplyFinancialTransactionLink();
        ServiceOrdersPage.AccountSummaryAndSelectAction.getInstance().selectChooseAction();
        ServiceOrdersPage.InputPaymentDetails.getInstance().inputPaymentDetail("120", "Regression Automation");
        ServiceOrdersPage.TransferExistingFunds.getInstance().inputAmountAgainstSubscription("120");
        HashMap<String,String> transfer = ServiceOrdersPage.ConfirmFundsTransfer.getInstance().getConfirmFundsTransfer();
        Assert.assertEquals("£120.00", transfer.get("PaymentAmount"));
        Assert.assertEquals("Regression Automation", transfer.get("Notes"));
        Assert.assertEquals("£0.00", transfer.get("OverdueInvoices"));
        Assert.assertEquals("£0.00", transfer.get("TopUps"));
        Assert.assertEquals("£120.00", transfer.get("CreditAgreement"));

        Assert.assertEquals("Maestro", transfer.get("CardType"));
        Assert.assertEquals("************5100", transfer.get("CardNumber"));
        Assert.assertEquals(owsActions.fullName, transfer.get("CardHolderName"));
        Assert.assertEquals("1", transfer.get("CardExpiryMonth"));
        Assert.assertEquals("2030", transfer.get("CardExpiryYear"));

        HashMap<String,String> wizard = ServiceOrdersPage.FundsTransferResults.getInstance().getFundsTransferResults();
        Assert.assertEquals("£120.00", wizard.get("PaymentAmount"));
        Assert.assertEquals("Regression Automation", wizard.get("Notes"));
        Assert.assertEquals("£0.00", wizard.get("OverdueInvoices"));
        Assert.assertEquals("1", wizard.get("AmountAllocatedToTopupsGrid"));
        Assert.assertEquals("1", wizard.get("AmountAllocatedToCreditAgreementsGrid"));
        Assert.assertEquals("£0.00", wizard.get("AmountAllocatedAsUnallocatePayment"));

        Assert.assertEquals("Maestro", wizard.get("CardType"));
        Assert.assertEquals("************5100", wizard.get("CardNumber"));
        Assert.assertEquals(owsActions.fullName, wizard.get("CardHolderName"));
        Assert.assertEquals("1", wizard.get("CardExpiryMonth"));
        Assert.assertEquals("2030", wizard.get("CardExpiryYear"));
        Assert.assertEquals("APPROVE", wizard.get("ReDSStatusAuthorisation"));

        test.get().info("Step 10 : Verify A Service Order Of Apply FT Created");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderApplyFinancialTransaction());
        Assert.assertEquals(1, serviceOrder.size());

        String serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);
        Assert.assertEquals("120", TasksContentPage.TaskPage.DetailsPage.getInstance().getPaymentAmount());
        Assert.assertEquals("Approved", TasksContentPage.TaskPage.DetailsPage.getInstance().getReDSStatusAuthorisation());
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Step 11 : Verify An Other Credit Created");
        MenuPage.LeftMenuPage.getInstance().clickOtherChargesCreditsItem();
        int rowOfChargeCredit =  OtherChargesCreditsContent.getInstance().getNumberOfChargeCredits(OtherChargeCreditsEntity.getOCCForAgreementAdjustmentProducts(subNo1, serviceOrderId));
        Assert.assertEquals(1, rowOfChargeCredit);

        test.get().info("Step 12 : Verify The Credit Exists In Live Bill Estimate");
        MenuPage.LeftMenuPage.getInstance().clickLiveBillEstimateItem();
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription billEstimatePerSubscription = new LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(subNo1 + "  Mobile Ref 1");
        billEstimatePerSubscription.expand();

        //subscription 1 should be active and it has payment and device in Adjustments, Charges and Credits
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription.AdjustmentsChargesAndCredits adjustmentsChargesAndCredits =  billEstimatePerSubscription.new AdjustmentsChargesAndCredits();
        adjustmentsChargesAndCredits.expand();
        int sizeOfAdjustment =  adjustmentsChargesAndCredits.getNumberOfServiceOrders(AdjustmentsChargesAndCreditsEntity.getTransferPaymentAdjustmentProducts(TimeStamp.TodayMinus20Days()));
        Assert.assertEquals(2, sizeOfAdjustment);

        //subscription 2 is inactive and it has only device in Adjustments, Charges and Credits
        billEstimatePerSubscription = new LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(subNo2 + "  Mobile Ref 2");
        billEstimatePerSubscription.expand();

        adjustmentsChargesAndCredits =  billEstimatePerSubscription.new AdjustmentsChargesAndCredits();
        adjustmentsChargesAndCredits.expand();
        sizeOfAdjustment =  adjustmentsChargesAndCredits.getNumberOfServiceOrders(AdjustmentsChargesAndCreditsEntity.getTransferPaymentAdjustmentProducts(TimeStamp.TodayMinus20Days()));
        Assert.assertEquals(1, sizeOfAdjustment);

        test.get().info("Step 13 : Veirfy the credit in CA");
        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();
        CreditAgreementsContentPage.CreditAgreementsGridPage.CADetailClass.getInstance().clickExpandButtonOfCABySubscription(subNo1);
        Assert.assertEquals("£120.00", CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(subNo1).otherPayments());
        Assert.assertEquals(1, CreditAgreementsContentPage.CreditAgreementPaymentsGrid.getInstance().getNumberOfCreditAgreementPayments(CreditAgreementPaymentsEntiy.getCreditAgreementPaymentWithAdHoc(subNo1)));
    }
}
