package suite.regression;

import javafx.util.Pair;
import logic.business.db.billing.BillingActions;
import logic.business.ws.ows.OWSActions;
import logic.business.entities.DiscountBundle;
import logic.business.helper.RemoteJobHelper;
import logic.pages.care.*;
import logic.pages.care.find.*;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TaskContentPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.TestBase;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DemoTest extends BaseTest {

    @Test(description = "TC29701 Deactivate Account with contracted subscription within 28 days with a delay return and immediate refund", groups = "Care", dataProvider = "serviceOrderData")
    public void TC29701_Deactivate_Account_with_contracted_subscription_within_28_days_with_a_delay_return_and_immediate_refund(List<HashMap<String, String>> hashMapList) {
        String requestPath = "C:\\GIT\\TM\\hub_testauto\\src\\test\\resources\\xml\\TC29699_createOrder.xml";
        test.get().info("Step 1 : Create a customer with NC and device");
        OWSActions owsActions = new OWSActions();
        owsActions.createOrderAndSignAgreementByUI();

        test.get().info("Step 2 : Create New Billing Group");
        TestBase.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        TestBase.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        TestBase.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus15Days();
        TestBase.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();

        //Deactivate Account With A Delay Return And Immediate Refund
        test.get().info("Step 6 : Deactivate Account With A Delay Return And Immediate Refund");
        ServiceOrdersPage serviceOrdersPage = new ServiceOrdersPage();
        ServiceOrdersPage.DeactivateSubscriptionPage deactivateSubscriptionPage = serviceOrdersPage.new DeactivateSubscriptionPage();
        deactivateSubscriptionPage.deactivateAccountWithADelayReturnAndImmediateRefund();

        //Verify Deactivate Account So Is In Provision Wait
        test.get().info("Step 7 : Verify Deactivate Account So Is In Provision Wait");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrder(hashMapList);
        Assert.assertEquals(1, serviceOrder.size(), "The service order is not exist in table");
        int serviceOrderId = ServiceOrdersContentPage.getServiceOrderId(serviceOrder);

        TestBase.updateThePdateAndBillDateForSO(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        //Verify Deactivate Account So Is Completed
        test.get().info("Step 8 : Verify Deactivate Account So Is Completed");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrder(hashMapList);
        Assert.assertEquals(1, serviceOrder.size(), "The service order is not exist in table");

        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrder(hashMapList);
        Assert.assertEquals(1, serviceOrder.size(), "The service order is not exist in table");
        serviceOrderId = ServiceOrdersContentPage.getServiceOrderId(serviceOrder);

        //Run refill job
        test.get().info("Step 9 : Run refill job");
        RemoteJobHelper.getInstance().submitDoRefillBcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoRefillNcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(TimeStamp.Today());

        //update customer end date
        test.get().info("Step 10 : Update customer end date");
        Date endDate = TimeStamp.TodayMinus2Days();
        TestBase.updateCustomerEndDate(customerNumber, endDate);

        //Verify Customer End Date Updated Successfully
        test.get().info("Step 11 : Verify Customer End Date Updated Successfully");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), SummaryContentsPage.getInstance().getSummaryEndDate());
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), SummaryContentsPage.getInstance().getValueInSubscriptionsTable(2, "End Date"));

        MenuPage.LeftMenuPage.getInstance().clickOtheChargesCreditsItem();
        Assert.assertEquals(2, OtherChargesCreditsContent.getInstance().getRowNumberOfOtherChargesCreditsContentTable());
        Assert.assertEquals(2, OtherChargesCreditsContent.getInstance().getCharngeCredits(getOtherChargeCredits(endDate)).size());

        //Verify Customer End Date And Subscription End Date Changed Successfully
        test.get().info("Step 12 : Verify Customer End Date And Subscription End Date Changed Successfully");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), SummaryContentsPage.getInstance().getSummaryEndDate());

        Assert.assertEquals(1, SummaryContentsPage.getInstance().getRownumberOfSubscriptionsTable());
        Assert.assertEquals(1, SummaryContentsPage.getInstance().getSubscriptions(getSummarySubscriptions(newStartDate, endDate)).size());

        //Verify NC discount bundles are removed in HUB
        test.get().info("Step 13 : Verify NC discount bundles are removed in HUB");
        SummaryContentsPage.getInstance().clickSubscriptionNumberLinkByIndex(2);
        Assert.assertEquals(3, SubscriptionDetailsContentPage.OtherProductsGridClass.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(3, SubscriptionDetailsContentPage.OtherProductsGridClass.getInstance().getSubscriptions(getOtherProduct(newStartDate, endDate)).size());
        String discountGroupCodeOfMobileRef1 = SubscriptionDetailsContentPage.General.getInstance().getDiscountGroupCode();
        String subscriptionNumber = SubscriptionDetailsContentPage.General.getInstance().getSubscriptionNumber();

        //Verify current and future discount bundle entries are marked as Deleted
        test.get().info("Step 14 : Verify current and future discount bundle entries are marked as Deleted");
        List<DiscountBundle> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        int countStatus = BillingActions.getInstance().countStatusOfDiscountBundles(discountBundles, "DELETED");
        Assert.assertEquals(9, discountBundles.size(), "Expected : 9 But Actual : " + discountBundles.size());
        Assert.assertEquals(9, countStatus, "Expected : 9 But Actual : " + countStatus);

        //Verify NC discount bundles are removed with O2
        test.get().info("Step 15 : Verify NC discount bundles are removed with O2");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);

        Pair<String, String> event = new Pair<>("Description", "PPB: SetSubscriberRatePlan: Request completed");
        Assert.assertEquals(1, TaskContentPage.EventsGridClass.getInstance().getEvents(event).size());
        event = new Pair<>("Description", "O2SOA: getAccountSummary: Request completed");
        Assert.assertEquals(1, TaskContentPage.EventsGridClass.getInstance().getEvents(event).size());
        event = new Pair<>("Description", "PPB: DeleteSubscription: Request completed");
        Assert.assertEquals(7, TaskContentPage.EventsGridClass.getInstance().getEvents(event).size());
        event = new Pair<>("Description", "Agreement AGRTESCOMOBILE cancelled");
        Assert.assertEquals(1, TaskContentPage.EventsGridClass.getInstance().getEvents(event).size());

        MenuPage.getInstance().clickCustomersTab();
        FindPage.getInstance().findCustomer(new Pair<String, String>("Customer Number", customerNumber));
        FindPage.getInstance().openCustomerByIndex(1);

        //Submit draft bill run
        test.get().info("Step 16 : Submit draft bill run");
        RemoteJobHelper.getInstance().submitDraftBillRun();

        //Submit confirm bill run
        test.get().info("Step 17 : Submit confirm bill run");
        RemoteJobHelper.getInstance().submitConfirmBillRun();

        //Verify an invoice was created
        test.get().info("Step 18 : Verify an invoice was created");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        Assert.assertEquals(1, InvoicesContentPage.getInstance().getRowNumberOfInvoiceTable());
        String invoiceNumber = InvoicesContentPage.getInstance().getInvoiceNumber();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(2);

        //Verify the generated invoice is a FINAL invoice
        test.get().info("Step 19 : Verify the generated invoice is a FINAL invoice");
        Assert.assertEquals("FINAL", BillingActions.getInstance().getInvoiceTypeByInvoiceNumber(invoiceNumber));

        //Verify the adjustment and miscellaneous are charged on Invoice PDF
        test.get().info("Step 20 : Verify the adjustment and miscellaneous are charged on Invoice PDF");
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().saveFileFromWebRequest(customerNumber);
        List<String> listInvoiceContent = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent();
        Assert.assertTrue(listInvoiceContent.contains(String.format("%s %s Credit Transfer from Prepay for Deactivation Refund -25.00", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF),Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF))));
        Assert.assertTrue(listInvoiceContent.contains(String.format("(%s  Mobile Ref 1)", subscriptionNumber)));
        Assert.assertTrue(listInvoiceContent.contains(String.format("%s %s Customer Care refund issued for %s 132.50", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF), subscriptionNumber)));
        Assert.assertTrue(listInvoiceContent.contains(String.format("%s %s Usage 28 days disconnection adjustment for %s 10.00",Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), subscriptionNumber)));
        Assert.assertTrue(listInvoiceContent.contains("Total Payments -132.50"));
    }


    @DataProvider(name = "serviceOrderData")
    private Object[][] getServiceOrderData() {
        List<HashMap<String, String>> hashMapList = new ArrayList<>();
        HashMap<String, String> so1 = new HashMap<String, String>();
        so1.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so1.put("Type", "Deactivate Account");
        so1.put("Status", "Provision Wait");

        HashMap<String, String> so2 = new HashMap<String, String>();
        so2.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so2.put("Type", "Deactivate Account");
        so2.put("Status", "Completed Task");

        HashMap<String, String> so3 = new HashMap<String, String>();
        so3.put("Date", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        so3.put("Type", "Deactivate Subscription Task");
        so3.put("Status", "Completed Task");

        hashMapList.add(so1);
        hashMapList.add(so2);
        hashMapList.add(so3);

        return new Object[][]{{hashMapList}};
    }


    private List<HashMap<String, String>> getOtherChargeCredits(Date endDate) {
        List<HashMap<String, String>> hashMapList = new ArrayList<>();
        HashMap<String, String> charngeCredit1 = new HashMap<String, String>();
        charngeCredit1.put("Start Date", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT));
        charngeCredit1.put("End Date", Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT));

        hashMapList.add(charngeCredit1);
        hashMapList.add(charngeCredit1);

        return hashMapList;
    }

    private List<HashMap<String, String>> getSummarySubscriptions(Date startDate, Date endDate) {
        List<HashMap<String, String>> hashMapList = new ArrayList<>();
        HashMap<String, String> summarySubscriptions = new HashMap<String, String>();
        summarySubscriptions.put("Start Date", Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT));
        summarySubscriptions.put("End Date", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT));
        summarySubscriptions.put("Status", "Inactive");

        hashMapList.add(summarySubscriptions);
        return hashMapList;
    }

    private List<HashMap<String, String>> getOtherProduct(Date startDate, Date endDate) {
        List<HashMap<String, String>> hashMapList = new ArrayList<>();
        HashMap<String, String> otherProduct1 = new HashMap<String, String>();
        otherProduct1.put("Start Date", Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT));
        otherProduct1.put("End Date", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT));

        hashMapList.add(otherProduct1);
        hashMapList.add(otherProduct1);
        hashMapList.add(otherProduct1);

        return hashMapList;
    }


}
