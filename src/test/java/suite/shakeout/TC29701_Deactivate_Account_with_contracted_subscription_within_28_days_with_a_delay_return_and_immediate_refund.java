package suite.shakeout;

import javafx.util.Pair;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.*;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import java.sql.Date;
import java.util.List;

public class TC29701_Deactivate_Account_with_contracted_subscription_within_28_days_with_a_delay_return_and_immediate_refund extends BaseTest  {

    @Test(enabled = true, description = "TC29701 Deactivate Account with contracted subscription within 28 days with a delay return and immediate refund", groups = "Smoke")
    public void TC29701_Deactivate_Account_with_contracted_subscription_within_28_days_with_a_delay_return_and_immediate_refund() {
        test.get().info("Step 1 : Create a customer with NC and device");
        OWSActions owsActions = new OWSActions();
        String TC29699_CREATE_ORDER = "src\\test\\resources\\xml\\ows\\TC29699_createOrder.xml";
        owsActions.createOrderAndSignAgreementByUI(TC29699_CREATE_ORDER, 1);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();

        test.get().info("Step 6 : Deactivate Account With A Delay Return And Immediate Refund");
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateAccountWithADelayReturnAndImmediateRefund();
        
        test.get().info("Step 7 : Verify Deactivate Account So Is In Provision Wait");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderDataForDeactivateAccount().get(0));
        Assert.assertEquals(1, serviceOrder.size(), "The service order is not exist in table");
        String serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);

        BaseTest.updateThePDateAndBillDateForSO(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 8 : Verify Deactivate Account So Is Completed");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderDataForDeactivateAccount().get(1));
        Assert.assertEquals(1, serviceOrder.size(), "The service order is not exist in table");

        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderDataForDeactivateAccount().get(2));
        Assert.assertEquals(1, serviceOrder.size(), "The service order is not exist in table");
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);

        test.get().info("Step 9 : Run refill job");
        RemoteJobHelper.getInstance().submitDoRefillBcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoRefillNcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(TimeStamp.Today());

        test.get().info("Step 10 : Update customer end date");
        Date endDate = TimeStamp.TodayMinus2Days();
        CommonActions.updateCustomerEndDate(customerNumber, endDate);

        test.get().info("Step 11 : Verify Customer End Date Updated Successfully");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), CommonContentPage.CustomerSummarySectionPage.getInstance().getCustomerSummaryEndDate());
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), CommonContentPage.SubscriptionsGridSectionPage.getInstance().getValueOfSubscriptionsTable(1, "End Date"));

        MenuPage.LeftMenuPage.getInstance().clickOtherChargesCreditsItem();
        Assert.assertEquals(2, OtherChargesCreditsContent.getInstance().getRowNumberOfOtherChargesCreditsContentTable());
        Assert.assertEquals(2, OtherChargesCreditsContent.getInstance().getChargeCredits(OtherChargeCreditsEntity.dataForOtherChargeCredits(endDate)).size());

        test.get().info("Step 12 : Verify Customer End Date And Subscription End Date Changed Successfully");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), CommonContentPage.CustomerSummarySectionPage.getInstance().getCustomerSummaryEndDate());
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGridSectionPage.getInstance().getRowNumberOfSubscriptionsTable());
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptions(SubscriptionEntity.dataForSummarySubscriptions(newStartDate, endDate)).size());

        test.get().info("Step 13 : Verify NC discount bundles are removed in HUB");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getOtherProducts(OtherProductEntiy.dataForOtherProduct(newStartDate, endDate)).size());
        String discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        String subscriptionNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();

        test.get().info("Step 14 : Verify current and future discount bundle entries are marked as Deleted");
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        int countStatus = BillingActions.getInstance().countStatusOfDiscountBundles(discountBundles, "DELETED");
        Assert.assertEquals(9, discountBundles.size(), "Expected : 9 But Actual : " + discountBundles.size());
        Assert.assertEquals(9, countStatus, "Expected : 9 But Actual : " + countStatus);

        test.get().info("Step 15 : Verify NC discount bundles are removed with O2");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);

        Pair<String,String> event = EventEntity.setEvents("Description", "PPB: SetSubscriberRatePlan: Request completed");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getEvents(event).size());
        event = EventEntity.setEvents("Description", "O2SOA: getAccountSummary: Request completed");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getEvents(event).size());
        event = EventEntity.setEvents("Description", "PPB: DeleteSubscription: Request completed");
        Assert.assertEquals(7, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getEvents(event).size());
        event = EventEntity.setEvents("Description", "Agreement AGRTESCOMOBILE cancelled");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getEvents(event).size());
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Step 16 : Submit draft bill run");
        RemoteJobHelper.getInstance().submitDraftBillRun();

        test.get().info("Step 17 : Submit confirm bill run");
        RemoteJobHelper.getInstance().submitConfirmBillRun();

        test.get().info("Step 18 : Verify an invoice was created");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        Assert.assertEquals(1, InvoicesContentPage.getInstance().getRowNumberOfInvoiceTable());
        String invoiceNumber = InvoicesContentPage.getInstance().getInvoiceNumber();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);

        test.get().info("Step 19 : Verify the generated invoice is a FINAL invoice");
        Assert.assertEquals("FINAL", BillingActions.getInstance().getInvoiceTypeByInvoiceNumber(invoiceNumber));

        test.get().info("Step 20 : Verify the adjustment and miscellaneous are charged on Invoice PDF");
        String downloadedPDFFile = BaseTest.getDownloadInvoicePDFFile(customerNumber);
        List<String> listInvoiceContent = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile,1);
        Assert.assertTrue(listInvoiceContent.contains(String.format("%s %s Credit Transfer from Prepay for Deactivation Refund -25.00", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF))));
        Assert.assertTrue(listInvoiceContent.contains(String.format("(%s  Mobile Ref 1)", subscriptionNumber)));
        Assert.assertTrue(listInvoiceContent.contains(String.format("%s %s Customer Care refund issued for %s 132.50", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF), subscriptionNumber)));
        Assert.assertTrue(listInvoiceContent.contains(String.format("%s %s Usage 28 days disconnection adjustment for %s 10.00", Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), subscriptionNumber)));
        Assert.assertTrue(listInvoiceContent.contains("Total Payments -132.50"));
    }
}
