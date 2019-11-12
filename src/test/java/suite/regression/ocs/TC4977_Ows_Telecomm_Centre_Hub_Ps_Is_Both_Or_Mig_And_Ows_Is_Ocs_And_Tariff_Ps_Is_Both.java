package suite.regression.ocs;

import framework.utils.Log;
import framework.utils.Pdf;
import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.helper.FTPHelper;
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

public class TC4977_Ows_Telecomm_Centre_Hub_Ps_Is_Both_Or_Mig_And_Ows_Is_Ocs_And_Tariff_Ps_Is_Both extends BaseTest {
    private String customerNumber = "47747488";
    private String orderId = "8701042";
    private String firstName = "first476917330";
    private String lastName = "last668092678";
    private String subNo1 = "07406981070";
    private String userName = "un340042601@hsntech.com";
    private String passWord = "password1";
    private String discountGroupCode;
    private OWSActions owsActions;

    @Test(enabled = true, description = "TC4977_Ows_Telecomm_Centre_Hub_Ps_Is_Both_Or_Mig_And_Ows_Is_Ocs_And_Tariff_Ps_Is_Both", groups = "OCS")
    public void TC4977_Ows_Telecomm_Centre_Hub_Ps_Is_Both_Or_Mig_And_Ows_Is_Ocs_And_Tariff_Ps_Is_Both() {
        test.get().info("Step 1 : New Customer, New Device & pheripherial Order - System PS = BOTH or MIG, OWS PSF = OCS & Tariff PS = OCS or BOTH");
        CommonActions.updateHubProvisionSystem("B");
        owsActions = new OWSActions();
        createACCCustomerWith1SubscriptionsOrder();

        test.get().info("Step 2 : Verify Create Ocs Account async task is displayed");
        customerNumber = owsActions.customerNo;
        orderId = owsActions.orderIdNo;
        firstName = owsActions.firstName;
        lastName = owsActions.lastName;
        CareTestBase.page().checkCreateOcsAccountCommand(orderId, true);

        test.get().info("Step 3 : Login to Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");

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
        orderAndContract.add("Phoneshop");
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
        String file =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4977_Get_Order_Response.xml")
                .replace("$orderId$", orderId)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$subNo1$", subNo1)
                .replace("$dateTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyy-MM-dd"));

        String expectedResponseFile = Common.saveXmlFile(customerNumber + localTime +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(48, size);
    }


    private void verifyContractInformation(){
        OrderConfirmationPage orderConfirmationPage = OrderConfirmationPage.getInstance();
        Assert.assertEquals(orderConfirmationPage.getOrderIdConfirmation(), String.format("Order #%s\n(15 Order Complete)", orderId));

        String fileName = orderConfirmationPage.saveContractPdfFile(customerNumber);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Pdf.getInstance().getText(localFile, 1,1);
        Assert.assertEquals(pdfList.get(2), String.format("%s %s £7.50", Parser.parseDateFormate(TimeStamp.Today(), "dd MMMM yyyy"), orderId));
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
        Assert.assertEquals(eventsGridSectionPage.getRowNumberOfEventGird(),17);
    }

    private void verifyTrustServerLog(){
        String localTime = Common.getCurrentLocalTime();
//        String ftpFile = "/opt/payara/payara5/glassfish/domains/trusted-R2-silo/logs/";
//        String localFile = Common.getFolderLogFilePath() + "sds.txt";
//        List<String> allFileName= FTPHelper.getInstance().getAllFileName(ftpFile);
//        FTPHelper.getInstance().downLoadFromDisk(ftpFile, allFileName.get(2), localFile);
//        Log.info("Server log file:" + localFile);

        String serverLog = Common.getFolderLogFilePath() + "TrustServerLog.txt";
        String doReservePath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4377_Do_Reserve.xml")
                .replace("$orderNumber$", orderId);
        String doReserveFile = Common.saveXmlFile(customerNumber + localTime +"_doReserve.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doReservePath)));
       Assert.assertFalse(Common.compareTextsFile(serverLog, doReserveFile));

        String doReserveResponsePath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4377_Do_Reserve_Response.xml")
                .replace("$orderNumber$", orderId)
                .replace("$mobilePhoneNumber$", subNo1);
        String doReserveResponseFile = Common.saveXmlFile(customerNumber + localTime +"_doReserveResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doReserveResponsePath)));
        Assert.assertFalse(Common.compareTextsFile(serverLog, doReserveResponseFile));

        String createOrderResponsePath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4377_Create_Order_Response.xml")
                .replace("$accountNumber$", customerNumber)
                .replace("$orderId$", orderId);
        String createOrderResponseFile = Common.saveXmlFile(customerNumber + localTime +"_createOrderResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(createOrderResponsePath)));
        Assert.assertFalse(Common.compareTextsFile(serverLog, createOrderResponseFile));

        String doConfirmPath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4377_Do_Confirm.xml")
                .replace("$accountNumber$", customerNumber)
                .replace("$orderNumber$", orderId)
                .replace("$mobilePhoneNumber$", subNo1)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$secondReference$", userName);
        String doConfirmFile = Common.saveXmlFile(customerNumber + localTime +"_doConfirm.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doConfirmPath)));
        Assert.assertFalse(Common.compareTextsFile(serverLog, doConfirmFile));

        String doConfirmResponsePath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4377_Do_Confirm_Response.xml")
                .replace("$orderNumber$", orderId);
        String doConfirmResponseFile = Common.saveXmlFile(customerNumber + localTime +"_doConfirmReponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doConfirmResponsePath)));
        Assert.assertFalse(Common.compareTextsFile(serverLog, doConfirmResponseFile));
    }

    private void createACCCustomerWith1SubscriptionsOrder(){
        subNo1 = CareTestBase.page().newSubscriptionNumber();
        String request = Common.readFile("src\\test\\resources\\xml\\ocs\\TC4977_Telecom_Centre_Ocs_Request_Both.xml")
                         .replace("$MPN$", subNo1);
        owsActions.createOcsCustomerRequestWithStringRequest(request, true, "OCS");
    }
}
