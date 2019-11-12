package suite.regression.selfcare.changebundle;

import framework.utils.RandomCharacter;
import logic.business.db.OracleDB;
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
import org.omg.CORBA.TIMEOUT;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

public class TC2060_SelfCare_Change_Bundle_Permitted_Add_Bundle_at_next_bill_date extends BaseTest {
    String sub="07087501760";
    String serviceOrderId;
    String expectedStatus;
    String discountGroupCodeOfMobileRef1;
    String customerNumber;

    @Test(enabled = true, description = "TC2060 SelfCare Change Bundle permitted add bundle at next bill date", groups = "SelfCare")
    public void TC2060_SelfCare_Change_Bundle_Permitted_Add_Bundle_at_next_bill_date() {
//        Create order based on the request TC2060_createOrder.xml
//        It contains 1 BC subscription and simonly and 1 Promitional Bundle
        String TC2060_createOrderRequest = "src\\test\\resources\\xml\\SelfCare\\changebundle\\TC2060_createOrderRequest";
        test.get().info("Step 1 : Create a customer with  1 BC subscription and simonly and 1 Promitional Bundle");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(TC2060_createOrderRequest);
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile Ref 1");
        sub = owsActions.serviceRef;

        test.get().info("Step 2 : Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3: Update the payment collection date is 10");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set bill group for customer");
        customerNumber = owsActions.customerNo;
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

        test.get().info("Step 7 : verify 24 months unlimited display in my tariff details page");
        sub = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("").getMobilePhoneNumber();
        Assert.assertEquals(String.format("24 months' unlimited data   ACTIVE  as of  %s",
                Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("").getMonthlyBundles());

        test.get().info("Step 8 : Click add or change bundle button");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickAddOrChangeABundleButton();

        test.get().info("Step 9 : verify prices are correct before changing bundle");
        verifyPricesAreCorrectBeforeChangingBundle();


        test.get().info("Step 10 : Uncheck the existing monthly family perk bundle");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().selectBundlesByName("Monthly data bundle - 500MB");

        test.get().info("Step 11 : verify prices are correct before changing bundle");
        verifyPricesAreCorrectAfterChangingBundle();

        test.get().info("Step 12 : click save button");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().clickSaveBtn();


        test.get().info("Step 13 : verify bundle removed successfully message displayed in my tariff details page");
        String date = Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF);
        List<String> successfulMessage = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(2, successfulMessage.size());
        Assert.assertEquals("Thanks, the bundle changes you’ve made have been successful.", successfulMessage.get(0));
        Assert.assertEquals("Your changes will take effect from " + date, successfulMessage.get(1));


        String tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getSecondMonthlyBundles();
        Assert.assertEquals("Monthly data bundle - 500MB   PENDING activation  as of  " + date, tariff);
        tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getMonthlyBundles();
        Assert.assertEquals("24 months' unlimited data   ACTIVE  as of  " + Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF), tariff);

        test.get().info("Step 14 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(sub + " Mobile Ref 1");
        discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();


        test.get().info("Step 15 : verify a new service order created for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(sub, "Change Bundle", "Provision Wait");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(temp);
        Assert.assertEquals(1, size);
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");


        test.get().info("Step 16 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 17 :  Verify SO details data are correct");
        String serviceOrderId = TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getSoID();
        verifyChangeBundleSODetailsAreCorrect();

        test.get().info("Step 18 : Update provision date of change bundle service order");
        BillingActions.getInstance().updateProvisionDateOfChangeBundleServiceOrder(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 19 : Load customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 20 : Verify customer has 1 expected change bundle SO record");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        temp = ServiceOrderEntity.dataServiceOrder(sub, "Change Bundle", "Completed Task");
        size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(temp);
        Assert.assertEquals(1, size);

        test.get().info("Step 21 :Open details change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 22 :verify change bundle SO details are correct after complete");
        Assert.assertEquals("BC12-1000-100 £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("Monthly data bundle - 500MB;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());

        test.get().info("Step 23: reload customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 24 : verify the other product");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        HashMap<String, String> otherProductEnity = OtherProductEntiy.dataForOtherBundleProductWithStartDate("BUNDLER - [500MB-DATA-500]", "Bundle", TimeStamp.Today());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));


        test.get().info("Step 25: submit the do refill BC Job");
        BaseTest.submitDoRefillBCJob();
        test.get().info("Step 26 : submit the do refill NC Job");
        BaseTest.submitDoRefillNCJob();
        test.get().info("Step 27 : submit bundle review Job");
        BaseTest.submitDoBundleRenewJob();

        test.get().info("Step 28 : verify on new discount bundle entries have been created");
        List<DiscountBundleEntity> discountBundlesList = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(7, discountBundlesList.size());
        Assert.assertEquals(1, BillingActions.findDiscountBundlesByConditionByBundleCode(discountBundlesList, "BC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "BC100", "ACTIVE"));

        test.get().info("Step 29 : submit the draft bill run");
        submitDraftBillRun();

        test.get().info("Step 30: reload customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 31 : Open invoice details screen");
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);

        test.get().info("Step 33 :verify pdf invoice");
        viewInvoicePDF();

    }

    private void viewInvoicePDF( ) {
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().clickViewPDFBtn();
        String fileName = String.format("%s_%s_%s.pdf", "2060", RandomCharacter.getRandomNumericString(9), customerNumber);
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().savePDFFile(fileName);

        String adjustmentsChargesCredits = String.format("%s %s Monthly Family perk - 150 Mins (Capped) for %s 0.00",
                Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF),
                Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), sub);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);
        String str = null;
        for (int i = 0; i < pdfList.size(); i++) {
            if (pdfList.get(i).contains(adjustmentsChargesCredits))
                str = str + pdfList.get(i);
        }
        String expectString = String.format("%s %s 24 months' unlimited data for %s 0.00", Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), sub);
        Assert.assertEquals(pdfList.get(51),expectString);
        expectString =String.format("%s %s Monthly data bundle - 500MB for %s 5.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), sub);
        Assert.assertEquals(pdfList.get(52),expectString);
        expectString =String.format("%s %s 24 months' unlimited data for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), sub);
        Assert.assertEquals(pdfList.get(53),expectString);
    }

    private void verifyChangeBundleSODetailsAreCorrect() {
        Assert.assertEquals(String.format("*** Service Order has been set to Status of Provision Wait, and is due to be processed on %s ***", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)), TasksContentPage.TaskPage.DetailsPage.getInstance().getEndOfWizardMessage());
        Assert.assertEquals(sub + " Mobile Ref 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("BC12-1000-100 £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("No", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("Monthly data bundle - 500MB;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());


        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait")), 1);

        Assert.assertTrue(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDateTimeByIndex(2).startsWith(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT)));
    }



    private void verifyPricesAreCorrectBeforeChangingBundle() {
        Assert.assertEquals("£0.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("24 months' unlimited data"));
        Assert.assertEquals("£5.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("Monthly data bundle - 500MB",2));
        Assert.assertEquals("£7.50 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("Monthly data bundle - 1GB",2));
        Assert.assertEquals("£0.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getSecondTotalPrice());
        Assert.assertEquals("£0.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTotalPrice());
        Assert.assertEquals("£10.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTariffCharge());
        Assert.assertEquals("£10.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTotalMonthlyCharge());
    }

    private void verifyPricesAreCorrectAfterChangingBundle() {
        Assert.assertEquals("£5.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("Monthly data bundle - 500MB",2));
        Assert.assertEquals("£7.50 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("Monthly data bundle - 1GB",2));
        Assert.assertEquals("£5.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getSecondTotalPrice());
        Assert.assertEquals("£10.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTariffCharge());
        Assert.assertEquals("£15.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTotalMonthlyCharge());
    }
}
