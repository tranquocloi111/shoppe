package suite.regression.selfcare.modifysubscription;

import framework.utils.Log;
import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.business.entities.SubscriptionEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.LiveBillEstimateContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.ChangeMySafetyBufferPage;
import logic.pages.selfcare.MonthlyBundlesAddChangeOrRemovePage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import oracle.sql.TIMESTAMP;
import org.omg.CORBA.TIMEOUT;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

public class TC31982_Change_SB_Apply_the_change_Now_and_keep_this_change_when_temporary_change_is_in_place extends BaseTest {
    /*
     *
     * Tran Quoc Loi
     * */

    String mpn;
    String serviceOrder;
    String ServiceRefOf1stSubscription;
    String groupCode = "10522";

    @Test(enabled = true, description = "TC31982 change SB apply the change now and keep this change when temporary change is in place", groups = "SelfCare")
    public void TC31982_Change_SB_Apply_the_change_Now_and_keep_this_change_when_temporary_change_is_in_place() {
//        test.get().info("Create a CC customer with no bundle and sim only");
//        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
//        OWSActions owsActions = new OWSActions();
//        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = "8728";//owsActions.customerNo;
//        owsActions.getSubscription(owsActions.orderIdNo, "Mobile Ref 1");
        ServiceRefOf1stSubscription = "07958039740";// owsActions.serviceRef;

//        test.get().info("create new billing group");
//        createNewBillingGroup();
//        test.get().info("update bill group payment collection date to 10 day later ");
//        updateBillGroupPaymentCollectionDateTo10DaysLater();
//        test.get().info("set bill group for customer");
//        setBillGroupForCustomer(customerNumber);
//        test.get().info("update start date for customer");
//        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus10Days());

//
//        test.get().info("Login in to selfcare");
//        SelfCareTestBase.page().LoginIntoSelfCarePage("un098274688@hsntech.com", "password1", customerNumber);
//        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();
//
//        test.get().info("Access the tariff detail page");
//        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
//        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();
//
//        test.get().info("click change my safety buffer button");
//        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickChangeMySafetyBufferBtn();
//        SelfCareTestBase.page().verifyChangeMySafetyBufferPage();
//
//        test.get().info("verify detail screen");
//        verifyDetailsScreen();
//
//
//
//        test.get().info("select temporary change safety buffer and tick to agree");
//        selectTemporaryChangeSafetyBufferAndTickToAgree();
//
//        test.get().info("click save button");
//        ChangeMySafetyBufferPage.getInstance().clickSaveBtn();
//
//        test.get().info("verify my tariff detail page displayed with successfull alert");
//        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();
//        List<String> successMssg = SelfCareTestBase.page().successfulMessageStack();
//        Assert.assertEquals("You’ve successfully changed your safety buffer.", successMssg.get(0));
//        String expectedSafetyBuffer=String.format("£30 safety buffer    ACTIVE  as of  {0}", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT4));
//        Assert.assertEquals(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getSafetyBuffer(),expectedSafetyBuffer);

//        test.get().info("load user in hub net");
//        CareTestBase.page().loadCustomerInHubNet(customerNumber);
//
//        test.get().info("access service order content page");
//        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
//
//
//        test.get().info("verify change bundle is completed");
//        serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");
//        HashMap<String, String> expectedSeriverOrder = ServiceOrderEntity.dataServiceOrderForChangePassword("Change Bundle", "Completed Task");
//        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(expectedSeriverOrder), 1);
//
//        test.get().info("access the subscription page");
//        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
//
//        test.get().info("verify safety buffer has been updated subscription detail page");
//        verifySafetyBufferHasBeenUpdatedInSubscriptionDetailsPage();

        test.get().info("verify all discount bundle entries align with bill run calendar entires");
        verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires();

        test.get().info("verify LCI request");
        verifyLCIRequest();

        test.get().info("verify live bill estimate has added new safetybyffer");
        MenuPage.LeftMenuPage.getInstance().clickLiveBillEstimateItem();
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription billEstimatePerSubscription = new
                LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.BillEstimatePerSubscription(ServiceRefOf1stSubscription + "  Mobile Ref 1");
        billEstimatePerSubscription.expand();


    }

    private void verifyAllDiscountBundleEntriesAlignWithBillRunCalendarEntires() {
        List<DiscountBundleEntity> discountBundles = BillingActions.getInstance().getDiscountBundlesByDiscountGroupCode(groupCode);
        Assert.assertEquals(10, discountBundles.size());
        Assert.assertEquals(1, BillingActions.getInstance().findNewDiscountBundlesByCondition(discountBundles, "FC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "FLX21", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findNewDiscountBundlesByCondition(discountBundles, "FC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "FLX21", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findNewDiscountBundlesByCondition(discountBundles, "FC", TimeStamp.TodayMinus10Days(), TimeStamp.TodayMinus1Day(), "FLX17", "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findNewDiscountBundlesByCondition(discountBundles, "FC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), "FLX17", "ACTIVE"));

        verifyNCDiscountBundles(discountBundles, TimeStamp.TodayMinus10Days(), "TM500");
        verifyNCDiscountBundles(discountBundles, TimeStamp.TodayMinus10Days(), "TMT5K");
    }

    private void verifyLCIRequest() {
        String sql = String.format("select lr.PartitionIDRef, lr.Amount from lcirequest lr where lr.discountbundleid in " +
                "(select d.discountbundleid from discountbundle d where d.discgrpcode ='%s')", groupCode);
        boolean flag=false;
        try {
            ResultSet rs = OracleDB.SetToNonOEDatabase().executeQuery(sql);
            while (rs.next()) {
                Assert.assertEquals(rs.getString(0), "TMT5K");
                Assert.assertEquals(rs.getString(1), "250");
                flag=true;

            }
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        Assert.assertTrue(flag);

    }

    private void verifySafetyBufferHasBeenUpdatedInSubscriptionDetailsPage() {
        HashMap<String, String> expectedSub = SubscriptionEntity.dataForFullSummarySubscriptions(TimeStamp.TodayMinus10Days(), "Safety Buffer - £30",
                "FC12-1000-500SO - £10 Tariff 12 Month Contract - £10.00", ServiceRefOf1stSubscription + " Mobile Ref 1", "Active");
        Assert.assertEquals(CommonContentPage.SubscriptionsGridSectionPage.getInstance().getNumberOfSubscription(expectedSub), 1);
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);

        HashMap<String, String> otherProduct1 = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("NK-2720", "Device", "Nokia 2720", "£0.00", TimeStamp.TodayMinus10Days());

        HashMap<String, String> otherProduct2 = OtherProductEntiy.dataForOtherBundleProduct
                ("FLEXCAP - [02000-SB-A]", "Bundle", "Flexible Cap - £20 - [£20 safety buffer]", "£0.00", TimeStamp.TodayMinus10Days(), TimeStamp.TodayMinus1Day());

        HashMap<String, String> otherProduct3 = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("FLEXCAP - [03000-SB-A]", "Bundle", "Flexible Cap - £30 - [£30 safety buffer]", "£0.00", TimeStamp.Today());

        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable(), 3);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct1), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct2), 1);
        Assert.assertEquals(SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(otherProduct3), 1);
        groupCode = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();
    }

    private void selectTemporaryChangeSafetyBufferAndTickToAgree() {
        ChangeMySafetyBufferPage.getInstance().selectSafetyBuffer("£30 safety buffer");
        ChangeMySafetyBufferPage.getInstance().selectWhenWouldLikeToChangeMethod("Change it now and keep it at this amount");
        ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodEnable("Change it now and keep it at this amount");
        ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodEnable("Change it from my next bill date");

        String message = "Your safety buffer will change now and stay in place until you change it again.\r\n"
                + "While our systems update you might still get texts about your original safety buffer. To find out your balance, just call 4488 free from your Tesco Mobile phone or go to "
                + "our iPhone and Android app.";
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().getComfirmMessage(), message);
    }


    private void verifyDetailsScreen() {
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().getMobilePhone(), ServiceRefOf1stSubscription + " - Mobile Ref 1");
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().getnextAllowanceDate(), ChangeMySafetyBufferPage.getInstance().calculateNextAllowanceDate());
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().getMonthlySafetyBuffer(), "£20.00");
        String msg = "Your safety buffer is £20.00. This is the maximum you’ve allowed yourself to spend on things outside your monthly allowance, so your bill will never be higher than you expect.";
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().getFlexibleCapMsg(), msg);
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().getLinkText(), "Find out more about safety buffers.");

        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().IsSaftyBufferSelected("£20 safety buffer"));
        Assert.assertFalse(ChangeMySafetyBufferPage.getInstance().IsSaftyBufferSelected("No safety buffer"));
        Assert.assertFalse(ChangeMySafetyBufferPage.getInstance().IsSaftyBufferEnable("£20 safety buffer"));

    }


}
