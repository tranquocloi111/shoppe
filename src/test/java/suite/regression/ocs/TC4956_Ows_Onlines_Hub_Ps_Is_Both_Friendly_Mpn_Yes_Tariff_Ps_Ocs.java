package suite.regression.ocs;

import framework.utils.Pdf;
import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
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

public class TC4956_Ows_Onlines_Hub_Ps_Is_Both_Friendly_Mpn_Yes_Tariff_Ps_Ocs extends BaseTest {
    private String customerNumber = "47747524";
    private String orderId = "8701049";
    private String firstName = "first846704518";
    private String lastName = "last217982496";
    private String subNo1 = "00671302980";
    private String userName = "un581590741@hsntech.com";
    private String passWord = "password1";
    private String discountGroupCode;

    @Test(enabled = true, description = "TC4956_Ows_Onlines_Hub_Ps_Is_Both_Friendly_Mpn_Yes_Tariff_Ps_Ocs", groups = "OCS")
    public void TC4956_Ows_Onlines_Hub_Ps_Is_Both_Friendly_Mpn_Yes_Tariff_Ps_Ocs() {
        test.get().info("Step 1 : Onlines: New Customer, New Device Only Order - System PS=BOTH or MIG, OWS PSF=null, Friendly MPN = Yes & Tariff PS=OCS");
        CommonActions.updateHubProvisionSystem("B");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC4996_Request_Ocs_Typee.xml";
        subNo1 = CareTestBase.page().newSubscriptionNumber();
        CommonActions.addSubscriptionToWhiteList(subNo1);
        owsActions.createOcsCustomerRequest(path, true, "", subNo1);

        test.get().info("Step 2 : Verify Create Ocs Account async task is displayed");
        customerNumber = owsActions.customerNo;
        orderId = owsActions.orderIdNo;
        firstName = owsActions.firstName;
        lastName = owsActions.lastName;
        checkCreateOcsAccountCommand();

        test.get().info("Step 3 : Login to Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 4 : Validate the Subscription details screen in HUB .NET");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage generalSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        discountGroupCode = generalSectionPage.getDiscountGroupCode();
        verifyOcsSubscriptionDetails("OCS", discountGroupCode + "S", discountGroupCode + "A");

        test.get().info("Step 5 : Validate Sales Order and Order Task Service Orders in HUB .NET");
        verifyServiceOrdersAreCreatedCorrectly();

        test.get().info("Step 6 : Validate the Order detail using Get Order OWS request");
        Xml xml = owsActions.getOrder(orderId);
        verifyGetOrderRequestAreCorrect(xml);

        test.get().info("Step 7 : Login to SelfCare ");
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
        String file =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4956_Get_Order_Response.xml")
                .replace("$orderId$", orderId)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$subNo1$", subNo1)
                .replace("$dateTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyy-MM-dd"));

        String expectedResponseFile = Common.saveXmlFile(customerNumber + localTime +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(51, size);
    }


    private void verifyContractInformation(){
        OrderConfirmationPage orderConfirmationPage = OrderConfirmationPage.getInstance();
        Assert.assertEquals(orderConfirmationPage.getOrderIdConfirmation(), String.format("Order #%s\n(15 Order Complete)", orderId));

        String fileName = orderConfirmationPage.saveContractPdfFile(customerNumber);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Pdf.getInstance().getText(localFile, 1,1);
        Assert.assertEquals(pdfList.get(2), String.format("%s %s £15.00", Parser.parseDateFormate(TimeStamp.Today(), "dd MMMM yyyy"), orderId));
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
        Assert.assertEquals(eventsGridSectionPage.getRowNumberOfEventGird(),20);
    }

    private void verifyTrustServerLog(){
        String localTime = Common.getCurrentLocalTime();
//        String ftpFile = "/opt/payara/payara5/glassfish/domains/trusted-R2-silo/logs/";
//        String localFile = Common.getFolderLogFilePath() + "sds.txt";
//        List<String> allFileName= FTPHelper.getInstance().getAllFileName(ftpFile);
//        FTPHelper.getInstance().downLoadFromDisk(ftpFile, allFileName.get(2), localFile);
//        Log.info("Server log file:" + localFile);

        String serverLog = Common.getFolderLogFilePath() + "TrustServerLog1.txt";
        String doReservePath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4377_Do_Reserve.xml")
                .replace("$orderNumber$", orderId);
        String doReserveFile = Common.saveXmlFile(customerNumber + localTime +"_doReserve.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doReservePath)));
       Assert.assertTrue(Common.compareTextsFile(serverLog, doReserveFile));

        String doReserveResponsePath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4377_Do_Reserve_Response.xml")
                .replace("$orderNumber$", orderId)
                .replace("$mobilePhoneNumber$", subNo1);
        String doReserveResponseFile = Common.saveXmlFile(customerNumber + localTime +"_doReserveResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doReserveResponsePath)));
        Assert.assertTrue(Common.compareTextsFile(serverLog, doReserveResponseFile));

        String createOrderResponsePath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4377_Create_Order_Response.xml")
                .replace("$accountNumber$", customerNumber)
                .replace("$orderId$", orderId);
        String createOrderResponseFile = Common.saveXmlFile(customerNumber + localTime +"_createOrderResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(createOrderResponsePath)));
        Assert.assertTrue(Common.compareTextsFile(serverLog, createOrderResponseFile));

        String doConfirmPath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4377_Do_Confirm.xml")
                .replace("$accountNumber$", customerNumber)
                .replace("$orderNumber$", orderId)
                .replace("$mobilePhoneNumber$", subNo1)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$secondReference$", userName);
        String doConfirmFile = Common.saveXmlFile(customerNumber + localTime +"_doConfirm.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doConfirmPath)));
        Assert.assertTrue(Common.compareTextsFile(serverLog, doConfirmFile));

        String doConfirmResponsePath =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4377_Do_Confirm_Response.xml")
                .replace("$orderNumber$", orderId);
        String doConfirmResponseFile = Common.saveXmlFile(customerNumber + localTime +"_doConfirmReponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(doConfirmResponsePath)));
        Assert.assertTrue(Common.compareTextsFile(serverLog, doConfirmResponseFile));
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
        Assert.assertTrue(isExist);
    }

}
