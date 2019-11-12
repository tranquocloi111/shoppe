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

public class TC4998_Ows_CSA_Existing_Customer_New_Device_Only_Order_Hub_Ps_Is_Ocs_And_Ows_Psf_Ocs_And_Tariff_Ps_Is_Both extends BaseTest {
    private String customerNumber = "47754448";
    private String orderId = "8701440";
    private String firstName = "first559850376";
    private String lastName = "last356674896";
    private String subNo1 = "07954476890";
    private String subNo2 = "07730240810";
    private String subNo3 = "07943700180";
    private String userName = "un475221267@hsntech.com";
    private String passWord = "password1";
    private String discountGroupCode = "67864469";

    @Test(enabled = true, description = "TC4998_Ows_CSA_Existing_Customer_New_Device_Only_Order_Hub_Ps_Is_Ocs_And_Ows_Psf_Ocs_And_Tariff_Ps_Is_Both", groups = "OCS")
    public void TC4998_Ows_CSA_Existing_Customer_New_Device_Only_Order_Hub_Ps_Is_Ocs_And_Ows_Psf_Ocs_And_Tariff_Ps_Is_Both() {
        test.get().info("Step 1 : OWS: Care(CSA): Existing Customer, New device only Order - System PS=OCS, OWS PSF=OCS& Tariff PS=BOTH");
        CommonActions.updateHubProvisionSystem("O");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC4998_Care_CSA_Ocs_Request.xml";
        owsActions.createOcsCustomerRequest(path, true, "OCS");
        customerNumber = owsActions.customerNo;
        path = "src\\test\\resources\\xml\\ocs\\TC4998_Care_CSA_Existing_Ocs_Both_Request_Type.xml";
        owsActions.createOcsCustomerRequest(path, true, "OCS", "", customerNumber);

        test.get().info("Step 2 : Verify Create Ocs Account async task is not displayed");
        orderId = owsActions.orderIdNo;
        firstName = owsActions.firstName;
        lastName = owsActions.lastName;
        checkCreateOcsAccountCommand();

        test.get().info("Step 3 : Login to Care screen");
        //updateReadWriteAccessBusinessCustomers();
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");
        subNo2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 2");
        subNo3 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 3");

        test.get().info("Step 4 : Validate the Subscription details screen in HUB .NET");
        verifyOcsKeyOfSubscription();

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
        orderAndContract.add("Telesales");
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
        String file =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4998_Get_Order_Response.xml")
                .replace("$orderId$", orderId)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$subNo3$", subNo3)
                .replace("$dateTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyy-MM-dd"));

        String expectedResponseFile = Common.saveXmlFile(customerNumber + localTime +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(43, size);
    }


    private void verifyContractInformation(){
        OrderConfirmationPage orderConfirmationPage = OrderConfirmationPage.getInstance();
        Assert.assertEquals(orderConfirmationPage.getOrderIdConfirmation(), String.format("Order #%s\n(15 Order Complete)", orderId));

        String fileName = orderConfirmationPage.saveContractPdfFile(customerNumber);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Pdf.getInstance().getText(localFile, 1, 1);
        Assert.assertEquals(pdfList.get(2), String.format("%s %s £12.50", Parser.parseDateFormate(TimeStamp.Today(), "dd MMMM yyyy"), orderId));
        Assert.assertEquals(pdfList.get(4), String.format("Name: Mr %s %s", firstName, lastName));
        Assert.assertEquals(pdfList.get(5), "Address: 6 LUKIN STREET, LONDON E1 0AA");
        Assert.assertEquals(pdfList.get(7), String.format("Number: %s", subNo3));
        Assert.assertEquals(pdfList.get(8), "Friendly name chosen: Mobile 3");
        Assert.assertEquals(pdfList.get(10), "Tariff: £7.50 Usage Contract (24 Months) 250 minutes and 5,000 texts");
        Assert.assertEquals(pdfList.get(15), "First month's payment: £12.50");
        Assert.assertEquals(pdfList.get(16), "Total upfront cost: £12.50");
        Assert.assertEquals(pdfList.get(18), "Monthly charges: £12.50");

    }

    private void verifyServiceOrdersAreCreatedCorrectly(){
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<String>(Arrays.asList("Completed Task", "Sales Order", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));
        lists.add(new ArrayList<String>(Arrays.asList("Completed Task", "Order Task", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));

        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        Assert.assertEquals(Common.compareLists(serviceOrders.getAllValueOfServiceOrder(), lists), 4);

        serviceOrders.clickServiceOrderByType("Order Task");
        TasksContentPage.TaskPage.EventsGridSectionPage eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals(eventsGridSectionPage.getRowNumberOfEventGird(),6);

        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrders.clickServiceOrderByType("Sales Order");
        eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals(eventsGridSectionPage.getRowNumberOfEventGird(),16);

        List<List<String>> salesOrderLists = new ArrayList<>();
        salesOrderLists.add(new ArrayList<String>(Arrays.asList("Terms And Conditions Accepted", "Completed Task")));
        salesOrderLists.add(new ArrayList<String>(Arrays.asList("MPNs Assigned", "Completed Task")));
        salesOrderLists.add(new ArrayList<String>(Arrays.asList("New sales order sent to billing", "Completed Task")));
        salesOrderLists.add(new ArrayList<String>(Arrays.asList("Order Complete", "Completed Task")));
        Assert.assertEquals(Common.compareLists(eventsGridSectionPage.getAllValueOfEvents(), salesOrderLists), 4);

    }

    private void verifyTrustServerLog(){
        String localTime = Common.getCurrentLocalTime();
        String ftpFile = "/opt/payara/payara5/glassfish/domains/trust-R2-serv/logs/server.log";
        String localFile = Common.getFolderLogFilePath() + customerNumber + localTime + "_TrustServerLog.txt";
        SFTPHelper.getGlassFishInstance().downloadGlassFishFile(localFile, ftpFile);
        Log.info("Server log file:" + localFile);

        //Trust Server
        String trustServerLog = localFile;
        String doCreateSubscriberRequestMsgPath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4998_Create_Subscriber_Request_Msg.xml")
                .replace("$custKey$", customerNumber)
                .replace("$acctKey1$", discountGroupCode)
                .replace("$subIdentity$", subNo3)
                .replace("$effectiveTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyyMMdd"));
        String createSubscriberRequestMsgFile = Common.saveXmlFile(customerNumber + localTime +"_CreateSubscriberRequestMsg.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doCreateSubscriberRequestMsgPath)));
        Assert.assertTrue(Common.compareTextsFile(trustServerLog, createSubscriberRequestMsgFile));

        String doCreateCustomerRequestMsgPath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4997_Create_Customer_Request_Msg.xml")
                .replace("$custKey$", customerNumber)
                .replace("$acctKey1$", discountGroupCode)
                .replace("$subIdentity$", subNo3)
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
                .replace("$subIdentity$", subNo3)
                .replace("$effectiveTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyyMMdd"));
        String publicCreateCustomerRequestMsgFile = Common.saveXmlFile(customerNumber + localTime +"_PublicCreateCustomerRequestMsg.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doPublicCreateCustomerRequestMsgPath)));
        Assert.assertTrue(Common.compareTextsFile(publicServerLog, publicCreateCustomerRequestMsgFile));
    }

    private void checkCreateOcsAccountCommand(){
        boolean isExist = false;
        List asyncCommand =  CommonActions.getAsynccommand(orderId);
        for (int i = 0; i < asyncCommand.size(); i++) {
            if (((HashMap) asyncCommand.get(i)).containsValue("CREATE_OCS_ACCOUNT")) {
                isExist = true;
                break;
            }
        }
        Assert.assertFalse(isExist);
    }

    private void verifyOcsKeyOfSubscription(){
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo1 + " Mobile 1");
        SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage generalSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        discountGroupCode = generalSectionPage.getDiscountGroupCode();
        verifyOcsSubscriptionDetails("OCS", discountGroupCode + "S", discountGroupCode + "A" , TimeStamp.Today());

        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo2 + " Mobile 2");
        generalSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        discountGroupCode = generalSectionPage.getDiscountGroupCode();
        verifyOcsSubscriptionDetails("OCS", discountGroupCode + "S", discountGroupCode+ "A", TimeStamp.Today());

        MenuPage.BreadCrumbPage.getInstance().clickParentLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo3 + " Mobile 3");
        generalSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        discountGroupCode = generalSectionPage.getDiscountGroupCode();
        verifyOcsSubscriptionDetails("OCS", discountGroupCode + "S", discountGroupCode + "A", TimeStamp.Today());
    }

}
