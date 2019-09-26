package suite.regression.soho;

import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.pages.BasePage;
import logic.pages.care.MenuPage;
import logic.pages.care.find.FindPage;
import logic.pages.care.options.ConfirmNewCustomerTypePage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

public class TC4313_Restrict_Access_To_Business_Customers_And_Change_Customer_Type extends BaseTest {
    String customerNumber;

    @Test(enabled = true, description = "TC4313_Restrict_Access_To_Business_Customers_And_Change_Customer_Type", groups = "Soho")
    public void TC4313_Restrict_Access_To_Business_Customers_And_Change_Customer_Type() {
        test.get().info("Step 1 : Create a Customer with business type");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC3796_request_business_type.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Add restrict Access to Business Customers And Change Customer Type");
        updateNoneAccessBusinessCustomers();

        test.get().info("Step 6 : Login to Care without opening customer");
        CareTestBase.page().loadCustomerInHubNetWithoutOpenCustomer(customerNumber);

        test.get().info("Step 7 : Verify has no undergo DPA validation");
        Assert.assertFalse(FindPage.getInstance().isUnderGoPresent());

        test.get().info("Step 8 : Add Access to Business Customers And Change Customer Type");
        updateReadWriteAccessBusinessCustomers();

        test.get().info("Step 9 : Re_login to Care without opening customer");
        CareTestBase.page().reLoadCustomerInHubNetWithoutOpenCustomer(customerNumber);

        test.get().info("Step 10 : Verify has undergo DPA validation");
        Assert.assertEquals(FindPage.getInstance().getUnderGoValue(), "underline solid rgb(26, 27, 30)");

        test.get().info("Step 11 : ReLogin to Care Screen");
        updateNoneAccessChangeTypeCustomer();
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickChangeCustomerTypeLink();

        test.get().info("Step 12 : Verify user has no permission to change customer type");
        ConfirmNewCustomerTypePage confirmNewCustomerTypePage  = ConfirmNewCustomerTypePage.getInstance();
        Assert.assertEquals(confirmNewCustomerTypePage.getErrorMessage(), "Your security settings do not allow access to this service order.");
        confirmNewCustomerTypePage.clickDeleteButton();
        confirmNewCustomerTypePage.acceptComfirmDialog();
        confirmNewCustomerTypePage.clickReturnToCustomer();


        test.get().info("Step 13 : Verify user has  permission to change customer type");
        updateReadWriteAccessChangeTypeCustomer();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.RightMenuPage.getInstance().clickChangeCustomerTypeLink();
        Assert.assertEquals(confirmNewCustomerTypePage.getCurrentCustomerType(), "Business");
        Assert.assertEquals(confirmNewCustomerTypePage.getNewCustomerType(), "Consumer");
    }
}
