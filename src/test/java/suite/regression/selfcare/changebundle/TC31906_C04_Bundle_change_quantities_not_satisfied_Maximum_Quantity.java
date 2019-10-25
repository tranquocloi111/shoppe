package suite.regression.selfcare.changebundle;

import logic.business.ws.ows.OWSActions;
import logic.pages.selfcare.MonthlyBundlesAddChangeOrRemovePage;
import logic.pages.selfcare.MyPersonalInformationPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.List;

public class TC31906_C04_Bundle_change_quantities_not_satisfied_Maximum_Quantity extends BaseTest {
 
     @Test(enabled = true, description = "TC31905 C04 bundle change quantities not satisfied minimun quantity", groups = "SelfCare")
    public void TC31905_C04_Bundle_change_quantities_not_satisfied_Minimum_Quantity() {

        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_DD_customer_with_FC_2_bundles_and_NK2720";
        test.get().info("Step 1 : Create a customer  with FC 2 bundles and NK2720");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 2: Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 3 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 4 : Click add or change bundles on my tariff page");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile 1").clickAddOrChangeABundleButton();
        SelfCareTestBase.page().verifyMonthlyBundlesAddChangeOrRemovePageDisplayed();
        test.get().info("Step 5 : change bundle for customer");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().selectBundlesByName("Monthly 250MB data allowance - 4G");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().selectBundlesByName("Monthly 1GB data allowance");

        test.get().info("Step 6 : verify error message ");
        List<String> errorMssg = SelfCareTestBase.page().errorMessageStack();
        Assert.assertEquals("You can only choose 2 bundle(s) max. from this group.", errorMssg.get(0));
    }


}
