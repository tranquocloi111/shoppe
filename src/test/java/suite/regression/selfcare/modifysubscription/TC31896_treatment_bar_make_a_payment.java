package suite.regression.selfcare.modifysubscription;

import framework.config.Config;
import framework.utils.RandomCharacter;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.Arrays;
import java.util.List;

public class TC31896_treatment_bar_make_a_payment extends BaseTest {


    String subNo1;

    @Test(enabled = true, description = "TC31896 treatment bar make a payment", groups = "SelfCare")
    public void TC31896_treatment_bar_make_a_payment() {
        test.get().info("Step 1: Create a CC customer with no bundle and sim only");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_NC_no_bundle_and_sim_only";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile NC 1");
        String customerNumber = owsActions.customerNo;
        subNo1 = owsActions.serviceRef;

        test.get().info("Step 2: load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3: Turn on all bars in Barring and Roaming options");
        turnOnAllBarsInBarringAndRoamingOptions();

        test.get().info("Step 4: Login to self care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 5: Verify the message customers service has been restricted is displayed");
        MyPersonalInformationPage.myAlertSection.getInstance().isMssgDisplayed("Your service has been restricted. Click here for more options.");

        test.get().info("Step 6: click service has been restricted for more options links");
        MyPersonalInformationPage.myAlertSection.getInstance().clickAlertMessageByText("Your service has been restricted. Click here for more options.");

        test.get().info("Step 7: verify my tariff details page is displayed");
        SelfCareTestBase.page().verifyMyTarriffAndCreditAgreementdocuments();

        test.get().info("Step 8: verify my tariff details page is displayed");
        Assert.assertEquals("Click here to make a payment", MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getUnpaidBill());

        test.get().info("Step 9: verify unpaid tool tip");
        String expectedToolTip = "Unpaid bill means that you have an outstanding bill with us that needs payment. By making this payment, we will remove any bars placed on your account.";
        Assert.assertEquals(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getUnPaidToolTip(), expectedToolTip);

        test.get().info("Step 10: click unpaid lin");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").clickUnPaidLink();


        test.get().info("Step 11: verify make a one off payment");
        SelfCareTestBase.page().verifyMakeAOneOffPayment();


    }

    public void turnOnAllBarsInBarringAndRoamingOptions() {
        MenuPage.RightMenuPage.getInstance().clickConfigureBarRoamingonLink();
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscriptionWithouAction(subNo1 + " Mobile NC 1");
        ServiceOrdersPage.ConfigureSubscription.getInstance().selectSubscriptionBarring("Outbound");
        ServiceOrdersPage.ConfigureSubscription.getInstance().selectSubscriptionRoaming("Barred");
        ServiceOrdersPage.ConfigureSubscription.getInstance().enterNote("turn on treatment for FC Mobile 1");
        ServiceOrdersPage.ConfigureSubscription.getInstance().selectSubscriptionBarReason("Treatment");
        ServiceOrdersPage.ConfigureSubscription.getInstance().clickNextButton();

        ServiceOrdersPage.ConfigureSubscription.getInstance().clickReturnToCustomer();
    }


}
