package suite.regression.soho;

import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.HeaderContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ApplyCreditPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TC4500_003_Business_Customer_Including_Business_Name_Has_Been_Deactivated_With_Last_Receipt_In_12_Months_Ago extends BaseTest {
    private String customerNumber = "12116";
    private Date newStartDate;
    private String subNo1;
    private Date endDate;
    private Date firstRunDate;
    private String receiptId;

    @Test(enabled = true, description = "TC4500_003_Business_Customer_Including_Business_Name_Has_Been_Deactivated_With_Last_Receipt_In_12_Months_Ago", groups = "SOHO")
    public void TC4500_003_Business_Customer_Including_Business_Name_Has_Been_Deactivated_With_Last_Receipt_In_12_Months_Ago() {
        test.get().info("Step 1 : Create a Customer with business type with CC method");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC4500_003_business_12_receipt_request.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create new billing group");
        createNewBillingGroupToMinusMonth(20);

        test.get().info("Step 3 : Update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update start date for customer");
        firstRunDate = Date.valueOf(TimeStamp.Today().toLocalDate().minusMonths(20));
        newStartDate = Parser.asDate(TimeStamp.Today().toLocalDate().minusYears(2));
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Get Subscription Number");
        updateReadWriteAccessBusinessCustomers();
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");

        test.get().info("Step 8 : Run refill job");
        updateBillRunCalendarRunDatesToRunFirstBillRun(firstRunDate);
        RemoteJobHelper.getInstance().submitDoRefillBcJob(firstRunDate);
        RemoteJobHelper.getInstance().submitDoRefillNcJob(firstRunDate);
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(firstRunDate);

        test.get().info("Step 9 : Submit draft bill run");
        submitDraftBillRun();

        test.get().info("Step 10 : Submit confirm bill run");
        RemoteJobHelper.getInstance().submitConfirmBillRun(firstRunDate);

        test.get().info("Step 11 : Verify One Invoice Generated With Issue Date Of Today");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        verifyOneInvoiceGeneratedWithIssueDateOfToday();

        test.get().info("Step 13 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateAccountWithOutReturnRefund(TimeStamp.Today());

        test.get().info("Step 14 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo1));

        test.get().info("Step 15 : Update End Date > 24 moth ago");
        endDate = Parser.asDate(TimeStamp.Today().toLocalDate().minusYears(2).minusMonths(4));
        CommonActions.updateCustomerEndDate(customerNumber, endDate);

        test.get().info("Step 16 : Apply Write Off Credit");
        applyATakePayment();

        test.get().info("Step 17 : Get Receipt Id");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickFinancialTransactionLink();
        receiptId = FinancialTransactionPage.FinancialTransactionGrid.getInstance().getRefNumberByDetail("Ad Hoc Payment");
        CommonActions.updateReceiptDate(receiptId, Parser.parseDateFormate(firstRunDate, "dd/MMM/yy"));

        test.get().info("Step 18 : Submit Anonymise Acount Job");
        submitAnonymiseAccountJob();

        test.get().info("Step 19 : Verify Customer Anonymised in result table");
        CareTestBase.page().reLoadCustomerInHubNetWithoutOpenCustomer(customerNumber);
        FindPage findPage = FindPage.getInstance();
        Assert.assertEquals(findPage.getNameOfResult(0), "xxx");
        findPage.openCustomerByIndex(0);

        test.get().info("Step 20 : Verify color and information of business information tab ");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        SummaryContentsPage.BusinessInformationPage business = SummaryContentsPage.BusinessInformationPage.getInstance();
        verifyInformationColorBoxHeaderBusiness();
        Assert.assertTrue(business.isBusinessPresent());
        Assert.assertEquals(business.getBusinessName(), "");

        test.get().info("Step 21 : Verify Main Header Name and Recent Customers For Business Customer");
        HeaderContentPage headerContentPage = HeaderContentPage.getInstance();
        Assert.assertEquals(headerContentPage.getMainContainBreadCrumbsBox(), "Customers > Summary for  (xxx, xxx)");
        Assert.assertEquals(headerContentPage.getMainHeaderSummaryText(), "(xxx, xxx)");
        Assert.assertEquals(CommonContentPage.RecentCustomersPage.getIntance().getFirstRecentCustomer(), "xxx, xxx");

        test.get().info("Step 22 : Verify Customer Summary");
        CommonContentPage.CustomerSummarySectionPage customerSummary = CommonContentPage.CustomerSummarySectionPage.getInstance();
        Assert.assertEquals(customerSummary.getAddress(), "");
        Assert.assertEquals(customerSummary.getStatus(), "Anonymised");
        Assert.assertEquals(customerSummary.getPassPhraseAnswer(), "xxx");

        test.get().info("Step 23 : Verify Subscriptions Table");
        verifySubscriptionNameIsAnonymised();
    }

    private void verifyOneInvoiceGeneratedWithIssueDateOfToday(){
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(1, grid.getRowNumberOfInvoiceTable());
    }

    private void applyATakePayment(){
        MenuPage.RightMenuPage.getInstance().clickApplyFinancialTransactionLink();
        ServiceOrdersPage.AccountSummaryAndSelectAction accountSummaryAndSelectAction =  ServiceOrdersPage.AccountSummaryAndSelectAction.getInstance();
        accountSummaryAndSelectAction.selectChooseAction("Take a Payment");

        ServiceOrdersPage.InputPaymentDetails inputPaymentDetails = ServiceOrdersPage.InputPaymentDetails.getInstance();
        inputPaymentDetails.inputPaymentDetail("70.00", "Regression Testing");
        inputPaymentDetails.clickNextButton();
        inputPaymentDetails.clickNextButton();
        inputPaymentDetails.clickNextButton();
        inputPaymentDetails.clickReturnToCustomer();
    }

    private void verifySubscriptionNameIsAnonymised(){
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage subscriptionsGrid = CommonContentPage.SubscriptionsGridSectionPage.getInstance();
        List<String> sub = new ArrayList<>();
        sub.add(subNo1 + " xxx");
        sub.add("Safety Buffer - £20");
        sub.add(Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT));
        sub.add(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT));
        sub.add("FC12-1000-500SO - £10 Tariff 12 Month Contract - £10.00");
        sub.add("Inactive");
        Assert.assertEquals(Common.compareList(subscriptionsGrid.getAllValueSubscription(), sub), 1);

        subscriptionsGrid.clickSubscriptionNumberLinkByIndex(1);
        SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage general = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        Assert.assertEquals(general.getReference(), "xxx");
    }

}
