package suite.regression.soho;

import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SummaryContentsPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;

public class TC_xxxx_soho extends BaseTest {
    private String customerNumber = "9289";
    private Date newStartDate;
    private String username;
    private String password;


    @Test(enabled = true, description = "TC_xxxx_soho", groups = "Tropicana")
    public void TC_xxxx_soho() {
        test.get().info("Step 1 : Create a Customer with business type");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC4562_request_business_type.xml";
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

        test.get().info("Step 7 : Verify color and information of business information tab ");
        SummaryContentsPage.BusinessInformationPage business = SummaryContentsPage.BusinessInformationPage.getInstance();
        verifyInformationColorBoxHeaderBusiness();
        Assert.assertTrue(business.isBusinessPresent());
        Assert.assertEquals(business.getBusinessName(), "Tom Cruise");

        test.get().info("Step 8 : Login to SelfCare");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password , customerNumber);

        test.get().info("Step 9 : Navigate to View Change my account detail page");
        MyPersonalInformationPage.myAccountSection.getInstance().clickViewOrChangeMyAccountDetails();
        MyAccountDetailsPage.personnalDetails personnalDetails = MyAccountDetailsPage.personnalDetails.getInstance();
        Assert.assertEquals(personnalDetails.getBusinessName(), "Tom Cruise");
    }

    private void verifyInformationColorBoxHeaderBusiness(){
        SummaryContentsPage summaryContentsPage = SummaryContentsPage.getInstance();
        for (int i = 0; i < summaryContentsPage.getBackGroundColorOfHeader().size(); i++) {
            Assert.assertEquals(summaryContentsPage.getBackGroundColorOfHeader().get(i), "rgba(255, 220, 0, 1)");
        }
    }
}
