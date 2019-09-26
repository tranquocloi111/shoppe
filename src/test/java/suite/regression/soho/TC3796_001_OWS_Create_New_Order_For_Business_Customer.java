package suite.regression.soho;

import framework.utils.Xml;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SummaryContentsPage;
import logic.pages.care.main.HeaderContentPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;


public class TC3796_001_OWS_Create_New_Order_For_Business_Customer extends BaseTest {
    private String customerNumber = "9289";
    private String username;
    private String password;

    @Test(enabled = true, description = "TC3796_001_OWS_Create_New_Order_For_Business_Customer ", groups = "Soho")
    public void TC3796_001_OWS_Create_New_Order_For_Business_Customer() {
        test.get().info("Step 1 : Create a Customer with business type");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC3796_request_business_type.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Get Subscription Number");
        customerNumber = owsActions.customerNo;
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3 : Verify color and information of business information tab ");
        SummaryContentsPage.BusinessInformationPage business = SummaryContentsPage.BusinessInformationPage.getInstance();
        verifyInformationColorBoxHeaderBusiness();
        Assert.assertTrue(business.isBusinessPresent());
        Assert.assertEquals(business.getBusinessName(), "Tom Cruise");

        test.get().info("Step 4 : Verify Main Header Name and Recent Customers For Business Customer ");
        HeaderContentPage headerContentPage = HeaderContentPage.getInstance();
        Assert.assertEquals(headerContentPage.getMainContainBreadCrumbsBox(), "Customers > Summary for Tom Cruise ("+owsActions.lastName+", "+ owsActions.firstName+")");
        Assert.assertEquals(headerContentPage.getMainHeaderSummaryText(), "Tom Cruise ("+owsActions.lastName+", "+ owsActions.firstName+")");
        Assert.assertEquals(CommonContentPage.RecentCustomersPage.getIntance().getFirstRecentCustomer(), owsActions.lastName+", "+ owsActions.firstName);

        test.get().info("Step 5 : Login to SelfCare");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password , customerNumber);

        test.get().info("Step 6 : Navigate to View Change my account detail page");
        MyPersonalInformationPage.myAccountSection.getInstance().clickViewOrChangeMyAccountDetails();
        MyAccountDetailsPage.personnalDetails personalDetails = MyAccountDetailsPage.personnalDetails.getInstance();
        Assert.assertEquals(personalDetails.getBusinessName(), "Tom Cruise");

        test.get().info("Step 7 : Verify Get Order Response");
        Xml xml = owsActions.getOrder(owsActions.orderIdNo);
        Assert.assertEquals(xml.getTextByXpath("//account//@type"), "BUSINESS");
        Assert.assertEquals(xml.getTextByTagName("businessName"), "Tom Cruise");
    }

    private void verifyInformationColorBoxHeaderBusiness(){
        SummaryContentsPage summaryContentsPage = SummaryContentsPage.getInstance();
        for (int i = 0; i < summaryContentsPage.getBackGroundColorOfHeader().size(); i++) {
            Assert.assertEquals(summaryContentsPage.getBackGroundColorOfHeader().get(i), "rgba(255, 220, 0, 1)");
        }
    }
}
