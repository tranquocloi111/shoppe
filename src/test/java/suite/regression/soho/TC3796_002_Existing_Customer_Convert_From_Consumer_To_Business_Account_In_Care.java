package suite.regression.soho;

import framework.utils.RandomCharacter;
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


public class TC3796_002_Existing_Customer_Convert_From_Consumer_To_Business_Account_In_Care extends BaseTest {
    private String customerNumber = "9289";
    private String username;
    private String password;


    @Test(enabled = true, description = "TC3796_002_Existing_Customer_Convert_From_Consumer_To_Business_Account_In_Care)", groups = "SOHO")
    public void TC3796_002_Existing_Customer_Convert_From_Consumer_To_Business_Account_In_Care() {
        test.get().info("Step 1 : Create a Customer with RESIDENTIAL type");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC3796_002_request_residential_type.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Get Subscription Number");
        updateReadWriteAccessChangeTypeCustomer();
        customerNumber = owsActions.customerNo;
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickChangeCustomerTypeLink();

        test.get().info("Step 3 : Verify user has permission to change customer type");
        ConfirmNewCustomerTypePage confirmNewCustomerTypePage  = ConfirmNewCustomerTypePage.getInstance();
        Assert.assertEquals(confirmNewCustomerTypePage.getCurrentCustomerType(), "Consumer");
        Assert.assertEquals(confirmNewCustomerTypePage.getNewCustomerType(), "Business");
        confirmNewCustomerTypePage.clickNextButton();

        test.get().info("Step 4 : Change Customer type from Consumer to Business Type");
        ChangeCustomerTypePage changeCustomerTypePage = ChangeCustomerTypePage.getInstance();
        String businessName = "Business_" + RandomCharacter.getRandomNumericString(9);
        String billStyle = "VAT";
        changeCustomerTypePage.ChangeCustomerTypeFromConsumerToBusinessType(businessName, billStyle);

        test.get().info("Step 5 : Verify color and information of business information tab ");
        CommonContentPage.CustomerSummarySectionPage customerSummarySectionPage = CommonContentPage.CustomerSummarySectionPage.getInstance();
        Assert.assertEquals(customerSummarySectionPage.getCustomerType(), "Business ( "+Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT) +" )");

        SummaryContentsPage.BusinessInformationPage business = SummaryContentsPage.BusinessInformationPage.getInstance();
        verifyInformationColorBoxHeaderBusiness();
        Assert.assertTrue(business.isBusinessPresent());
        Assert.assertEquals(business.getBusinessName(), businessName);

        test.get().info("Step 6 : Verify Main Header Name and Recent Customers For Business Customer ");
        HeaderContentPage headerContentPage = HeaderContentPage.getInstance();
        Assert.assertEquals(headerContentPage.getMainContainBreadCrumbsBox(), "Customers > Summary for "+businessName+ " ("+owsActions.lastName+", "+ owsActions.firstName+")");
        Assert.assertEquals(headerContentPage.getMainHeaderSummaryText(), businessName+ " ("+owsActions.lastName+", "+ owsActions.firstName+")");
        Assert.assertEquals(CommonContentPage.RecentCustomersPage.getIntance().getFirstRecentCustomer(), owsActions.lastName+", "+ owsActions.firstName);

        test.get().info("Step 7 : Login to SelfCare");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password , customerNumber);

        test.get().info("Step 8 : Navigate to View Change my account detail page");
        MyPersonalInformationPage.myAccountSection.getInstance().clickViewOrChangeMyAccountDetails();
        MyAccountDetailsPage.personnalDetails personalDetails = MyAccountDetailsPage.personnalDetails.getInstance();
        Assert.assertEquals(personalDetails.getBusinessName(), businessName);
    }

    private void verifyInformationColorBoxHeaderBusiness(){
        SummaryContentsPage summaryContentsPage = SummaryContentsPage.getInstance();
        for (int i = 0; i < summaryContentsPage.getBackGroundColorOfHeader().size(); i++) {
            Assert.assertEquals(summaryContentsPage.getBackGroundColorOfHeader().get(i), "rgba(255, 220, 0, 1)");
        }
    }
}
