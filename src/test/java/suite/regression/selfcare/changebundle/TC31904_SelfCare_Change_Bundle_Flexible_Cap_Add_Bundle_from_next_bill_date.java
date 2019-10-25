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

public class TC31904_SelfCare_Change_Bundle_Flexible_Cap_Add_Bundle_from_next_bill_date extends BaseTest {
    String serviceRefOf1stSubscription;
    String serviceOrderID;
    String discountBundleGroupCode;
    String expectedStatus;

    @Test(enabled = true, description = "TC31904 SelfCare bundle Flexible Cap Add Bundle from next bill date", groups = "SelfCare")
    public void TC31904_SelfCare_Change_Bundle_Flexible_Cap_Add_Bundle_from_next_bill_date() {

        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_of_SB_and_sim_only";
        test.get().info("Step 1 : Create a customer with 2 NC subscription");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile Ref 1");
        serviceRefOf1stSubscription = owsActions.serviceRef;

        test.get().info("Step 2 : Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3: Update the payment collection date is 10");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5: Update the start date of customer");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 7 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 8 : Click add or change bundles on my tariff page");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickAddOrChangeABundleButton();
        SelfCareTestBase.page().verifyMonthlyBundlesAddChangeOrRemovePageDisplayed();


        test.get().info("Step 9 : change bundle for customer");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().selectBundlesByName("Monthly 500MB data allowance");

        test.get().info("Step 10 : click save changes ");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().clickSaveBtn();

        test.get().info("Step 11 :verify my tariff details page displayed with correct data");
        verifyMyTariffDetailPageDisplayedWithCorrectData();

        test.get().info("Step 11 :verify my tariff details page displayed with correct data");
        verifyMyTariffDetailPageDisplayedWithCorrectData();

        test.get().info("Step 11 : click my account tab");
        SelfCareTestBase.page().clickMyAccountTab();

        test.get().info("Step 7 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();


        test.get().info("Step 9 : verify monthly 500MB data allowance display in my tariff  details page");
        Assert.assertEquals(String.format("Monthly 500MB data allowance   PENDING activation  as of  %s",
                Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT4)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getMonthlyBundles());

        test.get().info("Step 12 : Open service orders page in hub net for customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 13 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Provision Wait");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        serviceOrderID = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");
        Assert.assertEquals(1, size);

        test.get().info("Step 14 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 15 : Verify SO details data are updated");
        Assert.assertEquals(String.format("*** Service Order has been set to Status of Provision Wait, and is due to be processed on %s ***", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)), TasksContentPage.TaskPage.DetailsPage.getInstance().getEndOfWizardMessage());
        Assert.assertEquals(serviceRefOf1stSubscription + " Mobile Ref 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("No", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("Monthly 500MB data allowance;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());

        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        HashMap<String, String> enity = EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        Assert.assertTrue(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDateTimeByIndex(2).startsWith(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT)));

        test.get().info("Step 16: update the PDate and BillDate for provision wait SO");
        BillingActions.getInstance().updateThePDateAndBillDateForChangeBundle(serviceOrderID);

        test.get().info("Step 17: submit the do provision services batch job");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 18 : reload the user in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 19 : Verify customer has 1 expected change bundle SO record");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Completed Task");
        size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(temp);
        Assert.assertEquals(1, size);

        test.get().info("Step 20 :Open details bundle");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 21 : Verify SO details data are updated");
        Assert.assertEquals(serviceRefOf1stSubscription + " Mobile Ref 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("No", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("Monthly 500MB data allowance;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

        Assert.assertEquals(3, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        enity = EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait", "Batch");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        enity = EventEntity.dataForEventServiceOrder("Service Order Completed", "Completed Task", "Batch");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);


        test.get().info("Step 22: reload customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 23 : verify the bundle has been removed");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        serviceRefOf1stSubscription = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();
        discountBundleGroupCode = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        HashMap<String, String> otherProductEnity = OtherProductEntiy.dataForOtherBundleProductWithStartDate("BUNDLER - [500MB-DATA-500-FC]", "Bundle", TimeStamp.Today());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));


        test.get().info("Step 26: submit the do refill BC Job");
        BaseTest.submitDoRefillBCJob();
        test.get().info("Step 27 : submit the do refill NC Job");
        BaseTest.submitDoRefillNCJob();
        test.get().info("Step 28 : submit bundle review Job");
        BaseTest.submitDoBundleRenewJob();

        test.get().info("Step 30 : verify on new discount bundle entries have been created");
        List<DiscountBundleEntity> discountBundlesList = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountBundleGroupCode);
        Assert.assertEquals(11, discountBundlesList.size());
        Assert.assertEquals(1, BillingActions.findDiscountBundlesByConditionByPartitionIdRef(discountBundlesList, "FC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "FLX17", "ACTIVE"));

        test.get().info("Step 31 : submit the draft bill run");
        submitDraftBillRun();

        test.get().info("Step 32 : Open invoice details screen");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);

        test.get().info("Step 32 : download invoice");
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().clickViewPDFBtn();
        String fileName = String.format("%s_%s_%s.pdf", "TC31904", customerNumber, RandomCharacter.getRandomNumericString(9));
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().savePDFFile(fileName);

        test.get().info("Step 33 :verify pdf invoice");
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);
        verifyPDFFile(pdfList, newStartDate);
    }

    private void verifyPDFFile(List<String> pdfFile, Date newStartDate) {


        String adjmtChargesAndCredits1 = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);
        String adjmtChargesAndCredits2 = String.format("%s %s Monthly 500MB data allowance for %s 5.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);
        String adjmtChargesAndCredits3 = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);

        Assert.assertEquals(pdfFile.get(51), adjmtChargesAndCredits1);
        Assert.assertEquals(pdfFile.get(52), adjmtChargesAndCredits2);
        Assert.assertEquals(pdfFile.get(53), adjmtChargesAndCredits3);

    }


    public int findDiscountBundlesByCondition(List<DiscountBundleEntity> allDiscountBundles, String status, String bundleCode) {
        return Integer.parseInt(String.valueOf(allDiscountBundles.stream().filter(x -> x.bundleCode.equalsIgnoreCase(bundleCode) && x.status.equalsIgnoreCase(status)).count()));
    }

    public int findDiscountBundlesByStatus(List<DiscountBundleEntity> allDiscountBundles, String status) {
        return Integer.parseInt(String.valueOf(allDiscountBundles.stream().filter(x -> x.status.equalsIgnoreCase(status)).count()));
    }


    private void verifyMyTariffDetailPageDisplayedWithCorrectData() {
        List<String> alert = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(2, alert.size());
        Assert.assertEquals("Thanks, the bundle changes you’ve made have been successful.", alert.get(0));
        Assert.assertEquals(String.format("Your changes will take effect from %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)), alert.get(1));
    }

    private void verifyDiscountBundleRowsHaveBeenMarkAsDeleted() {
        List<DiscountBundleEntity> discountBundleEntityList = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountBundleGroupCode);
        Assert.assertEquals(3, findDiscountBundlesByCondition(discountBundleEntityList, expectedStatus, "500MB-DATA-500-FC"));
    }

    private void verifyDiscountBundleRowsHaveNotBeenMarkAsDeleted() {
        List<DiscountBundleEntity> discountBundleEntityList = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountBundleGroupCode);
        Assert.assertEquals(11, discountBundleEntityList.size());
        Assert.assertEquals(8, findDiscountBundlesByStatus(discountBundleEntityList, expectedStatus));
    }


}
