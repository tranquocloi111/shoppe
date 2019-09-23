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

public class TC5005_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Change_Safety_Buffer extends BaseTest {
    private String customerNumber = "15758";
    private String subscription2;

    @Test(enabled = true, description = "TC5005_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Change_Safety_Buffer", groups = "tropicana")
    public void TC5005_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Change_Safety_Buffer(){
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

        test.get().info("Step 7: Navigate to Change Bundle page");
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();

        test.get().info("Step 7 : Select FC mobile to Change Safety Buffer");
        String fcSubText = Common.findValueOfStream(subList, "Mobile Ref 2");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(fcSubText, "Change Safety Buffer");

        test.get().info("Step 8 : Observe the Current Bundle section");
        ServiceOrdersPage.ChangeBundle changeBundle = ServiceOrdersPage.ChangeBundle.getInstance();
        Assert.assertEquals(fcSubText, changeBundle.getSubscriptionNumber());
        String expectNextBillDate = String.format("%s (%s days from today)", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT_IN_PDF), TimeStamp.TodayPlus1MonthMinusToday());
        Assert.assertEquals(expectNextBillDate, changeBundle.getNextBillDateForThisAccount());
        Assert.assertEquals( changeBundle.getCurrentTariff(), "£10 Tariff 12 Month Contract");
        Assert.assertFalse(changeBundle.isBonusBundle());
        //Assert.assertTrue(ServiceOrdersPage.ChangeBundle.getInstance().bundleExists(BundlesToSelectEntity.getSafetyBuffersAToSelect()));
        //CareTestBase.page().checkFamilyPerkBundleToolTip(BundlesToSelectEntity.getSafetyBuffersAToSelect());



    }
}
