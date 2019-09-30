package suite.regression.tropicana;

import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.List;

public class TC4667_SC_Validation_For_Tropicana_Bundle_In_My_Tariff_Page extends BaseTest {
    private String customerNumber = "15758";
    private Date newStartDate;
    private String username;
    private String password;
    String subscription1;
    String subscription2;
    String serviceOrderId;

    //3 data type
    @Test(enabled = true, description = "TC4667_SC_Validation_For_Tropicana_Bundle_In_My_Tariff_Page", groups = "Tropicana")
    public void TC4667_SC_Validation_For_Tropicana_Bundle_In_My_Tariff_Page() {
        test.get().info("Step 1 : Create a customer has Subscription with Tropicana bundle");
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

        test.get().info("Step 8 : Login to self care");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password, customerNumber);

        test.get().info("Step 9 : Verify my personal information page is displayed");
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 10 : Click view or change my tariff details link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();

        test.get().info("Step 11 : Verify my tariff details page is displayed");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 12 : Verify Tropicana bundle is added as a Month as expected");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile1Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 2");
        Assert.assertEquals(mobile1Tariff.getFamilyPerkStack().get(0).getText(), String.format("Family perk - 250MB per month   ACTIVE  as of  %s", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT4)));

        test.get().info("Step 13 : Verify Hover your cursor over the question mark image next to the bundle to open the tooltip");
        Assert.assertEquals(mobile1Tariff.getFamilyPerkHelpIconText(), "???help.bonus.ACTIVE???");
    }
}
