package suite.regression.soho;

import framework.config.Config;
import framework.utils.RandomCharacter;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
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

public class TC3617_TC003_Care_Validate_Business_Customer_Invoice_Write_Off extends BaseTest {
    private String customerNumber = "11480";
    private Date newStartDate;
    Date firstRunDate;
    String subNo1;
    String subNo2;

    @Test(enabled = true, description = "TC3617_TC003_Care_Validate_Business_Customer_Invoice_Write_Off", groups = "SOHO")
    public void TC3617_TC003_Care_Validate_Business_Customer_Invoice_Write_Off() {
        test.get().info("Step 1 : Create a Customer with business type with CC method");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC3617_TC003_write_off_request.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create new billing group");
        createNewBillingGroupToMinusMonth(2);

        test.get().info("Step 3 : Update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update start date for customer");
        firstRunDate = Date.valueOf(TimeStamp.Today().toLocalDate().minusMonths(2));
        newStartDate = Date.valueOf(TimeStamp.Today().toLocalDate().minusMonths(2).minusDays(20));
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Get Subscription Number");
        updateReadWriteAccessBusinessCustomers();
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        subNo2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        test.get().info("Step 7 : Generate the cdr file then upload to server");
        generateCDRFileFromTemplateThenUploadToServer();
        RemoteJobHelper.getInstance().submitDoUsageRemoteJob();

        test.get().info("Step 8 : Run refill job");
        updateBillRunCalendarRunDatesToRunFirstBillRun(firstRunDate);
        RemoteJobHelper.getInstance().submitDoRefillBcJob(firstRunDate);
        RemoteJobHelper.getInstance().submitDoRefillNcJob(firstRunDate);
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(firstRunDate);

        test.get().info("Step 9 : Submit draft bill run");
        submitDraftBillRun();

        test.get().info("Step 10 : Submit confirm bill run");
        submitConfirmBillRun();

        test.get().info("Step 11 : Verify One Invoice Generated With Issue Date Of Today");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        verifyOneInvoiceGeneratedWithIssueDateOfToday();

        test.get().info("Step 12 : Apply Write Off Credit");
        applyACreditAmount();

        test.get().info("Step 13 : Verify Write Off Transaction Financial");
        verifyWriteOffFinancialTransactions();

        test.get().info("Step 14 : Verify Fully Paid invoice");
        verifyFullPaidInvoice();

        test.get().info("Step 15 : verify Text Of Unbilled Call Details");
        verifyTextOfUnbilledCallDetails();
    }

    private void verifyOneInvoiceGeneratedWithIssueDateOfToday(){
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(1, grid.getRowNumberOfInvoiceTable());
    }

    public void generateCDRFileFromTemplateThenUploadToServer(){
        String filePath = "src\\test\\resources\\txt\\care\\TM_DRAS_CDR_20150106170007";
        String cdrFileString = Common.readFile(filePath);
        String timeStamp =  TimeStamp.TodayMinus1HourReturnFullFormat();
        String fileName = "TM_DRAS_CDR_" + timeStamp + ".txt";
        cdrFileString = cdrFileString.replace("20150106170007", timeStamp)
                .replace("07847469610", subNo2)
                .replace("04/01/2015", Parser.parseDateFormate(TimeStamp.TodayMinus2Days(), TimeStamp.DATE_FORMAT4));

        String localPath = Common.getFolderLogFilePath() + fileName;
        Common.writeFile(cdrFileString, localPath);
        String remotePath = Config.getProp("CDRSFTPFolder");
        SFTPHelper.getInstance().upFileFromLocalToRemoteServer(localPath, remotePath);
        waitLoadCDRJobComplete();
    }

    private void applyACreditAmount(){
        MenuPage.RightMenuPage.getInstance().clickApplyFinancialTransactionLink();
        ServiceOrdersPage.AccountSummaryAndSelectAction accountSummaryAndSelectAction =  ServiceOrdersPage.AccountSummaryAndSelectAction.getInstance();
        accountSummaryAndSelectAction.selectChooseAction("Apply a Credit");
        accountSummaryAndSelectAction.clickNextButton();

        ApplyCreditPage applyCreditPage = ApplyCreditPage.getInstance();
        applyCreditPage.inputCreditAmount("105.00");
        applyCreditPage.clickNextButton();
        applyCreditPage.clickNextButton();
        applyCreditPage.clickReturnToCustomer();
    }

    private void verifyWriteOffFinancialTransactions(){
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickFinancialTransactionLink();
        List<String> ft = new ArrayList<>();
        ft.add(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        ft.add("Write Off Credit");
        ft.add("£105.00");
        ft.add("-£157.50");

        FinancialTransactionPage.FinancialTransactionGrid financialTransactionPage = FinancialTransactionPage.FinancialTransactionGrid.getInstance();
        Assert.assertEquals(Common.compareList(financialTransactionPage.getAllValueOfFinancialTransaction(), ft), 1);
    }

    private void verifyFullPaidInvoice(){
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        List<String> invoice = new ArrayList<>();
        invoice.add(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        invoice.add("Fully Paid");
        invoice.add("£105.00");
        invoice.add("£0.00");

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(Common.compareList(grid.getAllValueOfInvocie(), invoice), 1);
    }

    private void verifyTextOfUnbilledCallDetails(){
        MenuPage.LeftMenuPage.getInstance().clickUnBilledCallDetailsItem();

        UnbilledCallDetailsPage unbilledCallDetailsPage = UnbilledCallDetailsPage.getInstance();
        unbilledCallDetailsPage.selectSubscription(subNo2 + " Mobile Ref 2");
        unbilledCallDetailsPage.clickFindNowBrn();
        Assert.assertEquals( unbilledCallDetailsPage.get(3).trim(),"(+) Call charged from your perk.");
    }
}
