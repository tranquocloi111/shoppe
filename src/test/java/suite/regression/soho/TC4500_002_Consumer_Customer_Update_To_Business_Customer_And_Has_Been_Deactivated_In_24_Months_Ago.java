package suite.regression.soho;

import framework.utils.RandomCharacter;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.HeaderContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.care.options.ChangeCustomerTypePage;
import logic.pages.care.options.ConfirmNewCustomerTypePage;
import logic.pages.selfcare.MyAccountDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Common;
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


public class TC4500_002_Consumer_Customer_Update_To_Business_Customer_And_Has_Been_Deactivated_In_24_Months_Ago extends BaseTest {
    private String customerNumber = "11738";
    private Date newStartDate;
    private String subNo1;
    private Date endDate;
    private String businessName;
    private String billStyle;

    @Test(enabled = true, description = "TC4500_002_Consumer_Customer_Update_To_Business_Customer_And_Has_Been_Deactivated_In_24_Months_Ago)", groups = "SOHO")
    public void TC4500_002_Consumer_Customer_Update_To_Business_Customer_And_Has_Been_Deactivated_In_24_Months_Ago() {
        test.get().info("Step 1 : Create a Customer with RESIDENTIAL type");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC3796_002_request_residential_type.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Get Subscription Number");
        customerNumber = owsActions.customerNo;
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3 : Change Customer type from Consumer to Business Type");
        billStyle = "VAT";
        businessName = changeCustomerFromConsumerToBusiness(billStyle);

        test.get().info("Step 4 : Verify color and information of business information tab ");
        CommonContentPage.CustomerSummarySectionPage customerSummarySectionPage = CommonContentPage.CustomerSummarySectionPage.getInstance();
        Assert.assertEquals(customerSummarySectionPage.getCustomerType(), "Business ( "+Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT) +" )");
        SummaryContentsPage.BusinessInformationPage business = SummaryContentsPage.BusinessInformationPage.getInstance();
        verifyInformationColorBoxHeaderBusiness();
        Assert.assertTrue(business.isBusinessPresent());
        Assert.assertEquals(business.getBusinessName(), businessName);

        test.get().info("Step 5 : Verify Main Header Name and Recent Customers For Business Customer ");
        HeaderContentPage headerContentPage = HeaderContentPage.getInstance();
        Assert.assertEquals(headerContentPage.getMainContainBreadCrumbsBox(), "Customers > Summary for "+ businessName + " ("+owsActions.lastName+", "+ owsActions.firstName+")");
        Assert.assertEquals(headerContentPage.getMainHeaderSummaryText(), businessName+ " ("+owsActions.lastName + ", " + owsActions.firstName+")");
        Assert.assertEquals(CommonContentPage.RecentCustomersPage.getIntance().getFirstRecentCustomer(), owsActions.lastName + ", " + owsActions.firstName);

        test.get().info("Step 6 : verify Change Customer Type Before Anonymise");
        verifyChangeCustomerTypeBeforeAnonymised();

        test.get().info("Step 7 : Update Customer Start Date");
        newStartDate = Parser.asDate(TimeStamp.Today().toLocalDate().minusYears(2).minusMonths(8));
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 8 : Get Subscription Number");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");

        test.get().info("Step 9 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateAccountWithOutReturnRefund(TimeStamp.Today());

        test.get().info("Step 10 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo1));

        test.get().info("Step 11 : Update End Date to 24 moth ago");
        endDate = Parser.asDate(TimeStamp.Today().toLocalDate().minusYears(2).minusMonths(4));
        CommonActions.updateCustomerEndDate(customerNumber, endDate);

        test.get().info("Step 12 : Submit Anonymise Acount Job");
        submitAnonymiseAccountJob();

        test.get().info("Step 13 : Verify Customer Anonymised in result table");
        CareTestBase.page().reLoadCustomerInHubNetWithoutOpenCustomer(customerNumber);
        FindPage findPage = FindPage.getInstance();
        Assert.assertEquals(findPage.getNameOfResult(0), "xxx");
        findPage.openCustomerByIndex(0);

        test.get().info("Step 14 : Verify color and information of business information tab ");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        business = SummaryContentsPage.BusinessInformationPage.getInstance();
        verifyInformationColorBoxHeaderBusiness();
        Assert.assertTrue(business.isBusinessPresent());
        Assert.assertEquals(business.getBusinessName(), "");

        test.get().info("Step 15 : Verify Main Header Name and Recent Customers For Business Customer");
        headerContentPage = HeaderContentPage.getInstance();
        Assert.assertEquals(headerContentPage.getMainContainBreadCrumbsBox(), "Customers > Summary for  (xxx, xxx)");
        Assert.assertEquals(headerContentPage.getMainHeaderSummaryText(), "(xxx, xxx)");
        Assert.assertEquals(CommonContentPage.RecentCustomersPage.getIntance().getFirstRecentCustomer(), "xxx, xxx");

        test.get().info("Step 16 : Verify Customer Summary");
        CommonContentPage.CustomerSummarySectionPage customerSummary = CommonContentPage.CustomerSummarySectionPage.getInstance();
        Assert.assertEquals(customerSummary.getAddress(), "");
        Assert.assertEquals(customerSummary.getStatus(), "Anonymised");
        Assert.assertEquals(customerSummary.getPassPhraseAnswer(), "xxx");

        test.get().info("Step 17 : Verify Subscriptions Table");
        verifySubscriptionNameIsAnonymised();

        test.get().info("Step 18 : Verify Change Customer Type Service Order Anonymised ");
        verifyChangeCustomerTypeAnonymised();
    }

    private void verifySubscriptionNameIsAnonymised(){
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage subscriptionsGrid = CommonContentPage.SubscriptionsGridSectionPage.getInstance();
        List<String> sub = new ArrayList<>();
        sub.add(subNo1 + " xxx");
        sub.add("Safety Buffer - £20");
        sub.add(Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT));
        sub.add(Parser.parseDateFormate(endDate, TimeStamp.DATE_FORMAT));
        sub.add("FC12-1000-500SO - £10 Tariff 12 Month Contract - £10.00");
        sub.add("Inactive");
        Assert.assertEquals(Common.compareList(subscriptionsGrid.getAllValueSubscription(), sub), 1);

        subscriptionsGrid.clickSubscriptionNumberLinkByIndex(1);
        SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage general = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        Assert.assertEquals(general.getReference(), "xxx");
    }

    private void verifyChangeCustomerTypeAnonymised(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        serviceOrders.clickServiceOrderByType("Change Customer Type");

        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(detailsPage.getBusinessName(), "");
        Assert.assertEquals(detailsPage.getCurrentCustomerType(), "Consumer");
        Assert.assertEquals(detailsPage.getNewCustomerType(), "Business");
        Assert.assertEquals(detailsPage.getBillStyle(), "VAT");
    }

    private void verifyChangeCustomerTypeBeforeAnonymised(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        serviceOrders.clickServiceOrderByType("Change Customer Type");

        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(detailsPage.getBusinessName(), businessName);
        Assert.assertEquals(detailsPage.getCurrentCustomerType(), "Consumer");
        Assert.assertEquals(detailsPage.getNewCustomerType(), "Business");
        Assert.assertEquals(detailsPage.getBillStyle(), "VAT");
    }
}
