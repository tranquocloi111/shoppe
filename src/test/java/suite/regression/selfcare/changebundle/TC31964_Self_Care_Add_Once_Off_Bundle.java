package suite.regression.selfcare.changebundle;

import framework.utils.RandomCharacter;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.EventEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.business.entities.selfcare.OneOffBundleEnity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.AddOneOffBundle;
import logic.pages.selfcare.MonthlyBundlesAddChangeOrRemovePage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.apache.http.util.Asserts;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

public class TC31964_Self_Care_Add_Once_Off_Bundle extends BaseTest {
    String serviceRefOf1stSubscription;
    String serviceOrderID;
    String serviceOrderIDSms;
    String discountBundleGroupCode;
    String expectedStatus;

    @Test(enabled = true, description = "TC31964 SelfCare add once off bundle", groups = "SelfCare")
    public void TC31964_Self_Care_Add_Once_Off_Bundle() {

        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
        test.get().info("Step 1 : Create a customer with 2 NC subscription");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber =  owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile Ref 1");
        serviceRefOf1stSubscription = owsActions.serviceRef;

        test.get().info("Step 2 : Create the new billing group");
        BaseTest.createNewBillingGroupToMinus15days();

        test.get().info("Step 3: Update the payment collection date is 10");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set bill group for customer");
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
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");

        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");
        discountBundleGroupCode = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(newStartDate, discountBundleGroupCode);


        test.get().info("Step 9 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 10 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 11 : Click add or change bundles on my tariff page");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickAddOrViewOneOffBundlesButton();

        test.get().info("Step 12 : verify one off bundle screen displayed with correct data");
        verifyAddOneOffBundleScreenDisplayedWithCorrectData();

        test.get().info("Step 13 : change bundle for customer");
        AddOneOffBundle.getInstance().selectBundleByName("One-off data 250MB");

        test.get().info("Step 14 : verify one off bundle charge values are changed after selecting");


        test.get().info("Step 15 : click save changes ");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().clickSaveBtn();

        test.get().info("Step 16 :verify my tariff details page displayed with correct data");
        verifyMyTariffDetailPageDisplayedWithCorrectData();

        test.get().info("Step 17 : Open service orders page in hub net for customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 18 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Completed Task");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        serviceOrderIDSms = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Ad-hoc SMS Messages");
        serviceOrderID = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");
        Assert.assertEquals(1, size);

        test.get().info("Step 19 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 20 : Verify SO details data are updated");
        Assert.assertEquals(serviceRefOf1stSubscription + " Mobile Ref 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("One-off data 250MB;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());

        Assert.assertEquals(3, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        HashMap<String, String> enity = EventEntity.dataForEventServiceOrder("PPB: AddSubscription: Request completed", "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        enity = EventEntity.dataForEventServiceOrder("Service Order Completed", "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);

        enity = EventEntity.dataForEventServiceOrder("SMS Request Completed", "Completed Task");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(enity), 1);
        Assert.assertTrue(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDateTimeByIndex(2).startsWith(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT)));


        test.get().info("Step 21: verify context info of SMS service order is correct DB in db");
        verifyContextInfoOfSMSServiceOrderIsCorrectInDB();

        test.get().info("Step 22 :re-Load customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 23 : Open details for customer 1st subscription");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");

        test.get().info("Step 24: Verify the one off bundle just added is listed in other products grid");
        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage otherProductsGridSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance();

        HashMap<String, String> otherProducts = OtherProductEntiy.dataForAnOtherBundleProduct("BUNDLE - [250MB-TUDATA-0300-FC]", "Bundle", "Discount Bundle - [One-off data 250MB]", "£3.00", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus16Day());
        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProductsByProduct(otherProducts));

        test.get().info("Step 25: verify the one new discount bundle record generated for cusomter");
        verifyOneNewDiscountBundleRecordGeneratedForCustomer();
    }


    private void verifyContextInfoOfSMSServiceOrderIsCorrectInDB() {
        Assert.assertTrue(CommonActions.getSMSIsSent(serviceOrderIDSms, "") != null);
        String mess = String.format("<SMSGateway><sms:sendSms><smsRequest><type>OFCOM</type><mpn>%s</mpn><message>Tesco Mobile: You have added a 250MB one-off data bundle. This is valid until %s. Call 4488, free, to add a safety buffer or more data.</message>",
                serviceRefOf1stSubscription, Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus15Day(), TimeStamp.DATE_FORMAT6));
        List<String> str = CommonActions.getContextInfoOfSMSServiceOrderIsCorrectInDb(serviceOrderID, "");
        String result = str.get(2).substring(str.get(2).indexOf("<SMSGateway>"), str.get(2).indexOf("</smsRequest>"));
        Assert.assertEquals(mess, result);
    }

    private void verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(Date newStartDate, String discountGroupCode) {
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(8, discountBundles.size());
        verifyFCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TMT5K");
    }


    private void verifyMyTariffDetailPageDisplayedWithCorrectData() {
        List<String> alert = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(1, alert.size());
        Assert.assertEquals("Thanks, the bundle changes you’ve made have been successful.", alert.get(0));
        Assert.assertEquals(String.format("Any one-off bundles will expire on %s", Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus15Day(), TimeStamp.DATE_FORMAT_IN_PDF)), MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getOneOffBundles());
    }


    private void verifyAddOneOffBundleScreenDisplayedWithCorrectData() {
        Assert.assertEquals(serviceRefOf1stSubscription + " - Mobile Ref 1", AddOneOffBundle.getInstance().getMobilePhoneNumber());
        Assert.assertEquals("£10 Tariff 12 Month Contract", AddOneOffBundle.getInstance().getTariff());
        Assert.assertEquals("500 mins, 5000 texts (FC)", AddOneOffBundle.getInstance().getMonthlyAllowance());
        String expiryDate = Parser.parseDateFormate(TimeStamp.TodayPlus2MonthMinus16Days(), TimeStamp.DATE_FORMAT_IN_PDF);
        Assert.assertEquals(String.format("%s  (%s  days remaining)", expiryDate, TimeStamp.TodayPlus2MonthMinus16DaysMinusToday(), TimeStamp.DATE_FORMAT_IN_PDF), AddOneOffBundle.getInstance().getExpiryDate());

        Assert.assertEquals(1, AddOneOffBundle.getInstance().getRowCountCurrentBundle());
        Assert.assertEquals("£20 safety buffer", AddOneOffBundle.getInstance().getCurrentBundleDescriptionByCellValue("Bill cap", 1));
        Assert.assertEquals("0", AddOneOffBundle.getInstance().getCurrentBundleDescriptionByCellValue("Bill cap", 2));
        Assert.assertEquals("N/A", AddOneOffBundle.getInstance().getCurrentBundleDescriptionByCellValue("Bill cap", 3));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus16Day(), TimeStamp.DATE_FORMAT_IN_PDF), AddOneOffBundle.getInstance().getCurrentBundleDescriptionByCellValue("Bill cap", 4));
        Assert.assertEquals("£0.00", AddOneOffBundle.getInstance().getCurrentBundleDescriptionByCellValue("Bill cap", 5));
        Assert.assertEquals("£0.00", AddOneOffBundle.getInstance().gettotal());

        Assert.assertEquals(4, AddOneOffBundle.getInstance().getRowCountAvailableOneOffDataBundleGrid());

        HashMap<String, String> expectedEnity = OneOffBundleEnity.getAvailableOneOffDataBundleEnity("One-off FC data 4G - 500MB", "500", TimeStamp.TodayPlus1MonthMinus16Day(), "£5.00");
        Assert.assertEquals(1, AddOneOffBundle.getInstance().findRowInAvailableOneOffDataBundleGrid(expectedEnity));

        expectedEnity = OneOffBundleEnity.getAvailableOneOffDataBundleEnity("One-off data 250MB", "250", TimeStamp.TodayPlus1MonthMinus16Day(), "£3.00");
        Assert.assertEquals(1, AddOneOffBundle.getInstance().findRowInAvailableOneOffDataBundleGrid(expectedEnity));
        expectedEnity = OneOffBundleEnity.getAvailableOneOffDataBundleEnity("One-off data 500MB", "500", TimeStamp.TodayPlus1MonthMinus16Day(), "£5.00");
        Assert.assertEquals(1, AddOneOffBundle.getInstance().findRowInAvailableOneOffDataBundleGrid(expectedEnity));
        expectedEnity = OneOffBundleEnity.getAvailableOneOffDataBundleEnity("One-off data 1GB", "1024", TimeStamp.TodayPlus1MonthMinus16Day(), "£7.50");
        Assert.assertEquals(1, AddOneOffBundle.getInstance().findRowInAvailableOneOffDataBundleGrid(expectedEnity));

        Assert.assertTrue(AddOneOffBundle.getInstance().isReadOurFairUsagePolicyLinkDisplayed());
        String note = "* Note: To change your monthly bundle, go to the Monthly Bundles – Add, change or remove page.\nYou can use all of this data in the UK and Europe with Home From Home. Read our fair usage policy.";
        Assert.assertEquals(note, AddOneOffBundle.getInstance().getNote());

        Assert.assertEquals("£10.00", AddOneOffBundle.getInstance().getTariffCharge());
        Assert.assertEquals("£0.00", AddOneOffBundle.getInstance().getTotalyMonthlyBundlecharge());
        Assert.assertEquals("£0.00", AddOneOffBundle.getInstance().getNewOneOffBundleCharge());
        Assert.assertEquals("£0.00", AddOneOffBundle.getInstance().getTotalOneOffBundleCharge());
    }

    private void verifyOneNewDiscountBundleRecordGeneratedForCustomer() {
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountBundleGroupCode);
        Assert.assertEquals(9, discountBundles.size());

        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByBundleCode(discountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayMinus16DaysAdd1Month(), "250MB-TUDATA-0300-FC", "ACTIVE"));

    }
}
