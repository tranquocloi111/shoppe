package suite.regression.tropicana;

import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.utils.Common;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.List;

public class TC5000_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Add_One_off_Bundle extends BaseTest {
    private String customerNumber = "15758";
    private Date newStartDate;
    private String subscription1;
    private String serviceOrderId;

    @Test(enabled = true, description = "Care-VIEW-SUB- Validation for new Bonus bundle in Add One-off Bundle", groups = "tropicana")
    public void TC5000_Care_View_Sub_Validation_For_New_Bonus_Bundle_In_Add_One_off_Bundle(){
        test.get().info("Step 1 : Create a customer subscription related tariff linked with a Tropicana bundle group");
        String path = "\\src\\test\\resources\\xml\\tropicana\\TC4617_TC001_request.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscription1 = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");

        test.get().info("Step 6 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription1);

        test.get().info("Step 7 : Submit Provision Wait");
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderBySubAndType(subscription1, "Change Bundle"));
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        BaseTest.updateThePDateAndBillDateForSO(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 6: Navigate to Change Bundle page");
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();

        test.get().info("Step 7 : Select FC mobile to Add One-off Bundle");
        List<String> subList = CareTestBase.getAllSubscriptionsNumber();
        String fcSubText = Common.findValueOfStream(subList, "Mobile Ref 1");
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(fcSubText, "Add One-off Bundle");

        test.get().info("Step 8 : Observe the Current Bundle section");
        ServiceOrdersPage.AddOneOffBundle addOneOffBundle = ServiceOrdersPage.AddOneOffBundle.getInstance();
        Assert.assertEquals("", addOneOffBundle.getSubscriptionNumber());
        Assert.assertEquals("", addOneOffBundle.getNextBillDateForThisAccount());
        Assert.assertEquals("", addOneOffBundle.getCurrentTariff());
        Assert.assertEquals("", addOneOffBundle.getPackagedBundle());
        Assert.assertTrue(addOneOffBundle.isBonusBundleDisplayed(""));



    }
}
