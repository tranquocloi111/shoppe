package suite.regression.tropicana;

import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
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
import java.util.HashMap;
import java.util.List;

public class TC5000_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Add_One_off_Bundle extends BaseTest {
    private String customerNumber;
    private String subscription1;
    private String subscription2;

    @Test(enabled = true, description = "TC5000_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Add_One_off_Bundle", groups = "tropicana")
    public void TC5000_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Add_One_off_Bundle(){
        test.get().info("Step 1 : Create a customer subscription related tariff linked with a Tropicana bundle group");
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
        List<String> subList = getAllSubscriptionsNumber();
        subscription1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        subscription2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        test.get().info("Step 6 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        test.get().info("Step 6: Navigate to Change Bundle page");
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();

        test.get().info("Step 7 : Select FC mobile to Add One-off Bundle");
        String fcSubText = Common.findValueOfStream(subList, "Mobile Ref 2");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(fcSubText, "Add One-off Bundle");

        test.get().info("Step 8 : Observe the Current Bundle section with bonus");
        verifyCurrentBonusBundleSection();

        test.get().info("Step 9 : Select FC mobile to Add One-off Bundle");
        fcSubText = Common.findValueOfStream(subList, "Mobile Ref 1");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(fcSubText, "Add One-off Bundle");

        test.get().info("Step 10 : Observe the Current Bundle section without bonus");
        verifyCurrentWithoutBonusBundleSection();
    }

    private void verifyCurrentBonusBundleSection(){
        ServiceOrdersPage.AddOneOffBundle addOneOffBundle = ServiceOrdersPage.AddOneOffBundle.getInstance();
        Assert.assertEquals(addOneOffBundle.getSubscriptionNumber(), subscription2 + " Mobile Ref 2");
        Assert.assertEquals(addOneOffBundle.getNextBillDateForThisAccount(), Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT4) + " ("+TimeStamp.todayPlus1MonthMinusToday()+" days from today)");
        Assert.assertEquals(addOneOffBundle.getCurrentTariff(),"£10 Tariff 12 Month Contract");
        Assert.assertEquals(addOneOffBundle.getPackagedBundle(), "Bundle - 500 mins, 5000 texts (FC)");
        Assert.assertEquals(addOneOffBundle.getCellValueByIndex(3),"Double Data  0  1  Family perk - 250MB per month  250MB  N/A  N/A  £0.00 ");
        addOneOffBundle.clickPreButton();
    }

    private List<String> getAllSubscriptionsNumber(){
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        List<String> subscriptionNumberList = new ArrayList<>();
        for (int i = 0; i < 3; i++){
            String subNo = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(i);
            subscriptionNumberList.add(subNo);
        }
        return subscriptionNumberList;
    }

    private void verifyCurrentWithoutBonusBundleSection(){
        ServiceOrdersPage.AddOneOffBundle addOneOffBundle = ServiceOrdersPage.AddOneOffBundle.getInstance();
        Assert.assertEquals(addOneOffBundle.getSubscriptionNumber(), subscription1 + " Mobile Ref 1");
        Assert.assertEquals(addOneOffBundle.getNextBillDateForThisAccount(), Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT4) + " ("+TimeStamp.todayPlus1MonthMinusToday()+" days from today)");
        Assert.assertEquals(addOneOffBundle.getCurrentTariff(),"£10 Tariff 12 Month Contract");
        Assert.assertEquals(addOneOffBundle.getPackagedBundle(), "Bundle - 500 mins, 5000 texts (FC)");
        Assert.assertFalse(addOneOffBundle.isBonusBundleDisplayed("Double Data"));
    }
}
