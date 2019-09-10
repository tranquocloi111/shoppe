package suite.regression.selfcare.changebundle;


import logic.business.db.billing.BillingActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.EventEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.AddOrChangeAFamilyPerkPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

import logic.pages.care.find.SubscriptionContentPage;
import suite.regression.selfcare.SelfCareTestBase;


/**
 * User: Loi Tran
 * Date: 31/07/2019
 */
public class TC31887_Self_Care_Add_Family_Perk_Bundle_to_FC_tariff extends BaseTest {
    @Test(enabled = true, description = "TC31887 Self Care Add Family Perk Bundle to FC tariff", groups = "SelfCare")
    public void TC31887_Self_Care_Add_Family_Perk_Bundle_to_FC_tariff() {

        String TC31887_CREATE_ORDER = "src\\test\\resources\\xml\\selfcare\\changebundle\\TC31887_CreateOrder";
        test.get().info("Step 1 : Create a CC customer, having family perk Bundle");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(TC31887_CREATE_ORDER);

        test.get().info("Step 2 : Create the new billing group");
        BaseTest.createNewBillingGroupToMinus15days();

        test.get().info("Step 3: Update the payment collection date is 10");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5: Update the start date of customer");
        Date newStartDate = TimeStamp.TodayMinus10Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 7 : Verify customer data is updated ");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 8 : Verify all discount bundle entries align with bill run calendar entires");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 1");
        String serviceRefOf2stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 2");

        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " FC Mobile 1");
        String discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(newStartDate, discountGroupCodeOfMobileRef1);

        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf2stSubscription + " FC Mobile 2");
        String discountGroupCodeOfMobileRef2 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(newStartDate, discountGroupCodeOfMobileRef2);


        test.get().info("Step 11 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();


        test.get().info("Step 12 : Verify the Alert");
        String alert1 = MyPersonalInformationPage.myAlertSection.getInstance().getAlertMessageByText(serviceRefOf1stSubscription);
        String alert2 = MyPersonalInformationPage.myAlertSection.getInstance().getAlertMessageByText(serviceRefOf2stSubscription);

        String expectAlert1 = String.format("Don’t forget! You can get a free monthly Family Perk for %s. Click here to choose the one you want.", serviceRefOf1stSubscription);
        String expectAlert2 = String.format("Don’t forget! You can get a free monthly Family Perk for %s. Click here to choose the one you want.", serviceRefOf2stSubscription);

        Assert.assertEquals(expectAlert1, alert1);
        Assert.assertEquals(expectAlert2, alert2);


        test.get().info("Step 12 : Click free family perk link of service reference of 1st Subscription");
        MyPersonalInformationPage.myAlertSection.getInstance().clickAlertMessageByText(serviceRefOf1stSubscription);
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 13 : Verify tariff details screen ");
        verifyTariffDetailScreen(serviceRefOf2stSubscription, serviceRefOf1stSubscription, newStartDate);

        test.get().info("step 14: Click add or change a family perk button for mobile 1");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1");
        mobile1Tariff.clickAddOrChangeAFamilyPerkBtn();
        SelfCareTestBase.page().verifyAddOrChangeAFamilyPerkIsDisplayed();

        test.get().info("step 15: Verify mobile for mobile phone 1 is correct");
        AddOrChangeAFamilyPerkPage.InfoPage infoPage = AddOrChangeAFamilyPerkPage.InfoPage.getInstance();
        Assert.assertEquals(serviceRefOf1stSubscription + " - FC Mobile 1", infoPage.getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", infoPage.getTariff());
        Assert.assertEquals("500 mins, 5000 texts (FC)", infoPage.getMonthlyAllowance());
        Assert.assertTrue(infoPage.getMonthlyBundles().isEmpty());

        test.get().info("Step 16 : Verify expected warning message displayed");
        String message = String.format("Your changes will apply immediately.");
        Assert.assertEquals(message, AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getWarningMessage());

        test.get().info("Step 17 : Verify family perk bundle table is correct before select bundle");
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 1));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 1));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 2));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 2));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 4));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 4));

        test.get().info("Step 18 : Select 1 family perk bundle and accept terms and conditions");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().selectBundlesByName("Family perk - 500 Tesco Mobile only minutes per month");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().tickBoxToAcceptTheFamilyPerkTermsAndConditions();

        test.get().info("Step 19 : Verify family perk bundle table is correct before select bundle");
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 1));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 1));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 2));
        Assert.assertEquals("5000", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 2));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 3));
        Assert.assertEquals("500", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 3));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("Current allowance", 4));
        Assert.assertEquals("0", AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().getTextsRow("New allowance", 4));

        test.get().info("Step 20 : Click save changes button");
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().tickBoxToAcceptTheFamilyPerkTermsAndConditions();
        AddOrChangeAFamilyPerkPage.BundleAllowancePage.getInstance().clickSaveButton();

        test.get().info("Step 21 : Verify my tariff details page displayed with successful alert");
        List<String> listMessage = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(1, listMessage.size());
        Assert.assertEquals("Thanks, the bundle changes you’ve made have been successful.", listMessage.get(0));


        mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1");
        List<String> familyPerks = mobile1Tariff.familyPerkStack();
        String expectedResult = String.format("Family perk - 500 Tesco Mobile only minutes per month   ACTIVE  as of%s", Parser.parseDateFormate(TimeStamp.Today(), "dd/MM/yyyy")).replaceAll("\\s", "");
        Assert.assertEquals(expectedResult, familyPerks.get(0).replaceAll("\\s", ""));


        test.get().info("Step 22 : Open sevice orders page in hub net for customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 23 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Completed Task");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        Assert.assertEquals(1, size);

        test.get().info("Step 24 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 25 : Verify SO details data are correct");
        Assert.assertEquals(serviceRefOf1stSubscription + " FC Mobile 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("Family perk - 500 Tesco Mobile only minutes per month;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

        Assert.assertEquals(4, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(3, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("PPB: AddSubscription: Request completed", "Completed Task")));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("Service Order Completed", "Completed Task")));

        test.get().info("Step 26 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 27 : Open details for customer 1st subscription");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " FC Mobile 1");

        test.get().info("Step 28: Verify the one off bundle just added is listed in other products grid");
        HashMap<String, String> otherProducts = OtherProductEntiy.dataForAnOtherBundleProduct("BUNDLER - [500-FONMIN-0-FC]", "Bundle", "Discount Bundle Recurring - [Family perk - 500 Tesco Mobile only minutes per month]", "£0.00", newStartDate);
        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage otherProductsGridSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance();
        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProductsByProduct(otherProducts));


        test.get().info("Step 29: Verify 1 new discount bundle record generated for customer");
        discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCodeOfMobileRef1);
        Assert.assertEquals(11, discountBundles.size());
        verifyDiscountBundlesForNewNCTariff(discountBundles);

    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }

    private void verifyTariffDetailScreen(String serviceRefOf2stSubscription, String serviceRefOf1stSubscription, Date newStartDate) {
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

        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1");
        Assert.assertEquals("FC Mobile 1", mobile1Tariff.getDescription());
        Assert.assertEquals(serviceRefOf1stSubscription, mobile1Tariff.getMobilePhoneNumber());
        Assert.assertTrue(mobile1Tariff.hasSaveButton());
        Assert.assertEquals("£10 Tariff 12 Month Contract", mobile1Tariff.getTariff());
        Assert.assertEquals(String.format("ACTIVE   as of   %s    ", Parser.parseDateFormate(newStartDate, "dd/MM/yyyy")), mobile1Tariff.getStatus());
        Assert.assertEquals("£20 safety buffer    ACTIVE  as of  " + Parser.parseDateFormate(newStartDate, "dd/MM/yyyy"), mobile1Tariff.getSafetyBuffer());
        Assert.assertTrue(mobile1Tariff.hasChangeMySafetyBufferButton());
        Assert.assertTrue(mobile1Tariff.hasAddOrChangeABundleButton());
        Assert.assertTrue(mobile1Tariff.hasAddOrChangeAFamilyPerkButton());
        Assert.assertTrue(mobile1Tariff.hasAddOrViewOneoffBundlesButton());
        Assert.assertTrue(mobile1Tariff.hasUpdateButton());
    }

    private void verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(Date newStartDate, String discountGroupCode) {
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(8, discountBundles.size());

        verifyFCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TMT5K");
    }

    private static void verifyDiscountBundlesForNewNCTariff(List<DiscountBundleEntity> allDiscountBundles) {
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByBundleCode(allDiscountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayMinus16DaysAdd2Months(), "500-FONMIN-0-FC", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByBundleCode(allDiscountBundles, "NC", TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.TodayMinus16DaysAdd2Months(), "500-FONMIN-0-FC", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByBundleCode(allDiscountBundles, "NC", TimeStamp.TodayMinus15DaysAdd2Months(), TimeStamp.TodayMinus16DaysAdd3Months(), "500-FONMIN-0-FC", "ACTIVE"));
    }

}
