package suite.regression.selfcare.payment;

import logic.business.db.billing.BillingActions;
import logic.business.entities.CardDetailsEntity;
import logic.business.entities.FinancialTransactionEnity;
import logic.business.entities.PaymentInfoEnity;
import logic.business.entities.selfcare.MyBillAndPaymentEnity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.FinancialTransactionPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.MakeAOneOffPaymentPage;
import logic.pages.selfcare.MyBillsAndPaymentsPage;
import logic.pages.selfcare.MyPaymentDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.HashMap;

public class TC55_Self_Care_Make_a_one_off_payment extends BaseTest {
    String customerNumber = null;

    @Test(enabled = true, description = "TC55 self care make a one off payment", groups = "SelfCare")
    public void TC55_Self_Care_Make_a_one_off_payment() {

        test.get().info("Step 1: create an online cc customer");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_2_bundles_and_NK2720";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;
        String fullName = owsActions.fullName;


        test.get().info("Step 2: Login to selfcare page");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();


        test.get().info("Step 3: access change payment detail page verify correct current payment detail");
        SelfCareTestBase.page().clickMakeAOneOfPayment();
        SelfCareTestBase.page().verifyMakeAOneOffPayment();

        test.get().info("Step 4 : Verify make a one off payment page is displayed with correct current payment details");
        Assert.assertEquals(MakeAOneOffPaymentPage.PesonalDetailsSection.getInstance().getCardHolderName(), fullName);
        Assert.assertEquals(MakeAOneOffPaymentPage.PesonalDetailsSection.getInstance().getStreetaddress(), "6 LUKIN STREET");
        Assert.assertEquals(MakeAOneOffPaymentPage.PesonalDetailsSection.getInstance().getTown(), "LONDON");
        Assert.assertEquals(MakeAOneOffPaymentPage.PesonalDetailsSection.getInstance().getPostCode(), "E10AA");


        Assert.assertEquals(fullName, MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getCardHolderName());
        Assert.assertEquals("************5100", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getCardNumber());
        Assert.assertEquals("12/2030", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getExpiryDate());
        Assert.assertEquals("6 LUKIN STREET", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getStreetaddress());
        Assert.assertEquals("LONDON", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getTown());
        Assert.assertEquals("E10AA", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getPostCode());

        test.get().info("Step 5: Input the card detail ");
        CardDetailsEntity cardDetails = new CardDetailsEntity();
        cardDetails.setCardType("MasterCard");
        cardDetails.setCardNumber("5105105105105100");
        cardDetails.setCardHolderName("Mr Card Holder");
        cardDetails.setCardSecurityCode("333");
        cardDetails.setCardExpiryMonth("12");
        cardDetails.setCardExpiryYear(TimeStamp.TodayPlus5Year().toString().substring(2, 4));
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().inputCardDetail(cardDetails);

        test.get().info("Step 6: the payment amount and click submit");
        String paymentAmount = "12.22";
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().enterPaymentAmount(paymentAmount);
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().clickSubmitBtn();


        test.get().info("Step 7: verify my personal information page is displayed ");
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();
        String expectedMssg = "Your payment was successful.";
        Assert.assertEquals(SelfCareTestBase.page().successfulMessageStack().get(0), expectedMssg);

        test.get().info("Step 8:click view details of my bills and payments link");
        MyPersonalInformationPage.MyBillsAndPaymentsSection.getInstance().clickViewDetailsOfMyBillsAndPayments();

        test.get().info("Step 9: verify my bill and payments page displays correct records");
        HashMap<String, String> enity = MyBillAndPaymentEnity.dataForMyBillsAndPayment("Ad Hoc Payment", "£-12.22", "-£27.22");
        Assert.assertEquals(MyBillsAndPaymentsPage.getInstance().getNumberPaymentByEnity(enity), 1);

        enity = MyBillAndPaymentEnity.dataForMyBillsAndPayment("Online Payment", "£-15.00", "-£15.00");
        Assert.assertEquals(MyBillsAndPaymentsPage.getInstance().getNumberPaymentByEnity(enity), 1);


        test.get().info("Step 10: load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 11: open the financial transactions content for customer");
        MenuPage.LeftMenuPage.getInstance().clickFinancialTransactionLink();

        test.get().info("Step 12: verify my bill and payments page displays correct records");
        enity = FinancialTransactionEnity.dataFinancialTransactionForMakeAOneOffPayment("Ad Hoc Payment", "£12.22", "-£27.22");
        Assert.assertEquals(FinancialTransactionPage.FinancialTransactionGrid.getInstance().getNumberOfFinancialTransaction(enity), 1);

        enity = MyBillAndPaymentEnity.dataForMyBillsAndPayment("Online Payment", "£15.00", "-£15.00");
        Assert.assertEquals(FinancialTransactionPage.FinancialTransactionGrid.getInstance().getNumberOfFinancialTransaction(enity), 1);

    }
}
