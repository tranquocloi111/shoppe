package suite.regression.selfcare.modifyaccount;


import logic.business.db.billing.BillingActions;
import logic.business.entities.*;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.MakeAOneOffPaymentPage;
import logic.pages.selfcare.Test3DSecurePage;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TC33322_B04_Make_a_one_off_payment_via_3D_secure extends BaseTest {
/*
Tran Quoc Loi
paymentpage
FinancialTransaction
Taskcontent
Paymentgateenity
Paymentgatewayrespond
commonaction
service
 */

    @Test(enabled = true, description = "TC33322 Make a one off payment via 3D secure", groups = "SelfCare")
    public void TC33322_B04_Make_a_one_off_payment_via_3D_secure() {

        test.get().info("Step 1 : create an online cc customer with FC 1 bundle of SB and sim only");
        String path = "src\\test\\resources\\xml\\SelfCare\\viewaccount\\onlines_CC_customer_with_FC_1_bundle_of_SB_and_sim_only";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);

        test.get().info("Step 2 :Login in to self care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, owsActions.customerNo);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 3 : access the make a one off payment page");
        SelfCareTestBase.page().clickMakeAOneOfPayment();
        SelfCareTestBase.page().verifyMakeAOneOffPayment();

        test.get().info("Step 4 : verify amount outstanding on my account field");
        String currentAmount = MakeAOneOffPaymentPage.OutStandingSection.getInstance().getCurrentAmountValue();
        String oldestAmount = MakeAOneOffPaymentPage.OutStandingSection.getInstance().getOldestOutstandingValue();
        String option = MakeAOneOffPaymentPage.OutStandingSection.getInstance().getOptionValue().trim().replace("\n", "");
        String expectedOption = "Your options are:" +
                "Make a payment below for the total amount outstanding to bring the account up to date." +
                "If you are currently behind with your payments, pay off your oldest outstanding bill to avoid your mobile service being disconnected." +
                "If you can't pay the whole bill today, please pay what you can today to help with the overdue amount.";

        Assert.assertEquals(currentAmount, "£0.00");
        Assert.assertEquals(oldestAmount, "£0.00");
        Assert.assertEquals(option, expectedOption.trim());


        test.get().info("Step 5 : input the payment detail data then click submit button");
        String paymentAmount = "5.23";
        CardDetailsEntity cardDetails = new CardDetailsEntity();
        cardDetails.setCardType("MasterCard");
        cardDetails.setCardNumber("5105105105105100");
        cardDetails.setCardHolderName("Mr Card Holder");
        cardDetails.setCardSecurityCode("123");
        cardDetails.setCardExpiryMonth("01");
        cardDetails.setCardExpiryYear(TimeStamp.TodayPlus1Year().toString().substring(2, 4));

        MakeAOneOffPaymentPage.CardDetailSection.getInstance().enterPaymentAmount(paymentAmount);
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().inputCardDetail(cardDetails);
        String tooltip = "If you're on Anytime Upgrade and you just want to make a payment towards your device, call us on 4455 from your Tesco Mobile phone.";
        String actualToolTip = MakeAOneOffPaymentPage.CardDetailSection.getInstance().getMessageAfterSubmitPayment().replace("\n", "");
        Assert.assertEquals(actualToolTip.trim(), tooltip.trim());

        MakeAOneOffPaymentPage.CardDetailSection.getInstance().clickSubmitBtn();

        test.get().info("Step 6 : input 3D Password");
        Test3DSecurePage.getInstance().switchFrameByName("issuer");
        Test3DSecurePage.getInstance().enter3DPassword("1234");
        Test3DSecurePage.getInstance().clickVerifyMe();

        test.get().info("Step 7 : Verify my personal information page displayed with successfully message");
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();
        List<String> successfullMssg = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(successfullMssg.size(), 1);
        Assert.assertEquals(successfullMssg.get(0), "Your payment was successful.");

        test.get().info("Step 8 : load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 9 :get lastest subscription");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberByIndex(1).split(" ")[0];


        test.get().info("Step 10 :Open the financial transaction content for customer");
        MenuPage.LeftMenuPage.getInstance().clickFinancialTransactionLink();
        HashMap<String, String> financialTransaction = FinancialTransactionEnity.dataFinancialTransactionForMakeAOneOffPayment("Ad Hoc Payment", "£5.23");
        Assert.assertEquals(FinancialTransactionPage.FinancialTransactionGrid.getInstance().getNumberOfFinancialTransaction(financialTransaction), 1);

        test.get().info("Step 11: verify the adhoc payment transaction detail");
        FinancialTransactionPage.FinancialTransactionGrid.getInstance().clickFinancialTransactionByDetail("Ad Hoc Payment");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getReceiptType(), "Ad Hoc Payment");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getReceiptStatus(), "Fully Allocated");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getPaymentAmount(), "£5.23");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getPaymentCurrency(), "Great Britain Pound");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getCardType(), "MasterCard");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getCardNumber(), "************5100");

        test.get().info("Step 12 :Open the service order content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        HashMap<String, String> serviceOrder = ServiceOrderEntity.dataServiceOrderFinancialTransaction();
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(serviceOrder), 1);

        List<WebElement> serviceOrderList = ServiceOrdersContentPage.getInstance().getServiceOrders(serviceOrder);
        String numberServiceOrder = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrderList);


        test.get().info("Step 13 :verify the service order detail content for customer");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Ad-hoc Payment");
        Assert.assertEquals(TimeStamp.TodayPlus1Year().toString().substring(0, 4), TasksContentPage.TaskPage.DetailsPage.getInstance().getCreditCardExpiryYear());

        Assert.assertEquals("Approved", TasksContentPage.TaskPage.DetailsPage.getInstance().getReDSStatusAuthorisation());
        Assert.assertEquals("****************5100", TasksContentPage.TaskPage.DetailsPage.getInstance().getCardNumber());
        Assert.assertEquals("5.23", TasksContentPage.TaskPage.DetailsPage.getInstance().getAmountToBeDebited());
        Assert.assertEquals("0", TasksContentPage.TaskPage.DetailsPage.getInstance().getReDSResponseCode());
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird(), 5);

        test.get().info("Step 14 :Verify payment gateway request for order in OE db");
        List<PaymentGatewayEnity> result = BillingActions.getPaymentGatewayRequestForOrderInOEDb(numberServiceOrder);
        List<String> requestID = verifyPaymentGateWay(result);

        test.get().info("Step 15 :Verify payment gateway respond for order in OE db");
        List<PaymentGatewayRespondEnity> respondResult = BillingActions.getPaymentGatewayRespondForOrderInOEDb(requestID);
        verifyPaymentGateWayRespond(respondResult);
    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }


    public List<String> verifyPaymentGateWay(List<PaymentGatewayEnity> result) {
        List<String> requestId = new ArrayList<>();
        Assert.assertEquals(result.size(), 4);
        Assert.assertEquals(BillingActions.findPayemtGateWay(result, "PE", "ADHOC", "Onlines"), 1);
        Assert.assertEquals(BillingActions.findPayemtGateWay(result, "PA", "ADHOC", "Onlines"), 1);
        Assert.assertEquals(BillingActions.findPayemtGateWay(result, "OC", "ADHOC", "Onlines"), 1);
        Assert.assertEquals(BillingActions.findPayemtGateWay(result, "OA", "ADHOC", "Onlines"), 1);
        for (int i = 0; i < result.size(); i++) {
            requestId.add(result.get(i).getPaymentGTWRequestID());
        }
        return requestId;
    }

    public void verifyPaymentGateWayRespond(List<PaymentGatewayRespondEnity> result) {
        List<String> requestId = new ArrayList<>();
        Assert.assertEquals(result.size(), 4);
        Assert.assertEquals(BillingActions.findPaymentGateWayRespond(result, "PE", "APPROVE", "PAREQ", "00", "ENROLLED"), 1);
        Assert.assertEquals(BillingActions.findPaymentGateWayRespond(result, "PA", "APPROVE", "SUCCESS", "00", "CARD_SIGN_OK"), 1);
        Assert.assertEquals(BillingActions.findPayemtGateWayRespondByFraudStatus(result, "OC", "APPROVE", "APPROVE", "00", "ACCEPT"), 1);
        Assert.assertEquals(BillingActions.findPaymentGateWayRespondByTokenStatus(result, "OA", "APPROVE", "APPROVE", "00", "ACTIVE"), 1);
    }


}
