package suite.regression.soho;

import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SummaryContentsPage;
import logic.pages.care.main.HeaderContentPage;
import logic.pages.care.options.ChangeCustomerTypePage;
import logic.pages.care.options.ConfirmNewCustomerTypePage;
import logic.pages.selfcare.MyAccountDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TC3796_003_Existing_Customer_Convert_From_Consumer_To_Business_Account_And_Add_Subscriptions extends BaseTest {
    private String customerNumber = "10170";
    private String username;
    private String password;
    private String businessName;
    private String billStyle;

    @Test(enabled = true, description = "TC3796_003_Existing_Customer_Convert_From_Consumer_To_Business_Account_And_Add_Subscriptions", groups = "SOHO")
    public void TC3796_003_Existing_Customer_Convert_From_Consumer_To_Business_Account_And_Add_Subscriptions() {
        test.get().info("Step 1 : Create a Customer with RESIDENTIAL type");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC3796_003_request_residential_type.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Login to Care Screen");
        customerNumber = owsActions.customerNo;
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3 : Change Customer type from Consumer to Business Type");
        billStyle = "VAT";
        businessName = changeCustomerFromConsumerToBusiness(billStyle);

        test.get().info("Step 4 : Verify color and information of business information tab ");
        CommonContentPage.CustomerSummarySectionPage customerSummarySectionPage = CommonContentPage.CustomerSummarySectionPage.getInstance();
        Assert.assertEquals(customerSummarySectionPage.getCustomerType(), "Business ( " + Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT) + " )");

        SummaryContentsPage.BusinessInformationPage business = SummaryContentsPage.BusinessInformationPage.getInstance();
        verifyInformationColorBoxHeaderBusiness();
        Assert.assertTrue(business.isBusinessPresent());
        Assert.assertEquals(business.getBusinessName(), businessName);

        test.get().info("Step 5 : Verify Main Header Name and Recent Customers For Business Customer ");
        HeaderContentPage headerContentPage = HeaderContentPage.getInstance();
        Assert.assertEquals(headerContentPage.getMainContainBreadCrumbsBox(), "Customers > Summary for " + businessName + " (" + owsActions.lastName + ", " + owsActions.firstName + ")");
        Assert.assertEquals(headerContentPage.getMainHeaderSummaryText(), businessName + " (" + owsActions.lastName + ", " + owsActions.firstName + ")");
        Assert.assertEquals(CommonContentPage.RecentCustomersPage.getIntance().getFirstRecentCustomer(), owsActions.lastName + ", " + owsActions.firstName);

        test.get().info("Step 6 : Login to SelfCare");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password, customerNumber);

        test.get().info("Step 7 : Navigate to View Change my account detail page");
        MyPersonalInformationPage.myAccountSection.getInstance().clickViewOrChangeMyAccountDetails();
        MyAccountDetailsPage.personnalDetails personalDetails = MyAccountDetailsPage.personnalDetails.getInstance();
        Assert.assertEquals(personalDetails.getBusinessName(), businessName);

        test.get().info("Step 8 : Add More Than 6 Subscriptions To Customer with Business type");
        path = "src\\test\\resources\\xml\\soho\\TC3796_003_more_than_10_sub_request_business_type.xml";
        Xml xml = owsActions.createNewOrderWithExistCustomer(path, customerNumber);

        test.get().info("Step 9 : Verify the system does not allow to create new customer");
        Assert.assertEquals(xml.getTextsByTagName("errorCode").get(0), "CO_075");
        Assert.assertEquals(xml.getTextsByTagName("errorDescription").get(0), "Max Number of deals per basket exceeded");
        Assert.assertEquals(xml.getTextsByTagName("errorCode").get(1), "CO_060");
        Assert.assertEquals(xml.getTextsByTagName("errorDescription").get(1), "Max Subscriptions Per Account exceeded");

        test.get().info("Step 10 : Add More Than 5 Subscriptions To Customer with Business type");
        path = "src\\test\\resources\\xml\\soho\\TC3796_003_request_business_type.xml";
        owsActions.createNewOrderWithExistCustomer(path, customerNumber);

        test.get().info("Step 11 : Verify 10 Subscriptions were added to customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        List<String> subList = getAllSubscriptionsNumber();
        verifyNameOfSubscriptions(subList);
    }

    private List<String> getAllSubscriptionsNumber() {
        waitUntil10SubscriptionsPresent();
        List<String> subscriptionNumberList = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            String subNo = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(i);
            subscriptionNumberList.add(subNo);
        }
        return subscriptionNumberList;
    }

    private void verifyNameOfSubscriptions(List<String> subList) {
        Assert.assertEquals(subList.size(), 10);
        for (int i = 1; i < subList.size() + 1; i++) {
            int finalI = i;
            Assert.assertTrue(subList.stream().anyMatch(str -> str.contains("Mobile " + finalI)));
        }
    }

    private void waitUntil10SubscriptionsPresent(){
        int count =  CommonContentPage.SubscriptionsGridSectionPage.getInstance().getRowNumberOfSubscriptionsTable();
        while (count < 10){
            count = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getRowNumberOfSubscriptionsTable();
            MenuPage.RightMenuPage.getInstance().clickRefreshLink();
            MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        }
    }
}
