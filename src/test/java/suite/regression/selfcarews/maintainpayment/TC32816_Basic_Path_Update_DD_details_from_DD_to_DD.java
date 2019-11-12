package suite.regression.selfcarews.maintainpayment;

import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.AllocationEnity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.awt.*;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

public class TC32816_Basic_Path_Update_DD_details_from_DD_to_DD extends BaseTest {
    String customerNumber = "15209";
    String sub = "07601929020";

    @Test(enabled = true, description = "TC32816 basic path update DD details from cc to DD", groups = "SelfCare")
    public void TC32816_Basic_Path_Update_DD_details_from_DD_to_DD() {
        //-----------------------------------------
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_DD_customer_with_FC_2_bundles_and_NK2720";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile 1");
        sub = owsActions.serviceRef;

        test.get().info("Step 2 : create new billing group");
        createNewBillingGroup();

        test.get().info("Step 3 : update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5: Update the start date of customer");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6: load customer in hub net ");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 7: submit send DDi request Job");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();

        test.get().info("Step 8: verify  customer DDI status changed to inactive");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("Inactive",
                DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus());

        test.get().info("Step 9: open service order detail for send DDI to BACS items");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Send DDI to BACS");

        test.get().info("Step 10: verify service order status is sent and DDI reference has value");
        Assert.assertEquals("Sent", TasksContentPage.TaskPage.DetailsPage.getInstance().getServiceOrderStatus());
        Assert.assertTrue(TasksContentPage.TaskPage.DetailsPage.getInstance().getDDIReference() != "");
        String hierarchyMbr = TasksContentPage.TaskPage.DetailsPage.getInstance().getHierarchyMbr();
        String ddiReference = TasksContentPage.TaskPage.DetailsPage.getInstance().getDDIReference();

        test.get().info("Step 11: update customer DDI details in database");
        CommonActions.updateCustomerDDIDetailsInDatabase(Parser.parseDateFormate(newStartDate, "dd/MMM/YY"), hierarchyMbr, ddiReference);

        test.get().info("Step 12: verify DDI information is correct");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("Active", DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus());
        String expectedDDI = DetailsContentPage.PaymentInformationPage.getInstance().getDDIReference().split(" ")[0];
        Assert.assertEquals(String.format("%s ( %s )", expectedDDI, Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT)), DetailsContentPage.PaymentInformationPage.getInstance().getDDIReference());

        test.get().info("Step 13: verify other DDI information is also correct");
        String bankSortCode = "089999";
        String bankAccountHolderName = "MT5J2JYZ1285FVMM";
        String bankAccountNumber = "66374958";
        Assert.assertEquals("***999", DetailsContentPage.PaymentInformationPage.getInstance().getBankSortCode());
        Assert.assertEquals(bankAccountHolderName, DetailsContentPage.PaymentInformationPage.getInstance().getBankAccountHolderName());
        Assert.assertEquals(bankAccountNumber, DetailsContentPage.PaymentInformationPage.getInstance().getBankAccountNumber());

        test.get().info("Step 14: Build maintain payment detail request ");
        path = "src\\test\\resources\\xml\\sws\\maintainpayment\\TC32816_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildPaymentDetailRequestByIndex(customerNumber, path);

        test.get().info("Step 15:  submit the request to webservice");
        Xml response = swsActions.submitTheRequest();

        test.get().info("Step 16: verify maintain payment response");
        logic.business.entities.selfcare.MaintainPaymentResponseData maintainPaymentResponseData = new logic.business.entities.selfcare.MaintainPaymentResponseData();
        maintainPaymentResponseData.setAccountNumber(customerNumber);
        maintainPaymentResponseData.setResponseCode("0");
        maintainPaymentResponseData.setAction("UPDATE_DIRECT_DEBIT");
        Assert.assertEquals(maintainPaymentResponseData.getAccountNumber(), response.getTextByTagName("accountNumber"));
        Assert.assertEquals(maintainPaymentResponseData.getAction(), response.getTextByTagName("action"));
        Assert.assertEquals(maintainPaymentResponseData.getResponseCode(), response.getTextByTagName("responseCode"));

        test.get().info("Step 17:  open service order for customer");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        HashMap<String, String> enity = ServiceOrderEntity.dataServiceOrder("Completed Task", "Change Payment Details");
        Assert.assertEquals(1, ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(enity));
        String serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Payment Details");

        test.get().info("Step 18:  verify the details SO is successfully");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Payment Details");
        Assert.assertEquals("Barclays", TasksContentPage.TaskPage.DetailsPage.getInstance().getBankName());
        Assert.assertEquals("63748472", TasksContentPage.TaskPage.DetailsPage.getInstance().getBankAccountNumber());
        Assert.assertEquals("***959", TasksContentPage.TaskPage.DetailsPage.getInstance().getBankSortCode());
        Assert.assertEquals("Scott Harvey", TasksContentPage.TaskPage.DetailsPage.getInstance().getBankAccountHolderName());

        test.get().info("Step 19:  back to customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Step 20:  verify payment information have been updated");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(String.format("Direct Debit ( %s )", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))
                , DetailsContentPage.PaymentInformationPage.getInstance().getPaymentMethod());

        Assert.assertEquals("Barclays", DetailsContentPage.PaymentInformationPage.getInstance().getBankName());
        Assert.assertEquals("Scott Harvey", DetailsContentPage.PaymentInformationPage.getInstance().getBankAccountHolderName());
        Assert.assertEquals("***959", DetailsContentPage.PaymentInformationPage.getInstance().getBankSortCode());
        Assert.assertEquals("63748472", DetailsContentPage.PaymentInformationPage.getInstance().getBankAccountNumber());

        Assert.assertEquals("MA", DetailsContentPage.PaymentInformationPage.getInstance().getCardType());
        Assert.assertEquals(owsActions.fullName, DetailsContentPage.PaymentInformationPage.getInstance().getCreditCardHolderName());
        Assert.assertEquals("2030", DetailsContentPage.PaymentInformationPage.getInstance().getCardExpireYear());
        Assert.assertEquals("02", DetailsContentPage.PaymentInformationPage.getInstance().getCardExpireMonth());
        Assert.assertEquals("************5100", DetailsContentPage.PaymentInformationPage.getInstance().getCreditCardNumber());

        test.get().info("Step 21:  submit send DDI request job");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();

        test.get().info("Step 22: verify DDI status changed to Inactive");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("Inactive", DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus());

        test.get().info("Step 23: open service order details for send DDI to BACS item");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Send DDI to BACS");


        test.get().info("Step 24: verify service order status is send and DDI reference has value");
        Assert.assertEquals("Sent", TasksContentPage.TaskPage.DetailsPage.getInstance().getServiceOrderStatus());
        Assert.assertFalse(TasksContentPage.TaskPage.DetailsPage.getInstance().getDDIReference() == "");

        test.get().info("Step 25: Update customer DDI details in database");
        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        CommonActions.updateCustomerDDIDetailsInDatabase(Parser.parseDateFormate(newStartDate, "dd/MMM/YY"),
                detailsPage.getHierarchyMbr(), detailsPage.getDDIReference());

        test.get().info("Step 26: find customer then open the details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 27: verify DDI information is correct");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("Active", DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus());
        expectedDDI = DetailsContentPage.PaymentInformationPage.getInstance().getDDIReference().split(" ")[0];
        Assert.assertEquals(String.format("%s ( %s )", expectedDDI, Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT)), DetailsContentPage.PaymentInformationPage.getInstance().getDDIReference());

        test.get().info("Step 28 : submit the do refill BC Job");
        BaseTest.submitDoRefillBCJob();
        test.get().info("Step 29 : submit the do refill NC Job");
        BaseTest.submitDoRefillNCJob();
        test.get().info("Step 30 : submit bundle review Job");
        BaseTest.submitDoBundleRenewJob();

        test.get().info("Step 31 : submit the draft bill run");
        submitDraftBillRun();
        test.get().info("Step 32 : submit the confirm bill run");
        submitConfirmBillRun();

        test.get().info("Step 33 : Open the invoice details screen");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);

        test.get().info("Step 34 : verify invoice details are correct");
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getIssued());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT), InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getEnd());
        Assert.assertEquals(Parser.parseDateFormate(paymentCollectionDateEscapeNonWorkDay(10),TimeStamp.DATE_FORMAT),  InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getDueDate());
        Assert.assertEquals("Confirmed", InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getStatus(1));

        test.get().info("Step 35 : verify invoice pdf");
        verifyPDFFile();

        test.get().info("Step 36 : update invoice due date");
        BillingActions.updateInvoiceDueDate(customerNumber, TimeStamp.Today());

        test.get().info("Step 37 : verify invoice due date is updated successfully");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getDueDate());

        test.get().info("Step 38 : submit payment allocation batch job ");
        RemoteJobHelper.getInstance().submitPaymentAllocationBatchJobRun();

        test.get().info("Step 39 : verify payment allocated to invoice");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);
        HashMap<String, String> allocationEnity = AllocationEnity.dataAllocationEnity(TimeStamp.TodayMinus20Days(), "Online Payment", "£15.00");
        Assert.assertEquals(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getToTalRowInAllocationTable(), 1);
        Assert.assertEquals(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().findRowByEnity(allocationEnity), 1);

        test.get().info("Step 40 : Submit send DDI request Job");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();

        test.get().info("Step 41 : refresh hub net");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 42 : verify invoice status is direct debit pending");
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), InvoicesContentPage.getInstance().getDateIssuedByIndex(1));
        Assert.assertEquals("Direct Debit Pending", InvoicesContentPage.getInstance().getStatusByIndex(1));

        test.get().info("Step 43 : update collection date of latest ddbatch to day");
        BillingActions.updateCollectionDateOfLatestDDBatchToToday();

        test.get().info("Step 44 : run direct debit batch job to create payments");
        RemoteJobHelper.getInstance().submitRunDirectDebitBatchJobToCreatePayment();

        test.get().info("Step 45 : refresh hub net");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 46 : verify invoice status is fully paid");
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), InvoicesContentPage.getInstance().getDateIssuedByIndex(1));
        Assert.assertEquals("Fully Paid (pending bank confirmation)", InvoicesContentPage.getInstance().getStatusByIndex(1));

        test.get().info("Step 47 : verify the details of the invoice");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);

        Assert.assertEquals(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getToTalRowInAllocationTable(), 2);
        allocationEnity = AllocationEnity.dataAllocationEnity(TimeStamp.TodayMinus20Days(), "Online Payment", "£15.00");
        Assert.assertEquals(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().findRowByEnity(allocationEnity), 1);
        allocationEnity = AllocationEnity.dataAllocationEnity(TimeStamp.Today(), "Direct Debit Payment", "£15.00");
        Assert.assertEquals(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().findRowByEnity(allocationEnity), 1);

    }

    public void verifyPDFFile() {
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().clickViewPDFBtn();
        String fileName = String.format("%s_%s_%s.pdf", "32816", RandomCharacter.getRandomNumericString(9), customerNumber);
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().savePDFFile(fileName);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);
        String userCharges = String.format("User charges for %s  Mobile 1 (£10 Tariff 12 Month Contract)", sub);

        String monthlySubscription1 = String.format("Monthly subscription %s %s 10.00", Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF));
        String monthlySubscription2 = String.format("Monthly subscription %s %s 10.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF));

        String adjmtChargesAndCredits1 = String.format("%s %s Nokia 2720 for %s 0.00", Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF), sub);
        String adjmtChargesAndCredits2 = String.format("%s %s Monthly 500MB data allowance for %s 5.00", Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), sub);
        String adjmtChargesAndCredits3 = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), sub);
        String adjmtChargesAndCredits4 = String.format("%s %s Monthly 500MB data allowance for %s 5.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), sub);
        String adjmtChargesAndCredits5 = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), sub);


        Assert.assertEquals(pdfList.get(43), userCharges);

        Assert.assertEquals(pdfList.get(46), monthlySubscription1);
        Assert.assertEquals(pdfList.get(47), monthlySubscription2);

        Assert.assertEquals(pdfList.get(52), adjmtChargesAndCredits1);
        Assert.assertEquals(pdfList.get(53), adjmtChargesAndCredits2);
        Assert.assertEquals(pdfList.get(54), adjmtChargesAndCredits3);
        Assert.assertEquals(pdfList.get(55), adjmtChargesAndCredits4);
        Assert.assertEquals(pdfList.get(56), adjmtChargesAndCredits5);

    }


}




