package suite.regression.tropicana;

import logic.business.db.billing.CommonActions;
import logic.business.entities.BundlesToSelectEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TC5003_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Change_Permitted_Bundle extends BaseTest {
    private String customerNumber;
    private Date newStartDate;
    String subscription1;
    String subscription2;
    String subscription3;
    String subscription4;

    @Test(enabled = true, description = "TC5003_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Change_Permitted_Bundle", groups = "tropicana")
    public void TC5003_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Change_Permitted_Bundle(){
        test.get().info("Step 1 : Create a customer has Subscription with Tropicana bundle");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\tropicana\\TC4667_data_text_min_type_request.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update Customer Start Date");
        newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        List<String> subList = getAllSubscriptionsNumber();
        subscription1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        subscription2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");
        subscription3 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 3");
        subscription4 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 4");

        test.get().info("Step 7 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4667_min_type_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription3);

        selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4667_text_type_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription4);

        test.get().info("Step 8: Navigate to Change Bundle page");
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();

        test.get().info("Step 9 : Select FC mobile to change permitted bundle");
        String fcSubText = Common.findValueOfStream(subList, "Mobile Ref 2");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(fcSubText, "Change Permitted Bundle");

        test.get().info("Step 10 : Verify Bonus Data bundle is included in Current Bundle as expected");
        Assert.assertEquals(fcSubText, ServiceOrdersPage.ChangeBundle.getInstance().getSubscriptionNumber());
        String expectNextBillDate = String.format("%s (%s days from today)", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT_IN_PDF), TimeStamp.TodayPlus1MonthMinusToday());
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("£10 Tariff 12 Month Contract", ServiceOrdersPage.ChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 500 mins, 5000 texts (FC)", ServiceOrdersPage.ChangeBundle.getInstance().getPackagedBundle());
        Assert.assertEquals("Next Bill Date", ServiceOrdersPage.ChangeBundle.getInstance().getWhenToApplyChangeText());
        Assert.assertTrue(ServiceOrdersPage.ChangeBundle.getInstance().bundleExists(BundlesToSelectEntity.getFamilyPerkBonusBundles()));
        CareTestBase.page().checkFamilyPerkBundleToolTip(BundlesToSelectEntity.getFamilyPerkBonusBundles());

        test.get().info("Step 11 : Select a new Permitted bundle that has no an Equivalent Bonus bundle");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName(BundlesToSelectEntity.getStandardDataBundles(),"Monthly 500MB data allowance - £5.00 per Month (Recurring)");
        CareTestBase.page().clickNextButton();

        test.get().info("Step 12 : Observe the Bundles Before and Bundles After");
        ServiceOrdersPage.ConfirmChangeBundle confirmChangeBundle = ServiceOrdersPage.ConfirmChangeBundle.getInstance();
        Assert.assertEquals(confirmChangeBundle.getErrorMessage(), "This number is entitled to have a Bonus Bundle. We're sorry, we don't have a Bonus Bundle to match your tariff.");
        Assert.assertEquals(fcSubText, confirmChangeBundle.getSubscriptionNumber());
        Assert.assertEquals(expectNextBillDate, confirmChangeBundle.getNextBillDateForThisAccount());
        Assert.assertEquals("AUTO-FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", confirmChangeBundle.getCurrentTariff());
        Assert.assertEquals("Bundle - 500 mins, 5000 texts (FC)", confirmChangeBundle.getPackagedBundle());

        Assert.assertEquals("£5.00 per month", confirmChangeBundle.getTotalRecurringBundleChargeBefore());

        Assert.assertEquals("£10.00 per month", confirmChangeBundle.getTotalRecurringBundleChargeAfter());
        Assert.assertEquals(String.format("£5.00 per Month (Recurring).Valid from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy")), confirmChangeBundle.getBundleInfo("Monthly 500MB data allowance:"));
        Assert.assertEquals("Increase of £5.00 per month", confirmChangeBundle.getRecurringBundlesChargeDifference());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"), confirmChangeBundle.getEffective());
        confirmChangeBundle.clickPreButton();
        confirmChangeBundle.clickPreButton();

        test.get().info("Step 13 : Select FC mobile has mins bundle to change permitted bundle");
        fcSubText = Common.findValueOfStream(subList, "Mobile Ref 3");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(fcSubText, "Change Permitted Bundle");

        test.get().info("Step 14 : Verify Bonus Data bundle is included in Current Bundle as expected");
        Assert.assertEquals(fcSubText, ServiceOrdersPage.ChangeBundle.getInstance().getSubscriptionNumber());
        expectNextBillDate = String.format("%s (%s days from today)", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT_IN_PDF), TimeStamp.TodayPlus1MonthMinusToday());
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("£10 Tariff 12 Month Contract", ServiceOrdersPage.ChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 500 mins, 5000 texts (FC)", ServiceOrdersPage.ChangeBundle.getInstance().getPackagedBundle());
        Assert.assertEquals("Next Bill Date", ServiceOrdersPage.ChangeBundle.getInstance().getWhenToApplyChangeText());
        Assert.assertTrue(ServiceOrdersPage.ChangeBundle.getInstance().bundleExists(BundlesToSelectEntity.getLoyaltyMinBonusBundles()));
        CareTestBase.page().checkFamilyPerkBundleToolTip(BundlesToSelectEntity.getLoyaltyMinBonusBundles());

        test.get().info("Step 15 : Select a new Permitted bundle that has no an Equivalent Bonus bundle");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName(BundlesToSelectEntity.getStandardDataBundles(),"Monthly 500MB data allowance - £5.00 per Month (Recurring)");
        CareTestBase.page().clickNextButton();

        test.get().info("Step 16 : Observe the Bundles Before and Bundles After");
        confirmChangeBundle = ServiceOrdersPage.ConfirmChangeBundle.getInstance();
        Assert.assertEquals(confirmChangeBundle.getErrorMessage(), "This number is entitled to have a Bonus Bundle. We're sorry, we don't have a Bonus Bundle to match your tariff.");
        Assert.assertEquals(fcSubText, confirmChangeBundle.getSubscriptionNumber());
        Assert.assertEquals(expectNextBillDate, confirmChangeBundle.getNextBillDateForThisAccount());
        Assert.assertEquals("AUTO1-FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", confirmChangeBundle.getCurrentTariff());
        Assert.assertEquals("Bundle - 500 mins, 5000 texts (FC)", confirmChangeBundle.getPackagedBundle());

        Assert.assertEquals("£0.00 per month", confirmChangeBundle.getTotalRecurringBundleChargeBefore());

        Assert.assertEquals("£5.00 per month", confirmChangeBundle.getTotalRecurringBundleChargeAfter());
        Assert.assertEquals(String.format("£5.00 per Month (Recurring).Valid from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy")), confirmChangeBundle.getBundleInfo("Monthly 500MB data allowance:"));
        Assert.assertEquals("Increase of £5.00 per month", confirmChangeBundle.getRecurringBundlesChargeDifference());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"), confirmChangeBundle.getEffective());
        confirmChangeBundle.clickPreButton();
        confirmChangeBundle.clickPreButton();

        test.get().info("Step 17 : Select FC mobile has text bundle to change permitted bundle");
        fcSubText = Common.findValueOfStream(subList, "Mobile Ref 4");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(fcSubText, "Change Permitted Bundle");

        test.get().info("Step 18 : Verify Bonus Data bundle is included in Current Bundle as expected");
        Assert.assertEquals(fcSubText, ServiceOrdersPage.ChangeBundle.getInstance().getSubscriptionNumber());
        expectNextBillDate = String.format("%s (%s days from today)", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT_IN_PDF), TimeStamp.TodayPlus1MonthMinusToday());
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("£10 Tariff 12 Month Contract", ServiceOrdersPage.ChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 500 mins, 5000 texts (FC)", ServiceOrdersPage.ChangeBundle.getInstance().getPackagedBundle());
        Assert.assertEquals("Next Bill Date", ServiceOrdersPage.ChangeBundle.getInstance().getWhenToApplyChangeText());
        Assert.assertTrue(ServiceOrdersPage.ChangeBundle.getInstance().bundleExists(BundlesToSelectEntity.getLoyaltyTextBonusBundles()));
        CareTestBase.page().checkFamilyPerkBundleToolTip(BundlesToSelectEntity.getLoyaltyTextBonusBundles());

        test.get().info("Step 19 : Select a new Permitted bundle that has no an Equivalent Bonus bundle");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName(BundlesToSelectEntity.getStandardDataBundles(),"Monthly 500MB data allowance - £5.00 per Month (Recurring)");
        CareTestBase.page().clickNextButton();

        test.get().info("Step 20 : Observe the Bundles Before and Bundles After");
        confirmChangeBundle = ServiceOrdersPage.ConfirmChangeBundle.getInstance();
        Assert.assertEquals(confirmChangeBundle.getErrorMessage(), "This number is entitled to have a Bonus Bundle. We're sorry, we don't have a Bonus Bundle to match your tariff.");
        Assert.assertEquals(fcSubText, confirmChangeBundle.getSubscriptionNumber());
        Assert.assertEquals(expectNextBillDate, confirmChangeBundle.getNextBillDateForThisAccount());
        Assert.assertEquals("AUTO1-FC12-1000-500SO £10 Tariff 12 Month Contract {£10.00}", confirmChangeBundle.getCurrentTariff());
        Assert.assertEquals("Bundle - 500 mins, 5000 texts (FC)", confirmChangeBundle.getPackagedBundle());

        Assert.assertEquals("£0.00 per month", confirmChangeBundle.getTotalRecurringBundleChargeBefore());

        Assert.assertEquals("£5.00 per month", confirmChangeBundle.getTotalRecurringBundleChargeAfter());
        Assert.assertEquals(String.format("£5.00 per Month (Recurring).Valid from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy")), confirmChangeBundle.getBundleInfo("Monthly 500MB data allowance:"));
        Assert.assertEquals("Increase of £5.00 per month", confirmChangeBundle.getRecurringBundlesChargeDifference());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"), confirmChangeBundle.getEffective());
    }

    private List<String> getAllSubscriptionsNumber(){
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        List<String> subscriptionNumberList = new ArrayList<>();
        for (int i = 0; i < 5; i++){
            String subNo = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(i);
            subscriptionNumberList.add(subNo);
        }
        return subscriptionNumberList;
    }

}
