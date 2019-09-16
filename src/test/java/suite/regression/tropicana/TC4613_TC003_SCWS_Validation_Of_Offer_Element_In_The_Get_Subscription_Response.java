package suite.regression.tropicana;

import framework.report.elasticsearch.ExecutionListener;
import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Listeners(ExecutionListener.class)
public class TC4613_TC003_SCWS_Validation_Of_Offer_Element_In_The_Get_Subscription_Response extends BaseTest {
    private String customerNumber;
    private Date newStartDate;
    String subscription1;
    String subscription2;
    String serviceOrderId;

    @Test(enabled = true, description = "TC4613_TC003_SCWS_Validation_Of_Offer_Element_In_The_Get_Subscription_Response", groups = "Tropicana")
    public void TC4613_TC003_SCWS_Validation_Of_Offer_Element_In_The_Get_Subscription_Response(){
        test.get().info("Step 1 : Create a account having 2 subscriptions offered Bonus bundle and Bonus bundles are ended on subscriptions");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\tropicana\\TC4613_TC003_request.xml";
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
        subscription1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        subscription2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        test.get().info("Step 6 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        Xml xml = swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription1);
        Assert.assertTrue(xml.getTextByTagName("description").contains("Maintain Bundle request successful"));

        xml = swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);
        Assert.assertTrue(xml.getTextByTagName("description").contains("Maintain Bundle request successful"));

        test.get().info("Step 7 : Update Customer Start Date");
        newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 8 : The Bonus bundle has end-dated equal to the current day.");
        selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4613_TC002_maintain_remove_request.xml";
        xml = swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);
        Assert.assertTrue(xml.getTextByTagName("description").contains("Maintain Bundle request successful"));

        test.get().info("Step 9 : Submit Provision Wait");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<WebElement> serviceOrder = ServiceOrdersContentPage.getInstance().getServiceOrders(ServiceOrderEntity.dataServiceOrderBySubAndType(subscription2, "Change Bundle", "Provision Wait"));
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByElementServiceOrders(serviceOrder);
        BaseTest.updateThePDateAndBillDateForSO(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 10 : Add Another Bonus Bundle to Subscription");
        selfCarePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        xml = swsActions.submitMaintainBundleRequest(selfCarePath, customerNumber, subscription2);
        Assert.assertTrue(xml.getTextByTagName("description").contains("Maintain Bundle request successful"));

        test.get().info("Step 11 : Submit a 'Get Subscription Summary' SCWS request including Inactive Subscription Flag = TRUE, accountNumber and subscriptionNumber (having active Bonus bundle).");
        selfCarePath = "src\\test\\resources\\xml\\sws\\getsubscription\\Get_Invalid_Subscription_Summary_Request.xml";
        xml = swsActions.submitGetByCustomerAndSubscriptionNumbersRequest(selfCarePath, customerNumber, subscription2);

        test.get().info("Step 12 : Verify Response returned");
        Assert.assertEquals("SC_006", xml.getTextByTagName("faultcode"));
        Assert.assertEquals("Request is not valid with both Account Number and Subscription Number", xml.getTextByTagName("faultstring"));

        test.get().info("Step 13 : Submit a 'Get Subscription Summary' SCWS request including Inactive Subscription Flag = FALSE and accountNumber.");
        xml = swsActions.submitGetSubscriptionSummaryRequestByCusNumber(customerNumber, false);

        test.get().info("Step 14 : Validate value of Group attribute for Bonus Bundle.");
        verifyGetSubscriptionSummaryRequestAreCorrect(xml);

    }

    private void verifyGetSubscriptionSummaryRequestAreCorrect(Xml xml){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH_mm_ss");
        String localTime = "_" + LocalTime.now().format(formatter);
        String actualFile = Common.saveXmlFile(customerNumber + localTime +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xml.toString())));
        String file =  Common.readFile("src\\test\\resources\\xml\\tropicana\\TC4613_TC003_response.xml")
                .replace("$accountNumber$", customerNumber)
                .replace("$subscriptionNumber1$", subscription1)
                .replace("$subscriptionNumber2$", subscription2)
                .replace("$startDate$", Parser.parseDateFormate(newStartDate,"yyyy-MM-dd"))
                .replace("$startDateBonus$", Parser.parseDateFormate(TimeStamp.Today(),"yyyy-MM-dd"))
                .replace("$nextBillDate$", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"yyyy-MM-dd"));

        String expectedResponseFile = Common.saveXmlFile(customerNumber + localTime +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(1, size);
    }
}
