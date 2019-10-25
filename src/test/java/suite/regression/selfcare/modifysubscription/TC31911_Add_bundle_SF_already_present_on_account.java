package suite.regression.selfcare.modifysubscription;

import framework.utils.Log;
import framework.utils.Soap;
import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.business.entities.SubscriptionEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TC31911_Add_bundle_SF_already_present_on_account extends BaseTest {
    /*
     * Tran Quoc Loi
     * */

    String mpn;
    String serviceOrder;

    @Test(enabled = true, description = "TC31911 add bundle SF already present on account", groups = "SelfCare")
    public void TC31911_Add_bundle_SF_already_present_on_account() {
        test.get().info("Step 1: Create a CC customer with no bundle and sim only");
        String path = "src\\test\\resources\\xml\\selfcare\\modifysubscription\\TC31911_createOrder";
        OWSActions owsActions = new OWSActions();
        mpn = owsActions.createACCCustomerWith1FCSubscriptions(path);
        String customerNumber = owsActions.customerNo;


        test.get().info("Step 2: create new billing group");
        createNewBillingGroup();
        test.get().info("Step 3: update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();
        test.get().info("Step 4: set bill group for customer");
        setBillGroupForCustomer(customerNumber);
        test.get().info("Step 5: update start date for customer");
        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus20Days());
        test.get().info("Step 6: Load customer in hub net ");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        HashMap<String, String> expectedSub = SubscriptionEntity.dataForFullSummarySubscriptions(TimeStamp.TodayMinus20Days(),
                "Safety Buffer - £20", "FC24-2000-500 - £20 Tariff 24 Month Contract - £20.00", mpn + " Mobile Ref 1", "Active");
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGridSectionPage.getInstance().getNumberOfSubscription(expectedSub));
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGridSectionPage.getInstance().getRowNumberOfSubscriptionsTable());
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickLinkByText(mpn + " Mobile Ref 1");

        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature(), "4G Service=ON");


        test.get().info("Step 7: Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePageWithOutPin(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 8: Access the tariff detail page");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 9: verify detail screen");
        verifyDetailsScreen();

        test.get().info("Step 10: click add or change a bundle button");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickAddOrChangeABundleButton();

        test.get().info("Step 11: verify monthly bundle displayed");
        SelfCareTestBase.page().verifyMonthlyBundleDisplayed();
        MonthlyBundlesAddChangeOrRemovePage.getInstance().selectBundlesByName();

        test.get().info("Step 12: select 3GB 4G data bunble and save changes");
        select3GB4GDataBundlesAndSaveChanges();

        test.get().info("Step 13: verify successfull message");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();
        List<String> successMssg = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(successMssg.get(0), "Thanks, the bundle changes you’ve made have been successful.");
        String mssg = String.format("Your changes will take effect from %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF));
        Assert.assertEquals(successMssg.get(1), mssg);

        String expectedResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getMonthlyBundles();
        Assert.assertEquals(String.format("4G data - 1GB   ACTIVE  as of  %s", Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF)), expectedResult.trim());

        expectedResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getSecondMonthlyBundles();
        Assert.assertEquals(String.format("4G data - 3GB   PENDING activation  as of  %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF)), expectedResult.trim());


        test.get().info("Step 14: Load  user in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 15: Go to service order page");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        verifyOneServiceOrderIsProvisionWait();

        test.get().info("Step 16: Run provision services job");
        updateThePDateAndBillDateForChangeBundleForSo(serviceOrder);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Step 17: refresh the customer");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 18: Go to service order page");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        verifyOneServiceOrderIsCompleteTask();
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Step 19: verify  SOA returns response as expected");
        String outPut = CommonActions.getResponse("GETRESPONSE", mpn);
        Assert.assertTrue(outPut.contains("<responseCode>901</responseCode>"));
        Assert.assertTrue(outPut.contains("<responseCodeDescription>4G already present</responseCodeDescription>"));

        test.get().info("Step 20: verify summary information is correct");
        verifySummaryInformationIsCorrect();

        test.get().info("Step 21: Open subscription details and 4G service is on");
        openSubscriptionDetailsAnd4GServiceIsOn();


    }


    public void openSubscriptionDetailsAnd4GServiceIsOn() {
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getServiceFeature(), "4G Service=ON");

        HashMap<String, String> otherProduct = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("SIM-ONLY", "SIM-Only", "Standard/Micro SIM", "£0.00", TimeStamp.TodayMinus20Days());

        HashMap<String, String> otherProduct1 = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("BUNDLER - [1GB-4GDATA-0750-FC]", "Bundle", "Discount Bundle Recurring - [4G data - 1GB]", "£7.50", TimeStamp.TodayMinus20Days());

        HashMap<String, String> otherProduct2 = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("FLEXCAP - [02000-SB-A]", "Bundle", "Flexible Cap - £20 - [£20 safety buffer]", "£0.00", TimeStamp.TodayMinus20Days());

        HashMap<String, String> otherProduct3 = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("BUNDLER - [3GB-4GDATA-1250-FC]", "Bundle", "Discount Bundle Recurring - [4G data - 3GB]", "£12.50", TimeStamp.Today());

        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable(), 4);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct1), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct2), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct3), 1);
    }

    public void verifySummaryInformationIsCorrect() {
        HashMap<String, String> expectedEnity = SubscriptionEntity.dataForFullSummarySubscriptions(TimeStamp.TodayMinus20Days(),
                "Safety Buffer - £20", "FC24-2000-500 - £20 Tariff 24 Month Contract - £20.00", mpn + " Mobile Ref 1", "Active");
        Assert.assertEquals(CommonContentPage.SubscriptionsGridSectionPage.getInstance().getNumberOfSubscription(expectedEnity), 1);
        Assert.assertEquals(CommonContentPage.SubscriptionsGridSectionPage.getInstance().getRowNumberOfSubscriptionsTable(), 1);
    }

    private void verifyOneServiceOrderIsProvisionWait() {
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");
        List<HashMap<String, String>> expectedSeriverOrder = ServiceOrderEntity.dataServiceOrderProvisionWaitChangeBundle(serviceOrder, mpn);
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(expectedSeriverOrder), 1);

        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrder);

        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Provision Wait");

        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber(), mpn + " Mobile Ref 1");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate(), Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded(), "4G data - 3GB;");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff(), "FC24-2000-500 £20 Tariff 24 Month Contract {£20.00}");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag(), "No");

    }

    private void verifyOneServiceOrderIsCompleteTask() {
        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");
        List<HashMap<String, String>> expectedSeriverOrder = ServiceOrderEntity.dataServiceOrderCompletedTask(serviceOrder, mpn);
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(expectedSeriverOrder), 1);

        ServiceOrdersContentPage.getInstance().clickServiceOrderIdLink(serviceOrder);

        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");

        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber(), mpn + " Mobile Ref 1");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate(), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded(), "4G data - 3GB;");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff(), "FC24-2000-500 £20 Tariff 24 Month Contract {£20.00}");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag(), "No");

    }

    private void verifyDetailsScreen() {
        Assert.assertEquals(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getDescription(), "Mobile Ref 1");
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").hasSaveButton());
        Assert.assertEquals(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getMobilePhoneNumber(), mpn);
        Assert.assertEquals(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getTariff(), "£20 Tariff 24 Month Contract");

        String expectedResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getStatus();
        Assert.assertEquals(String.format("ACTIVE   as of   %s", Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF)), expectedResult.trim());

        expectedResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getSafetyBuffer();
        Assert.assertEquals(String.format("£20 safety buffer    ACTIVE  as of  %s", Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF)), expectedResult.trim());

        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").hasAddOrChangeABundleButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").hasAddOrViewOneoffBundlesButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").hasAddOrChangeAFamilyPerkButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").hasUpdateButton());


    }

    public void select3GB4GDataBundlesAndSaveChanges() {
        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().getMobilePhoneNumber(), mpn + " - Mobile Ref 1");
        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().getTariff(), "£20 Tariff 24 Month Contract");
        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyAllowance(), "500 mins, 5000 texts (FC)");

        String expireDate = Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF);
        long numberRemainDays = Math.abs(TimeStamp.todayPlus1MonthMinus1DayMinusToday());
        String expectedMssg = String.format("%s  (%s  days remaining)", expireDate, String.valueOf(numberRemainDays));
        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthAllowanceExpiryDate(), expectedMssg);

        expectedMssg = String.format("Remaining allowance to be used by %s  :  1024 MB;", Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF));
        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleDescriptionByValue("4G data - 1GB"), expectedMssg);

        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().getMonthlyDataBundleByValue("4G data - 1GB"), "£7.50 per month");

        MonthlyBundlesAddChangeOrRemovePage.getInstance().selectBundlesByName("4G data - 3GB");

        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().getTotalPrice(), "£20.00 per month");
        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().totalMonthlyCharge(), "£40.00 per month");

        expectedMssg = "* Note: Saving this change will cancel any pending bundle changes made previously.";
        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().noteSavingMessage(), expectedMssg);

        expectedMssg = "Your bundle will be available for you to use from  " + Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF);
        Assert.assertEquals(MonthlyBundlesAddChangeOrRemovePage.getInstance().bundleAvailableDateMessage(), expectedMssg);


        SelfCareTestBase.page().clickSaveBtn();
    }

}
