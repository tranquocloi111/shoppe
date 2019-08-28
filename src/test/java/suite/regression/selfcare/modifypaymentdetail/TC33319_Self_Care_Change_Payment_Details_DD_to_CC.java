package suite.regression.selfcare.modifypaymentdetail;

import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.CardDetailsEntity;
import logic.business.entities.EventEntity;
import logic.business.entities.PaymentInfoEnity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.MakeAOneOffPaymentPage;
import logic.pages.selfcare.MyPaymentDetailsPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TC33319_Self_Care_Change_Payment_Details_DD_to_CC extends BaseTest {

    String customerNumber = null;

    @Test(enabled = true, description = "self care change payment details DD to CC", groups = "SelfCare")
    public void TC33319_Self_Care_Change_Payment_Details_DD_to_CC() {

        test.get().info("Step 1 : create an online DD customer");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_DD_customer_with_FC_2_bundles_and_NK2720";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);
        customerNumber = owsActions.customerNo;
        int hrmid = BillingActions.getHmbrid(customerNumber);
        String fullName = owsActions.fullName;

        test.get().info("Create new billing group");
        createNewBillingGroup();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Back Date the payment method start date to today minus 1 day");
        backDateThePaymentMethodStartDateToTodayMinus1Day();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Submit send DDI request job");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();

        test.get().info("Load user in the hub Net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);

        test.get().info("verify DDI status change to inactive");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus(), "Inactive");

        test.get().info("Open service order details for send DDI to BACS item");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Send DDI to BACS");

        test.get().info("Verify service order status is sent and DDI Reference has value");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getServiceOrderStatus(), "Sent");
        String dDIReference = TasksContentPage.TaskPage.DetailsPage.getInstance().getDDIReference();
        Assert.assertNotEquals(dDIReference, "");


        test.get().info("update customer DDI details in database");
        super.updateCustomerDDIDetailsInDatabase(dDIReference, BillingActions.getHmbrid(customerNumber), TimeStamp.Today().toString());

        test.get().info("reload customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Verify DDI information is correct");
        String newDate = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT);
        String exptedDDIReference = String.format("%s ( %s )", dDIReference, newDate);

        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getDDIReference(), exptedDDIReference);
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus(), "Active");

        test.get().info("Login in to selfcare page");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);


        test.get().info("access change payment detail page verify correct current payment detail");
        SelfCareTestBase.page().clickChangeMyPaymentDetails();
        SelfCareTestBase.page().verifyMyPaymentDetail();

        Assert.assertEquals("MT5J2JYZ1285FVMM", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getAccountHolderName());
        Assert.assertEquals("****4958", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getBankAcountNumber());
        Assert.assertEquals("***999", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getBankSortCode());
        Assert.assertEquals("6 LUKIN STREET", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getStreetaddress());
        Assert.assertEquals("LONDON", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getTown());
        Assert.assertEquals("E10AA", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getPostCode());

        test.get().info("input new payment detail");

        CardDetailsEntity cardDetails = new CardDetailsEntity();
        cardDetails.setCardType("Visa");
        cardDetails.setCardNumber("5105105105105100");
        cardDetails.setCardSecurityCode("123");
        cardDetails.setCardExpiryMonth("12");
        cardDetails.setCardExpiryYear(TimeStamp.TodayPlus1Year().toString().substring(2, 4));

        MyPaymentDetailsPage.NewPaymentDetailsSection.getInstance().inputCardNewPaymentDetail(cardDetails);
        MyPaymentDetailsPage.NewPaymentDetailsSection.getInstance().clickContinueBtn();

        test.get().info("verify my personal information page is displayed ");
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();
        String expectedMssg = "Your payment details have been successfully changed.";
        Assert.assertEquals(SelfCareTestBase.page().successfulMessageStack().get(0), expectedMssg);

        test.get().info("Load user in the hub care");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("verify payment infomation have been updated successfully");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        String expectedPaymentMethod = String.format("Credit Card ( %s )", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getPaymentMethod(), expectedPaymentMethod);
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getCardType(), cardDetails.getCardType());
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getCreditCardNumber(), "************5100");
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getCardExpireYear(), TimeStamp.TodayPlus1Year().toString().substring(0, 4));
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getCardExpireMonth(), cardDetails.cardExpiryMonth);

        test.get().info("verify 1 change bundle SO Record generated");
        HashMap<String, String> changePaymentMethodEnity = ServiceOrderEntity.dataServiceOrderChangePaymentMethod("Completed Task", "Change Payment Details");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(changePaymentMethodEnity), 1);

        test.get().info("Open SO detail and verify the detail");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Payment Details");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getNewPaymentMethod(), "CARD");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getCardType(), cardDetails.getCardType());
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getCardNumber(), "****************5100");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getCreditCardExpiryYear(), TimeStamp.TodayPlus1Year().toString().substring(0, 4));
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getCreditCardExpiryMonth(), cardDetails.getCardExpiryMonth());
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getCreditCardSecurityCode(), "");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getCreditCardHolderName(), fullName);

        Assert.assertTrue(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(2).contains("Red Authentication passed - APPROVE TransactionID"));
        HashMap<String, String> expecteventEnity = EventEntity.dataForEventChangeBundle("Payment Details update Successful", "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(expecteventEnity), 1);


    }

    public void backDateThePaymentMethodStartDateToTodayMinus1Day() {
        String disableTrigger = "ALTER TABLE hmbrproperty DISABLE ALL TRIGGERS";
        String enableTrigger = "ALTER TABLE hmbrproperty ENABLE ALL TRIGGERS";
        String sql = String.format("update hmbrproperty set datestart=sysdate-1 where hmbrid IN (SELECT hmbrid FROM hierarchymbr WHERE buid IN " +
                "(SELECT buid FROM businessunit WHERE buid =%s " +
                "OR rootbuid=%s)) AND datestart IS NOT NULL AND propertykey " +
                "IN ('PAYMT', 'CLUBNUM')", customerNumber, customerNumber);
        OracleDB.SetToNonOEDatabase().executeNonQueryWithoutTrigger(disableTrigger, enableTrigger, sql);
    }


}
