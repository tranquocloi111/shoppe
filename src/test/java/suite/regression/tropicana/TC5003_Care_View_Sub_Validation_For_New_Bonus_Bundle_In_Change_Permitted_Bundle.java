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
import java.util.List;

public class TC5003_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Change_Permitted_Bundle extends BaseTest {
    private String customerNumber = "15758";
    private String subscription2;
    private String serviceOrderId;

    @Test(enabled = true, description = "TC5003_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Change_Permitted_Bundle", groups = "tropicana")
    public void TC5003_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Change_Permitted_Bundle(){
        test.get().info("Step 1 : Create a customer subscription related tariff linked with a Bonus bundle group");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\tropicana\\TC4682_request.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        List<String> subList = CareTestBase.getAllSubscriptionsNumber();
        subscription2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        test.get().info("Step 6 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        test.get().info("Step 6: Navigate to Change Bundle page");
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();

        test.get().info("Step 7 : Select FC mobile to change permitted bundle");
        String fcSubText = Common.findValueOfStream(subList, "Mobile Ref 2");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(fcSubText, "Change Permitted Bundle");

        test.get().info("Step 14 : Verify Bonus Data bundle is included in Current Bundle as expected");
        Assert.assertEquals(fcSubText, ServiceOrdersPage.ChangeBundle.getInstance().getSubscriptionNumber());
        String expectNextBillDate = String.format("%s (%s days from today)", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT_IN_PDF), TimeStamp.TodayPlus1MonthMinusToday());
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("£10 Tariff 12 Month Contract", ServiceOrdersPage.ChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 500 mins, 5000 texts (FC)", ServiceOrdersPage.ChangeBundle.getInstance().getPackagedBundle());
        Assert.assertEquals("Next Bill Date", ServiceOrdersPage.ChangeBundle.getInstance().getWhenToApplyChangeText());
        Assert.assertTrue(ServiceOrdersPage.ChangeBundle.getInstance().bundleExists(BundlesToSelectEntity.getStandardDataBundles()));
        CareTestBase.page().checkBundleToolTip(BundlesToSelectEntity.getStandardDataBundles());

        test.get().info("Step 15 : Select a new Permitted bundle that has an Equivalent Bonus bundle");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName(BundlesToSelectEntity.getFCBundleToSelect(),"Monthly 250MB data allowance - 4G - £5.00 per Month (Recurring)");
        CareTestBase.page().clickNextButton();

        test.get().info("Step 16 : Observe the Bundles Before and Bundles After");
        Assert.assertEquals(fcSubText, ServiceOrdersPage.ConfirmChangeBundle.getInstance().getSubscriptionNumber());
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ConfirmChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("FC1-0750-150SO £7.50 SIM Only Tariff 1 Month Contract {£7.50}", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 500 mins, 5000 texts (FC)", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getPackagedBundle());

        Assert.assertEquals("No Current Recurring Bundles", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getInfoBefore());
        Assert.assertEquals("£0.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getTotalRecurringBundleChargeBefore());

        Assert.assertEquals("£5.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getTotalRecurringBundleChargeAfter());
        Assert.assertEquals(String.format("£5.00 per Month (Recurring).Valid from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy")), ServiceOrdersPage.ConfirmChangeBundle.getInstance().getBundleInfo("3G data   -   1GB:"));
        Assert.assertEquals("Increase of £5.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getRecurringBundlesChargeDifference());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"), ServiceOrdersPage.ConfirmChangeBundle.getInstance().getEffective());

        test.get().info("Step 17 : Click next button to service order complete screen");
        CareTestBase.page().clickNextButton();

        test.get().info("Step 18 : Verify service order complete screen has provision wait message");
        CareTestBase.page().verifyServiceOrderCompleteScreenHasProvisionWaitMessage();

        test.get().info("Step 19 : Click return to customer button on service order complete screen");
        CareTestBase.page().clickReturnToCustomer();

        test.get().info("Step 20 : Select change bundle from RHS actions");
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();

        test.get().info("Step 15 : Select a new Permitted bundle that has NO Equivalent Bonus bundle");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName(BundlesToSelectEntity.getFCBundleToSelect(),"data - 250MB - £0.00 per Month (Recurring)");
        CareTestBase.page().clickNextButton();

        test.get().info("Step 16 : Observe the Bundles Before and Bundles After");
        Assert.assertEquals(fcSubText, ServiceOrdersPage.ConfirmChangeBundle.getInstance().getSubscriptionNumber());
        Assert.assertEquals(expectNextBillDate, ServiceOrdersPage.ConfirmChangeBundle.getInstance().getNextBillDateForThisAccount());
        Assert.assertEquals("FC1-0750-150SO £7.50 SIM Only Tariff 1 Month Contract {£7.50}", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getCurrentTariff());
        Assert.assertEquals("Bundle - 150 mins, 5000 texts (FC)", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getPackagedBundle());

        Assert.assertEquals("No Current Recurring Bundles", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getInfoBefore());
        Assert.assertEquals("£0.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getTotalRecurringBundleChargeBefore());

        Assert.assertEquals("£5.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getTotalRecurringBundleChargeAfter());
        Assert.assertEquals(String.format("£5.00 per Month (Recurring).Valid from %s.", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy")), ServiceOrdersPage.ConfirmChangeBundle.getInstance().getBundleInfo("3G data   -   1GB:"));
        Assert.assertEquals("Increase of £5.00 per month", ServiceOrdersPage.ConfirmChangeBundle.getInstance().getRecurringBundlesChargeDifference());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"), ServiceOrdersPage.ConfirmChangeBundle.getInstance().getEffective());
    }
}
