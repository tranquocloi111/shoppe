package suite.regression.soho;

import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SummaryContentsPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;

public class TC5023_002_SelfCare_Validation_For_Amend_The_Self_Care_Business_Customer_Service extends BaseTest{
    private String customerNumber = "9629";
    private Date newStartDate;
    private String username;
    private String password;
    private String subNo1;
    private String subNo2;

    @Test(enabled = true, description = "TC5023_002_SelfCare_Validation_For_Amend_The_Self_Care_Business_Customer_Service", groups = "Soho")
    public void TC5023_001_SelfCare_Validation_For_Amend_The_Self_Care_Business_Customer_Service() {
        test.get().info("Step 1 : Consumer customer that has multi subs one of them is Inactivated");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC5023_002_request_residential_type.xml";
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
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");
        subNo2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 2");

        test.get().info("Step 7 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateSubscriptionWithoutEtc();

        test.get().info("Step 8 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo2));

        test.get().info("Step 9 : Login to SelfCare");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password, customerNumber);

        test.get().info("Step 10 : Navigate to the My tariff and credit agreement documents page");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();

        test.get().info("Step 11 : Click on the button Add or change a Perk button then validate the pop-up message");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile2Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile 2");
        mobile2Tariff.clickAddOrChangeAFamilyPerkBtn();
        Assert.assertEquals(mobile2Tariff.getPopupMessageOfPerk(), "Sorry, your tariff isn’t eligible for Family Perks. If you’d like to change your tariff, please call our Customer Care team on 4455 from your Tesco Mobile phone.");

    }

    private void verifyInformationColorBoxHeaderBusiness(){
        SummaryContentsPage summaryContentsPage = SummaryContentsPage.getInstance();
        for (int i = 0; i < summaryContentsPage.getBackGroundColorOfHeader().size(); i++) {
            Assert.assertEquals(summaryContentsPage.getBackGroundColorOfHeader().get(i), "rgba(255, 220, 0, 1)");
        }
    }
}
