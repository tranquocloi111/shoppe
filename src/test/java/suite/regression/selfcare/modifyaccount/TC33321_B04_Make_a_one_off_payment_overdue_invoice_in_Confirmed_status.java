package suite.regression.selfcare.modifyaccount;


import framework.config.Config;
import framework.utils.Log;
import framework.utils.RandomCharacter;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.CardDetailsEntity;
import logic.business.entities.FinancialTransactionEnity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.FTPHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.MakeAOneOffPaymentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.pages.selfcare.Test3DSecurePage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

public class TC33321_B04_Make_a_one_off_payment_overdue_invoice_in_Confirmed_status extends BaseTest {
    /*
    Tran Quoc Loi
    makeapaymentpage
    Taskcontent

    commonaction
    service
    ftphelper

     */
    String amountOverDue = null;
    String invoiceNumber = null;
    String subScriptNo1 = null;

    @Test(enabled = true, description = "TC33321 Make a one off payment overdue invoice in confirm status", groups = "SelfCare")
    public void TC33321_B04_Make_a_one_off_payment_overdue_invoice_in_Confirmed_status() {

        test.get().info("Step 1 : create an online cc customer with FC 1 bundle of SB and sim only");
        String path = "src\\test\\resources\\xml\\SelfCare\\viewaccount\\onlines_CC_customer_with_2_FC_1_bundle_and_NK2720";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);
        owsActions.getSubscription(owsActions.orderIdNo, "FC Mobile 1");
        subScriptNo1 = owsActions.serviceRef;
        String customerNumber = owsActions.customerNo;
        String fullName = owsActions.fullName;

        createCustomerWithAOverdueInvoice(customerNumber);


        test.get().info("Step 2 :Login in to self care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 3 :Verify alert has overdue balance displayed in my personal information");
        String expectedAlert = String.format("You currently have an overdue balance of  £%s.  Click here to make payment.", amountOverDue);
        String alert = MyPersonalInformationPage.myAlertSection.getInstance().getAlertMessagebForOverDuePayment();
        Assert.assertEquals(expectedAlert.trim(), alert.trim());

        test.get().info("Step 4 : Access make a one off payment page");
        SelfCareTestBase.page().clickMakeAOneOfPayment();
        SelfCareTestBase.page().verifyMakeAOneOffPayment();

        test.get().info("Step 5 : Verify make a one off payment page is displayed with correct current payment details");
        Assert.assertEquals(MakeAOneOffPaymentPage.PesonalDetailsSection.getInstance().getCardHolderName(), fullName);
        Assert.assertEquals(MakeAOneOffPaymentPage.PesonalDetailsSection.getInstance().getStreetaddress(), "6 LUKIN STREET");
        Assert.assertEquals(MakeAOneOffPaymentPage.PesonalDetailsSection.getInstance().getTown(), "LONDON");
        Assert.assertEquals(MakeAOneOffPaymentPage.PesonalDetailsSection.getInstance().getPostCode(), "E10AA");

        test.get().info("Step 6 : verify amount outstanding on my account field");
        String currentAmount = MakeAOneOffPaymentPage.OutStandingSection.getInstance().getCurrentAmountValue();
        String oldestAmount = MakeAOneOffPaymentPage.OutStandingSection.getInstance().getOldestOutstandingValue();
        String option = MakeAOneOffPaymentPage.OutStandingSection.getInstance().getOptionValue().trim().replace("\n", "");
        String expectedOption = "Your options are:" +
                "Make a payment below for the total amount outstanding to bring the account up to date." +
                "If you are currently behind with your payments, pay off your oldest outstanding bill to avoid your mobile service being disconnected." +
                "If you can't pay the whole bill today, please pay what you can today to help with the overdue amount.";

        Assert.assertEquals(currentAmount, "£20.00");
        Assert.assertEquals(oldestAmount, "£20.00");
        Assert.assertEquals(option, expectedOption.trim());

        String tooltip = "If you're on Anytime Upgrade and you just want to make a payment towards your device, call us on 4455 from your Tesco Mobile phone.";
        String actualToolTip = MakeAOneOffPaymentPage.CardDetailSection.getInstance().getMessageDesirePayment().replace("\n", "");
        Assert.assertEquals(actualToolTip.trim(), tooltip.trim());


        test.get().info("Step 7 : input the payment  ");
        String paymentAmount = "5";
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().enterPaymentAmount(paymentAmount);

        test.get().info("Step 8 : verify pay less error message ");
        String expectedNote = "Note: The amount you’ve selected to pay is less than the current overdue amount of £20.00 " +
                "for this account.If you don’t want your account to be disconnected, you’ll need to pay at least the amount " +
                "due on your oldest invoice.Select Submit to continue with the current amount or change the amount and then " +
                "submit to proceed with your payment request.";
        Assert.assertEquals(expectedNote, MakeAOneOffPaymentPage.CardDetailSection.getInstance().getAlertMessagePaymentLessThan().replace("\n", ""));

        test.get().info("Step 9 : input the payment detail ");
        paymentAmount = "100";
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().enterPaymentAmount(paymentAmount);

        test.get().info("Step 10 : verify pay less error message ");
        expectedNote = "Note: The amount you’ve selected to pay is more than the current" +
                " overdue amount of £20.00 for this account.Select Submit to continue with the " +
                "current amount or change the amount and then submit to proceed with your payment request.";
        Assert.assertEquals(expectedNote, MakeAOneOffPaymentPage.CardDetailSection.getInstance().getAlertGreaterThanMssg().replace("\n", ""));

        test.get().info("Step 11: input the payment detail data then click submit button");
        paymentAmount = "20.00";
        CardDetailsEntity cardDetails = new CardDetailsEntity();
        cardDetails.setCardType("MasterCard");
        cardDetails.setCardNumber("5105105105105100");
        cardDetails.setCardHolderName("Mr Card Holder");
        cardDetails.setCardSecurityCode("123");
        cardDetails.setCardExpiryMonth("01");
        cardDetails.setCardExpiryYear(TimeStamp.TodayPlus1Year().toString().substring(2, 4));
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().enterPaymentAmount(paymentAmount);
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().inputCardDetail(cardDetails);

        MakeAOneOffPaymentPage.CardDetailSection.getInstance().clickSubmitBtn();

        test.get().info("Step 12: input 3D Password");
        Test3DSecurePage.getInstance().switchFrameByName("issuer");
        Test3DSecurePage.getInstance().enter3DPassword("1234");
        Test3DSecurePage.getInstance().clickVerifyMe();

        test.get().info("Step 13 : Verify my personal information page displayed with successfully message");
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();
        List<String> successfullMssg = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(successfullMssg.size(), 1);
        Assert.assertEquals(successfullMssg.get(0), "Your payment was successful.");

        test.get().info("Step 14 : load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 15 :get lastest subscription");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberByIndex(1).split(" ")[0];


        test.get().info("Step 16 :Open the financial transaction content for customer");
        MenuPage.LeftMenuPage.getInstance().clickFinancialTransactionLink();
        HashMap<String, String> financialTransaction = FinancialTransactionEnity.dataFinancialTransactionForMakeAOneOffPayment("Ad Hoc Payment", "£20.00");
        Assert.assertEquals(FinancialTransactionPage.FinancialTransactionGrid.getInstance().getNumberOfFinancialTransaction(financialTransaction), 1);

        test.get().info("Step 17: verify the adhoc payment transaction detail");
        FinancialTransactionPage.FinancialTransactionGrid.getInstance().clickFinancialTransactionByDetail("Ad Hoc Payment");
        verifyAdHocPaymentTransactionDetail();

        test.get().info("Step 17 :Open the service order content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        HashMap<String, String> serviceOrder = ServiceOrderEntity.dataServiceOrderFinancialTransaction();
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(serviceOrder), 1);

        List<WebElement> serviceOrderList = ServiceOrdersContentPage.getInstance().getServiceOrders(serviceOrder);
        String numberServiceOrder = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrderList);


        test.get().info("Step 18 :verify the service order detail content for customer");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Ad-hoc Payment");

        Assert.assertEquals(TasksContentPage.TaskSummarySectionPage.getInstance().getDescription(), "Ad-hoc Payment");
        Assert.assertEquals(TasksContentPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals(TimeStamp.TodayPlus1Year().toString().substring(0, 4), TasksContentPage.TaskPage.DetailsPage.getInstance().getCreditCardExpiryYear());
        Assert.assertEquals("Approved", TasksContentPage.TaskPage.DetailsPage.getInstance().getReDSStatusAuthorisation());
        Assert.assertEquals("****************5100", TasksContentPage.TaskPage.DetailsPage.getInstance().getCardNumber());
        Assert.assertEquals("20", TasksContentPage.TaskPage.DetailsPage.getInstance().getAmountToBeDebited());
        Assert.assertEquals("0", TasksContentPage.TaskPage.DetailsPage.getInstance().getReDSResponseCode());
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird(), 6);

        test.get().info("Step 19 : Back to customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);


        test.get().info("Step 20 :access invoice page and verify invoice status");
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        Assert.assertEquals(InvoicesContentPage.getInstance().getStatusByIndex(1), "Fully Paid");
        Assert.assertEquals(InvoicesContentPage.getInstance().getDateIssuedByIndex(1), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(InvoicesContentPage.getInstance().getAmountByIndex(1), "£40.00");
        Assert.assertEquals(InvoicesContentPage.getInstance().getAmountOutStandingByIndex(1), "£0.00");

        test.get().info("Step 21 :access invoice detail page");
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);

        test.get().info("Step 22 :verify the invoice detail page");
        Assert.assertEquals(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getStatus(1), "Fully Paid");
        Assert.assertEquals(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getEnd(), Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus1Day(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getIssued(), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getNetAmount(), "£40.00");

        test.get().info("Step 23 :access subscription page");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 24 :verify treatment bar is turned off");
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickLinkByText(subScriptNo1+" FC Mobile 1");
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getBarring(), "No Barring");
        String expectedBarringStatus = "Capped Excess=OFF, Fraud=OFF, Treatment=OFF, Customer=OFF, HighUsage=OFF";
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getBarringStatus(), expectedBarringStatus);

        test.get().info("Step 25 :verify red log in glassfish 3");
//        verifyRedLogInGlassFish3();

    }


    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }

    public void verifyAdHocPaymentTransactionDetail() {
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getReceiptType(), "Ad Hoc Payment");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getReceiptStatus(), "Fully Allocated");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getPaymentAmount(), "£20.00");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getPaymentCurrency(), "Great Britain Pound");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getCardType(), "MasterCard");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getCardNumber(), "************5100");
    }

    public void createCustomerWithAOverdueInvoice(String customerNumber) {
        super.createNewBillingGroup();
        setBillGroupForCustomer(customerNumber);
        updateBillGroupPaymentCollectionDateTo10DaysLater();
        Date newStartDate = TimeStamp.TodayMinus35Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        BillingActions.updateRunAsAtDateOfCurrentDateMinus1MonthAnd1Day();

        RemoteJobHelper.getInstance().submitDoRefillBcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoRefillNcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDraftBillRun();
        RemoteJobHelper.getInstance().submitConfirmBillRun();
        RemoteJobHelper.getInstance().submitPaymentAllocationBatchJobRun();

        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        getInvoiceNumberAndAmountOverDue();

    }

    public void getInvoiceNumberAndAmountOverDue() {
        amountOverDue = CommonContentPage.CustomerSummarySectionPage.getInstance().getAmountBalance().replace("£", "");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        invoiceNumber = InvoicesContentPage.getInstance().getInvoiceNumber();

        MenuPage.RightMenuPage.getInstance().clickConfigureBarRoamingonLink();
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscriptionWithouAction(subScriptNo1 + " FC Mobile 1");

        ServiceOrdersPage.ConfigureSubscription.getInstance().selectSubscriptionBarring("Outbound");
        ServiceOrdersPage.ConfigureSubscription.getInstance().selectSubscriptionBarReason("Treatment");
        ServiceOrdersPage.ConfigureSubscription.getInstance().selectSubscriptionRoaming("Barred");
        ServiceOrdersPage.ConfigureSubscription.getInstance().enterNote("turn on treatment for FC Mobile 1");

        ServiceOrdersPage.SelectSubscription.getInstance().clickNextButton();
        ServiceOrdersPage.SelectSubscription.getInstance().clickReturnToCustomer();
    }

//    public  void verifyRedLogInGlassFish3()
//    {
//        String env = Config.getProp("cdrFolder");
//        env=env.substring(env.indexOf("/En")-1,env.indexOf("/En"));
//        String ftpFile = "/opt/payara/payara5/glassfish/domains/public-" + env + "-apps/logs/" + "reds.log";
//        String localFile = Common.getFolderLogFilePath();
//        String fileName =Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT2)+ RandomCharacter.getRandomNumericString(9)+"reds.log";
//        FTPHelper.getInstance().downLoadFromGrassFish(ftpFile,fileName,localFile);
//        Log.info("Reds log file:" + localFile);
//        String redslogfile = Common.readFile(localFile+fileName);
//        Assert.assertTrue(redslogfile.contains("DIV_NUM:TESCOGBPWEBAH"));
//        Assert.assertTrue(redslogfile.contains("Amount:2000"));
//
//
//    }

}
