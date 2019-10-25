package suite.regression.selfcare.changebundle;

import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.*;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.MonthlyBundlesAddChangeOrRemovePage;
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

public class TC31909_Subscription_with_SF_is_None_Add_PB_with_SF_is_4G extends BaseTest {

    String serviceRefOf1stSubscription;
    String serviceOrderID;
    Date newStartDate;
    String discountBundleGroupCode;

    @Test(enabled = true, description = "TC31909 Subscription with SF is none add PB with SF is 4G", groups = "SelfCare")
    public void TC31909_Subscription_with_SF_is_None_Add_PB_with_SF_is_4G() {
        String path = "src\\test\\resources\\xml\\selfcare\\changebundle\\TC31909_createOrder_request.xml";
        test.get().info("Step 1 : Create a customer with 2 NC subscription");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;

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

        test.get().info("Step 7 :open the subscription detail");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile BC");

        test.get().info("Step 8 : Verify subscription service feature is none");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile BC");
        Assert.assertEquals("None", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());


        test.get().info("Step 9 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 10 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 11 : verify  tariff detail page");
        verifyTariffDetailScreen();

        test.get().info("Step 12 : Click add or change a bundle button");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").clickAddOrChangeABundleButton();

        test.get().info("Step 13 : verify add or change a bundle page is displayed");
        SelfCareTestBase.page().verifyMonthlyBundlesAddChangeOrRemovePageDisplayed();

        test.get().info("Step 14 : select 1GB bundles ");
        select1GBBundles();

        test.get().info("Step 15 : click save button");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().clickSaveBtn();

        test.get().info("Step 16 : verify my tariff details page displayed with sucessfull message");
        List<String> alert = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(2, alert.size());
        Assert.assertEquals("Thanks, the bundle changes you’ve made have been successful.", alert.get(0));
        Assert.assertEquals(String.format("Your changes will take effect from %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)), alert.get(1));
        Assert.assertEquals(String.format("Monthly data bundle - 1GB   PENDING activation  as of  %s",
                Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").getMonthlyBundles());


        test.get().info("Step 17 : Open service orders page in hub net for customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 18 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Provision Wait");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        Assert.assertEquals(1, size);
        serviceOrderID = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");

        test.get().info("Step 19 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 20 : Verify SO details data are updated");
        Assert.assertEquals(serviceRefOf1stSubscription + " Mobile BC", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("BC12-1000-100 £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("No", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals("Monthly data bundle - 1GB;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("Provision Wait", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());

        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        HashMap<String, String> enity = EventEntity.dataForEventServiceOrder("Service Order set to Provision Wait", "Provision Wait");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);


        test.get().info("Step 21: update the PDate and BillDate for provision wait SO");
        BillingActions.getInstance().updateThePDateAndBillDateForChangeBundle(serviceOrderID);

        test.get().info("Step 22: submit the do provision services batch job");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 23 : reload the user in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();


        test.get().info("Step 24 : Verify customer has 1 expected change bundle SO record");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Completed Task");
        size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(temp);
        Assert.assertEquals(1, size);

        test.get().info("Step 25 :Open details bundle");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 26 : Verify SO details data are updated");
        Assert.assertEquals(serviceRefOf1stSubscription + " Mobile BC", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("BC12-1000-100 £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("No", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        Assert.assertEquals("Monthly data bundle - 1GB;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("Completed Task", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());

        enity = EventEntity.dataForEventServiceOrder("Service Feature 4G Service is added", "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);


        test.get().info("Step 27: reload customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 28 : verify summary information is correct");
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGridSectionPage.getInstance().getRowNumberOfSubscriptionsTable());

        HashMap<String, String> subscriptionEnity = SubscriptionEntity.dataForFullSummarySubscriptions(newStartDate, "Billing Cap Products", "BC12-1000-100 - £10 Tariff 12 Month Contract - £10.00", serviceRefOf1stSubscription + " Mobile BC", "Active");
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGridSectionPage.getInstance().getNumberOfSubscription(subscriptionEnity));

        test.get().info("Step 29 : Open subscription detail and 4G service is on");
        OpenSubscriptionDetailAnd4GServiceIsOn(newStartDate);
    }

    private void OpenSubscriptionDetailAnd4GServiceIsOn(Date newStartDate) {
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile BC");
        Assert.assertEquals("4G Service=ON", SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature());

        Assert.assertEquals(2, SubscriptionContentPage.TariffComponentsGridPage.getInstance().rowOfTariffComponents());
        HashMap<String, String> tariffComponentEnity = TariffComponentEntity.dataTariffComponentForNC("TMRENT", "TMRENT - Monthly subscription", newStartDate, "£10.00");
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.TariffComponentsGridPage.getInstance().getTariffComponents(tariffComponentEnity).size());

        tariffComponentEnity = TariffComponentEntity.dataTariffComponentForNC("TMOBUSE", "TMOBUSE - Usage", newStartDate, "BC-HS-1000-100");
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.TariffComponentsGridPage.getInstance().getTariffComponents(tariffComponentEnity).size());

        HashMap<String, String> otherProductEnity = OtherProductEntiy.dataForOtherBundleProductNoEndDate("SIM-ONLY", "SIM-Only", "Standard/Micro SIM", "£0.00", newStartDate);
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

        otherProductEnity = OtherProductEntiy.dataForOtherBundleProductNoEndDate("BUNDLER - [500MB-SWDATA-0]", "Bundle", "Discount Bundle Recurring - [Loyalty Bundle - 500MB]", "£0.00", newStartDate);
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

        otherProductEnity = OtherProductEntiy.dataForOtherBundleProductNoEndDate("BUNDLER - [1GB-DATA-750]", "Bundle", "Discount Bundle Recurring - [Monthly data bundle - 1GB]", "£7.50", TimeStamp.Today());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProductEnity));

    }

    private void select1GBBundles() {
        Assert.assertEquals(serviceRefOf1stSubscription + " - Mobile BC", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTariff());
        Assert.assertEquals("100 mins, 5000 texts", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyAllowance());
        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthAllowanceExpiryDate(),
                String.format("%s  (%s  days remaining)", Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF),
                        TimeStamp.todayPlus1MonthMinusTodayPlus1Day()));


        MonthlyBundlesAddChangeOrRemovePage.getInstance().selectBundlesByName("Monthly data bundle - 1GB");
        Assert.assertEquals("£7.50 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("Monthly data bundle - 1GB"));
        Assert.assertEquals("£5.00 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("Monthly data bundle - 500MB"));
        Assert.assertEquals("£7.50 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTotalPrice());

        String note = "* Note: Saving this change will cancel any pending bundle changes made previously.";
        Assert.assertEquals(note, MonthlyBundlesAddChangeOrRemovePage.getInstance().noteSavingMessage());

        Assert.assertTrue(MonthlyBundlesAddChangeOrRemovePage.getInstance().isNoteSavingMessageRed());

        String fairUsagePolicyMessage = "You can use all of this data in the UK and in Europe with Home From Home. Read our fair usage policy.";
        Assert.assertEquals(fairUsagePolicyMessage, MonthlyBundlesAddChangeOrRemovePage.getInstance().fairUsagePolicyMessage());

        Assert.assertTrue(MonthlyBundlesAddChangeOrRemovePage.getInstance().readOurFairUsagePolicyLinkDisplayed());

        String underneathLinkText = "* Note: To add a one-off bundle, go to the One-off bundle page";
        Assert.assertEquals(underneathLinkText, MonthlyBundlesAddChangeOrRemovePage.getInstance().underneathLinkText());

        Assert.assertTrue(MonthlyBundlesAddChangeOrRemovePage.getInstance().underneathLinkDisplayed());

        Assert.assertEquals("Your bundle will be available for you to use from  "
                        + Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF),
                MonthlyBundlesAddChangeOrRemovePage.getInstance().bundleAvailableDateMessage());

        Assert.assertEquals("£17.50 per month", MonthlyBundlesAddChangeOrRemovePage.getInstance().getTotalMonthlyCharge());
    }

    private void verifyTariffDetailScreen() {

        Assert.assertEquals("Mobile BC", MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").getDescription());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").hasSaveButton());
        Assert.assertEquals(serviceRefOf1stSubscription, MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").getTariff());
        Assert.assertEquals(String.format("ACTIVE   as of   %s    ", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT_IN_PDF)), MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").getStatus());
        Assert.assertEquals("100 mins, 5000 texts", MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").getMonthlyAllowance().trim());

        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").hasAddOrChangeABundleButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").hasAddOrChangeAFamilyPerkButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").hasAddOrViewOneoffBundlesButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile BC").hasUpdateButton());

    }


}
