package suite.regression.selfcare.modifysubscription;

import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.options.DeactivateSubscriptionPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

public class TC31973_C07_Change_family_perk_not_eligible extends BaseTest {
    String sub;
    String subno1;
    String customerNumber ;

    @Test(enabled = true, description = "TC331973 Change family perk not eligible", groups = "SelfCare")
    public void TC31973_C07_Change_family_perk_not_eligible() {
        test.get().info("Step 1 : Create a Customer with one off bundle subscription active");
        String path = "src\\test\\resources\\xml\\selfcare\\modifysubscription\\TC31973_createOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile FC");
        subno1 = owsActions.serviceRef;

        test.get().info("load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("deactive a subscription and return to customer");
        String deactivationReason = "Customer Cancelled";
        deactiveASubScriptionAndReturnToCustomer("Mobile FC", "Customer Cancelled");

        test.get().info("Verify FC subscription status is inactive");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        Assert.assertEquals(CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue("Mobile FC"), "Inactive");

        test.get().info("Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("access tariff detail page");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("click add or change a family park button for inactive subscription");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance().clickAddOrChangeAFamilyPerkOfInacitveSubscription();

        test.get().info("verify error message displayed");
        String message = "Sorry, your tariff isn’t eligible for Family Perks. If you’d like to change your tariff, please call our Customer Care team on 4455 from your Tesco Mobile phone.";
        Assert.assertEquals(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance().getErrorMssgDialog(), message);
    }

    private void deactiveASubScriptionAndReturnToCustomer(String subScriptionNumber, String reason) {

            MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
            for (int i = 0; i < 3; i++) {
                i = i + 1;
                String subNo = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(i);
                if (subNo.endsWith(subScriptionNumber)) {
                    sub = subNo.split(" ")[0];
                    break;
                }
            }

        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().selectDeactivateBySubscription(sub);
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().enterDeactivateReason(reason);
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().clickNextButton();
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().clickNextButton();
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().clickReturnToCustomer();
    }

}
