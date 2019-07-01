package suite.regression;

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
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.pages.selfcare.OrderConfirmationPage;
import logic.pages.selfcare.SelfCareTestBase;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DemoTest extends BaseTest {

    @Test(enabled = false, description = "TC29701 Deactivate Account with contracted subscription within 28 days with a delay return and immediate refund", groups = "Smoke")
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
        String serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderId(serviceOrder);

        BaseTest.updateThePDateAndBillDateForSO(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 8 : Verify Deactivate Account So Is Completed");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderDataForDeactivateAccount().get(1));
        Assert.assertEquals(1, serviceOrder.size(), "The service order is not exist in table");

        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderDataForDeactivateAccount().get(2));
        Assert.assertEquals(1, serviceOrder.size(), "The service order is not exist in table");
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderId(serviceOrder);

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
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().saveFileFromWebRequest(customerNumber);
        List<String> listInvoiceContent = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent();
        Assert.assertTrue(listInvoiceContent.contains(String.format("%s %s Credit Transfer from Prepay for Deactivation Refund -25.00", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF))));
        Assert.assertTrue(listInvoiceContent.contains(String.format("(%s  Mobile Ref 1)", subscriptionNumber)));
        Assert.assertTrue(listInvoiceContent.contains(String.format("%s %s Customer Care refund issued for %s 132.50", Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT_IN_PDF), subscriptionNumber)));
        Assert.assertTrue(listInvoiceContent.contains(String.format("%s %s Usage 28 days disconnection adjustment for %s 10.00", Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), subscriptionNumber)));
        Assert.assertTrue(listInvoiceContent.contains("Total Payments -132.50"));
    }

    @Test(enabled = false, description = "TC32533 Maintain Bundle Change FP Add Bundle Service Feature Off On", groups = "Smoke")
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
        List<Integer> listBundleFLX17 = BaseTest.verifyFCDiscountBundles(discountBundles, newStartDate, "FLX17");
        Assert.assertEquals(2, Common.steamFilterCondition(listBundleFLX17, 1));
        List<Integer> listBundleTM500 = BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        Assert.assertEquals(3, Common.steamFilterCondition(listBundleTM500, 1));
        List<Integer> listBundleTMT5K = BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");
        Assert.assertEquals(3, Common.steamFilterCondition(listBundleTMT5K, 1));
        List<Integer> listBundleTD250 = BaseTest.verifyNCDiscountBundles(discountBundles, newStartDate, "TD250");
        Assert.assertEquals(3, Common.steamFilterCondition(listBundleTD250, 1));

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
        String serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderId(serviceOrder);
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
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfServiceOrder(EventEntity.dataForEventChangeBundle()));

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
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderId(serviceOrder);
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

        listBundleTD250 = verifySpecificNCDiscountBundles(discountBundles, "TD250");
        Assert.assertEquals(2, Common.steamFilterCondition(listBundleTD250, 1));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", newStartDate, TimeStamp.TodayPlus1MonthMinus1Day(), "TD250", "DELETED"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "TMDAT", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "TMDAT", "ACTIVE"));

        test.get().info("Step 30 : Verify 2nd subscription service feature is none");
        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGirdSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf2stSubscription + " Mobile Ref 2");
        Assert.assertEquals("None", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());
    }

    @Test(enabled = false, description = "TC30432_Transfer_amount_to_Credit_Agreement_subscription_deactivated_within_Trial_Period", groups = "Smoke")
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

        String serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderId(serviceOrder);
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

    @Test(enabled = false, description = "TC34669 A New Customer New Order Multiple deals MAX number of deals with CCA", groups = "Smoke")
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


    @Test(enabled = false, description = "TC1358 OWS Basic Path New Customer New Order via Care Inhand Master Card", groups = "Smoke")
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

    @Test(enabled = true, description = "TC29787 Add Permitted Bundle Apply at next bill sssdate", groups = "Smoke")
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
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.TariffComponentsGridPage.getInstance().getTariffComponents(TariffComponentEntity.dataTMOBUSETariffComponentForFC(newStartDate)));

        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProducts(OtherProductEntiy.dataBundlerForOtherProductForFC(newStartDate)));

        test.get().info("Step 10 : Verify all discount bundle entries align with bill run calendar entires for NC");
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
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.TariffComponentsGridPage.getInstance().getTariffComponents(TariffComponentEntity.dataTMOBUSETariffComponentForNC(newStartDate)));

        Assert.assertEquals(2, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProducts(OtherProductEntiy.dataBundlerForOtherProductForNC(newStartDate)));

        test.get().info("Step 12 : Select change bundle from RHS actions");
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();

        test.get().info("Step 13 : Select FC mobile to change permitted bundle");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(fcSubText, "Change Permitted Bundle");

        test.get().info("Step 14 : Verify FC mobile change bundle page displays");
        Assert.assertEquals(fcSubText, ServiceOrdersPage.ChangeBundle.getInstance().getSubscriptionNumber());
        String expectNextBillDate = String.format("%s (%s days from today)", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinusToday(),TimeStamp.DATE_FORMAT_IN_PDF));
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("£7.50 SIM Only Tariff 1 Month Contract", ServiceOrdersPage.ChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 150 mins, 5000 texts (FC)", ServiceOrdersPage.ChangeBundle.getInstance().getPackagedBundle());
        Assert.assertEquals("No Current Bundles", ServiceOrdersPage.ChangeBundle.getInstance().getInfo());
        Assert.assertEquals("Next Bill Date", ServiceOrdersPage.ChangeBundle.getInstance().getWhenToApplyChangeText());
        Assert.assertTrue(ServiceOrdersPage.ChangeBundle.getInstance().bundleExists(BundlesToSelectEntity.getBundleToSelect()));
        CareTestBase.page().checkBundleToolTip(BundlesToSelectEntity.getBundleToSelect());
        ServiceOrdersPage.ChangeBundle.getInstance().clickNextButton();

        test.get().info("Step 15 : Select 1 available bundles for FC and click next button");
        //ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName();


    }


    private void verifyChangeBundleSoDetails(String serviceSubscription){
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals(serviceSubscription + " Mobile Ref 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals("Family perk - 500MB per month - 4G", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("Family perk - 250MB per month", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
    }

    private List<Integer> verifySpecificNCDiscountBundles(List<DiscountBundleEntity> allDiscountBundles, String partitionIdRef){
        List<Integer> listResult = new ArrayList<Integer>();
        int result1 = BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), partitionIdRef, "DELETED");
        listResult.add(result1);
        int result2 = BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), partitionIdRef, "DELETED");
        listResult.add(result2);

        return listResult;
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
        Assert.assertEquals(1, CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCreditAgreement(CreditAgreementsGirdEntity.getCreditAgreementGird(subscriptionNumber)).size());
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

    public void verifyContractPDFFile(OWSActions owsActions ,String value){
        String path =  System.getProperty("user.home")+"\\Desktop\\QA_Project";
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

}
