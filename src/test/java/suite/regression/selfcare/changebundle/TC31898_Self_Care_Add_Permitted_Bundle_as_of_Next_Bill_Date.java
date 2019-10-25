package suite.regression.selfcare.changebundle;

import framework.utils.RandomCharacter;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.EventEntity;
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
import logic.pages.selfcare.MonthlyBundlesAddChangeOrRemovePage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Common;
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
import java.util.stream.Collectors;

public class TC31898_Self_Care_Add_Permitted_Bundle_as_of_Next_Bill_Date extends BaseTest {

    String serviceRefOf1stSubscription = "07223807610";
    String serviceOrderID;
    Date newStartDate;
    String discountGroupCodeOfMobileRef1;

    @Test(enabled = true, description = "TC31898 SelfCare add permitted bundle as of next bill date", groups = "SelfCare")
    public void TC31898_Self_Care_Add_Permitted_Bundle_as_of_Next_Bill_Date() {
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile Ref 1");
        serviceRefOf1stSubscription = owsActions.serviceRef;
        String customerNumber = owsActions.customerNo;
        String userName = owsActions.username;
        String password = owsActions.password;

        test.get().info("Step 2 : Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3: Update the payment collection date is 10");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5: Update the start date of customer");
        newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 7 : Verify customer data is updated ");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 8 : Verify all discount bundle entries align with bill run calendar entires");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");

        discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(newStartDate, discountGroupCodeOfMobileRef1);


        test.get().info("Step 9 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(userName, password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 10 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 11 : verify tariff detail screen");
        verifyTariffDetailScreen(serviceRefOf1stSubscription, newStartDate);

        test.get().info("Step 12 : Click add or change a bundle button");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickAddOrChangeABundleButton();

        test.get().info("Step 13 : Click add or change a bundle page is displayed");
        SelfCareTestBase.page().verifyMonthlyBundlesAddChangeOrRemovePageDisplayed();

        test.get().info("Step 14 : verify monthly bundles add change or remove page displayed correct");
        verifyMonthlyBundleAddChangeOrRemovePageResultIsCorrect();

        test.get().info("Step 15 : select monthly 1GD data allowance bundle");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().selectBundlesByName("Monthly 1GB data allowance");

        test.get().info("Step 16 : verify bundle results updated");
        Assert.assertEquals("£7.50 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("Monthly 1GB data allowance"));
        Assert.assertEquals("£5.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("Monthly 500MB data allowance"));
        Assert.assertEquals("£7.50 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTotalPrice());

        Assert.assertEquals("£10.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTariffCharge());
        Assert.assertEquals("£17.50 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTotalMonthlyCharge());

        test.get().info("Step 17 : click save button");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().clickSaveBtn();

        test.get().info("Step 18 : verify the tariff page is displayed with sucessfull message");
        List<String> mssg = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(2, mssg.size());

        Assert.assertEquals("Thanks, the bundle changes you’ve made have been successful.", mssg.get(0));
        Assert.assertEquals(String.format("Your changes will take effect from %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT4)), mssg.get(1));

        Assert.assertEquals(String.format("Monthly 1GB data allowance   PENDING activation  as of  %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT4)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getMonthlyBundles());

        test.get().info("Step 19 : Open service orders page in hub net for customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 20 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Provision Wait");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        Assert.assertEquals(1, size);

        test.get().info("Step 21 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 22 : Verify SO details data are updated");
        Assert.assertEquals(serviceRefOf1stSubscription + " Mobile Ref 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("Monthly 1GB data allowance;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        HashMap<String, String> enity = EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);
        serviceOrderID = TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getSoID();

        test.get().info("Step 23: update the PDate and BillDate for provision wait SO");
        BillingActions.getInstance().updateProvisionDateOfChangeBundleServiceOrder(serviceOrderID);

        test.get().info("Step 24: submit the do provision services batch job");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 25 : reload the user in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();


        test.get().info("Step 26 : Verify customer has 1 expected change bundle SO record");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Completed Task");
        size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(temp);
        serviceOrderID = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");
        Assert.assertEquals(1, size);

        test.get().info("Step 27 :Open details bundle");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 28 : Verify SO details data are updated");
        Assert.assertEquals(serviceRefOf1stSubscription + " Mobile Ref 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("Monthly 1GB data allowance;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

        Assert.assertEquals(3, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        enity = EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait", "Batch");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        enity = EventEntity.dataForEventServiceOrder("Service Order Completed", "Completed Task", "Batch");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        enity = EventEntity.dataForEventServiceOrder("PPB: AddSubscription: Request completed", "Completed Task", "Batch");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        test.get().info("Step 29: Login to self care without pin");
        SelfCareTestBase.page().LoginIntoSelfCarePageWithOutPin(userName, password, customerNumber);

        test.get().info("Step 30: Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 31: verify tariff page is updated after running provision");
        verifyTarfiffPageIsUpdatedAfterRunningProvision();

        test.get().info("Step 32: load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 33 : verify the one off bundle just added is listed in other products grid");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");
        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        HashMap<String, String> otherProductEnity = OtherProductEntiy.dataForOtherBundleProductNoEndDate("BUNDLER - [1GB-DATA-750-FC]", "Bundle", "Discount Bundle Recurring - [Monthly 1GB data allowance]", "£7.50", TimeStamp.Today());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

        test.get().info("Step 34 :verify one new discount bundle recoded generated customer");
        verifyOneNewDiscountBundleRecordGeneratedForCustomer(newStartDate);

        test.get().info("Step 35 : submit the do refill BC Job");
        BaseTest.submitDoRefillBCJob();
        test.get().info("Step 36 : submit the do refill NC Job");
        BaseTest.submitDoRefillNCJob();
        test.get().info("Step 37 : submit bundle review Job");
        BaseTest.submitDoBundleRenewJob();
        test.get().info("Step 38 : verify on new discount bundle entries have been created");
        verifyNewDiscountBundleDiscountBundleEntriesHaveBeenCreated();

        test.get().info("Step 39 : submit the draft bill run");
        submitDraftBillRun();
        test.get().info("Step 40 : submit the confirm bill run");
        submitConfirmBillRun();

        test.get().info("Step 41 : Open invoice details screen");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);

        test.get().info("Step 42 : download invoice");
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().clickViewPDFBtn();
        String fileName = String.format("%s_%s_%s.pdf", "TC31898", customerNumber, RandomCharacter.getRandomNumericString(9));
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().savePDFFile(fileName);

        test.get().info("Step 43 : download invoice");
        String adjustmentsChargesCredits = String.format("%s %s %s months' unlimited data for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription, serviceRefOf1stSubscription);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);

        verifyPDFFile(pdfList, newStartDate);


    }

    private void verifyTarfiffPageIsUpdatedAfterRunningProvision() {
        Assert.assertEquals("Mobile Ref 1", MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getDescription());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").hasSaveButton());
        Assert.assertEquals(serviceRefOf1stSubscription, MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getTariff());
        Assert.assertEquals(String.format("ACTIVE   as of   %s    ", Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF)), MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getStatus());
        Assert.assertEquals("£20 safety buffer    ACTIVE  as of  " + Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF), MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getSafetyBuffer());
        Assert.assertEquals("500 mins, 5000 texts (FC)", MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getMonthlyAllowance().trim());
        Assert.assertEquals(String.format("Monthly 1GB data allowance   ACTIVE  as of  %s", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF)), MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getMonthlyBundles());
    }

    private void verifyMonthlyBundleAddChangeOrRemovePageResultIsCorrect() {

        Assert.assertEquals(serviceRefOf1stSubscription + " - Mobile Ref 1", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTariff());
        Assert.assertEquals("500 mins, 5000 texts (FC)", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyAllowance());
        Assert.assertEquals("£20.00", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlySafetyBuffer());

        Assert.assertEquals("£7.50 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("Monthly 1GB data allowance"));
        Assert.assertEquals("£5.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("Monthly 500MB data allowance"));
        Assert.assertEquals("£0.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTotalPrice());

        String note = "* Note: Saving this change will cancel any pending bundle changes made previously.";
        Assert.assertEquals(note, MonthlyBundlesAddChangeOrRemovePage.getInstance().noteSavingMessage());
        Assert.assertTrue(MonthlyBundlesAddChangeOrRemovePage.getInstance().isNoteSavingMessageRed());

        String fairUsagePolicyMessage = "You can use all of this data in the UK and in Europe with Home From Home. Read our fair usage policy.";
        Assert.assertEquals(fairUsagePolicyMessage, MonthlyBundlesAddChangeOrRemovePage.getInstance().fairUsagePolicyMessage());
        Assert.assertTrue(MonthlyBundlesAddChangeOrRemovePage.getInstance().readOurFairUsagePolicyLinkDisplayed());

        String underneathLinkText = "* Note: To add a one-off bundle, go to the One-off bundle page";
        Assert.assertEquals(underneathLinkText, MonthlyBundlesAddChangeOrRemovePage.getInstance().underneathLinkText());
        Assert.assertTrue(MonthlyBundlesAddChangeOrRemovePage.getInstance().underneathLinkDisplayed());

        Assert.assertEquals("Your bundle will be available for you to use from  " + Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF), MonthlyBundlesAddChangeOrRemovePage.getInstance().bundleAvailableDateMessage());
        Assert.assertEquals("£10.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTariffCharge());
        Assert.assertEquals("£10.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTotalMonthlyCharge());

    }

    private void verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(Date newStartDate, String discountGroupCode) {
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(8, discountBundles.size());

        verifyFCDiscountBundles(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundles(discountBundles, newStartDate, "TMT5K");
    }


    private void verifyTariffDetailScreen(String serviceRefOf1stSubscription, Date newStartDate) {
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1");
        Assert.assertEquals("Mobile Ref 1", mobile1Tariff.getDescription());
        Assert.assertEquals(serviceRefOf1stSubscription, mobile1Tariff.getMobilePhoneNumber());
        Assert.assertTrue(mobile1Tariff.hasSaveButton());
        Assert.assertEquals("£10 Tariff 12 Month Contract", mobile1Tariff.getTariff());
        Assert.assertEquals(String.format("ACTIVE   as of   %s    ", Parser.parseDateFormate(newStartDate, "dd/MM/yyyy")), mobile1Tariff.getStatus());
        Assert.assertEquals("500 mins, 5000 texts (FC)", mobile1Tariff.getMonthlyAllowance().trim());
        Assert.assertEquals("£20 safety buffer    ACTIVE  as of  " + Parser.parseDateFormate(newStartDate, "dd/MM/yyyy"), mobile1Tariff.getSafetyBuffer());

        Assert.assertTrue(mobile1Tariff.hasChangeMySafetyBufferButton());
        Assert.assertTrue(mobile1Tariff.hasAddOrChangeABundleButton());
        Assert.assertTrue(mobile1Tariff.hasAddOrChangeAFamilyPerkButton());
        Assert.assertTrue(mobile1Tariff.hasAddOrViewOneoffBundlesButton());
        Assert.assertTrue(mobile1Tariff.hasUpdateButton());
    }

    private void verifyOneNewDiscountBundleRecordGeneratedForCustomer(Date newStartDate) {
        List<DiscountBundleEntity> discountBundleEntityList = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(10, discountBundleEntityList.size());

        verifyFCDiscountBundles(discountBundleEntityList, newStartDate, "FLX17");
        verifyNCDiscountBundles(discountBundleEntityList, newStartDate, "TM500");
        verifyNCDiscountBundles(discountBundleEntityList, newStartDate, "TMT5K");

        Assert.assertEquals(1, BillingActions.findDiscountBundlesByConditionByBundleCode(discountBundleEntityList, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "1GB-DATA-750-FC", "TM1GB", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.findDiscountBundlesByConditionByBundleCode(discountBundleEntityList, "NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "1GB-DATA-750-FC", "TM1GB", "ACTIVE"));

    }

    private void verifyNewDiscountBundleDiscountBundleEntriesHaveBeenCreated() {
        List<DiscountBundleEntity> discountBundleEntityList = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(11, discountBundleEntityList.size());
        Assert.assertEquals(1, BillingActions.findNewDiscountBundlesByCondition(discountBundleEntityList, "FC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "FLX17", "ACTIVE"));
    }

    private void verifyPDFFile(List<String> pdfFile, Date newStartDate) {

        Assert.assertEquals("Summary of charges", pdfFile.get(42));

        Assert.assertEquals("Tariff", pdfFile.get(45));

        Assert.assertEquals(String.format("Monthly subscription %s %s 10.00", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF)), pdfFile.get(46));

        Assert.assertEquals(String.format("Monthly subscription %s %s 10.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF)), pdfFile.get(47));


        String adjmtChargesAndCredits1 = String.format("%s %s Nokia 2720 for %s 0.00", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);
        String adjmtChargesAndCredits2 = String.format("%s %s Monthly 1GB data allowance for %s 7.50", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);

        String adjmtChargesAndCredits3 = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);
        String adjmtChargesAndCredits4 = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);

        String totalAdjmtChargesAndCredits = "Total Adjustments, charges & credits 7.50";
        String paymentRecived = "Payments received";
        String totalPayments = "Total Payments -10.00";


        String userChargeSubscription1 = String.format("User charges for %s  Mobile Ref 1 (£10 Tariff 12 Month Contract)", serviceRefOf1stSubscription);
        Assert.assertEquals(userChargeSubscription1, pdfFile.get(43));
        Assert.assertEquals(pdfFile.get(52), adjmtChargesAndCredits1);
        Assert.assertEquals(pdfFile.get(54), adjmtChargesAndCredits2);
        Assert.assertEquals(pdfFile.get(53), adjmtChargesAndCredits3);
        Assert.assertEquals(pdfFile.get(55), adjmtChargesAndCredits4);
        Assert.assertEquals(pdfFile.get(56), totalAdjmtChargesAndCredits);
        Assert.assertEquals(pdfFile.get(57), paymentRecived);
        Assert.assertEquals(pdfFile.get(60), totalPayments);
    }
}
