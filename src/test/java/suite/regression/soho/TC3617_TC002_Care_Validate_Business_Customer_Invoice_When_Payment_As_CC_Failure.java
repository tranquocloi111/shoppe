package suite.regression.soho;

import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.TasksContentPage;
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

public class TC3617_TC002_Care_Validate_Business_Customer_Invoice_When_Payment_As_CC_Failure extends BaseTest {
    private String customerNumber;
    private Date newStartDate;
    String invoiceId;
    String firstName;
    String lastName ;
    String hierarchyMbr;

    @Test(enabled = true, description = "TC3617_TC002_Care_Validate_Business_Customer_Invoice_When_Payment_As_CC_Failure", groups = "SOHO")
    public void TC3617_TC002_Care_Validate_Business_Customer_Invoice_When_Payment_As_CC_Failure() {
        test.get().info("Step 1 : Create a Customer with business type with CC method");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC3617_TC002_CC_card_request.xml";
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
        firstName = owsActions.firstName;
        lastName = owsActions.lastName;
        verifyCustomerPaymentInformation();

        test.get().info("Step 9 : Get Hierarchy Mbr");
        openServiceOrderDetailsForSendDDIToBACSItem();
        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        hierarchyMbr = detailsPage.getHierarchyMbr();

        test.get().info("Step 10 : Run refill job");
        submitDoRefillBCJob();
        submitDoRefillNCJob();
        submitDoBundleRenewJob();

        test.get().info("Step 12 : Submit draft bill run");
        submitDraftBillRun();

        test.get().info("Step 13 : Submit confirm bill run");
        submitConfirmBillRun();

        test.get().info("Step 14 : Verify One Invoice Generated With Issue Date Of Today");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        verifyOneInvoiceGeneratedWithIssueDateOfToday();

        test.get().info("Step 15 : Update Credit card date to past");
        CommonActions.updateCreditCardDateToPast(hierarchyMbr);

        test.get().info("Step 16 : Verify Customer Payment Information Is Updated");
        verifyCustomerPaymentInformationIsUpdated();

        test.get().info("Step 17 : Update invoice due date to Current day");
        String dueDate = Parser.parseDateFormate(TimeStamp.Today(), "dd/MMM/YY");
        CommonActions.updateDueDateInvoice(dueDate, invoiceId);

        test.get().info("Step 18 : Verify Invoice Due Date Is Updated()");
        verifyInvoiceDueDateIsUpdated();

        test.get().info("Step 19 : Run Credit Card batch jobs from Unix");
        RemoteJobHelper.getInstance().submitCreditCardBatchJobRun();

        test.get().info("Step 20 : Verify Credit Card Failure Service Order");
        verifyInvoiceStatusIsUpdatedToDirectDebitRejected();

        test.get().info("Step 21 : Verify Service Order Direct Debit Rejected");
        verifyServiceOrderDirectDebitRejected();
    }


    private void verifyCustomerPaymentInformation(){
        DetailsContentPage.PaymentInformationPage paymentInfo =  DetailsContentPage.PaymentInformationPage.getInstance();
        Assert.assertEquals(paymentInfo.getPaymentMethod(), "Credit Card ( "+Parser.parseDateFormate(TimeStamp.Today(), "dd MMM yyyy")+" )");
        Assert.assertEquals(paymentInfo.getCardType(), "MasterCard");
        Assert.assertEquals(paymentInfo.getCreditCardHolderName(), firstName + " " + lastName);
        Assert.assertEquals(paymentInfo.getCardExpireMonth(), "12");
        Assert.assertEquals(paymentInfo.getCardExpireYear(), "2030");
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

    private void verifyServiceOrderDirectDebitRejected(){
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        List<String> rejected = new ArrayList<>();
        rejected.add(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        rejected.add("Rejected");
        rejected.add("Credit Card Failure");
        Assert.assertEquals(1, Common.compareList(serviceOrders.getAllValueOfServiceOrder(), rejected));

        serviceOrders.clickServiceOrderByType("Credit Card Failure");

        TasksContentPage.TaskPage.TaskSummarySectionPage taskSummary = TasksContentPage.TaskSummarySectionPage.getInstance();
        Assert.assertEquals(taskSummary.getDescription(), "Credit Card Failure");
        Assert.assertEquals(taskSummary.getStatus(), "Rejected");
    }

    private void verifyCustomerPaymentInformationIsUpdated(){
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        DetailsContentPage.PaymentInformationPage paymentInfo =  DetailsContentPage.PaymentInformationPage.getInstance();
        String timeStamp[] = Parser.parseDateFormate(Date.valueOf(TimeStamp.Today().toLocalDate().minusYears(1)), "dd/MM/yyyy").split("/");
        Assert.assertEquals(paymentInfo.getPaymentMethod(), "Credit Card ( "+Parser.parseDateFormate(TimeStamp.Today(), "dd MMM yyyy")+" )");
        Assert.assertEquals(paymentInfo.getCardType(), "MasterCard");
        Assert.assertEquals(paymentInfo.getCreditCardHolderName(), firstName + " " + lastName);
        Assert.assertEquals(paymentInfo.getCardExpireMonth(), timeStamp[1]);
        Assert.assertEquals(paymentInfo.getCardExpireYear(), timeStamp[2]);
    }

    private void verifyInvoiceDueDateIsUpdated(){
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        grid.clickInvoiceNumberByIndex(1);
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), grid.getDueDate());
    }

}
