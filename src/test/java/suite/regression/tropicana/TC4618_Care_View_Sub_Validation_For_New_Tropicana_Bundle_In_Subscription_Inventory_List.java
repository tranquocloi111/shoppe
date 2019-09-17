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
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

public class TC4618_Care_View_Sub_Validation_For_New_Tropicana_Bundle_In_Subscription_Inventory_List extends BaseTest {
    private String customerNumber;
    private Date newStartDate;
    private String subscription1;
    private String subscription2;

    @Test(enabled = true, description = "TC4618 SC - Care-VIEW-SUB- Validation for new Tropicana bundle in subscription inventory list", groups = "Tropicana")
    public void TC4618Care_View_Sub_Validation_For_New_Tropicana_Bundle_In_Subscription_Inventory_List(){
        test.get().info("Step 1 :  Create a Customer has 2 Subscription that has Tropicana bundle and has no Tropicana");
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

        test.get().info("Step 5 : Update Customer Start Date");
        newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscription1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        subscription2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        test.get().info("Step 7 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);

        test.get().info("Step 8 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue("Mobile Ref 2");

        test.get().info("Step 9 : Validate new Tropicana bundle added in subscription inventory list");
        validateNewTropicanaBundle();
    }

    private void validateNewTropicanaBundle(){
        HashMap<String,String> bonus = new HashMap<String,String>();
        bonus.put("Product Code","BUNDLER - [250MB-FDATA-0-FC]");
        bonus.put("Type","Bundle");
        bonus.put("Description","BUNDLER - [250MB-FDATA-0-FC]");
        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage otherProductsGrid = SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance();
        otherProductsGrid.getOtherProduct(bonus);
    }

    private void verifyNewTropicanaBundleInDB(){
        List bundle = CommonActions.getBundleByCustomerId(customerNumber);


    }
}
