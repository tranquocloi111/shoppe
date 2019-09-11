package suite.shakeout;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

public class TC32193_Self_Care_WS_Subscription_is_Active_Family_perk_Bundle extends BaseTest {

    @Test(enabled = false, description = "TC32193 Self Care WS Subscription is Active Family perk Bundle", groups = "Smoke")
    public void TC32193_Self_Care_WS_Subscription_is_Active_Family_perk_Bundle(){
        test.get().info("Step 1 : Create a customer with family perk bundle subscription active");
        String path = "src\\test\\resources\\xml\\sws\\getbundle\\TC3363_createOrder.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getOrder(owsActions.orderIdNo);
        String subscriptionNumber = owsActions.getOrderMpnByReference(1);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 6 : Record discount bundle monthly refill SO id");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        String serviceOrderId =  ServiceOrdersContentPage.getInstance().getServiceOrderIdByOrderServices(ServiceOrderEntity.dataServiceOrderBySubAndType(subscriptionNumber, "Discount Bundle Monthly Refill"));

        test.get().info("Step 7 : Build and Submit get bundle request with subscription number and account number to selfcare WS");
        SWSActions swsActions = new SWSActions();
        Xml xml = swsActions.submitGetBundleRequest(customerNumber, subscriptionNumber);

        test.get().info("Step 8 : Verify get bundle response are correct");
        verifyGetBundleResponseAreCorrect(xml, customerNumber, subscriptionNumber, serviceOrderId, newStartDate);
    }

    private void verifyGetBundleResponseAreCorrect(Xml xml, String customerId, String subscriptionNumber, String serviceOrderId, Date newStartDate){
        String actualFile = Common.saveXmlFile(customerId +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xml.toString())));
        String file =  Common.readFile("src\\test\\resources\\xml\\sws\\getbundle\\TC3363_response.xml")
                .replace("$accountNumber$", customerId)
                .replace("$subscriptionNumber$", subscriptionNumber)
                .replace("$startDate$", Parser.parseDateFormate(newStartDate,"yyyy-MM-dd"))
                .replace("$nextScheduledRefill$", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"yyyy-MM-dd"))
                .replace("$SOId$", serviceOrderId);;
        String expectedResponseFile = Common.saveXmlFile(customerId +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(1, size);
    }
}
