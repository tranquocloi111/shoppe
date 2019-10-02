package suite.regression.soho;

import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SummaryContentsPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;
import java.sql.Date;

public class TC5023_001_Business_Customer_That_Has_Multi_Subs_One_Of_Them_Is_Inactivated extends BaseTest{
    private String customerNumber = "9613";
    private Date newStartDate;
    private String username;
    private String password;
    private String subNo1;
    private String subNo2;

    @Test(enabled = true, description = "TC5023_001_Business_Customer_That_Has_Multi_Subs_One_Of_Them_Is_Inactivated", groups = "Soho")
    public void TC5023_001_Business_Customer_That_Has_Multi_Subs_One_Of_Them_Is_Inactivated() {
        test.get().info("Step 1 :  Business customer that has multi subs, one of them is Inactivated");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC5023_001_request_business_type.xml";
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

        test.get().info("Step 7 : Verify color and information of business information tab ");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        SummaryContentsPage.BusinessInformationPage business = SummaryContentsPage.BusinessInformationPage.getInstance();
        verifyInformationColorBoxHeaderBusiness();
        Assert.assertTrue(business.isBusinessPresent());
        Assert.assertEquals(business.getBusinessName(), "Tom Cruise");

        test.get().info("Step 8 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateSubscriptionWithoutEtc();

        test.get().info("Step 9 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo2));

        test.get().info("Step 10 : Login to SelfCare");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password, customerNumber);

        test.get().info("Step 11 : Navigate to the My tariff and credit agreement documents  page");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();

        test.get().info("Step 12 : Click on the button Add or change a Perk button then validate the pop-up message");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage mobile2Tariff = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile 2");
        mobile2Tariff.clickAddOrChangeAPerkBtn();
        Assert.assertEquals(mobile2Tariff.getPopupMessageOfPerk(), "Sorry, your tariff isn’t eligible for Perks. If you’d like to change your tariff, please call our Customer Care team on 0800 032 1160 from your Tesco Mobile phone.");

    }

    private void verifyInformationColorBoxHeaderBusiness(){
        SummaryContentsPage summaryContentsPage = SummaryContentsPage.getInstance();
        for (int i = 0; i < summaryContentsPage.getBackGroundColorOfHeader().size(); i++) {
            Assert.assertEquals(summaryContentsPage.getBackGroundColorOfHeader().get(i), "rgba(255, 220, 0, 1)");
        }
    }
}
