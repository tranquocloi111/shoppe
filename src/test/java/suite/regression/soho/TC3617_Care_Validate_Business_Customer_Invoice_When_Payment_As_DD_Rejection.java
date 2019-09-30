package suite.regression.soho;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.care.options.ChangePaymentDetailsPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.List;

public class TC3617_Care_Validate_Business_Customer_Invoice_When_Payment_As_DD_Rejection extends BaseTest {
    private String customerNumber = "14063";
    private Date newStartDate;
    private String serviceRefOf1stSubscription;
    private String serviceOrderId;
    String discountGroupCodeOfMobileRef1;
    String invoiceId;
    String hmId = "19341";
    String ddrefe = "330000016811A";

    @Test(enabled = true, description = "TC3796_001_OWS_Create_New_Order_For_Business_Customer ", groups = "SOHO")
    public void TC3796_001_OWS_Create_New_Order_For_Business_Customer() {
        test.get().info("Step 1 : Create a Customer with business type with DD method");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\onlines_DD_business_customer_with_FC_2_bundles_and_NK2720.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create new billing group");
        createNewBillingGroup();

        test.get().info("Step 3 : Update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update start date for customer");
        newStartDate = TimeStamp.TodayMinus1MonthMinus20Day();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Get Subscription Number");
        updateReadWriteAccessBusinessCustomers();
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 7 : Verify customer start date and billing group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 8 : Verify customer bill style is set to Summary");
        verifyCustomerBillStyleIsSetToSummary();

        test.get().info("Step 9 : Verify all discount bundle entries align with bill run calendar entires");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");

        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile 1");
        discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyFCDiscountBundleBeforeChangingBundle(discountGroupCodeOfMobileRef1);

        test.get().info("Step 14 : Update customer DDI details in database");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();
        openServiceOrderDetailsForSendDDIToBACSItem();
        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        hmId = detailsPage.getHierarchyMbr();
        ddrefe = detailsPage.getDDIReference();
        CommonActions.updateCustomerDDIDetailsInDatabase(Parser.parseDateFormate(TimeStamp.TodayPlus10Days(), "dd/MMM/YY"), hmId, ddrefe);

        MenuPage.RightMenuPage.getInstance().clickChangePaymentDetailsLink();
        ChangePaymentDetailsPage changePaymentDetail = ChangePaymentDetailsPage.getInstance();
        changePaymentDetail.enterNewPaymentDetailsForDD(new String[]{"Direct Debit","Mr Bank Holder","107999","88837491"});

        RemoteJobHelper.getInstance().submitSendDDIRequestJob();
        openServiceOrderDetailsForSendDDIToBACSItem();
        detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        hmId = detailsPage.getHierarchyMbr();
        ddrefe = detailsPage.getDDIReference();
        CommonActions.updateCustomerDDIDetailsInDatabase(Parser.parseDateFormate(TimeStamp.TodayPlus10Days(), "dd/MMM/YY"), hmId, ddrefe);

        test.get().info("Step 10 : Run refill job");
        submitDoRefillBCJob();
        submitDoRefillNCJob();
        submitDoBundleRenewJob();

        test.get().info("Step 11 : Verify new discount bundle entries have been created by first bill run");
        //verifyNewDiscountBundleEntriesHaveBeenCreatedByFirstBillRun();

        test.get().info("Step 12 : Submit draft bill run");
        submitDraftBillRun();

        test.get().info("Step 13 : Submit confirm bill run");
        submitConfirmBillRun();

        test.get().info("Step 14 : Verify One Invoice Generated With Issue Date Of Today");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        verifyOneInvoiceGeneratedWithIssueDateOfToday();

        test.get().info("Step 15 : Update invoice due date to Current day +1");
        String dueDate = Parser.parseDateFormate(TimeStamp.Today(), "dd/MMM/YY");
        CommonActions.updateDueDateInvoice(dueDate, invoiceId);

        RemoteJobHelper.getInstance().submitPaymentAllocationBatchJobRun();

        test.get().info("Step 16 : Run 2 Direct Debit batch jobs from Unix");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();
        RemoteJobHelper.getInstance().runDirectDebitBatchJobToCreatePayments();

    }


    private void verifyCustomerBillStyleIsSetToSummary(){
        Assert.assertEquals("Summary", DetailsContentPage.BillingInformationSectionPage.getInstance().getBillStyle());
    }

    private void verifyFCDiscountBundleBeforeChangingBundle(String discountGroupCode){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(11, discountBundles.size());
        BaseTest.verifyFCDiscountBundles(discountBundles, newStartDate, "FLX13");
        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");
        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TMDAT");
    }

    private void verifyOneInvoiceGeneratedWithIssueDateOfToday(){
        //MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(1, grid.getRowNumberOfInvoiceTable());
        invoiceId = grid.getInvoiceNumber();

        grid.clickInvoiceNumberByIndex(1);
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), grid.getIssued());

    }

    private void verifyNewDiscountBundleEntriesHaveBeenCreatedByFirstBillRun(){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(13, discountBundles.size());
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "FC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "FLX13", "ACTIVE"));
        BaseTest.verifyFCDiscountBundles(discountBundles, newStartDate, "FLX13");
        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");
        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TMDAT");
    }


}
