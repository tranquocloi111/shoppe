package suite.regression.ocs;

import framework.utils.Log;
import framework.utils.RandomCharacter;
import logic.business.db.billing.CommonActions;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.care.options.ApplyCreditPage;
import logic.pages.care.options.TransferExistingFundsPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class TC5340_001_Customer_Has_An_Invoice_Already_And_Clubcard_Tradein_Voucher_More_Than_Total_Order_Within_Trial_Period extends BaseTest {
    private String customerNumber = "47765562";
    private String orderId = "8702060";
    private String subNo1 = "07647064770";
    private OWSActions owsActions;
    private Date newStartDate = TimeStamp.TodayMinus20Days();
    private String discountGroupCode;
    private String clubCardCode;
    private String lastName;
    private String firstName;
    private String serviceOrderId;

    //HTM-5342
    @Test(enabled = true, description = "TC5340_001_Customer_Has_An_Invoice_Already_And_Clubcard_Tradein_Voucher_More_Than_Total_Order_Within_Trial_Period", groups = "OCS")
    public void TC5340_001_Customer_Has_An_Invoice_Already_And_Clubcard_Tradein_Voucher_More_Than_Total_Order_Within_Trial_Period() {
        test.get().info("Step 1 : Create a Customer has an invoice already, and Clubcard +Tradein voucher > Total Order, within trial period");
        CommonActions.updateHubProvisionSystem("O");
        owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC5340_Single_Deals_Ocs_Clubcard_Tradein_Residential.xml";
        owsActions.createOcsCustomerRequestAcceptUrl(path,2, "OCS", "", "", "", "Voucher");

        test.get().info("Step 2 : Create new billing group");
        createNewBillingGroup();

        test.get().info("Step 3 : Update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        orderId = owsActions.orderIdNo;
        clubCardCode = owsActions.clubcardPartner;
        lastName = owsActions.lastName;
        firstName = owsActions.firstName;
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update start date for customer");
        newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Verify Create Ocs Account async task is not displayed");
        CareTestBase.page().checkCreateOcsAccountCommand(orderId, true);

        test.get().info("Step 7 : Login to Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");
        verifyOcsKeyOfSubscription();

        test.get().info("Step 8 : Run Inclusive Spend Refill for Billing Capped and Network Capped. Run Discount Bundle Renewal job.");
        submitDoRefillBCJob();
        submitDoRefillNCJob();
        submitDoBundleRenewJob();

        test.get().info("Step 9 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateSubscription(true,true);

        test.get().info("Step 10 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo1));

        test.get().info("Step 11 : Update customer end date");
        Date endDate = TimeStamp.TodayMinus5Days();
        CommonActions.updateCustomerEndDateWithoutProcedure(customerNumber, endDate);

        test.get().info("Step 12 : Submit Bill Run Job");
        submitDraftBillRun();
        submitConfirmBillRun();
        submitPaymentAllocationBatchJob();

        test.get().info("Step 13 : Navigate to the Apply Financial Transaction. Verify The Account Summary and Select Action page is displayed.");
        verifyClubcardPartnerDiscountOfBill();

        test.get().info("Step 14 : Transfer Existing Funds");
        applyTransferExistingFunds();

        test.get().info("Step 15 : The Apply Financial Transaction SO is completed");
        verifyServiceOrdersAreCreatedCorrectly();

        test.get().info("Step 16 : Verify refund amount to customer is the tradein voucher amount.");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        verifyOtherChargesCreditsAreCorrect();

        test.get().info("Step 17 : Verify ChangeSubLifeCycleRequest validation");
        verifyTrustServerLog();
    }

    private void verifyOcsKeyOfSubscription(){
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo1 + " Mobile 1");
        SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage generalSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        discountGroupCode = generalSectionPage.getDiscountGroupCode();
        verifyOcsSubscriptionDetails("OCS", discountGroupCode + "S", discountGroupCode + "A", newStartDate);
    }

    private void verifyClubcardPartnerDiscountOfBill(){
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickFinancialTransactionLink();
        FinancialTransactionPage.FinancialTransactionGrid financialTransactionGrid = FinancialTransactionPage.FinancialTransactionGrid.getInstance();
        financialTransactionGrid.clickFinancialTransactionByDetail("Clubcard Partner discount off bill(s)");

        PaymentDetailPage.ReceiptDetail receiptDetail = PaymentDetailPage.ReceiptDetail.getInstance();
        Assert.assertEquals(receiptDetail.getReceiptType(), "Clubcard Partner discount off bill(s)");
        Assert.assertEquals(receiptDetail.getReceiptStatus(), "Dishonoured Payment");
        Assert.assertEquals(receiptDetail.getPaymentAmount(), "£195.50");
        Assert.assertEquals(receiptDetail.getPaymentCurrency(), "Great Britain Pound");
        Assert.assertEquals(receiptDetail.getPaymentReference(), "Voucher code - " + clubCardCode);
        Assert.assertEquals(receiptDetail.getVoucherSupplier(), "Voucher code");
        Assert.assertEquals(receiptDetail.getVoucherCode(), clubCardCode);
        Assert.assertEquals(receiptDetail.getVoucherProductCode(), "Club Card Voucher");
        Assert.assertEquals(receiptDetail.getOrderId(), orderId);

        PaymentDetailPage.ReceiptAllocation receiptAllocation = PaymentDetailPage.ReceiptAllocation.getInstance();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList(String.format("%s, %s",lastName, firstName), Parser.parseDateFormate(newStartDate, "dd MMM yyyy"), "£195.50")));
        lists.add(new ArrayList<>(Arrays.asList(String.format("%s, %s",lastName, firstName), Parser.parseDateFormate(TimeStamp.Today(), "dd MMM yyyy"), "-£195.50")));
        Assert.assertEquals(Common.compareLists(receiptAllocation.getAllValueOfReceiptAllocations(), lists), 2);
    }

    private void applyTransferExistingFunds(){
        MenuPage.RightMenuPage.getInstance().clickApplyFinancialTransactionLink();
        ServiceOrdersPage.AccountSummaryAndSelectAction accountSummaryAndSelectAction =  ServiceOrdersPage.AccountSummaryAndSelectAction.getInstance();
        accountSummaryAndSelectAction.selectChooseAction("Transfer Existing Funds");
        accountSummaryAndSelectAction.clickNextButton();

        TransferExistingFundsPage transferExistingFundsPage = TransferExistingFundsPage.getInstance();
        transferExistingFundsPage.inputAmountToRefund("450");
        transferExistingFundsPage.clickNextButton();
        transferExistingFundsPage.clickNextButton();
        transferExistingFundsPage.clickReturnToCustomer();
    }

    private void verifyServiceOrdersAreCreatedCorrectly(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("Apply Financial Transaction", "Completed Task", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));
        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        Assert.assertEquals(Common.compareLists(serviceOrders.getAllValueOfServiceOrder(), lists), 1);

        serviceOrderId = serviceOrders.getServiceOrderidByType("Apply Financial Transaction");
        serviceOrders.clickServiceOrderByType("Apply Financial Transaction");
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getDescription(), "Apply Financial Transaction");

        TasksContentPage.TaskPage.EventsGridSectionPage eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        List<List<String>> eventsLists = new ArrayList<>();
        eventsLists.add(new ArrayList<>(Arrays.asList("Service Order created", "Open Service Order")));
        eventsLists.add(new ArrayList<>(Arrays.asList("Sending ReD Payment request with transaction id " + serviceOrderId, "In Progress")));
        eventsLists.add(new ArrayList<>(Arrays.asList(String.format("Payment Accepted by REDS. TransactionID: %s. Status Code: APPROVE. Response Code: 00", serviceOrderId), "In Progress")));
        eventsLists.add(new ArrayList<>(Arrays.asList("Refund task Completed", "Completed Task")));
        Assert.assertEquals(Common.compareLists(eventsGridSectionPage.getAllValueOfEvents(), eventsLists), 4);
    }

    private void verifyOtherChargesCreditsAreCorrect(){
        MenuPage.LeftMenuPage.getInstance().clickOtherChargesCreditsItem();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("Miscellaneous Products", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT), "IMMEDIATE-REFUND - Customer Care refund issued - £450.00")));
        OtherChargesCreditsContent otherChargesCreditsContent = OtherChargesCreditsContent.getInstance();
        Assert.assertEquals(Common.compareLists(otherChargesCreditsContent.getAllValueOfOtherChargesCredits(), lists), 1);
    }

    private void verifyTrustServerLog(){
        String localTime = Common.getCurrentLocalTime();
        String ftpFile = "/opt/payara/payara5/glassfish/domains/trust-R2-serv/logs/server.log";
        String localFile = Common.getFolderLogFilePath() + customerNumber + localTime + "_TrustServerLog.txt";
        SFTPHelper.getGlassFishInstance().downloadGlassFishFile(localFile, ftpFile);
        Log.info("Server log file:" + localFile);

        //Trust Server
        String trustServerLog = localFile;
        String changeSubLifeCycleRequestMsgPath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC5340_001_Change_Sub_Life_Cycle_Request_Msg_Log.xml")
                .replace("$SubscriberKey$","TMPAYM" + discountGroupCode + "S");
        String changeSubLifeCycleRequestMsgFile = Common.saveXmlFile(customerNumber + localTime +"_ChangeSubLifeCycleRequestMsg.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(changeSubLifeCycleRequestMsgPath)));
        Assert.assertTrue(Common.compareTextsFile(trustServerLog, changeSubLifeCycleRequestMsgFile));
    }
}
