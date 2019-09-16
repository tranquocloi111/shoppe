package suite.regression.tropicana;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import java.sql.Date;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class TC4613_TC001_SCWS_Validation_Of_Offer_Element_In_The_Get_Subscription_Response extends BaseTest {
    private String customerNumber;
    private Date newStartDate;
    String subscription1;
    String subscription2;
    String subscription3;
    String subscription4;

    @Test(enabled = true, description = "TC4613_TC001_SCWS_Validation_Of_Offer_Element_In_The_Get_Subscription_Response", groups = "Tropicana")
    public void TC4613_SCWS_Validation_Of_Offer_Element_In_The_Get_Subscription_Response(){
        test.get().info("Step 1 : Create a account having 2 subscriptions offered Bonus bundle and 2 subscriptions not offered Bonus bundle");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\tropicana\\TC4613_TC001_request.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage subscriptionsGridSectionPage = CommonContentPage.SubscriptionsGridSectionPage.getInstance();
        subscription1 = subscriptionsGridSectionPage.getSubscriptionNumberValue("Mobile Ref 1");
        subscription2 = subscriptionsGridSectionPage.getSubscriptionNumberValue("Mobile Ref 2");
        subscription3 = subscriptionsGridSectionPage.getSubscriptionNumberValue("Mobile Ref 3");
        subscription4 = subscriptionsGridSectionPage.getSubscriptionNumberValue("Mobile Ref 4");

        test.get().info("Step 6 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription3);

        test.get().info("Step 8 : Add Bonus Bundle to Subscription");
        swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription4);

        test.get().info("Step 10 : Submit a 'Get Subscription Summary' SCWS request including Inactive Subscription Flag = TRUE and accountNumber.");
        Xml xml = swsActions.submitGetSubscriptionSummaryRequestByCusNumber(customerNumber, true);

        test.get().info("Step 11 : Validate value of Group attribute for Bonus Bundle. ");
        verifyGetSubscriptionSummaryRequestAreCorrect(xml);

        test.get().info("Step 12 : Submit a 'Get Subscription Summary' SCWS request including Inactive Subscription Flag = TRUE and subscriptionNumber.");
        xml = swsActions.submitGetSubscriptionSummaryRequestBySubNumber(subscription3,true);

        test.get().info("Step 13 : Validate value of Group attribute for Bonus Bundle. ");
        verifyGetSubscriptionSummaryRequestAreCorrect(xml, subscription3);
    }

    private void verifyGetSubscriptionSummaryRequestAreCorrect(Xml xml){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH_mm_ss");
        String localTime = "_" + LocalTime.now().format(formatter);
        String actualFile = Common.saveXmlFile(customerNumber + localTime +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xml.toString())));
        String file =  Common.readFile("src\\test\\resources\\xml\\tropicana\\TC4613_TC001_01_response.xml")
                .replace("$accountNumber$", customerNumber)
                .replace("$subscriptionNumber1$", subscription1)
                .replace("$subscriptionNumber2$", subscription2)
                .replace("$subscriptionNumber3$", subscription3)
                .replace("$subscriptionNumber4$", subscription4)
                .replace("$startDate$", Parser.parseDateFormate(newStartDate,"yyyy-MM-dd"))
                .replace("$startDateBonus$", Parser.parseDateFormate(TimeStamp.Today(),"yyyy-MM-dd"))
                .replace("$nextBillDate$", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"yyyy-MM-dd"));

        String expectedResponseFile = Common.saveXmlFile(customerNumber + localTime +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(1, size);
    }

    private void verifyGetSubscriptionSummaryRequestAreCorrect(Xml xml, String subscription){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH_mm_ss");
        String localTime = "_" + LocalTime.now().format(formatter);
        String actualFile = Common.saveXmlFile(customerNumber + localTime +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xml.toString())));
        String file =  Common.readFile("src\\test\\resources\\xml\\tropicana\\TC4613_TC001_02_response.xml")
                .replace("$accountNumber$", customerNumber)
                .replace("$subscriptionNumber$", subscription)
                .replace("$startDate$", Parser.parseDateFormate(newStartDate,"yyyy-MM-dd"))
                .replace("$startDateBonus$", Parser.parseDateFormate(TimeStamp.Today(),"yyyy-MM-dd"))
                .replace("$nextBillDate$", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"yyyy-MM-dd"));

        String expectedResponseFile = Common.saveXmlFile(customerNumber + localTime +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(1, size);
    }
}
