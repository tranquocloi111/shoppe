package suite.regression.soho;

import framework.config.Config;
import framework.utils.Log;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.care.options.ChangePaymentDetailsPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import java.sql.Date;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TC3617_TC001_Care_Validate_Business_Customer_Invoice_When_Payment_As_DD_Rejection extends BaseTest {
    private String customerNumber;
    private Date newStartDate;
    private String serviceRefOf1stSubscription;
    String discountGroupCodeOfMobileRef1;
    String invoiceId;
    String hierarchyMbr;
    String ddiReference;

    @Test(enabled = true, description = "TC3617_TC001_Care_Validate_Business_Customer_Invoice_When_Payment_As_DD_Rejection ", groups = "SOHO")
    public void TC3617_TC001_Care_Validate_Business_Customer_Invoice_When_Payment_As_DD_Rejection() {
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
        newStartDate = TimeStamp.TodayMinus20Days();
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

        test.get().info("Step 10 : Update customer DDI details in database");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();
        openServiceOrderDetailsForSendDDIToBACSItem();
        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        hierarchyMbr = detailsPage.getHierarchyMbr();
        ddiReference = detailsPage.getDDIReference();
        CommonActions.updateCustomerDDIDetailsInDatabase(Parser.parseDateFormate(newStartDate, "dd/MMM/YY"), hierarchyMbr, ddiReference);

        test.get().info("Step 11 : Update Payment Card Detail");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickChangePaymentDetailsLink();
        ChangePaymentDetailsPage changePaymentDetail = ChangePaymentDetailsPage.getInstance();
        changePaymentDetail.enterNewPaymentDetailsForDD(new String[]{"Direct Debit","Mr Bank Holder","107999","88837491"});
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 12 : Update customer DDI details in database");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();
        openServiceOrderDetailsForSendDDIToBACSItem();
        detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        hierarchyMbr = detailsPage.getHierarchyMbr();
        ddiReference = detailsPage.getDDIReference();
        CommonActions.updateCustomerDDIDetailsInDatabase(Parser.parseDateFormate(newStartDate, "dd/MMM/YY"), hierarchyMbr, ddiReference);

        test.get().info("Step 13 : Run refill job");
        submitDoRefillBCJob();
        submitDoRefillNCJob();
        submitDoBundleRenewJob();

        test.get().info("Step 14 : Verify new discount bundle entries have been created by first bill run");
        //verifyNewDiscountBundleEntriesHaveBeenCreatedByFirstBillRun();

        test.get().info("Step 15 : Submit draft bill run");
        submitDraftBillRun();

        test.get().info("Step 16 : Submit confirm bill run");
        submitConfirmBillRun();

        test.get().info("Step 17 : Verify One Invoice Generated With Issue Date Of Today");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        verifyOneInvoiceGeneratedWithIssueDateOfToday();

        test.get().info("Step 18 : Update invoice due date to Current day");
        String dueDate = Parser.parseDateFormate(TimeStamp.Today(), "dd/MMM/YY");
        CommonActions.updateDueDateInvoice(dueDate, invoiceId);

        test.get().info("Step 19 : Run Direct Debit batch jobs");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();

        test.get().info("Step 20 : verify Invoice Status Is Updated To Direct Debit Pending");
        verifyInvoiceStatusIsUpdatedToDirectDebitPending();
        RemoteJobHelper.getInstance().runDirectDebitBatchJobToCreatePayments();

        test.get().info("Step 21 : Submit ARUDD file");
        submitAruddFile();

        test.get().info("Step 22 : verify Invoice Status Is Updated To Direct Debit Rejected");
        verifyInvoiceStatusIsUpdatedToDirectDebitRejected();
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
        Assert.assertEquals(12, discountBundles.size());
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "FC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "FLX13", "ACTIVE"));
        BaseTest.verifyFCDiscountBundles(discountBundles, newStartDate, "FLX13");
        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");
        BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TMDAT");
    }

    private String[] getBatFile() {
        String fileName = CommonActions.getFileNameByInvoiceNumber(invoiceId);
        try {
            String ftpFile = Config.getProp("cdrFolder").replace("Feed/a2aInterface/fileinbox", "Feed/DIRECTDEBIT/");
            String localFile = Common.getFolderLogFilePath();
            BaseTest.downLoadFile(ftpFile, fileName, localFile);
            Log.info(fileName + " file:" + localFile);
            return Common.getDatFile(localFile + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void submitAruddFile(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH_mm_ss");
        String localTime = LocalTime.now().format(formatter);
        String bat[] = getBatFile()[0].split(",");
        String batString =  Common.readFile("src\\test\\resources\\xml\\soho\\TC3617_arudd_file.xml")
                .replace("$ref$", bat[0])
                .replace("$transCode$", bat[5])
                .replace("$number$", bat[3])
                .replace("$currentProcessingDate$", Parser.parseDateFormate(TimeStamp.Today(),"yyyy-MM-dd"));

        String fileName = "ARUDD_"+ localTime +"_02.xml";
        Common.writeFile(batString, fileName);
        String remotePath= Config.getProp("CDRSFTPFolder");
        SFTPHelper.getInstance().upFileFromLocalToRemoteServer(fileName,remotePath);

        CommonActions.isFileProcessInboundNonXmlProcessed(fileName);
        RemoteJobHelper.getInstance().waitEncryptFileComplete();
    }

    private void verifyInvoiceStatusIsUpdatedToDirectDebitPending(){
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(1, grid.getRowNumberOfInvoiceTable());
        List<String> invoice = new ArrayList<>();
        invoice.add(invoiceId);
        invoice.add("Direct Debit Pending");
        Assert.assertEquals(1, Common.compareList(grid.getAllValueOfInvocie(), invoice));

        grid.clickInvoiceNumberByIndex(1);
        Assert.assertEquals(grid.getStatus(1), "Direct Debit Pending");

    }

    private void verifyInvoiceStatusIsUpdatedToDirectDebitRejected(){
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(1, grid.getRowNumberOfInvoiceTable());
        List<String> invoice = new ArrayList<>();
        invoice.add(invoiceId);
        invoice.add("Direct Debit Rejected");
        Assert.assertEquals(1, Common.compareList(grid.getAllValueOfInvocie(), invoice));

        grid.clickInvoiceNumberByIndex(1);
        Assert.assertEquals(grid.getStatus(1), "Direct Debit Rejected");

    }

}
