package suite.regression.selfcare.modifypaymentdetail;

import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.entities.CardDetailsEntity;
import logic.business.entities.PaymentInfoEnity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.MyPaymentDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

public class TC33318_Self_Care_Change_Payment_method_CC_to_DD extends BaseTest {

    String customerNumber = null;

    @Test(enabled = true, description = "TC33318 self care change payment details CC to DD", groups = "SelfCare")
    public void TC33318_Self_Care_Change_Payment_method_CC_to_DD() {

        test.get().info("Step 1 : create an online cc customer");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_2_bundles_and_NK2720";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);
        customerNumber = owsActions.customerNo;
        int hrmid = BillingActions.getHmbrid(customerNumber);
        String fullName = owsActions.fullName;

        backDateThePaymentMethodStartDateToTodayMinus1Day();

        test.get().info("Load user in the hub Net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);

        test.get().info("Verify payment method start date has been back dated");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        String expectedPaymentMethod = String.format("Credit Card ( %s )", Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(expectedPaymentMethod, DetailsContentPage.PaymentInformationPage.getInstance().getPaymentMethod());

        test.get().info("Login to selfcare page");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();


        test.get().info("access change payment detail page verify correct current payment detail");
        SelfCareTestBase.page().clickChangeMyPaymentDetails();
        SelfCareTestBase.page().verifyMyPaymentDetail();


        Assert.assertEquals(fullName, MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getCardHolderName());
        Assert.assertEquals("************5100", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getCardNumber());
        Assert.assertEquals("12/2030", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getExpiryDate());
        Assert.assertEquals("6 LUKIN STREET", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getStreetaddress());
        Assert.assertEquals("LONDON", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getTown());
        Assert.assertEquals("E10AA", MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().getPostCode());

        test.get().info("Input the card detail and click continue button");
        PaymentInfoEnity paymentInfoEnity = new PaymentInfoEnity();
        paymentInfoEnity.setPaymentMethod("Direct Debit");
        paymentInfoEnity.setdbankAccountHolderName("Self Care Test");
        paymentInfoEnity.setbankSortCode("10 79 99");
        paymentInfoEnity.setbankAccountNumber("88837491");

        MyPaymentDetailsPage.NewPaymentDetailsSection.getInstance().inputNewPaymentDetail(paymentInfoEnity);
        MyPaymentDetailsPage.CurrentPaymentDetailsSection.getInstance().clickContinueBtn();

        test.get().info("verify my personal information page is displayed ");
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();
        String expectedMssg = "Your payment details have been successfully changed.";
        Assert.assertEquals(SelfCareTestBase.page().successfulMessageStack().get(0), expectedMssg);

        test.get().info("Load user in the hub care");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("verify payment infomation have been updated successfully");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        expectedPaymentMethod = String.format("Direct Debit ( %s )", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getPaymentMethod(),expectedPaymentMethod);
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getBankSortCode(),"***999");
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getBankAccountHolderName(),"Self Care Test");
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getBankAccountNumber(),"88837491");
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getDDIReference(),"");
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus(),"");

        test.get().info("run submit send DDI request Job");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();

        test.get().info("verify DDI status change to inactive");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus(),"Inactive");

        test.get().info("Open service order details for send DDI to BACS item");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Send DDI to BACS");

        test.get().info("Verify service order status is sent and DDI Reference has value");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getServiceOrderStatus(), "Sent");
        String dDIReference = TasksContentPage.TaskPage.DetailsPage.getInstance().getDDIReference();
        Assert.assertNotEquals(dDIReference, "");

        test.get().info("update customer DDI details in database");
        super.updateCustomerDDIDetailsInDatabase(dDIReference,BillingActions.getHmbrid(customerNumber),TimeStamp.Today().toString());

        test.get().info("reload customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Verify DDI information is correct");
        String newDate =Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT).toString();
        String exptedDDIReference=String.format("%s ( %s )",dDIReference,newDate);

        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getDDIReference(),exptedDDIReference);
        Assert.assertEquals(DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus(),"Active");

    }

    public void backDateThePaymentMethodStartDateToTodayMinus1Day() {
        String disableTrigger = "ALTER TABLE hmbrproperty DISABLE ALL TRIGGERS";
        String enableTrigger = "ALTER TABLE hmbrproperty ENABLE ALL TRIGGERS";
        String sql = String.format("update hmbrproperty set datestart=sysdate-1 where hmbrid IN (SELECT hmbrid FROM hierarchymbr WHERE buid IN (SELECT buid FROM businessunit WHERE buid =%s OR rootbuid=%s)) AND datestart IS NOT NULL AND propertykey IN ('PAYMT', 'CLUBNUM')", customerNumber, customerNumber);
        OracleDB.SetToNonOEDatabase().executeNonQueryWithoutTrigger(disableTrigger, enableTrigger, sql);
    }


}
