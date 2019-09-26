package suite.regression.soho;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.AdjustmentsChargesAndCreditsEntity;
import logic.business.entities.PaymentGridEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import java.sql.Date;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TC3799_OWS_Upgrade_Business_Order extends BaseTest {
    private String customerNumber = "10492";
    private String subNo1 = "07372223320";
    private String serviceOrderId = "12453";
    OWSActions owsActions;
    String ccaNo1;
    private Date newStartDate;

    @Test(enabled = true, description = "TC3796_001_OWS_Create_New_Order_For_Business_Customer ", groups = "SOHO")
    public void TC3796_001_OWS_Create_New_Order_For_Business_Customer() {
        test.get().info("Step 1 : Create a Customer with business type");
        owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC3796_request_business_type.xml";
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

        test.get().info("Step 6 : Get subscription number");
        owsActions.getOrder(owsActions.orderIdNo);
        subNo1 = owsActions.getOrderMpnByReference(1);

        test.get().info("Step 7 : Upgrade Order With Business Type");
        path = "src\\test\\resources\\xml\\soho\\TC3796_upgrade.xml";
        String ePath = "src\\test\\resources\\xml\\soho\\TC3799_accept.xml";
        owsActions.upgradeOrder(path, ePath,customerNumber, subNo1);
        owsActions.getOrder(owsActions.orderIdNo);
        ccaNo1 = owsActions.getCreditAgreementNumberByReference("Mobile 1");

        test.get().info("Step 8 : Reload Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 9 : Verify account number minus 114");
        VerifyCustomerAccountBalanceIsMinus114();

        test.get().info("Step 10 : Verify 2 service orders are created");
        verify2ServiceOrdersAreCreated();

        test.get().info("Step 11 : Verify Detail Of the 2 services");
        verifyDetailsOfThe2ServiceOrder();

        test.get().info("Step 12 : Verify A Payment For ETC Charge Was Created");
        verifyAPaymentForETCChargeWasCreated();

        test.get().info("Step 13 : Verify Credit Agreement Details And Payment Information");
        verifyCreditAgreementDetailsAndPaymentInformation();

        test.get().info("Step 14 : Verify Subscription Details Of Other Products");
        verifySubscriptionDetailsOfOtherProducts();

        test.get().info("Step 15 : Submit Provision wait");
        BaseTest.updateThePDateAndBillDateForSO(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 16 : Verify The Upgrade Tariff SO Is Completed");
        verifyTheUpgradeTariffSOIsCompleted();

        test.get().info("Step 17 : Verify Old Tariff Is Inactive And Has EndDate");
        verifyOldTariffIsInactiveAndHasEndDate();

        test.get().info("Step 18 : Verify New Tariff Other Products Details");
        verifyNewTariffOtherProductsDetails();

        test.get().info("Step 19 : Verify Old Tariff Other Products Details");
        verifyOldTariffOtherProductsDetails();

        test.get().info("Step 20 : verify Credit Agreement Details");
        verifyCreditAgreementDetails();

        test.get().info("Step 21 : Build get Contract Summary Request");
        SWSActions swsActions = new SWSActions();
        Xml xml = swsActions.submitGetContractSummaryRequest(subNo1);

        test.get().info("Step 22 : Verify Get Contract Summary Request Are Correct");
        verifyGetContractSummaryRequestAreCorrect(xml);

        test.get().info("Step 23 : Verify Live Bill Estimate Are Correct");
        verifyLiveBillEstimateAreCorrect();
    }

    private void VerifyCustomerAccountBalanceIsMinus114(){
        Assert.assertEquals("-£114.00", CommonContentPage.CustomerSummarySectionPage.getInstance().getAmountBalance());
    }

    private void verify2ServiceOrdersAreCreated(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        ServiceOrdersContentPage grid = ServiceOrdersContentPage.getInstance();
        HashMap upgradeTariff = ServiceOrderEntity.dataServiceOrderCreditCardPayment("Provision Wait", "Upgrade Tariff");
        Assert.assertEquals(grid.getNumberOfServiceOrders(upgradeTariff), 1);

        HashMap upgradeOrder = ServiceOrderEntity.dataServiceOrderCreditCardPayment("Completed Task", "Upgrade Order");
        Assert.assertEquals(grid.getNumberOfServiceOrders(upgradeOrder), 1);

        serviceOrderId = grid.getServiceOrderidByType("Upgrade Tariff");
    }

    private void verifyDetailsOfThe2ServiceOrder(){
        ServiceOrdersContentPage grid = ServiceOrdersContentPage.getInstance();
        grid.clickServiceOrderByType("Upgrade Order");

        TasksContentPage.TaskSummarySectionPage summary = TasksContentPage.TaskSummarySectionPage.getInstance();
        Assert.assertEquals("Upgrade Order", summary.getDescription());
        Assert.assertEquals("Completed Task", summary.getStatus());

        TasksContentPage.TaskPage.DetailsPage details = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(subNo1, details.getServiceNo());
        Assert.assertEquals("FC24-2000-500", details.getTariffProductCode());
        Assert.assertEquals("Upgrade Order", details.getOrderType());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), "dd MMM yyyy"), details.getReceiptDate());
        Assert.assertEquals("Completed Task", details.getTransactionStatus());

        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        grid.clickServiceOrderByType("Upgrade Tariff");

        summary = TasksContentPage.TaskSummarySectionPage.getInstance();
        Assert.assertEquals("Upgrade Tariff", summary.getDescription());
        Assert.assertEquals("Provision Wait", summary.getStatus());

        details = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(subNo1 + " Mobile 1", details.getSubscriptionNumber());
        Assert.assertEquals("Upgrade Tariff", details.getOrderType());
        Assert.assertEquals("Provision Wait", details.getServiceOrderStatus());
        Assert.assertEquals("90", details.getETCAmount());
        Assert.assertEquals("90", details.getETCOverrideAmount());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), "dd MMM yyyy"), details.getServiceOrderStartDate());
        Assert.assertEquals("FC24-2000-500", details.getTariffProductCode());
        Assert.assertEquals("Provision Wait", details.getServiceOrderStatus());
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
    }

    private void verifyAPaymentForETCChargeWasCreated(){
        MenuPage.LeftMenuPage.getInstance().clickPaymentsLink();
        HashMap<String, String> paymentEntity = PaymentGridEntity.getPaymentEntity("Online Payment", "£99.00");
        Assert.assertEquals(PaymentDetailPage.paymentConentGrid.getInstance().getNumberPaymentRecord(paymentEntity), 1);
    }

    private void verifyCreditAgreementDetailsAndPaymentInformation(){
        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();
        CreditAgreementsContentPage.CreditAgreementsGridPage creditAgreementsGridPage = CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance();
        List<String> list = new ArrayList<>();
        list.add(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        list.add("Credit Agreement");
        list.add("£384.000");
        list.add("Pending");
        Assert.assertEquals(1,Common.compareList(creditAgreementsGridPage.getAllValueCreditAgreement(), list));
    }

    private void verifySubscriptionDetailsOfOtherProducts(){
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage subscriptions= CommonContentPage.SubscriptionsGridSectionPage.getInstance();
        subscriptions.clickSubscriptionNumberLinkByIndex(1);

        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage otherProductsGrid = SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance();
        HashMap<String,String> Bundle = new HashMap<String,String>();
        Bundle.put("Product Code","FLEXCAP - [02000-SB-A]");
        Bundle.put("Type","Bundle");
        Bundle.put("Description","Flexible Cap - £20 - [£20 safety buffer]");
        Assert.assertEquals(1, otherProductsGrid.getNumberOfOtherProductsByProduct(Bundle));

        HashMap<String,String> device = new HashMap<String,String>();
        device.put("Product Code","HTC-DESIRE-XXX-99");
        device.put("Type","Device");
        device.put("Description","HTC Desire HD");
        Assert.assertEquals(1, otherProductsGrid.getNumberOfOtherProductsByProduct(device));

        HashMap<String,String> agreement = new HashMap<String,String>();
        agreement.put("Product Code","AGRTESCOMOBILE");
        agreement.put("Type","Agreement");
        agreement.put("Description","Credit Agreement");
        Assert.assertEquals(1, otherProductsGrid.getNumberOfOtherProductsByProduct(agreement));

        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
    }

    private void verifyTheUpgradeTariffSOIsCompleted(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage serviceOrder = ServiceOrdersContentPage.getInstance();
        HashMap upgradeTariff = ServiceOrderEntity.dataServiceOrderCreditCardPayment("Completed Task", "Upgrade Tariff");
        Assert.assertEquals(1, serviceOrder.getNumberOfServiceOrders(upgradeTariff));
    }

    private void verifyOldTariffIsInactiveAndHasEndDate() {
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage grid = CommonContentPage.SubscriptionsGridSectionPage.getInstance();

        List<String> list = new ArrayList<>();
        list.add(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        list.add("Safety Buffer - £20");
        list.add("FC24-2000-500 - £20 Tariff 24 Month Contract - £20.00");
        list.add(subNo1 + " Mobile 1");
        list.add("Active");
        Assert.assertEquals(1, Common.compareList(grid.getAllValueSubscription(), list));

        list = new ArrayList<>();
        list.add(Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), TimeStamp.DATE_FORMAT));
        list.add("Safety Buffer - £20");
        list.add("FC12-1000-500SO - £10 Tariff 12 Month Contract - £10.00");
        list.add(subNo1 + " Mobile 1");
        list.add("Inactive");
        Assert.assertEquals(1, Common.compareList(grid.getAllValueSubscription(), list));
    }

    private void verifyNewTariffOtherProductsDetails(){
        CommonContentPage.SubscriptionsGridSectionPage subscriptions= CommonContentPage.SubscriptionsGridSectionPage.getInstance();
        subscriptions.clickSubscriptionNumberByStatus("Active");

        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage otherProductsGrid = SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance();
        HashMap<String,String> Bundle = new HashMap<String,String>();
        Bundle.put("Product Code","FLEXCAP - [02000-SB-A]");
        Bundle.put("Type","Bundle");
        Bundle.put("Description","Flexible Cap - £20 - [£20 safety buffer]");
        Assert.assertEquals(1, otherProductsGrid.getNumberOfOtherProductsByProduct(Bundle));

        HashMap<String,String> device = new HashMap<String,String>();
        device.put("Product Code","HTC-DESIRE-XXX-99");
        device.put("Type","Device");
        device.put("Description","HTC Desire HD");
        Assert.assertEquals(1, otherProductsGrid.getNumberOfOtherProductsByProduct(device));

        HashMap<String,String> agreement = new HashMap<String,String>();
        agreement.put("Product Code","AGRTESCOMOBILE");
        agreement.put("Type","Agreement");
        agreement.put("Description","Credit Agreement");
        Assert.assertEquals(1, otherProductsGrid.getNumberOfOtherProductsByProduct(agreement));

        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
    }


    private void verifyOldTariffOtherProductsDetails(){
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage subscriptions= CommonContentPage.SubscriptionsGridSectionPage.getInstance();
        subscriptions.clickSubscriptionNumberByStatus("Inactive");

        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage otherProductsGrid = SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance();
        HashMap<String,String> Bundle = new HashMap<String,String>();
        Bundle.put("Product Code","BUNDLER - [500MB-DATA-500-FC]");
        Bundle.put("Type","Bundle");
        Bundle.put("Description", "Discount Bundle Recurring - [Monthly 500MB data allowance]");
        Bundle.put("Start Date", Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), "dd MMM yyyy"));
        Bundle.put("End Date", Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), "dd MMM yyyy"));
        Bundle.put("Charge","£5.00");
        Assert.assertEquals(1, otherProductsGrid.getNumberOfOtherProductsByProduct(Bundle));

        HashMap<String,String> device = new HashMap<String,String>();
        device.put("Product Code","NK-2720");
        device.put("Type","Device");
        device.put("Description","Nokia 2720");
        Bundle.put("Start Date", Parser.parseDateFormate(newStartDate, "dd MMM yyyy"));
        Bundle.put("End Date", Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), "dd MMM yyyy"));
        Bundle.put("Charge","£0.00");
        Assert.assertEquals(1, otherProductsGrid.getNumberOfOtherProductsByProduct(device));

        HashMap<String,String> bundle2 = new HashMap<String,String>();
        bundle2.put("Product Code","FLEXCAP - [02000-SB-A]");
        bundle2.put("Type","Bundle");
        bundle2.put("Description","Flexible Cap - £20 - [£20 safety buffer]");
        Bundle.put("Start Date", Parser.parseDateFormate(newStartDate, "dd MMM yyyy"));
        Bundle.put("End Date", Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), "dd MMM yyyy"));
        Bundle.put("Charge","£0.00");
        Assert.assertEquals(1, otherProductsGrid.getNumberOfOtherProductsByProduct(bundle2));
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
    }

    private void verifyCreditAgreementDetails(){
        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();
        CreditAgreementsContentPage.CreditAgreementsGridPage creditAgreementsGridPage = CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance();
        List<String> list = new ArrayList<>();
        list.add(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        list.add(Parser.parseDateFormate(Date.valueOf(TimeStamp.TodayMinus1Day().toLocalDate().plusYears(2)), TimeStamp.DATE_FORMAT));
        list.add("Credit Agreement");
        list.add("£384.000");
        list.add("Active");
        Assert.assertEquals(1, Common.compareList(creditAgreementsGridPage.getAllValueCreditAgreement(), list));
    }

    private void verifyGetContractSummaryRequestAreCorrect(Xml xml){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH_mm_ss");
        String localTime = "_" + LocalTime.now().format(formatter);
        String actualFile = Common.saveXmlFile(customerNumber + localTime +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xml.toString())));
        String file =  Common.readFile("src\\test\\resources\\xml\\soho\\TC3799_response.xml")
                .replace("$accountNumber$", customerNumber)
                .replace("$accountName$", String.format("Mr %s %s", owsActions.firstName, owsActions.lastName))
                .replace("$accountstartDate$", Parser.parseDateFormate(newStartDate, "yyyy-MM-dd"))
                .replace("$subscriptionNumber$", subNo1)
                .replace("$tariffstartDate$", Parser.parseDateFormate(TimeStamp.Today(), "yyyy-MM-dd"))
                .replace("$tariffendDate$", Parser.parseDateFormate(Date.valueOf(TimeStamp.Today().toLocalDate().plusYears(2)), "yyyy-MM-dd"))
                .replace("$agreementNumber$", ccaNo1)
                .replace("$agreementstartDate$", Parser.parseDateFormate(TimeStamp.Today(), "yyyy-MM-dd"))
                .replace("$agreementendDate$", Parser.parseDateFormate(Date.valueOf(TimeStamp.TodayMinus1Day().toLocalDate().plusYears(2)), "yyyy-MM-dd"));
        String expectedResponseFile = Common.saveXmlFile(customerNumber + localTime +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(1, size);
    }


    private void verifyLiveBillEstimateAreCorrect(){
        MenuPage.LeftMenuPage.getInstance().clickLiveBillEstimateItem();
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription billEstimatePerSubscription = new LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(subNo1 + "  Mobile 1");
        billEstimatePerSubscription.expand();

        //subscription 1 should be active and it has payment and device in Adjustments, Charges and Credits
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription.AdjustmentsChargesAndCredits adjustmentsChargesAndCredits =  billEstimatePerSubscription.new AdjustmentsChargesAndCredits();
        adjustmentsChargesAndCredits.expand();
        List<String> list = new ArrayList<>();
        list.add(Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT));
        list.add(Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT));
        list.add("£0.00");
        list.add("Nokia 2720");
        Assert.assertEquals(1, Common.compareList(adjustmentsChargesAndCredits.getAllValueAdjustmentsOrders(), list));

        list = new ArrayList<>();
        list.add(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        list.add("HTC Desire HD");
        list.add("£99.00");
        Assert.assertEquals(1, Common.compareList(adjustmentsChargesAndCredits.getAllValueAdjustmentsOrders(), list));
    }
}
