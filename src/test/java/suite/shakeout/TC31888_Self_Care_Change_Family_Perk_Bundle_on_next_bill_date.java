package suite.shakeout;

import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.AddOrChangeAFamilyPerkPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

public class TC31888_Self_Care_Change_Family_Perk_Bundle_on_next_bill_date extends BaseTest{

    @Test(enabled = false, description = "TC31888 Self Care Change Family Perk Bundle on next bill date", groups = "Smoke")
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
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 1");
        String serviceRefOf2stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 2");

        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " FC Mobile 1");
        String discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyDiscountBundleBeforeChangingBundle(newStartDate, discountGroupCodeOfMobileRef1);

        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf2stSubscription + " FC Mobile 2");
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
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrderCreated("Service Order set to Provision Wait","Provision Wait")));
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
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrderCreated("Service Order set to Provision Wait","Provision Wait")));
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrderCreated("PPB: AddSubscription: Request completed","Completed Task")));
//        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrderCreated("Service Order Completed","Completed Task")));

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
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " FC Mobile 1");

        test.get().info("Step 38 : Verify the one off bundle just added is listed in other products grid");
        List<HashMap<String,String>> otherProducts = OtherProductEntiy.dataSampleForOtherProduct(newStartDate);
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

    private void verifyDiscountBundleBeforeChangingBundle(Date newStartDate ,String  discountGroupCode){
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(11, discountBundles.size());
        verifyFCDiscountBundles(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TMDAT");
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
}
