package suite.shakeout;

import javafx.util.Pair;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.*;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TC29787_Add_Permitted_Bundle_Apply_at_next_bill_date extends BaseTest {

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
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(fcSubText);
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
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(ncSubText);
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
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventServiceOrderCreated()));
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
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventServiceOrderCreated()));
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
        listServiceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderCompletedTaskChangeBundle(fcSubNumber));
        Assert.assertEquals(1, listServiceOrder.size());

        listServiceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderCompletedTaskChangeBundle(ncSubNumber));
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
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventServiceOrderCreated()));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundleProvisionWait()));

//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrderCreated("PPB: AddSubscription: Request completed","Completed Task")));
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrderCreated("Service Order Completed","Completed Task")));


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
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventServiceOrderCreated()));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(EventEntity.dataForEventChangeBundleProvisionWait()));

//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrderCreated("PPB: AddSubscription: Request completed","Completed Task")));
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrderCreated("Service Order Completed","Completed Task")));

        test.get().info("Step 40 : Back to customer");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 41 : Verify FC other products are updated");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(fcSubText);
        Assert.assertEquals(4, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().
                getNumberOfOtherProduct(OtherProductEntiy.dataBundleForOtherProduct("BUNDLER - [1GB-3GDATA-0500-FC]", "Bundle", "Discount Bundle Recurring - [3G data - 1GB]", "£5.00")));

        test.get().info("Step 42 : Verify the discount bundle record FC is updated");
        List<DiscountBundleEntity> bundleEntityList = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfFCMobile);
        Assert.assertEquals(13, bundleEntityList.size());
        verifyNewNCDiscountBundles(bundleEntityList, "TM1GB","1GB-3GDATA-0500-FC");

        test.get().info("Step 43 : Go back to subscriptions");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 44 : Verify the discount bundle record NC is updated");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(ncSubText);
        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().
                getNumberOfOtherProduct(OtherProductEntiy.dataBundleForOtherProduct("BUNDLER - [1GB-DATA-750-NC]", "Bundle", "Discount Bundle Recurring - [Monthly data bundle - 1GB (Capped)]", "£7.50")));

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

    private List<String> getAllSubscriptionsNumber(){
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        List<String> subscriptionNumberList = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            String subNo = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(i);
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
}
