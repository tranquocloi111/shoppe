package suite.regression.ocs;

import framework.utils.Log;
import framework.utils.Pdf;
import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.pages.selfcare.OrderConfirmationPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TC4987_Ows_Onlines_New_Device_Pheripherial_Order_Hub_Ps_Is_Both_Ows_Psf_Ocs_Friendly_Mpn_Yes_Tariff_Ps_Ocs extends BaseTest {
    private String customerNumber = "47752412";
    private String orderId = "8701337";
    private String firstName = "first122508435";
    private String lastName = "last221998091";
    private String subNo1 = "00734491910";
    private String userName = "un197856092@hsntech.com";
    private String passWord = "password1";
    private String discountGroupCode;

    //4988
    @Test(enabled = true, description = "TC4987_Ows_Onlines_New_Device_Pheripherial_Order_Hub_Ps_Is_Both_Ows_Psf_Ocs_Friendly_Mpn_Yes_Tariff_Ps_Ocs", groups = "OCS")
    public void TC4987_Ows_Onlines_New_Device_Pheripherial_Order_Hub_Ps_Is_Both_Ows_Psf_Ocs_Friendly_Mpn_Yes_Tariff_Ps_Ocs() {
        test.get().info("Step 1 : Onlines: New Customer, New Device & pheripherial Order - System PS=BOTH or MIG, OWS PSF=OCS & Tariff PS=OCS or BOTH, Friendly MPN=Yes");
        CommonActions.updateHubProvisionSystem("B");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC4987_Device_Peripheral_Request_Ocs_Type.xml";
        subNo1 = CareTestBase.page().newSubscriptionNumber();
        CommonActions.addSubscriptionToWhiteList(subNo1);
        owsActions.createOcsCustomerRequestAcceptUrl(path, 2, "OCS", subNo1);

        test.get().info("Step 2 : Verify Create Ocs Account async task is displayed");
        customerNumber = owsActions.customerNo;
        orderId = owsActions.orderIdNo;
        firstName = owsActions.firstName;
        lastName = owsActions.lastName;
        CareTestBase.page().checkCreateOcsAccountCommand(orderId, true);

        test.get().info("Step 3 : Login to Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 4 : Validate the Subscription details screen in HUB .NET");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage generalSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        discountGroupCode = generalSectionPage.getDiscountGroupCode();
        verifyOcsSubscriptionDetails("OCS", discountGroupCode + "S", discountGroupCode + "A", TimeStamp.Today());

        test.get().info("Step 5 : Validate Sales Order and Order Task Service Orders in HUB .NET");
        verifyServiceOrdersAreCreatedCorrectly();

        test.get().info("Step 6 : Validate the Order detail using Get Order OWS request");
        Xml xml = owsActions.getOrder(orderId);
        verifyGetOrderRequestAreCorrect(xml);

        test.get().info("Step 7 : Login to SelfCare ");
        userName = owsActions.username;
        passWord = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(userName, passWord, customerNumber);

        test.get().info("Step 8 : Validate the order confirmations screen in Self Care");
        MyPersonalInformationPage.MyPreviousOrdersPage myPreviousOrdersPage = MyPersonalInformationPage.MyPreviousOrdersPage.getInstance();
        List<String> orderAndContract = new ArrayList<>();
        orderAndContract.add("#"+orderId);
        orderAndContract.add("Online");
        Assert.assertEquals(Common.compareList(myPreviousOrdersPage.getAllValueOfOrdersAndContractPage(), orderAndContract), 1);

        test.get().info("Step 9 : Validate the Contract PDF in Self Care");
        myPreviousOrdersPage.clickViewByIndex(1);
        verifyContractInformation();

        test.get().info("Step 10 : Validate the server log in public/trust server");
        verifyTrustServerLog();
    }


    private void verifyGetOrderRequestAreCorrect(Xml xml){
        String localTime = Common.getCurrentLocalTime();
        String actualFile = Common.saveXmlFile(customerNumber + localTime +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xml.toString())));
        String file =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4987_Get_Order_Response.xml")
                .replace("$orderId$", orderId)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$subNo1$", subNo1)
                .replace("$dateTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyy-MM-dd"));

        String expectedResponseFile = Common.saveXmlFile(customerNumber + localTime +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(55, size);
    }


    private void verifyContractInformation(){
        OrderConfirmationPage orderConfirmationPage = OrderConfirmationPage.getInstance();
        Assert.assertEquals(orderConfirmationPage.getOrderIdConfirmation(), String.format("Order #%s\n(15 Order Complete)", orderId));

        String fileName = orderConfirmationPage.saveContractPdfFile(customerNumber);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Pdf.getInstance().getText(localFile, 1,1);
        Assert.assertEquals(pdfList.get(2), String.format("%s %s £15.00", Parser.parseDateFormate(TimeStamp.Today(), "dd MMMM yyyy"), orderId));
        Assert.assertEquals(pdfList.get(4), String.format("Name: Mr %s %s", firstName, lastName));
        Assert.assertEquals(pdfList.get(7), String.format("Number: %s", subNo1));
        Assert.assertEquals(pdfList.get(8), "Friendly name chosen: Mobile 1");
        Assert.assertEquals(pdfList.get(10), "Tariff: £10 36Mth Smartphone Tariff 100 Mins 5000 Texts");
        Assert.assertEquals(pdfList.get(15), "First month's payment: £15.00");
        Assert.assertEquals(pdfList.get(16), "Total upfront cost: £15.00");
        Assert.assertEquals(pdfList.get(18), "Monthly charges: £15.00");
    }

    private void verifyServiceOrdersAreCreatedCorrectly(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<String>(Arrays.asList("Completed Task", "Sales Order", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));
        lists.add(new ArrayList<String>(Arrays.asList("Completed Task", "Order Task", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));

        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        Assert.assertEquals(Common.compareLists(serviceOrders.getAllValueOfServiceOrder(), lists), 2);

        serviceOrders.clickServiceOrderByType("Order Task");
        TasksContentPage.TaskPage.EventsGridSectionPage eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals(eventsGridSectionPage.getRowNumberOfEventGird(),6);

        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrders.clickServiceOrderByType("Sales Order");
        eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals(eventsGridSectionPage.getRowNumberOfEventGird(),22);
    }

    private void verifyTrustServerLog(){
        String localTime = Common.getCurrentLocalTime();
        String ftpFile = "/opt/payara/payara5/glassfish/domains/trust-R2-serv/logs/server.log";
        String localFile = Common.getFolderLogFilePath() + customerNumber + localTime + "_TrustServerLog.txt";
        SFTPHelper.getGlassFishInstance().downloadGlassFishFile(localFile, ftpFile);
        Log.info("Server log file:" + localFile);

        //Trust Server
        String trustServerLog = localFile;
        String doCreateSubscriberRequestMsgPath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4997_Create_Subscriber_Request_Msg.xml")
                .replace("$custKey$", customerNumber)
                .replace("$acctKey1$", discountGroupCode)
                .replace("$subIdentity$", subNo1)
                .replace("$effectiveTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyyMMdd"));
        String createSubscriberRequestMsgFile = Common.saveXmlFile(customerNumber + localTime +"_CreateSubscriberRequestMsg.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doCreateSubscriberRequestMsgPath)));
        Assert.assertTrue(Common.compareTextsFile(trustServerLog, createSubscriberRequestMsgFile));

        String doCreateCustomerRequestMsgPath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4997_Create_Customer_Request_Msg.xml")
                .replace("$custKey$", customerNumber)
                .replace("$acctKey1$", discountGroupCode)
                .replace("$subIdentity$", subNo1)
                .replace("$effectiveTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyyMMdd"));
        String createCustomerRequestMsgFile = Common.saveXmlFile(customerNumber + localTime +"_CreateCustomerRequestMsg.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doCreateCustomerRequestMsgPath)));
        Assert.assertTrue(Common.compareTextsFile(trustServerLog, createCustomerRequestMsgFile));

        //Public Server
        ftpFile = "/opt/payara/payara5/glassfish/domains/public-R2-serv/logs/server.log";
        localFile = Common.getFolderLogFilePath() + customerNumber + localTime + "_PublicServerLog.txt";
        SFTPHelper.getGlassFishInstance().downloadGlassFishFile(localFile, ftpFile);
        Log.info("Server log file:" + localFile);

        String publicServerLog = localFile;
        Assert.assertTrue(Common.compareTextsFile(publicServerLog, createSubscriberRequestMsgFile));

        String doPublicCreateCustomerRequestMsgPath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4997_Public_Create_Customer_Request_Msg.xml")
                .replace("$custKey$", customerNumber)
                .replace("$acctKey1$", discountGroupCode)
                .replace("$subIdentity$", subNo1)
                .replace("$effectiveTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyyMMdd"));
        String publicCreateCustomerRequestMsgFile = Common.saveXmlFile(customerNumber + localTime +"_PublicCreateCustomerRequestMsg.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doPublicCreateCustomerRequestMsgPath)));
        Assert.assertTrue(Common.compareTextsFile(publicServerLog, publicCreateCustomerRequestMsgFile));
    }
}
