package suite.shakeout;

import framework.utils.Pdf;
import framework.utils.Xml;
import javafx.util.Pair;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.*;
import logic.business.ws.ows.OWSActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.*;
import logic.pages.care.find.*;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.AddOrChangeAFamilyPerkPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.pages.selfcare.OrderConfirmationPage;
import suite.regression.selfcare.SelfCareTestBase;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ShakeOutTest extends BaseTest {

    @Test(enabled = true, description = "TC29701 Deactivate Account with contracted subscription within 28 days with a delay return and immediate refund", groups = "Smoke")
    public void TC29701_Deactivate_Account_with_contracted_subscription_within_28_days_with_a_delay_return_and_immediate_refund() {
        test.get().info("Step 1 : Create a customer with NC and device");
        OWSActions owsActions = new OWSActions();
        owsActions.createOrderAndSignAgreementByUI();

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

        //Deactivate Account With A Delay Return And Immediate Refund
        test.get().info("Step 6 : Deactivate Account With A Delay Return And Immediate Refund");
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateAccountWithADelayReturnAndImmediateRefund();

        //Verify Deactivate Account So Is In Provision Wait
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
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getValueOfSubscriptionsTable(1, "End Date"));

        MenuPage.LeftMenuPage.getInstance().clickOtherChargesCreditsItem();
        Assert.assertEquals(2, OtherChargesCreditsContent.getInstance().getRowNumberOfOtherChargesCreditsContentTable());
        Assert.assertEquals(2, OtherChargesCreditsContent.getInstance().getChargeCredits(OtherChargeCreditsEntity.dataForOtherChargeCredits(endDate)).size());

        test.get().info("Step 12 : Verify Customer End Date And Subscription End Date Changed Successfully");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        Assert.assertEquals(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT), CommonContentPage.CustomerSummarySectionPage.getInstance().getCustomerSummaryEndDate());
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getRowNumberOfSubscriptionsTable());
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptions(SubscriptionEntity.dataForSummarySubscriptions(newStartDate, endDate)).size());

        test.get().info("Step 13 : Verify NC discount bundles are removed in HUB");
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
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

    @Test(enabled = true, description = "TC32533 Maintain Bundle Change FP Add Bundle Service Feature Off On", groups = "Smoke")
    public void TC32533_Maintain_Bundle_Change_FP_Add_Bundle_Service_Feature_Off_On() {
        test.get().info("Step 1 : Create Order Having Family Perk Bundle");
        OWSActions owsActions = new OWSActions();
        owsActions.createOrderHavingFamilyPerkBundle();

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 6 : Verify customer start date and billing group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 7 : Verify all discount bundle entries align with bill run calendar entires");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        String serviceRefOf2stSubscription = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");
        String discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();

        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(11, discountBundles.size());
        verifyFCDiscountBundles(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");
        verifySpecificNCDiscountBundles(discountBundles, "TD250");

        test.get().info("Step 8 : Back to subscription content page");
        MenuPage.BreadCrumbPage.getInstance().clickParentLink();

        test.get().info("Step 9 : Verify subscription 1 service feature is none");
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");
        Assert.assertEquals("None", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());

        test.get().info("Step 10 : Verify subscription 2 service feature is none");
        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf2stSubscription + " Mobile Ref 2");
        Assert.assertEquals("None", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());

        test.get().info("Step 11 : Submit maintain bundle request to Selfcare WS");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitMaintainBundleRequest(customerNumber, serviceRefOf1stSubscription);

        test.get().info("Step 12 : Verify normal maintain bundle response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifyNormalMaintainBundleResponse(response);

        test.get().info("Step 13 : Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 14 : Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 15 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        selfCareWSTestBase.verifyNormalMaintainBundleResponse(response);

        test.get().info("Step 16 : Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 17 : Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 18 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderChangeBundle());
        String serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        String serviceSubscription = ServiceOrdersContentPage.getInstance().getSubscriptionNumber(serviceOrder);

        test.get().info("Step 19 : Verify a new service order created for customer");
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderProvisionWait(serviceOrderId, serviceSubscription));
        Assert.assertEquals(1, serviceOrder.size());

        test.get().info("Step 20 : Click service order id to open details");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);

        test.get().info("Step 21 : Verify change bundle so details are correct");
        verifyChangeBundleSoDetails(serviceSubscription);
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("Change Bundle", TasksContentPage.TaskSummarySectionPage.getInstance().getDescription());
        Assert.assertEquals("Provision Wait", TasksContentPage.TaskSummarySectionPage.getInstance().getStatus());

        serviceOrderId = TasksContentPage.TaskSummarySectionPage.getInstance().getSoID();
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundle()));

        test.get().info("Step 22 : Update Provision Date Of Change Bundle Service Order");
        CommonActions.updateProvisionDateOfChangeBundleServiceOrder(serviceOrderId);

        test.get().info("Step 23 : Run Provision Services Job");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 24 : Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 25 : Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 26 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderChangeBundle());
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        serviceSubscription = ServiceOrdersContentPage.getInstance().getSubscriptionNumber(serviceOrder);

        test.get().info("Step 25 : Verify a new service order created for customer");
        Assert.assertEquals(1, ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(ServiceOrderEntity.dataServiceOrderCompletedTask(serviceOrderId, serviceSubscription)));
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);

        test.get().info("Step 26 : Verify change bundle so details are correct and 4 events generated after submit provision services job");
        verifyChangeBundleSoDetails(serviceSubscription);
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals(8, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());

        Pair<String, String> event = EventEntity.setEvents("Description", "Service Order created");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "PPB: AddSubscription: Request completed");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "PPB: DeleteSubscription: Request completed");
        Assert.assertEquals(2, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "O2SOA: getAccountSummary: Request completed");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "Service Feature 4G Service is added");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));

        test.get().info("Step 27 : Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 28 : Verify subscription 1 service feature is turned on");
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");
        Assert.assertEquals("4G Service=ON", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());

        test.get().info("Step 29 : Verify the old FP bundle is removed and new FP bundle is added");
        Assert.assertEquals(4, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(2, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProducts(OtherProductEntiy.dataForOtherProductOldFPBundle(newStartDate)));

        discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(13, discountBundles.size());

        verifySpecificNCDiscountBundles(discountBundles, "TD250");
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", newStartDate, TimeStamp.TodayPlus1MonthMinus1Day(), "TD250", "DELETED"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "TMDAT", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "TMDAT", "ACTIVE"));

        test.get().info("Step 30 : Verify 2nd subscription service feature is none");
        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf2stSubscription + " Mobile Ref 2");
        Assert.assertEquals("None", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());
    }

    @Test(enabled = true, description = "TC30432_Transfer_amount_to_Credit_Agreement_subscription_deactivated_within_Trial_Period", groups = "Smoke")
    public void TC30432_Transfer_amount_to_Credit_Agreement_subscription_deactivated_within_Trial_Period(){
        test.get().info("Step 1 : Create a customer with FC and device");
        OWSActions owsActions = new OWSActions();
        owsActions.createCustomerWithFCAndDevice();

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 6 : Get subscription number and agreement number");
        String subNo1 = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        String subNo2 = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");
        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();
        CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().clickExpandButtonOfCABySubscription(subNo1);
        String agreementNo1 =  CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(subNo1).agreementNumber();
        CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().clickExpandButtonOfCABySubscription(subNo2);
        String agreementNo2 =  CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(subNo2).agreementNumber();

        test.get().info("Step 7 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateSubscription();

        test.get().info("Step 8 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getStatusValue(subNo2));

        test.get().info("Step 9 : Take a payment to master subscription");
        MenuPage.RightMenuPage.getInstance().clickApplyFinancialTransactionLink();
        ServiceOrdersPage.AccountSummaryAndSelectAction.getInstance().selectChooseAction();
        ServiceOrdersPage.InputPaymentDetails.getInstance().inputPaymentDetail("120", "Regression Automation");
        ServiceOrdersPage.TransferExistingFunds.getInstance().inputAmountAgainstSubscription("120");
        HashMap<String,String> transfer = ServiceOrdersPage.ConfirmFundsTransfer.getInstance().getConfirmFundsTransfer();
        Assert.assertEquals("£120.00", transfer.get("PaymentAmount"));
        Assert.assertEquals("Regression Automation", transfer.get("Notes"));
        Assert.assertEquals("£0.00", transfer.get("OverdueInvoices"));
        Assert.assertEquals("£0.00", transfer.get("TopUps"));
        Assert.assertEquals("£120.00", transfer.get("CreditAgreement"));

        Assert.assertEquals("Maestro", transfer.get("CardType"));
        Assert.assertEquals("************5100", transfer.get("CardNumber"));
        Assert.assertEquals(owsActions.fullName, transfer.get("CardHolderName"));
        Assert.assertEquals("1", transfer.get("CardExpiryMonth"));
        Assert.assertEquals("2030", transfer.get("CardExpiryYear"));

        HashMap<String,String> wizard = ServiceOrdersPage.FundsTransferResults.getInstance().getFundsTransferResults();
        Assert.assertEquals("£120.00", wizard.get("PaymentAmount"));
        Assert.assertEquals("Regression Automation", wizard.get("Notes"));
        Assert.assertEquals("£0.00", wizard.get("OverdueInvoices"));
        Assert.assertEquals("1", wizard.get("AmountAllocatedToTopupsGrid"));
        Assert.assertEquals("1", wizard.get("AmountAllocatedToCreditAgreementsGrid"));
        Assert.assertEquals("£0.00", wizard.get("AmountAllocatedAsUnallocatePayment"));

        Assert.assertEquals("Maestro", wizard.get("CardType"));
        Assert.assertEquals("************5100", wizard.get("CardNumber"));
        Assert.assertEquals(owsActions.fullName, wizard.get("CardHolderName"));
        Assert.assertEquals("1", wizard.get("CardExpiryMonth"));
        Assert.assertEquals("2030", wizard.get("CardExpiryYear"));
        Assert.assertEquals("APPROVE", wizard.get("ReDSStatusAuthorisation"));

        test.get().info("Step 10 : Verify A Service Order Of Apply FT Created");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderApplyFinancialTransaction());
        Assert.assertEquals(1, serviceOrder.size());

        String serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderId);
        Assert.assertEquals("120", TasksContentPage.TaskPage.DetailsPage.getInstance().getPaymentAmount());
        Assert.assertEquals("Approved", TasksContentPage.TaskPage.DetailsPage.getInstance().getReDSStatusAuthorisation());
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Step 11 : Verify An Other Credit Created");
        MenuPage.LeftMenuPage.getInstance().clickOtherChargesCreditsItem();
        int rowOfChargeCredit =  OtherChargesCreditsContent.getInstance().getNumberOfChargeCredits(OtherChargeCreditsEntity.getOCCForAgreementAdjustmentProducts(subNo1, serviceOrderId));
        Assert.assertEquals(1, rowOfChargeCredit);

        test.get().info("Step 12 : Verify The Credit Exists In Live Bill Estimate");
        MenuPage.LeftMenuPage.getInstance().clickLiveBillEstimateItem();
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription billEstimatePerSubscription = new LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(subNo1 + "  Mobile Ref 1");
        billEstimatePerSubscription.expand();

        //subscription 1 should be active and it has payment and device in Adjustments, Charges and Credits
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription.AdjustmentsChargesAndCredits adjustmentsChargesAndCredits =  billEstimatePerSubscription.new AdjustmentsChargesAndCredits();
        adjustmentsChargesAndCredits.expand();
        int sizeOfAdjustment =  adjustmentsChargesAndCredits.getNumberOfServiceOrders(AdjustmentsChargesAndCreditsEntity.getTransferPaymentAdjustmentProducts(TimeStamp.TodayMinus20Days()));
        Assert.assertEquals(2, sizeOfAdjustment);

        //subscription 2 is inactive and it has only device in Adjustments, Charges and Credits
        billEstimatePerSubscription = new LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(subNo2 + "  Mobile Ref 2");
        billEstimatePerSubscription.expand();

        adjustmentsChargesAndCredits =  billEstimatePerSubscription.new AdjustmentsChargesAndCredits();
        adjustmentsChargesAndCredits.expand();
        sizeOfAdjustment =  adjustmentsChargesAndCredits.getNumberOfServiceOrders(AdjustmentsChargesAndCreditsEntity.getTransferPaymentAdjustmentProducts(TimeStamp.TodayMinus20Days()));
        Assert.assertEquals(1, sizeOfAdjustment);

        test.get().info("Step 13 : Veirfy the credit in CA");
        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();
        CreditAgreementsContentPage.CreditAgreementsGridPage.CADetailClass.getInstance().clickExpandButtonOfCABySubscription(subNo1);
        Assert.assertEquals("£120.00", CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(subNo1).otherPayments());
        Assert.assertEquals(1, CreditAgreementsContentPage.CreditAgreementPaymentsGrid.getInstance().getNumberOfCreditAgreementPayments(CreditAgreementPaymentsEntiy.getCreditAgreementPaymentWithAdHoc(subNo1)));

    }

    @Test(enabled = true, description = "TC34669 A New Customer New Order Multiple deals MAX number of deals with CCA", groups = "Smoke")
    public void TC34669_A_New_Customer_New_Order_Multiple_deals_MAX_number_of_deals_with_CCA() {
        test.get().info("Step 1 : Create a customer with FC and device");
        OWSActions owsActions = new OWSActions();
        owsActions.submitAddNewTariffRequest();

        test.get().info("Step 2 : Verify Agreement Is Signed In DB");
        String orDerId = owsActions.orderIdNo;
        Assert.assertEquals(4, CommonActions.getAgreementIsSigned(orDerId));

        test.get().info("Step 3 : Load customer in hub net");
        String customerNumber = owsActions.customerNo;
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 4 : Verify 4 Subscriptions And 4 Credit Agreements");
        String [] ccaArr = Verify4SubscriptionsAnd4CreditAgreements(owsActions, orDerId);

        test.get().info("Step 5 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 6 : Open service orders content for customer");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Sales Order");
        Pair<String, String> event = EventEntity.setEvents("Description", "MPNs Assigned");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "Terms And Conditions Requested for CA");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "Agreement Accepted");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));

        Assert.assertEquals("New Order", TasksContentPage.TaskPage.DetailsPage.getInstance().getOrderType());
        Assert.assertEquals("Completed Task", TasksContentPage.TaskPage.DetailsPage.getInstance().getTransactionStatus());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getReceiptDate());

        test.get().info("Step 7 : Back to customer");
        CareTestBase.page().reLoadCustomerInHubNet( customerNumber);

        test.get().info("Step 8 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 9 : Verify order task details");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Order Task");
        Assert.assertEquals("Order Task", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getDescription());
        Assert.assertEquals("Completed Task", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals(6, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());

        event = EventEntity.setEvents("Description", String.format("Refill subscription has been requested. (Order id: %s)", orDerId));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", String.format("Activate subscription has been requested. (Order id: %s)", orDerId));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", String.format("Inventory successfully created in HUB. Inventory created for orderId: %s. (Order id: %s)", orDerId,orDerId));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        Assert.assertTrue(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(2).contains("Products Released. Confirm Response: Supplier [Hansen HUB]") || TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(3).contains("Products Released. Confirm Response: Supplier [Hansen HUB]"));
        Assert.assertTrue( TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(2).contains("Products Released. Confirm Response: Supplier [Oakwood]") || TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(3).contains("Products Released. Confirm Response: Supplier [Oakwood]"));
        Assert.assertTrue( TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(5).contains("Receipt Created. Receipt(s) created:"));

        test.get().info("Step 10 : Login to self care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 11 : Verify new agreement details in self care");
        MyPersonalInformationPage.MyPreviousOrdersPage.getInstance().clickViewByIndex(1);

        String [] productSummary = SelfCareProductSummaryEntity.setProductSummary("1 x","Credit Agreement","£16.00");
        Assert.assertEquals(4, OrderConfirmationPage.ProductSummary.getInstance().getNumberOfProductSummary(productSummary));
        productSummary = SelfCareProductSummaryEntity.setProductSummary("","-  HTC Desire HD","£99.00");
        Assert.assertEquals(4, OrderConfirmationPage.ProductSummary.getInstance().getNumberOfProductSummary(productSummary));
        productSummary = SelfCareProductSummaryEntity.setProductSummary("4 x","Insurance Band A £4/month (collected by separate payment)","£0.00");
        Assert.assertEquals(1, OrderConfirmationPage.ProductSummary.getInstance().getNumberOfProductSummary(productSummary));

        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().countRowOfDetailItemsByFirstRow("Mobile 1"));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().countRowOfDetailItemsByFirstRow("Mobile 3"));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().countRowOfDetailItemsByFirstRow("Mobile 4"));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().countRowOfDetailItemsByFirstRow("Mobile 5"));

        Assert.assertEquals(4, OrderConfirmationPage.OrderDetails.getInstance().getItemsCountByRowText("Credit Agreement"));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().getItemsCountByRowText(ccaArr[0]));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().getItemsCountByRowText(ccaArr[1]));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().getItemsCountByRowText(ccaArr[2]));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().getItemsCountByRowText(ccaArr[3]));


    }


    @Test(enabled = true, description = "TC1358 OWS Basic Path New Customer New Order via Care Inhand Master Card", groups = "Smoke")
    public void TC1358_OWS_Basic_Path_New_Customer_New_Order_via_Care_Inhand_Master_Card(){
        test.get().info("Step 1 : Create a CC cusotmer with via Care Inhand MasterCard");
        OWSActions owsActions = new OWSActions();
        Xml response =  owsActions.createACCCustomerWithViaCareInhandMasterCard();

        test.get().info("Step 2 : Verify_create_order_response");
        CareTestBase.page().verifyCreateOrderResponse(owsActions, response);

        test.get().info("Step 3 : Call get contract ws method");
        Xml getContractResponseXml =  owsActions.getContract(owsActions.orderIdNo);

        test.get().info("Step 4 : Verify get contract response");
        Assert.assertEquals(owsActions.orderRef, getContractResponseXml.getTextByTagName("orderRef"));

        test.get().info("Step 5 : Verify contract PDF file");
        verifyContractPDFFile(owsActions, getContractResponseXml.getTextByXpath("//contractDetails//contractContent"));

        test.get().info("Step 6 : Accept order for customer");
        response =  owsActions.acceptOrderForCustomer();

        test.get().info("Step 7 : verify accept order response");
        CareTestBase.page().verifyAcceptOrderResponse(owsActions, response);

        test.get().info("Step 8 : Load customer in hub net");
        String customerId = owsActions.customerNo;
        CareTestBase.page().loadCustomerInHubNet(customerId);

        test.get().info("Step 9 : Open subscriptions content for customer");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 10 : Verify HTC WILDFIRE XXX 60 exist in other product and subscription is same as assigned");
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        Assert.assertEquals(owsActions.subscriptionNumber, SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProducts(OtherProductEntiy.dataForOtherProductHTCWILDFIRE()));

        test.get().info("Step 11 : Verify the details of HTC WILDFIRE XXX 60");
        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().clickProductCode("HTC-WILDFIRE-XXX-60");
        Assert.assertTrue(Integer.parseInt(SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getHUbInternalId()) > 0);
        Assert.assertEquals("137695587771463", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getIMEI());
        Assert.assertEquals(owsActions.subscriptionNumber, SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getReference());
        Assert.assertEquals("60", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getInitialPurchasePrice());
        Assert.assertEquals("HTC", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getMake());
        Assert.assertEquals("", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getAgreementNumber());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getStartDate());
        Assert.assertTrue(SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getOrderReference().endsWith("-1_DEALINHAND"));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1YearMinus1Day(), TimeStamp.DATE_FORMAT), SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getWarrantyEndDate());
        Assert.assertEquals("1", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getQuantity());
        Assert.assertEquals("60", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getAmountPaid());
        Assert.assertEquals("Wildfire", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getModel());
        Assert.assertEquals("", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getUpgradeOrderReference());

        test.get().info("Step 12 : Verify customer card type");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("MasterCard", DetailsContentPage.PaymentInformationPage.getInstance().getCardType());

        test.get().info("Step 13 : Verify customer sales channel");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Sales Order");
        Assert.assertEquals("Telecomm Centre", TasksContentPage.TaskPage.DetailsPage.getInstance().getSalesChannel());
    }

    @Test(enabled = true, description = "TC29787 Add Permitted Bundle Apply at next bill date", groups = "Smoke")
    public void TC29787_Add_Permitted_Bundle_Apply_at_next_bill_date(){
        test.get().info("Step 1 : Create a CC cusotmer with via Care Inhand MasterCard");
        String path = "src\\test\\resources\\xml\\ows\\changebundle\\TC29787_createOrderRequest.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 6 : Verify customer start date and billing group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 7 : Get all subscriptions number");
        List<String> subList = getAllSubscriptionsNumber();

        test.get().info("Step 8 : Verify all discount bundle entries align with bill run calendar entires for FC");
        String fcSubText = Common.findValueOfStream(subList, "FC Mobile");
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(fcSubText);
        String discountGroupCodeOfFCMobile = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        String fcSubNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();

        List<DiscountBundleEntity> discountBundle = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfFCMobile);
        Assert.assertEquals(11, discountBundle.size());
        BaseTest.verifyNCDiscountBundles(discountBundle, newStartDate, "TMT5K");
        BaseTest.verifyNCDiscountBundles(discountBundle, newStartDate, "TM150");
        BaseTest.verifyNCDiscountBundles(discountBundle, newStartDate, "TM5HO");
        BaseTest.verifyFCDiscountBundles(discountBundle, newStartDate, "FLX01");

        test.get().info("Step 9 : Verify FC tariff and other products are correct");
        String tariffHeaderText = "Tariff Components (2 found) FC1-0750-150SO - £7.50 SIM Only Tariff 1 Month Contract";
        Assert.assertEquals(tariffHeaderText, SubscriptionContentPage.SubscriptionDetailsPage.TariffComponentsGridPage.getInstance().getHeaderText());
        Assert.assertEquals(2, SubscriptionContentPage.SubscriptionDetailsPage.TariffComponentsGridPage.getInstance().rowOfTariffComponents());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.TariffComponentsGridPage.getInstance().getTariffComponents(TariffComponentEntity.dataTMOBUSETariffComponentForFC(newStartDate)).size());

        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(OtherProductEntiy.dataBundlerForOtherProductForFC(newStartDate)));

        test.get().info("Step 10 : Verify all discount bundle entries align with bill run calendar entires for NC");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        String ncSubText = Common.findValueOfStream(subList, "NC Mobile");
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(ncSubText);
        String discountGroupCodeOfNCMobile = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        String ncSubNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();

        List<DiscountBundleEntity> discountBundleOfNC = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfNCMobile);
        Assert.assertEquals(9, discountBundleOfNC.size());
        BaseTest.verifyNCDiscountBundles(discountBundle, newStartDate, "TMT5K");
        BaseTest.verifyNCDiscountBundles(discountBundle, newStartDate, "TM250");
        BaseTest.verifyNCDiscountBundles(discountBundle, newStartDate, "TM5HO");

        test.get().info("Step 11 : Verify FC tariff and other products are correct");
        tariffHeaderText = "Tariff Components (2 found) NC1-1000-250 - £10 SIM Only Tariff";
        Assert.assertEquals(tariffHeaderText, SubscriptionContentPage.SubscriptionDetailsPage.TariffComponentsGridPage.getInstance().getHeaderText());
        Assert.assertEquals(2, SubscriptionContentPage.SubscriptionDetailsPage.TariffComponentsGridPage.getInstance().rowOfTariffComponents());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.TariffComponentsGridPage.getInstance().getTariffComponents(TariffComponentEntity.dataTMOBUSETariffComponentForNC(newStartDate)).size());

        Assert.assertEquals(2, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(OtherProductEntiy.dataBundlerForOtherProductForNC(newStartDate)));

        test.get().info("Step 12 : Select change bundle from RHS actions");
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();

        test.get().info("Step 13 : Select FC mobile to change permitted bundle");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(fcSubText, "Change Permitted Bundle");

        test.get().info("Step 14 : Verify FC mobile change bundle page displays");
        Assert.assertEquals(fcSubText, ServiceOrdersPage.ChangeBundle.getInstance().getSubscriptionNumber());
        String expectNextBillDate = String.format("%s (%s days from today)", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT_IN_PDF), TimeStamp.TodayPlus1MonthMinusToday());
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("£7.50 SIM Only Tariff 1 Month Contract", ServiceOrdersPage.ChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 150 mins, 5000 texts (FC)", ServiceOrdersPage.ChangeBundle.getInstance().getPackagedBundle());
        Assert.assertEquals("No Current Bundles", ServiceOrdersPage.ChangeBundle.getInstance().getInfo());
        Assert.assertEquals("Next Bill Date", ServiceOrdersPage.ChangeBundle.getInstance().getWhenToApplyChangeText());
        Assert.assertTrue(ServiceOrdersPage.ChangeBundle.getInstance().bundleExists(BundlesToSelectEntity.getFCBundleToSelect()));
        CareTestBase.page().checkBundleToolTip(BundlesToSelectEntity.getFCBundleToSelect());
        ServiceOrdersPage.ChangeBundle.getInstance().clickNextButton();

        test.get().info("Step 15 : Select 1 available bundles for FC and click next button");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName(BundlesToSelectEntity.getFCBundleToSelect(),"3G data - 1GB - £5.00 per Month (Recurring)");
        CareTestBase.page().clickNextButton();

        test.get().info("Step 16 : Verify FC mobile confirm change bundle is correct");
        Assert.assertEquals(fcSubText, ServiceOrdersPage.ConfirmChangeBundle.getInstance().getSubscriptionNumber());
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ConfirmChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("FC1-0750-150SO £7.50 SIM Only Tariff 1 Month Contract {£7.50}", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 150 mins, 5000 texts (FC)", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getPackagedBundle());

        Assert.assertEquals("No Current Recurring Bundles", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getInfoBefore());
        Assert.assertEquals("£0.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getTotalRecurringBundleChargeBefore());

        Assert.assertEquals("£5.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getTotalRecurringBundleChargeAfter());
        Assert.assertEquals(String.format("£5.00 per Month (Recurring).Valid from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy")), ServiceOrdersPage.ConfirmChangeBundle.getInstance().getBundleInfo("3G data   -   1GB:"));
        Assert.assertEquals("Increase of £5.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getRecurringBundlesChargeDifference());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"), ServiceOrdersPage.ConfirmChangeBundle.getInstance().getEffective());

        test.get().info("Step 17 : Click next button to service order complete screen");
        CareTestBase.page().clickNextButton();

        test.get().info("Step 18 : Verify service order complete screen has provision wait message");
        CareTestBase.page().verifyServiceOrderCompleteScreenHasProvisionWaitMessage();

        test.get().info("Step 19 : Click return to customer button on service order complete screen");
        CareTestBase.page().clickReturnToCustomer();

        test.get().info("Step 20 : Select change bundle from RHS actions");
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();

        test.get().info("Step 21 : Select NC mobile to change permitted bundle");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(ncSubText, "Change Permitted Bundle");

        test.get().info("Step 22 : Verify NC mobile change bundle page displays");
        Assert.assertEquals(ncSubText, ServiceOrdersPage.ChangeBundle.getInstance().getSubscriptionNumber());
        expectNextBillDate = String.format("%s (%s days from today)", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT_IN_PDF), TimeStamp.TodayPlus1MonthMinusToday());
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("£10 SIM Only Tariff", ServiceOrdersPage.ChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 250 mins, 5000 texts (Capped)", ServiceOrdersPage.ChangeBundle.getInstance().getPackagedBundle());
        Assert.assertEquals("No Current Bundles", ServiceOrdersPage.ChangeBundle.getInstance().getInfo());

        Assert.assertEquals("Next Bill Date", ServiceOrdersPage.ChangeBundle.getInstance().getWhenToApplyChangeText());

        Assert.assertTrue(ServiceOrdersPage.ChangeBundle.getInstance().bundleExists(BundlesToSelectEntity.getNCBundleToSelect()));
        CareTestBase.page().checkBundleToolTip(BundlesToSelectEntity.getNCBundleToSelect());
        ServiceOrdersPage.ChangeBundle.getInstance().clickNextButton();

        test.get().info("Step 23 : Select 1 available bundles for FC and click next button");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName(BundlesToSelectEntity.getNCBundleToSelect(),"Monthly data bundle - 1GB (Capped) - £7.50 per Month (Recurring)");
        CareTestBase.page().clickNextButton();

        test.get().info("Step 24 : Verify NC mobile confirm change bundle is correct");
        Assert.assertEquals(ncSubText, ServiceOrdersPage.ConfirmChangeBundle.getInstance().getSubscriptionNumber());
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ConfirmChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("NC1-1000-250 £10 SIM Only Tariff {£10.00}", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 250 mins, 5000 texts (Capped)", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getPackagedBundle());

        Assert.assertEquals("No Current Recurring Bundles", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getInfoBefore());
        Assert.assertEquals("£0.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getTotalRecurringBundleChargeBefore());

        Assert.assertEquals("£7.50 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getTotalRecurringBundleChargeAfter());
        Assert.assertEquals(String.format("£7.50 per Month (Recurring).Valid from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy")), ServiceOrdersPage.ConfirmChangeBundle.getInstance().getBundleInfo("Monthly data bundle - 1GB (Capped):"));
        Assert.assertEquals("Increase of £7.50 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getRecurringBundlesChargeDifference());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"), ServiceOrdersPage.ConfirmChangeBundle.getInstance().getEffective());

        test.get().info("Step 25 : Click next button to service order complete screen");
        CareTestBase.page().clickNextButton();

        test.get().info("Step 26 : Verify service order complete screen has provision wait message");
        String provisionWaitStatusMessage = CareTestBase.page().verifyServiceOrderCompleteScreenHasProvisionWaitMessage();

        test.get().info("Step 27 : Click return to customer button on service order complete screen");
        CareTestBase.page().clickReturnToCustomer();

        test.get().info("Step 28 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 29 : Verify there are 3 change bundle service orders are created");
        Pair<String,String> serviceOrder = EventEntity.setEvents("Type", "Change Bundle");
        List<WebElement> listServiceOrder = ServiceOrdersContentPage.getInstance().getServiceOrder(serviceOrder);
        Assert.assertEquals(2, listServiceOrder.size());

        listServiceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataFCServiceOrderProvisionWait(fcSubNumber));
        Assert.assertEquals(1, listServiceOrder.size());
        String serviceOrderIdFC = ServiceOrdersContentPage.getInstance().getServiceOrderIdByOrderServices(ServiceOrderEntity.dataServiceOrderBySub(fcSubNumber));

        listServiceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataNCServiceOrderProvisionWait(ncSubNumber));
        Assert.assertEquals(1, listServiceOrder.size());
        String serviceOrderIdNC = ServiceOrdersContentPage.getInstance().getServiceOrderIdByOrderServices(ServiceOrderEntity.dataServiceOrderBySub(ncSubNumber));

        test.get().info("Step 30 : Verify FC change bundle SO details");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderIdFC);

        Assert.assertEquals("Provision Wait", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals(provisionWaitStatusMessage, TasksContentPage.TaskPage.DetailsPage.getInstance().getEndOfWizardMessage());
        Assert.assertEquals(fcSubText, TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC1-0750-150SO £7.50 SIM Only Tariff 1 Month Contract {£7.50}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), "dd MMM yyyy"), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("No", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getEUDataConsentFlag());
        Assert.assertEquals("3G data - 1GB;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

        Assert.assertEquals(2, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundle()));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundleProvisionWait()));


        test.get().info("Step 31 : Back to customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Step 32 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 33 : Verify NC change bundle SO details");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderIdNC);

        Assert.assertEquals("Provision Wait", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals(provisionWaitStatusMessage, TasksContentPage.TaskPage.DetailsPage.getInstance().getEndOfWizardMessage());
        Assert.assertEquals(ncSubText, TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("NC1-1000-250 £10 SIM Only Tariff {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), "dd MMM yyyy"), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("No", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getEUDataConsentFlag());
        Assert.assertEquals("Monthly data bundle - 1GB (Capped);", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

        Assert.assertEquals(2, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundle()));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundleProvisionWait()));

        test.get().info("Step 34 : Update the PDATE and BILLDATE for change bundle FC and NC");
        BaseTest.updateThePDateAndBillDateForChangeBundleForSo(serviceOrderIdFC);
        BaseTest.updateThePDateAndBillDateForChangeBundleForSo(serviceOrderIdNC);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 35 : Back to customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 36 : Verify there are 3 change bundle service orders are completed");
        listServiceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderProvisionWait(fcSubNumber));
        Assert.assertEquals(1, listServiceOrder.size());

        listServiceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderProvisionWait(ncSubNumber));
        Assert.assertEquals(1, listServiceOrder.size());

        test.get().info("Step 37 : Verify FC change bundle SO details are updated");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderIdFC);

        Assert.assertEquals("Completed Task", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals(provisionWaitStatusMessage, TasksContentPage.TaskPage.DetailsPage.getInstance().getEndOfWizardMessage());
        Assert.assertEquals(fcSubText, TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC1-0750-150SO £7.50 SIM Only Tariff 1 Month Contract {£7.50}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), "dd MMM yyyy"), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("No", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getEUDataConsentFlag());
        Assert.assertEquals("3G data - 1GB;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

        Assert.assertEquals(4, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundle()));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundleProvisionWait()));

//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventChangeBundle("PPB: AddSubscription: Request completed","Completed Task")));
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventChangeBundle("Service Order Completed","Completed Task")));


        test.get().info("Step 38 : Back to customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 39 : Verify NC change bundle SO details are updated");
        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrderIdNC);

        Assert.assertEquals("Completed Task", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals(provisionWaitStatusMessage, TasksContentPage.TaskPage.DetailsPage.getInstance().getEndOfWizardMessage());
        Assert.assertEquals(ncSubText, TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("NC1-1000-250 £10 SIM Only Tariff {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), "dd MMM yyyy"), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("No", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getEUDataConsentFlag());
        Assert.assertEquals("Monthly data bundle - 1GB (Capped);", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

        Assert.assertEquals(4, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundle()));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundleProvisionWait()));

//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventChangeBundle("PPB: AddSubscription: Request completed","Completed Task")));
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventChangeBundle("Service Order Completed","Completed Task")));

        test.get().info("Step 40 : Back to customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 41 : Verify FC other products are updated");
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(fcSubText);
        Assert.assertEquals(4, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().
                getNumberOfOtherProduct(OtherProductEntiy.dataBundlerForOtherProduct("BUNDLER - [1GB-3GDATA-0500-FC]", "Bundle", "Discount Bundle Recurring - [3G data - 1GB]", "", "£5.00")));

        test.get().info("Step 42 : Verify the discount bundle record FC is updated");
        List<DiscountBundleEntity> bundleEntityList = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfFCMobile);
        Assert.assertEquals(13, bundleEntityList.size());
        verifyNewNCDiscountBundles(bundleEntityList, "TM1GB","1GB-3GDATA-0500-FC");

        test.get().info("Step 43 : Go back to subscriptions");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 44 : Verify the discount bundle record NC is updated");
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(ncSubText);
        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().
                getNumberOfOtherProduct(OtherProductEntiy.dataBundlerForOtherProduct("BUNDLER - [1GB-DATA-750-NC]", "Bundle", "Discount Bundle Recurring - [Monthly data bundle - 1GB (Capped)]", "", "£7.50")));

        test.get().info("Step 45 : Run refill job");
        RemoteJobHelper.getInstance().submitDoRefillBcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoRefillNcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(TimeStamp.Today());

        test.get().info("Step 46 : Verify new discount bundle entries have been created");
        List<DiscountBundleEntity> discountBundlesNC = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfNCMobile);
        Assert.assertEquals(11, discountBundlesNC.size());

        List<DiscountBundleEntity> discountBundlesFC = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfFCMobile);
        Assert.assertEquals(14, discountBundlesFC.size());
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundlesFC, "FC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "FLX01" , "ACTIVE"));

        test.get().info("Step 47 : Submit draft bill run");
        RemoteJobHelper.getInstance().submitDraftBillRun();

        test.get().info("Step 48 : Submit confirm bill run");
        RemoteJobHelper.getInstance().submitConfirmBillRun();

        test.get().info("Step 49 : Open invoice details screen");
        CareTestBase.page().openInvoiceDetailsScreen();

        test.get().info("Step 50 : Verify Invoice detail are correct");
        CareTestBase.page().verifyInvoiceDetailsAreCorrect(Parser.parseDateFormate(TimeStamp.Today(), "dd MMM yyyy"), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), "dd MMM yyyy"), Parser.parseDateFormate(BaseTest.paymentCollectionDateEscapeNonWorkDay(10), "dd MMM yyyy"),"Confirmed");

        test.get().info("Step 51 : Verify PDF file");
        verifyPDFFile(customerNumber, fcSubNumber, ncSubNumber);

    }

    @Test(enabled = true, description = "TC31888 Self Care Change Family Perk Bundle on next bill date", groups = "Smoke")
    public void TC31888_Self_Care_Change_Family_Perk_Bundle_on_next_bill_date(){
        test.get().info("Step 1 : Create a CC cusotmer with via Care Inhand MasterCard");
        String path = "src\\test\\resources\\xml\\selfcare\\changebundle\\TC62_createOrder.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 6 : Verify customer start date and billing group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 7 : Verify all discount bundle entries align with bill run calendar entires");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 1");
        String serviceRefOf2stSubscription = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 2");

        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " FC Mobile 1");
        String discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyDiscountBundleBeforeChangingBundle(newStartDate, discountGroupCodeOfMobileRef1);

        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf2stSubscription + " FC Mobile 2");
        String discountGroupCodeOfMobileRef2 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyDiscountBundleBeforeChangingBundle(newStartDate, discountGroupCodeOfMobileRef2);

        test.get().info("Step 8 : Login to self care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 9 : Verify my personal information page is displayed");
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 10 : Click view or change my tariff details link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();

        test.get().info("Step 11 : Verify my tariff details page is displayed");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 12 : Verify tariff details screen");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile2Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 2");
        Assert.assertEquals("FC Mobile 2", mobile2Tariff.getDescription());
        Assert.assertEquals(serviceRefOf2stSubscription, mobile2Tariff.getMobilePhoneNumber());
        Assert.assertTrue(mobile2Tariff.hasSaveButton());
        Assert.assertEquals("£10 Tariff 12 Month Contract", mobile2Tariff.getTariff());
        Assert.assertEquals(String.format("ACTIVE   as of   %s    ", Parser.parseDateFormate(newStartDate, "dd/MM/yyyy")), mobile2Tariff.getStatus());
        Assert.assertEquals("£20 safety buffer    ACTIVE  as of  " + Parser.parseDateFormate(newStartDate, "dd/MM/yyyy"), mobile2Tariff.getSafetyBuffer());
        Assert.assertTrue(mobile2Tariff.hasChangeMySafetyBufferButton());
        Assert.assertTrue(mobile2Tariff.hasAddOrChangeABundleButton());
        Assert.assertTrue(mobile2Tariff.hasAddOrChangeAFamilyPerkButton());
        Assert.assertTrue(mobile2Tariff.hasAddOrViewOneoffBundlesButton());
        Assert.assertTrue(mobile2Tariff.hasUpdateButton());

        test.get().info("Step 13 : Click add or change a family perk button for mobile 1");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1");
        mobile1Tariff.clickAddOrChangeAFamilyPerkBtn();

        test.get().info("Step 14 : Verify mobile phone info is correct");
        AddOrChangeAFamilyPerkPage.InfoPage infoPage = AddOrChangeAFamilyPerkPage.InfoPage.getInstance();
        Assert.assertEquals(serviceRefOf1stSubscription + " - FC Mobile 1", infoPage.getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", infoPage.getTariff());
        Assert.assertEquals("500 mins, 5000 texts (FC)", infoPage.getMonthlyAllowance());
        Assert.assertTrue(infoPage.getMonthlyBundles().isEmpty());

        test.get().info("Step 15 : Verify expected warning message displayed");
        String message = String.format("Your changes will take effect from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"));
        Assert.assertEquals(message, AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getWarningMessage());

        test.get().info("Step 16 : Unselect 1 family perk 150 Mins per month");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().unselectBundlesByName("Family perk - 150 Mins per month");

        test.get().info("Step 17 : Verify family perk bundle table is correct after unselect 150 per month");
        Assert.assertEquals("650", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 1));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 1));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 2));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 2));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 4));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 4));

        test.get().info("Step 18 : Select 1 family perk 250 MB");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().selectBundlesByName("Family perk - 250MB per month");

        test.get().info("Step 19 : Verify family perk bundle table is correct after select 250MB per month");
        Assert.assertEquals("650", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 1));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 1));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 2));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 2));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 4));
        Assert.assertEquals("250", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 4));

        test.get().info("Step 20 : Click save changes button");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().tickBoxToAcceptTheFamilyPerkTermsAndConditions();
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().clickSaveButton();

        test.get().info("Step 21 : Verify my tariff details page displayed with successful alert");
        List<String> listMessage = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(1, listMessage.size());
        Assert.assertEquals(String.format("Your changes will take effect from %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), "dd/MM/yyyy")), listMessage.get(0));

        mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1");
        List<String> familyPerks = mobile1Tariff.familyPerkStack();
        Assert.assertEquals(2, familyPerks.size());
        Assert.assertEquals(String.format("Family perk - 150 Mins per month   PENDING removal  as of  %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), "dd/MM/yyyy")), familyPerks.get(0));
        Assert.assertEquals(String.format("Family perk - 250MB per month   PENDING activation  as of  %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), "dd/MM/yyyy")), familyPerks.get(1));

        test.get().info("Step 22 : Open sevice orders page in hub net for customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 23 : Verify customer has 1 expected change bundle SO record");
        Assert.assertEquals(1, ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription,"Change Bundle","Provision Wait")));

        test.get().info("Step 24 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 25 : Verify SO details data are correct");
        Assert.assertEquals(serviceRefOf1stSubscription + " FC Mobile 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("Family perk - 250MB per month;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("Family perk - 150 Mins per month;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());

        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventChangeBundle("Service Order set to Provision Wait","Provision Wait")));
        String serviceOrderId = TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getSoID();

        test.get().info("Step 26 : Update provision date of change bundle service order");
        BillingActions.getInstance().updateProvisionDateOfChangeBundleServiceOrder(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 27 : Find customer then open details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Step 28 : Refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 29 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 30 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 31 : Verify change bundle so details are correct and 3 events generated after submit provision services job");
        Assert.assertEquals(serviceRefOf1stSubscription + " FC Mobile 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("Family perk - 250MB per month;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("Family perk - 150 Mins per month;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());

        Assert.assertEquals(3,  TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventChangeBundle("Service Order set to Provision Wait","Provision Wait")));
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventChangeBundle("PPB: AddSubscription: Request completed","Completed Task")));
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventChangeBundle("Service Order Completed","Completed Task")));

        test.get().info("Step 32 : Login to self care without Pin");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 33 : Click view or change my tariff details link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();

        test.get().info("Step 34 : Verify my tariff details page is displayed");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 35 : Verify tariff details paged after running provision");
        mobile1Tariff  = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1");
        familyPerks = mobile1Tariff.familyPerkStack();
        Assert.assertEquals(1, familyPerks.size());
        Assert.assertEquals(String.format("Family perk - 250MB per month   ACTIVE  as of  %s", Parser.parseDateFormate(TimeStamp.Today(),"dd/MM/yyyy")), familyPerks.get(0));

        test.get().info("Step 36 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 37 : Open details for customer 1st subscription");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " FC Mobile 1");

        test.get().info("Step 38 : Verify the one off bundle just added is listed in other products grid");
        List<HashMap<String,String>> otherProducts = OtherProductEntiy.dataForOtherProduct(newStartDate);
        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage otherProductsGridSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance();
        Assert.assertEquals(4, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProducts(otherProducts));

        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProduct(otherProducts.get(0)));
        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProduct(otherProducts.get(1)));
        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProduct(otherProducts.get(2)));
        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProduct(otherProducts.get(3)));

        test.get().info("Step 39 : Verify 1 new discount bundle record generated for customer");
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(13, discountBundles.size());
        BaseTest.verifyFCDiscountBundles(discountBundles, newStartDate, "FLX17");
        BaseTest.verifyFCDiscountBundles(discountBundles, newStartDate, "TM500");
        BaseTest.verifyFCDiscountBundles(discountBundles, newStartDate, "TMT5K");

        Assert.assertEquals(1, BillingActions.getInstance().findNewDiscountBundlesByCondition(discountBundles,"NC" ,newStartDate, TimeStamp.TodayPlus1MonthMinus1Day(), "TM150","150-FMIN-0-FC","ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDeletedDiscountBundlesByCondition(discountBundles, TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), Integer.parseInt(serviceOrderId), TimeStamp.Today(),"NC","TM150","150-FMIN-0-FC"));
        Assert.assertEquals(1, BillingActions.getInstance().findDeletedDiscountBundlesByCondition(discountBundles, TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), Integer.parseInt(serviceOrderId), TimeStamp.Today(),"NC","TM150","150-FMIN-0-FC"));

        Assert.assertEquals(1, BillingActions.getInstance().findNewDiscountBundlesByCondition(discountBundles,"NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "TD250","250MB-FDATA-0-FC","ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findNewDiscountBundlesByCondition(discountBundles,"NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "TD250","250MB-FDATA-0-FC","ACTIVE"));

        test.get().info("Step 40 : Submit Remote Job");
        RemoteJobHelper.getInstance().submitDoRefillBcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoRefillNcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(TimeStamp.Today());

        test.get().info("Step 41 : Verify new discount bundle entries have been created");
        discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(14, discountBundles.size());
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles,"FC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "FLX17","ACTIVE"));

        test.get().info("Step 42 : Submit Draft Bill");
        RemoteJobHelper.getInstance().submitDraftBillRun();
        RemoteJobHelper.getInstance().submitConfirmBillRun();

        test.get().info("Step 43 : Open invoice details screen");
        CareTestBase.page().openInvoiceDetailsScreen();

        test.get().info("Step 44 : Verify PDF File");
        verifyPDFFile(customerNumber, newStartDate, serviceRefOf1stSubscription, serviceRefOf2stSubscription);

    }


    @Test(enabled = true, description = "TC32193 Self Care WS Subscription is Active Family perk Bundle", groups = "Smoke")
    public void TC32193_Self_Care_WS_Subscription_is_Active_Family_perk_Bundle(){
        test.get().info("Step 1 : Create a customer with family perk bundle subscription active");
        String path = "src\\test\\resources\\xml\\sws\\getbundle\\TC3363_createOrder.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getOrder(owsActions.orderIdNo);
        String subscriptionNumber = owsActions.getOrderMpnByReference(1);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 6 : Record discount bundle monthly refill SO id");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        String serviceOrderId =  ServiceOrdersContentPage.getInstance().getServiceOrderIdByOrderServices(ServiceOrderEntity.dataServiceOrderBySubAndType(subscriptionNumber, "Discount Bundle Monthly Refill"));

        test.get().info("Step 7 : Build and Submit get bundle request with subscription number and account number to selfcare WS");
        SWSActions swsActions = new SWSActions();
        Xml xml = swsActions.submitGetBundleRequest(customerNumber, subscriptionNumber);

        test.get().info("Step 8 : Verify get bundle response are correct");
        verifyGetBundleResponseAreCorrect(xml, customerNumber, subscriptionNumber, serviceOrderId, newStartDate);


    }

    private void verifyChangeBundleSoDetails(String serviceSubscription){
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals(serviceSubscription + " Mobile Ref 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals("Family perk - 500MB per month - 4G", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("Family perk - 250MB per month", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
    }

    private void verifySpecificNCDiscountBundles(List<DiscountBundleEntity> allDiscountBundles, String partitionIdRef){
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), partitionIdRef, "DELETED"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), partitionIdRef, "DELETED"));
    }

    private String [] Verify4SubscriptionsAnd4CreditAgreements(OWSActions owsActions, String orderId){
        Xml response = owsActions.getOrder(orderId);

        String ccaNo1 = response.getTextByXpath("//orderItem[1]//device//agreement//number");
        String ccaNo3 = response.getTextByXpath("//orderItem[2]//device//agreement//number");
        String ccaNo4 = response.getTextByXpath("//orderItem[3]//device//agreement//number");
        String ccaNo5 = response.getTextByXpath("//orderItem[4]//device//agreement//number");


        String subNo1 = response.getTextByXpath("//orderItem[1]//serviceRef");
        String subNo3 = response.getTextByXpath("//orderItem[2]//serviceRef");
        String subNo4 = response.getTextByXpath("//orderItem[3]//serviceRef");
        String subNo5 = response.getTextByXpath("//orderItem[4]//serviceRef");

        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();
        Assert.assertEquals(4, CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getRowOfCreditAgreementsGrid());
        Assert.assertEquals(4, CreditAgreementsContentPage.CreditAgreementPaymentsGrid.getInstance().getRowOfCreditAgreementPaymentsGrid());

        verifyAgreementDetails(subNo1, ccaNo1);
        verifyAgreementDetails(subNo3, ccaNo3);
        verifyAgreementDetails(subNo4, ccaNo4);
        verifyAgreementDetails(subNo5, ccaNo5);

        return new String[]{ccaNo1,ccaNo3,ccaNo4,ccaNo5};

    }

    private void verifyAgreementDetails(String subscriptionNumber, String agreementNumber){
        Assert.assertEquals(1, CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCreditAgreement(CreditAgreementsGridEntity.getCreditAgreementGird(subscriptionNumber)).size());
        CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().clickExpandButtonOfCABySubscription(subscriptionNumber);

        CreditAgreementsContentPage.CreditAgreementsGridPage.CADetailClass caDetailClass = CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(subscriptionNumber);
        Assert.assertEquals("AU Standard", caDetailClass.dealType());
        Assert.assertEquals("1", caDetailClass.ccaPhases());
        Assert.assertEquals("AU Standard", caDetailClass.dealType());
        Assert.assertEquals("24", caDetailClass.ccaFirstPhaseMonths());
        Assert.assertEquals("£16.00", caDetailClass.ccaFirstPhaseMonthlyCharge());
        Assert.assertEquals("0", caDetailClass.ccaSecondPhaseMonths());
        Assert.assertEquals("£0.00", caDetailClass.ccaSecondPhaseMonthlyCharge());
        Assert.assertEquals("24", caDetailClass.ccaTotalDurationMonths());
        Assert.assertEquals("£384.00", caDetailClass.contractValue());
        Assert.assertEquals("£16.00", caDetailClass.monthlyChargePaid());
        Assert.assertEquals("£0.00", caDetailClass.otherPayments());
        Assert.assertEquals(agreementNumber, caDetailClass.agreementNumber());
        Assert.assertEquals("HTC Desire HD", caDetailClass.device());

        Assert.assertEquals(1, CreditAgreementsContentPage.CreditAgreementPaymentsGrid.getInstance().getNumberOfCreditAgreementPayments(CreditAgreementPaymentsEntiy.getCreditAgreementPaymentWithInitialPayment(subscriptionNumber)));
    }

    public void verifyContractPDFFile(OWSActions owsActions, String value){
        String path =  System.getProperty("user.home")+"\\Desktop\\QA_Project\\";
        if(!new File(path).exists())
            Common.createUserDir(path);
        String locationFileName = path + String.format("TC1358Contract_%s.pdf", TimeStamp.Today());
        Pdf.getInstance().saveToPDF(locationFileName, value);

        List<String>  pdfList = Pdf.getInstance().getText(locationFileName);
        CareTestBase.page().verifyContractPdfCommonData(pdfList, "£75.00", owsActions);

        Assert.assertEquals("Your device details", pdfList.get(6));
        Assert.assertTrue( pdfList.get(7).startsWith("Number:"));
        Assert.assertEquals("Friendly name chosen: Mobile Ref 1",  pdfList.get(8));
        Assert.assertEquals("Your order details",  pdfList.get(9));
        Assert.assertEquals("Tariff: £10 12Mth SIM Only Tariff 500 Mins 5000 Texts",  pdfList.get(10));
        Assert.assertEquals("Device: HTC Wildfire",  pdfList.get(11));
        Assert.assertEquals("Monthly bundle: Monthly 500MB data allowance",  pdfList.get(12));
        Assert.assertTrue( pdfList.get(13).startsWith("IMEI: 137695587771463"));
        Assert.assertEquals("Any upfront costs", pdfList.get(16));
        Assert.assertEquals("First month's payment: £15.00", pdfList.get(17));
        Assert.assertEquals("Upfront device cost: £60.00", pdfList.get(18));
        Assert.assertEquals("Total upfront cost: £75.00", pdfList.get(19));
        Assert.assertEquals("Your monthly costs", pdfList.get(20));
        Assert.assertEquals("Monthly charges: £15.00", pdfList.get(21));
    }

    private List<String> getAllSubscriptionsNumber(){
       MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
       List<String> subscriptionNumberList = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            String subNo = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(i);
            subscriptionNumberList.add(subNo);
        }

        return subscriptionNumberList;
    }

    public void verifyPDFFile(String customerId, String fcScriptionNumber, String ncScriptionNumber){
        String downloadedPDFFile = BaseTest.getDownloadInvoicePDFFile(customerId);
        List<String> pdfList = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile,1, 1);

        String expectFirstBill = "Your first bill   £30.00 Clubcard";
        Assert.assertTrue(pdfList.contains(expectFirstBill));

        pdfList = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile,3);
        String summaryOfUserCharges = "Summary of charges";

        String userChargesFC = String.format("User charges for %s  FC Mobile (£7.50 SIM Only Tariff 1 Month Contract)", fcScriptionNumber);
        String userChargesNC = String.format("User charges for %s  NC Mobile (£10 SIM Only Tariff)", ncScriptionNumber);

        Assert.assertTrue(pdfList.contains(summaryOfUserCharges));
        Assert.assertTrue(pdfList.contains(userChargesFC));
        Assert.assertTrue(pdfList.contains(userChargesNC));

        String adjmtChargesAndCredits2 = String.format("%s %s Family perk - 500 Tesco Mobile only minutes per month for 0.00", Parser.parseDateFormate(TimeStamp.Today(), "dd/MM/yyyy"), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), "dd/MM/yyyy"));//_FCScriptionNumber
        String adjmtChargesAndCredits3 = String.format("%s %s Monthly data bundle - 1GB (Capped) for %s 7.50", Parser.parseDateFormate(TimeStamp.Today(), "dd/MM/yyyy"), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), "dd/MM/yyyy"), ncScriptionNumber);//_NCScriptionNumber

        Assert.assertTrue(pdfList.contains(adjmtChargesAndCredits2));
        Assert.assertTrue(pdfList.contains(adjmtChargesAndCredits3));

    }

    private void verifyPDFFile(String customerId, Date newStartDate, String serviceRefOf1stSubscription, String serviceRefOf2ndSubscription){
        String downloadedPDFFile = BaseTest.getDownloadInvoicePDFFile(customerId);
        List<String> pdfList = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(downloadedPDFFile,3);
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.startsWith("Summary of charges")));
        Assert.assertTrue(pdfList.get(7).equalsIgnoreCase("From Date To Date Cost Charge (£)"));

        Assert.assertTrue(pdfList.get(9).equalsIgnoreCase(String.format("Monthly subscription %s %s 10.00", Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF))));
        Assert.assertTrue(pdfList.get(10).equalsIgnoreCase(String.format("Monthly subscription %s %s 10.00", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF))));

        Assert.assertTrue(pdfList.get(13).equalsIgnoreCase("From Date To Date Cost Charge (£)"));
        Assert.assertTrue(pdfList.get(15).equalsIgnoreCase(String.format("Monthly subscription %s %s 10.00", Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF))));
        Assert.assertTrue(pdfList.get(16).equalsIgnoreCase(String.format("Monthly subscription %s %s 10.00", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF))));

        String userChargeSubscription1 = String.format("User charges for %s  FC Mobile 1 (£10 Tariff 12 Month Contract)", serviceRefOf1stSubscription);
        String userChargeSubscription2 = String.format("User charges for %s  FC Mobile 2 (£10 Tariff 12 Month Contract)", serviceRefOf2ndSubscription);
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(userChargeSubscription1)));
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(userChargeSubscription2)));


        String adjmtChargesAndCredits1 = String.format("%s %s Nokia 2720 for %s 0.00", Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);
        String adjmtChargesAndCredits2 = String.format("%s %s Nokia 2720 for %s 0.00", Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf2ndSubscription);

        String adjmtChargesAndCredits3 = String.format("%s %s Family perk - 150 Mins per month for %S 0.00", Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);
        String adjmtChargesAndCredits4 = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);

        String adjmtChargesAndCredits5 = String.format("%s %s Family perk - 150 Mins per month for %s 0.00", Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf2ndSubscription);
        String adjmtChargesAndCredits6 = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf2ndSubscription);

        String adjmtChargesAndCredits7 = String.format("%s %s Family perk - 150 Mins per month for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf2ndSubscription);
        String adjmtChargesAndCredits8 = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription );
        String adjmtChargesAndCredits9 = String.format("%s %s Family perk - 250MB per month for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);
        String adjmtChargesAndCredits10 = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf2ndSubscription);

        String familyPerk150MinsNoLongerCharging = String.format("%s %s Family perk - 150 Mins per month for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);
        Assert.assertFalse(pdfList.stream().anyMatch(x -> x.contains(familyPerk150MinsNoLongerCharging)));

        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(adjmtChargesAndCredits1)));
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(adjmtChargesAndCredits2)));
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(adjmtChargesAndCredits3)));
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(adjmtChargesAndCredits4)));
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(adjmtChargesAndCredits5)));
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(adjmtChargesAndCredits6)));
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(adjmtChargesAndCredits7)));
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(adjmtChargesAndCredits8)));
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(adjmtChargesAndCredits9)));
        Assert.assertTrue(pdfList.stream().anyMatch(x -> x.equalsIgnoreCase(adjmtChargesAndCredits10)));
    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][] { { "gc" }, { "ff" }, { "ie" } };
    }

    private void verifyDiscountBundleBeforeChangingBundle(Date newStartDate ,String  discountGroupCode){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(11, discountBundles.size());
        verifyFCDiscountBundles(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TMDAT");
    }

    private void verifyGetBundleResponseAreCorrect(Xml xml, String customerId, String subscriptionNumber, String serviceOrderId, Date newStartDate){
        String actualFile = Common.saveXmlFile(customerId +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xml.toString())));
        String file =  Common.readFile("src\\test\\resources\\xml\\sws\\getbundle\\TC3363_response.xml")
                .replace("$accountNumber$", customerId)
                .replace("$subscriptionNumber$", subscriptionNumber)
                .replace("$startDate$", Parser.parseDateFormate(newStartDate,"yyyy-MM-dd"))
                .replace("$nextScheduledRefill$", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"yyyy-MM-dd"))
                .replace("$SOId$", serviceOrderId);;
        String expectedResponseFile = Common.saveXmlFile(customerId +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(1, size);
    }

}
