package suite.regression.selfcare;

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
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import logic.pages.care.find.SubscriptionContentPage;

public class TC31889_SelfCare_Change_Bundle_Remove_Family_Perk_Bundle_on_next_bill_date extends BaseTest {

    @Test(enabled = true, description = "TC31889 SelfCare Change Bundle Remove Family Perk Bundle on next bill date", groups = "SelfCare")
    public void TC31889_SelfCare_Change_Bundle_Remove_Family_Perk_Bundle_on_next_bill_date()
    {
        String TC19999_CreateOrder  = "src\\test\\resources\\xml\\SelfCare\\changebundle\\TC19999_CreateOrder";
        test.get().info("Step 1 : Create a CC customer, having family perk Bundle");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(TC19999_CreateOrder);

        test.get().info("Step 2 : Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3: Update the payment collection date is 10");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5: Update the start date of customer");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 7 : Verify customer data is updated ");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);

        test.get().info("Step 8 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

    }
}
