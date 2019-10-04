package suite.regression.soho;

import logic.business.db.billing.CommonActions;
import logic.business.entities.BundlesToSelectEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.find.SummaryContentsPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ChangeBundlePage;
import logic.pages.care.options.ConfirmChangeBundlePage;
import logic.pages.selfcare.AddOrChangeAFamilyPerkPage;
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
import java.util.HashMap;
import java.util.List;

public class TC4314_001_Business_Validation_SMS_For_Loss_Of_Perk_Eligibility extends BaseTest{
    private String customerNumber = "11139";
    private Date newStartDate;
    private String username;
    private String password;
    private String subNo1;
    private String subNo2;
    private String subNo3;
    private String serviceOrderId;

    //include 5016 - 4959
    @Test(enabled = true, description = "TC4314_001_Business_Validation_SMS_For_Loss_Of_Perk_Eligibility", groups = "SOHO")
    public void TC4314_001_Business_Validation_SMS_For_Loss_Of_Perk_Eligibility() {
        test.get().info("Step 1 :  Business customer that has multi subs, one of them is Inactivated");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC4314_4505_request_business_type.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");
        subNo2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 2");
        subNo3 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 3");

        test.get().info("Step 6 : Verify color and information of business information tab ");
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        SummaryContentsPage.BusinessInformationPage business = SummaryContentsPage.BusinessInformationPage.getInstance();
        verifyInformationColorBoxHeaderBusiness();
        Assert.assertTrue(business.isBusinessPresent());
        Assert.assertEquals(business.getBusinessName(), "Tom Cruise");

        test.get().info("Step 7 : Add Family Perk Bundle To Sub1");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4314_family_perk_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subNo1);

        test.get().info("Step 8 : Verify A Family Perk Bundle Was Added To Subscription 1");
        VerifyAFamilyPerkBundleWasAddedToSubscription(subNo1, "Mobile 1");

        test.get().info("Step 9 : Add Family Perk Bundle To Sub2");
        selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4314_family_perk_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subNo2);

        test.get().info("Step 10 : Verify A Family Perk Bundle Was Added To Subscription 2");
        VerifyAFamilyPerkBundleWasAddedToSubscription(subNo2, "Mobile 2");

        test.get().info("Step 11 : Add Family Perk Bundle To Sub3");
        selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4314_family_perk_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subNo3);

        test.get().info("Step 12 : Verify A Family Perk Bundle Was Added To Subscription 3");
        VerifyAFamilyPerkBundleWasAddedToSubscription(subNo3, "Mobile 3");

        test.get().info("Step 13 : Update Customer Start Date");
        newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 14 : Deactivate subscription 2");
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateSubscriptionWithoutEtc();

        test.get().info("Step 15 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo2));

        test.get().info("Step 16 : Verify No Ad Hoc SMS Message Is Sent");
        verifyNoAdHocSMSMessageIsSent();

        test.get().info("Step 17 : Deactivate subscription 3");
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        ServiceOrdersPage.DeactivateSubscriptionPage.getInstance().deactivateSubscriptionWithoutEtc();

        test.get().info("Step 18 : Verify the subscription status is Inactive");
        Assert.assertEquals("Inactive", CommonContentPage.SubscriptionsGridSectionPage.getInstance().getStatusValue(subNo3));

        test.get().info("Step 19 : Verify Ad Hoc SMS Message is Sent in service order page");
        verifyAdHocSMSMessageIsSent();

        test.get().info("Step 19 : Verify Ad Hoc SMS Message is Sent in database correctly");
        verifyAdHocSMSMessagesIsSentInDatabase();

        test.get().info("Step 20 : Login to SelfCare");
        username = owsActions.username;
        password = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(username, password, customerNumber);

        test.get().info("Step 21 : Verify Alert Message correctly");
        MyPersonalInformationPage.MyTariffPage.myAlertSection myAlert = MyPersonalInformationPage.MyTariffPage.myAlertSection.getInstance();
        Assert.assertEquals(myAlert.getAllMessage().get(0), "As you’ve only got one eligible subscription on your account you’ll no longer get Perks. Click here for more info.");
    }

    private void VerifyAFamilyPerkBundleWasAddedToSubscription(String sub, String des){
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(sub +  " " + des);
        HashMap<String, String> newOtherProduct = OtherProductEntiy.dataForOtherBundleProductNoEndDate
                ("BUNDLER - [500MB-FDATA-0-FC-4G]", "Bundle", "Discount Bundle Recurring - [Family perk - 500MB per month - 4G]", "£0.00", TimeStamp.Today());
        Assert.assertEquals(1, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProduct(newOtherProduct));
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
    }

    private void verifyNoAdHocSMSMessageIsSent(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<String> serviceOrder = new ArrayList<>();
        serviceOrder.add("Created");
        serviceOrder.add("Ad-hoc SMS Message");
        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        Assert.assertEquals(0, Common.compareList(serviceOrders.getAllValueOfServiceOrder(),serviceOrder));
    }

    private void verifyAdHocSMSMessageIsSent(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<String> serviceOrder = new ArrayList<>();
        serviceOrder.add("Created");
        serviceOrder.add("Ad-hoc SMS Messages");
        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        Assert.assertEquals(1, Common.compareList(serviceOrders.getAllValueOfServiceOrder(),serviceOrder));
        serviceOrderId = serviceOrders.getServiceOrderidByType("Ad-hoc SMS Messages");
    }

    private void verifyAdHocSMSMessagesIsSentInDatabase() {
        List hiTransactionEvent = CommonActions.getHiTransactionEvent(serviceOrderId);
        HashMap hashMap = ((HashMap) hiTransactionEvent.get(0));
        String mess = String.format("<SMSREQUEST><MPN>%s</MPN><BODY>Tesco Mobile: The number of subscriptions on your account has dropped to 1 so next month we'll remove your perk.</BODY></SMSREQUEST>", subNo1);
        Assert.assertTrue(hashMap.get("CONTEXTINFO").toString().contains(mess));
    }
}
