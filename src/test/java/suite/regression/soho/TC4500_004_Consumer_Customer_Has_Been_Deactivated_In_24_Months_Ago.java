package suite.regression.soho;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.FindPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.find.SummaryContentsPage;
import logic.pages.care.main.HeaderContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TC4500_004_Consumer_Customer_Has_Been_Deactivated_In_24_Months_Ago extends BaseTest{
    private String customerNumber = "12232";
    private Date newStartDate;
    private String subNo1 = "07014145870";
    private Date endDate;
    private String userName = "un856631019@hsntech.com";
    private String passWord = "password1";
    private String orderId = "3285";

    //include HTM-4520
    @Test(enabled = true, description = "TC4500_004_Consumer_Customer_Has_Been_Deactivated_In_24_Months_Ago", groups = "SOHO")
    public void TC4500_004_Consumer_Customer_Has_Been_Deactivated_In_24_Months_Ago() {
        test.get().info("Step 1 :  Consumer customer one of them is Inactivated");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC4500_004_request_residential_type.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        userName = owsActions.username;
        passWord = owsActions.password;
        orderId = owsActions.orderIdNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update Customer Start Date");
        newStartDate = Parser.asDate(TimeStamp.Today().toLocalDate().minusYears(2).minusMonths(8));
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");

        test.get().info("Step 7 : Deactivate subscription");
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateAccountWithOutReturnRefund(TimeStamp.Today());

        test.get().info("Step 8 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo1));

        test.get().info("Step 9 : Update End Date to 24 moth ago");
        endDate = Parser.asDate(TimeStamp.Today().toLocalDate().minusYears(2).minusMonths(4));
        CommonActions.updateCustomerEndDate(customerNumber, endDate);

        test.get().info("Step 10 : Submit Anonymise Account Job");
        submitAnonymiseAccountJob();

        test.get().info("Step 11 : Verify Customer Anonymised in result table");
        CareTestBase.page().reLoadCustomerInHubNetWithoutOpenCustomer(customerNumber);
        FindPage findPage = FindPage.getInstance();
        Assert.assertEquals(findPage.getNameOfResult(0), "xxx");
        findPage.openCustomerByIndex(0);

        test.get().info("Step 12 : Verify Main Header Name and Recent Customers For Business Customer");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        HeaderContentPage headerContentPage = HeaderContentPage.getInstance();
        Assert.assertEquals(headerContentPage.getMainContainBreadCrumbsBox(), "Customers > Summary for xxx, xxx");
        Assert.assertEquals(headerContentPage.getMainHeaderSummaryText(), "xxx, xxx");
        Assert.assertEquals(CommonContentPage.RecentCustomersPage.getIntance().getFirstRecentCustomer(), "xxx, xxx");

        test.get().info("Step 13 : Verify Customer Summary");
        CommonContentPage.CustomerSummarySectionPage customerSummary = CommonContentPage.CustomerSummarySectionPage.getInstance();
        Assert.assertEquals(customerSummary.getAddress(), "");
        Assert.assertEquals(customerSummary.getStatus(), "Anonymised");
        Assert.assertEquals(customerSummary.getPassPhraseAnswer(), "xxx");

        test.get().info("Step 14 : Verify Subscriptions Table");
        verifySubscriptionNameIsAnonymised();

        test.get().info("Step 15 : Verify Find Order Request");
        path = "src\\test\\resources\\xml\\ows\\findorder\\find_order_by_mpn.xml";
        Xml xml = owsActions.submitFindOrder(path, subNo1);
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "No Orders Found");

        test.get().info("Step 16 : Verify Get Account Auth Request");
        xml = owsActions.submitGetAccountAuth(userName, passWord);
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "Username and password do not match");

        test.get().info("Step 17 : Verify Get Customer Request");
        xml = owsActions.submitGetCustomer(customerNumber);
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "Account Number could not be found");

        test.get().info("Step 18 : Verify Get Order Request");
        xml = owsActions.getOrder(orderId);
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "Order ID could not be found");

        test.get().info("Step 19 : Verify Get Contract Request");
        xml = owsActions.getContract(orderId);
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "Order ID could not be found");

        test.get().info("Step 20 : Verify Get Customer Request");
        path = "src\\test\\resources\\xml\\ows\\findcustomer\\find_customer_by_mpn.xml";
        xml = owsActions.submitFindCustomer(path, subNo1);
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "No Customers Found");
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

}
