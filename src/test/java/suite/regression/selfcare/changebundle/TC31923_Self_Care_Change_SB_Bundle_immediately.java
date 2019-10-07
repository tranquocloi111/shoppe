package suite.regression.selfcare.changebundle;

import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.EventEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.AddASafeTyBufferPage;
import logic.pages.selfcare.ChangeMySafetyBufferPage;
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

public class TC31923_Self_Care_Change_SB_Bundle_immediately extends BaseTest {
    String serviceRefOf1stSubscription;
    String serviceOrderID;

   // @Test(enabled = true, description = "TC319238 SelfCare change SB bundle immediately", groups = "SelfCare")
    public void TC31923_Self_Care_Change_SB_Bundle_immediately() {

        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
        test.get().info("Step 1 : Create a customer with 2 NC subscription");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;

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
        serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");

        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");
        String discountGroupCodeOfMobileRef1 = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
        verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(newStartDate, discountGroupCodeOfMobileRef1);

        test.get().info("Step 9 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 10 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 11 : Click add or change a family per page is correct");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickChangeMySafetyBufferBtn();
        SelfCareTestBase.page().verifyChangeMySafetyBufferPage();

        test.get().info("Step 12 : verify change my safety buffer page result is correct");
        verifyChangeMySafetyBufferPageResultIsCorrect();

        test.get().info("Step 13 : select 40 safety buffer and change it keep this amount");
        ChangeMySafetyBufferPage.getInstance().selectSafetyBuffer("£40 safety buffer");
        ChangeMySafetyBufferPage.getInstance().selectWhenWouldLikeToChangeMethod("Change it now and keep it at this amount");

        test.get().info("Step 14 : verify confirming your changes displays correct result");
        Assert.assertEquals("£20.00", ChangeMySafetyBufferPage.getInstance().getPreviousSafetyBuffer());
        Assert.assertEquals("£40.00", ChangeMySafetyBufferPage.getInstance().getNewSafetyBuffer());

        String message = "Your safety buffer will change now and stay in place until you change it again.\r\n"
                + "While our systems update you might still get texts about your original safety buffer. To find out your balance, just call 4488 free from your Tesco Mobile phone or go to "
                + "our iPhone and Android app.";

        Assert.assertEquals(message, ChangeMySafetyBufferPage.getInstance().getComfirmMessage());
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().IsWhenWouldYouLikeYourSafetyBufferToChangeBlockDisplayed());

        test.get().info("Step 15 : click save changes in changes my safety buffer page");
        ChangeMySafetyBufferPage.getInstance().clickSaveBtn();

        test.get().info("Step 16 :verify my tariff details page displayed with successful alert");
        List<String> alert = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(1, alert.size());
        Assert.assertEquals("You’ve successfully changed your safety buffer.", alert.get(0));
        Assert.assertEquals(String.format("£40 safety buffer    ACTIVE  as of  %s", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF)), MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").getTariff());


        test.get().info("Step 17 : Open sevice orders page in hub net for customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 18 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Completed Task");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        Assert.assertEquals(1, size);
        serviceOrderID = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");

        test.get().info("Step 19 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 20 : Verify SO details data are updated");
        Assert.assertEquals(serviceRefOf1stSubscription + " Mobile Ref 1", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", TasksContentPage.TaskPage.DetailsPage.getInstance().getTariff());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getProvisioningDate());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getNotificationOfLowBalance());
        Assert.assertEquals("Family perk - 500 Tesco Mobile only minutes per month;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());

        Assert.assertEquals(6, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("Remove offer on network", "In Progress")));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("Refill Amount: £20.00 - Completed", "Completed Task")));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("Bonus Money reset to zero", "In Progress")));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("Service Order Completed", "Completed Task")));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("PPB: AddSubscription: Request completed", "Completed Task")));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("SSMS Request Completed", "Completed Task")));

        test.get().info("Step 21 : verify a new sms message so create and details");
        verifyANewSMSMessageSOCreatedAndDetails();

        test.get().info("Step 22 :re-Load customer in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 23 : Open details for customer 1st subscription");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");

        test.get().info("Step 24: Verify the one off bundle just added is listed in other products grid");
        HashMap<String, String> otherProducts = OtherProductEntiy.dataForAnOtherBundleProduct("NK-2720", "Device", "Nokia 2720", "£0.00", newStartDate);
        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage otherProductsGridSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance();
        Assert.assertEquals(3, otherProductsGridSectionPage.getRowNumberOfOtherProductsGridTable());

        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProductsByProduct(otherProducts));

        otherProducts = OtherProductEntiy.dataForAnEndedOtherBundleProduct("BUNDLER - [500-FONMIN-0-FC]", "Bundle", "Flexible Cap - £20 - [£20 safety buffer]", "£0.00", newStartDate);
        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProductsByProduct(otherProducts));

        otherProducts = OtherProductEntiy.dataForAnOtherBundleProduct("FLEXCAP - [04000-SB-A]", "Bundle", "Flexible Cap - £40 - [£40 safety buffer]", "£0.00", newStartDate);
        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProductsByProduct(otherProducts));

        test.get().info("Step 25: Verify new discount bundle entries have been created and old ones has been deleted");
        verifyTheSafetyBufferHasBeenUpdatedCreatedAndOldOnesHasBeenDelete(newStartDate, discountGroupCodeOfMobileRef1);
    }

    private void verifyTheSafetyBufferHasBeenUpdatedCreatedAndOldOnesHasBeenDelete(Date newStartDate, String discountGroupCode) {
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(10, discountBundles.size());

        BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "FC", TimeStamp.Today(), TimeStamp.TodayMinus16DaysAdd1Month(), "FLX29", "ACTIVE");
        BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "FC", TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.TodayMinus16DaysAdd2Months(), "FLX29", "ACTIVE");
        findDeletedDiscountBundlesByCondition(discountBundles, Integer.parseInt(serviceOrderID), TimeStamp.TodayMinus15DaysAdd1Month(), "02000-SB-A");


    }

    private int findDeletedDiscountBundlesByCondition(List<DiscountBundleEntity> allDiscountBundles, int deleteHitransactionID, Date deleteDate, String bundleCode) {
        return Integer.parseInt(String.valueOf(allDiscountBundles.stream().filter(x -> x.bundleCode.equalsIgnoreCase(bundleCode) && x.status.equalsIgnoreCase("DELETED") && x.deleteHitransactionID == deleteHitransactionID && x.deleteDate.equals(deleteDate) && x.deleteFLG.equalsIgnoreCase("Y")).count()));
    }

    private void verifyANewSMSMessageSOCreatedAndDetails() {
        Assert.assertFalse(CommonActions.getSMSIsSent(serviceOrderID, "") != null);
        String mess = String.format("<SMSGateway><sms:sendSms><smsRequest><type>OFCOM</type><mpn>%s</mpn><message>Tesco Mobile: You have changed your safety buffer to £40.00. This takes effect immediately.</message></smsRequest></sms:sendSms></SMSGateway>", serviceRefOf1stSubscription);
        List<String> str = CommonActions.getContextInfoOfSMSServiceOrderIsCorrectInDb(serviceOrderID, "");
        Assert.assertTrue(str.get(5).contains(mess));
    }

    private void verifyChangeMySafetyBufferPageResultIsCorrect() {
        Assert.assertEquals(serviceRefOf1stSubscription + " - Mobile Ref 1", ChangeMySafetyBufferPage.getInstance().getMobilePhone());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus15Day(), TimeStamp.DATE_FORMAT_IN_PDF), ChangeMySafetyBufferPage.getInstance().getNextAllowanceDate());
        Assert.assertEquals("£20.00", ChangeMySafetyBufferPage.getInstance().getMonthlySafetyBuffer());
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().checkRadioBoxExists("£2.50 safety buffer"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().checkRadioBoxExists("£5 safety buffer"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().checkRadioBoxExists("£7.50 safety buffer"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().checkRadioBoxExists("£10 safety buffer"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().checkRadioBoxExists("£20 safety buffer"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().checkRadioBoxExists("£30 safety buffer"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().checkRadioBoxExists("£40 safety buffer"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().checkRadioBoxExists("No safety buffer"));

        Assert.assertEquals(String.format("Your safety buffer will change now and go back to £20.00 on %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus15Day(), TimeStamp.DATE_FORMAT_IN_PDF3)), ChangeMySafetyBufferPage.getInstance().getChangeItNowButOnlyUntillMyNextBillDate());
        Assert.assertEquals("Your safety buffer will change now and stay in place until you change it again.", ChangeMySafetyBufferPage.getInstance().getChangeITNowAndKeepAtThisAmount());
        Assert.assertEquals(String.format("Your safety buffer will change on %s and stay in place until you change it again.", Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus15Day(), TimeStamp.DATE_FORMAT_IN_PDF3)), ChangeMySafetyBufferPage.getInstance().getChangeItFromMyTextBillDate());
        Assert.assertFalse(ChangeMySafetyBufferPage.getInstance().IsWhenWouldYouLikeYourSafetyBufferToChangeBlockDisplayed());
    }


    private void verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires(Date newStartDate, String discountGroupCode) {
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(discountGroupCode);
        Assert.assertEquals(8, discountBundles.size());

        verifyFCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "FLX17");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TM500");
        verifyNCDiscountBundlesFoBillingGroupMinus15days(discountBundles, newStartDate, "TMT5K");
    }
}
