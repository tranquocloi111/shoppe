package suite.regression.selfcare.changebundle;

import framework.utils.RandomCharacter;
import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.EventEntity;
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
    String sub;
    String serviceOrderId;
    String expectedStatus;
    String discountGroupCodeOfMobileRef1;
    String customerNumber;

    @Test(enabled = true, description = "TC31889 SelfCare Change Bundle Remove Family Perk Bundle on next bill date", groups = "SelfCare")
    public void TC31889_SelfCare_Change_Bundle_Remove_Family_Perk_Bundle_on_next_bill_date() {
        //Create order based on the request TC2060_createOrder.xml
        //It contains 1 BC subscription and simonly and 1 Promitional Bundle
        String TC2060_createOrderRequest = "src\\test\\resources\\xml\\SelfCare\\changebundle\\TC2060_createOrderRequest";
        test.get().info("Step 1 : Create a customer with  1 BC subscription and simonly and 1 Promitional Bundle");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(TC2060_createOrderRequest);
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile1 - NC");
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
        Assert.assertEquals(String.format("24 months' unlimited data   ACTIVE  as of  {0}",
                Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("").getMonthlyBundles());

        test.get().info("Step 8 : Click add or change bundle button");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC").clickAddOrChangeABundleButton();

        test.get().info("Step 9 : Verify expected warning message displayed");
        String message = String.format("Your changes will take effect from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), "dd/MM/yyyy"));
        Assert.assertEquals(message, AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getWarningMessage());

        test.get().info("Step 10 : Uncheck the existing monthly family perk bundle");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().unselectBundlesByName("Monthly Family perk - 150 Mins (Capped)");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().tickBoxToAcceptTheFamilyPerkTermsAndConditions();


        test.get().info("Step 11 : verify bundle removed successfully message displayed in my tariff details page");
        String date = Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF);
        List<String> successfulMessage = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(1, successfulMessage.size());
        Assert.assertEquals("Your changes will take effect from " + date, successfulMessage.get(0));


        String tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile1 - NC").getMonthlyBundles();
        Assert.assertEquals("Monthly Family perk - 150 Mins (Capped)   PENDING removal  as of  " + date, tariff);

        test.get().info("Step 12 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(sub + " Mobile1 - NC");
        discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();

        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(9, discountBundles.size());

        test.get().info("Step 13 : verify a new service order created for cusomter");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(sub, "Change Bundle", "Completed Task");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(temp);
        Assert.assertEquals(1, size);
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");


        test.get().info("Step 14 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 15 :  Verify SO details data are correct");
        String serviceOrderId = TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getSoID();
        verifyChangeBundleSODetailsAreCorrect();

        test.get().info("Step 16 : Update provision date of change bundle service order");
        BillingActions.getInstance().updateProvisionDateOfChangeBundleServiceOrder(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 17 : Load customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 18 : Verify customer has 1 expected change bundle SO record");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        temp = ServiceOrderEntity.dataServiceOrder(sub, "Change Bundle", "Completed Task");
        size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(temp);
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");
        Assert.assertEquals(1, size);

        test.get().info("Step 19 :Open details bundle");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 20 :verify change bundle SO details are correct after complete");
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());

        test.get().info("Step 21 : Load customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 22 : verify the famlily perk has been removed");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(sub + " Mobile1 - NC");
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());

        test.get().info("Step 23 : verify discount bundle rows have marked as deleted");
        expectedStatus = "DELETED";
        verifyDiscountBundleRowsHaveBeenMarkAsDeleted();

        test.get().info("Step 24 : verify discount bundle rows have not marked as deleted");
        expectedStatus = "Active";
        verifyDiscountBundleRowsHaveBeenMarkAsDeleted();

        test.get().info("Step 25 : submit the do refill BC Job");
        BaseTest.submitDoRefillBCJob();
        test.get().info("Step 26 : submit the do refill NC Job");
        BaseTest.submitDoRefillNCJob();
        test.get().info("Step 27 : submit bundle review Job");
        BaseTest.submitDoBundleRenewJob();

        test.get().info("Step 28 : verify no new discount bundle entries have been created");
        discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(9, discountBundles.size());

        test.get().info("Step 29 : submit the draft bill run");
        submitDraftBillRun();

        test.get().info("Step 30 : Open invoice details screen");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(0);

        test.get().info("Step 30 : view invoice PDF");
        viewInvoicePDF();

    }

    private void viewInvoicePDF() {
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().clickViewPDFBtn();
        String fileName = String.format("%s_%s_%s.pdf", "2060", RandomCharacter.getRandomNumericString(9),customerNumber);
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().savePDFFile(fileName);

        String adjustmentsChargesCredits = String.format("%s %s Monthly Family perk - 150 Mins (Capped) for %s 0.00",
                Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF),
                Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), sub);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);
        String str=null;
        for (int i = 0; i < pdfList.size(); i++) {
            if (pdfList.get(i).contains(adjustmentsChargesCredits))
                str = str + pdfList.get(i);
        }
        String expectString=  String.format("%s %s 24 months' unlimited data for %s 0.00", Parser.parseDateFormate(TimeStamp.TodayMinus20Days(),TimeStamp.DATE_FORMAT_IN_PDF),  Parser.parseDateFormate(TimeStamp.TodayMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), sub);
        Assert.assertTrue(str.contains(expectString));
        String.format("%s %s Monthly data bundle - 500MB for %s 5.00", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_IN_PDF),  Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT_IN_PDF), sub);
        Assert.assertTrue(str.contains(expectString));
        String.format("%s %s 24 months' unlimited data for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_IN_PDF),  Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(),TimeStamp.DATE_FORMAT_IN_PDF), sub);
        Assert.assertTrue(str.contains(expectString));
    }

    private void verifyChangeBundleSODetailsAreCorrect() {
        Assert.assertEquals(String.format("*** Service Order has been set to Status of Provision Wait, and is due to be processed on {0} ***", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)), TasksContentPage.TaskPage.DetailsPage.getInstance().getEndOfWizardMessage());
        Assert.assertEquals(sub + " Mobile1 - NC", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("NC1-1000-250 £10 SIM Only Tariff {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("No", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("Monthly Family perk - 150 Mins (Capped);", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertTrue(TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded() == null);
        Assert.assertTrue(TasksContentPage.TaskPage.DetailsPage.getInstance().getEUDataConsentFlag() == null);

        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait"), 1);

        Assert.assertTrue(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDateTimeByIndex(0).startsWith(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT)));
    }

    private void verifyDiscountBundleRowsHaveBeenMarkAsDeleted() {
        String sql = String.format("select status, deletedate, deletehitransactionid from discountbundle where discgrpcode = %s and deletehitransactionid = %s", discountGroupCodeOfMobileRef1, serviceOrderId);
        try {
            ResultSet rs = OracleDB.SetToNonOEDatabase().executeQuery(sql);
            while (rs.next()) {
                Assert.assertEquals(expectedStatus, rs.getString(0));
                Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), rs.getString(1));
                Assert.assertEquals(serviceOrderId, rs.getString(2));
            }
        } catch (Exception ex) {
        }
    }
}
