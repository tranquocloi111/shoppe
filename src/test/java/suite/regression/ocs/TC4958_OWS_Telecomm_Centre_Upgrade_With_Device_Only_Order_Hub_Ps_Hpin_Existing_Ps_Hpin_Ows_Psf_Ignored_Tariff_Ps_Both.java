package suite.regression.ocs;

import framework.utils.Pdf;
import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.PaymentGridEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
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

import java.sql.Date;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class TC4958_OWS_Telecomm_Centre_Upgrade_With_Device_Only_Order_Hub_Ps_Hpin_Existing_Ps_Hpin_Ows_Psf_Ignored_Tariff_Ps_Both extends BaseTest {
    private String customerNumber = "47747501";
    private String orderId = "8701044";
    private String firstName = "first290383441";
    private String lastName = "last892849311";
    private String subNo1 = "07406981070";
    private String userName = "un598252383@hsntech.com";
    private String passWord = "password1";
    private String serviceOrderId = "12453";
    private OWSActions owsActions;
    private Date newStartDate;

    @Test(enabled = true, description = "TC4958_OWS_Telecomm_Centre_Upgrade_With_Device_Only_Order_Hub_Ps_Hpin_Existing_Ps_Hpin_Ows_Psf_Ignored_Tariff_Ps_Both", groups = "OCS")
    public void TC3796_001_OWS_Create_New_Order_For_Business_Customer() {
        test.get().info("Step 1 : Create a Customer with HPIN Account");
        CommonActions.updateHubProvisionSystem("H");
        owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC4996_Hpin_Request.xml";
        owsActions.createOcsCustomerRequest(path, true, "");

        test.get().info("Step 2 : Create new billing group");
        createNewBillingGroup();

        test.get().info("Step 3 : Update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update start date for customer");
        newStartDate = TimeStamp.TodayMinus1MonthMinus20Day();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Get subscription number");
        owsActions.getOrder(owsActions.orderIdNo);
        subNo1 = owsActions.getOrderMpnByReference(1);

        test.get().info("Step 7 : Upgrade Order With Business Type");
        path = "src\\test\\resources\\xml\\ocs\\TC4958_upgrade.xml";
        owsActions.upgradeOrderWithoutAcceptUrl(path, customerNumber, subNo1);
        orderId = owsActions.orderIdNo;

        test.get().info("Step 8 : Load Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Upgrade Tariff");

        test.get().info("Step 9 : Submit Provision wait");
        updateThePDateAndBillDateForSO(serviceOrderId);
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 10 : Validate the Subscription details screen in HUB .NET");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Upgrade Mobile");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        verifyOcsSubscriptionDetails("HPIN", "", "", newStartDate);

        test.get().info("Step 11 : Validate Sales Order and Order Task Service Orders in HUB .NET");
        verifyServiceOrdersAreCreatedCorrectly();

        test.get().info("Step 12 : Validate the Order detail using Get Order OWS request");
        Xml xml = owsActions.getOrder(orderId);
        verifyGetOrderRequestAreCorrect(xml);

        test.get().info("Step 13 : Login to SelfCare ");
        userName = owsActions.username;
        passWord = owsActions.password;
        SelfCareTestBase.page().LoginIntoSelfCarePage(userName, passWord, customerNumber);

        test.get().info("Step 14 : Validate the order confirmations screen in Self Care");
        MyPersonalInformationPage.MyPreviousOrdersPage myPreviousOrdersPage = MyPersonalInformationPage.MyPreviousOrdersPage.getInstance();
        List<String> orderAndContract = new ArrayList<>();
        orderAndContract.add("#"+orderId);
        orderAndContract.add("Phoneshop");
        Assert.assertEquals(Common.compareList(myPreviousOrdersPage.getAllValueOfOrdersAndContractPage(), orderAndContract), 1);

        test.get().info("Step 15 : Validate the Contract PDF in Self Care");
        myPreviousOrdersPage.clickViewByIndex(1);
        verifyContractInformation();

        test.get().info("Step 16 : Validate the server log in public/trust server");
        verifyTrustServerLog();

    }

    private void verifyGetOrderRequestAreCorrect(Xml xml){
        String localTime = Common.getCurrentLocalTime();
        String actualFile = Common.saveXmlFile(customerNumber + localTime +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xml.toString())));
        String file =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC4958_Get_Order_Response.xml")
                .replace("$orderId$", orderId)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$subNo1$", subNo1)
                .replace("$dateTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyy-MM-dd"));

        String expectedResponseFile = Common.saveXmlFile(customerNumber + localTime +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(47, size);
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
        Assert.assertEquals(eventsGridSectionPage.getRowNumberOfEventGird(),18);
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
        Assert.assertFalse(isExist);
    }

}
