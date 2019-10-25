package suite.regression.ocs;

import framework.utils.Pdf;
import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.InvoicesContentPage;
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

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class TC5469_OWS_Onlines_Upgrade_Order_Change_Tariff_Validation_From_HPIN_To_OCS extends BaseTest {
    private String customerNumber = "47755393";
    private String orderId = "8701480";
    private String firstName = "first737863077";
    private String lastName = "last679564728";
    private String subNo1 = "07647064770";
    private String inactiveSubNo1 = "07647064770";
    private String userName = "un448969177@hsntech.com";
    private String passWord = "password1";
    private String serviceOrderId = "12453";
    private OWSActions owsActions;
    private Date newStartDate;
    private String discountGroupCode;

    @Test(enabled = true, description = "TC5469_OWS_Onlines_Upgrade_Order_Change_Tariff_Validation_From_HPIN_To_OCS", groups = "OCS")
    public void TC5469_OWS_Onlines_Upgrade_Order_Change_Tariff_Validation_From_HPIN_To_OCS() {
        test.get().info("Step 1 : Create a Customer with HPIN Account");
        CommonActions.updateHubProvisionSystem("H");
        owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC4996_Hpin_Request.xml";
//        owsActions.createOcsCustomerRequest(path, true, "");
//
//        test.get().info("Step 2 : Create new billing group");
//        createNewBillingGroup();
//
//        test.get().info("Step 3 : Update bill group payment collection date to 10 day later ");
//        updateBillGroupPaymentCollectionDateTo10DaysLater();
//
//        test.get().info("Step 4 : Set bill group for customer");
//        customerNumber = owsActions.customerNo;
//        setBillGroupForCustomer(customerNumber);
//
//        test.get().info("Step 5 : Update start date for customer");
//        newStartDate = TimeStamp.TodayMinus1MonthMinus20Day();
//        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
//
//        test.get().info("Step 6 : Get subscription number");
//        orderId = owsActions.orderIdNo;
//        owsActions.getOrder(orderId);
//        subNo1 = owsActions.getOrderMpnByReference(1);

//        test.get().info("Step 7 : Upgrade Order With Business Type");
//        CommonActions.updateHubProvisionSystem("B");
//        path = "src\\test\\resources\\xml\\ocs\\TC5469_upgrade_request_ocs_type.xml";
//        owsActions.upgradeOrderWithoutAcceptUrl(path, customerNumber, subNo1);
//        orderId = owsActions.orderIdNo;

        test.get().info("Step 8 : Load Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Upgrade Mobile");
        inactiveSubNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile 1");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Upgrade Tariff");

//        test.get().info("Step 9 : Submit Provision wait");
//        updateThePDateAndBillDateForSO(serviceOrderId);
//        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 10 : Validate the Subscription details screen in HUB .NET");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        verifyOcsKeyOfSubscription();

        test.get().info("Step 11 : Validate Sales Order and Order Task Service Orders in HUB .NET");
        verifyServiceOrdersAreCreatedCorrectly();

        test.get().info("Step 12 : Validate the Order detail using Get Order OWS request");
        Xml xml = owsActions.getOrder(orderId);
        verifyGetOrderRequestAreCorrect(xml);

//        test.get().info("Step 13 : Run Inclusive Spend Refill for Billing Capped and Network Capped.Run Discount Bundle Renewal job.");
//        submitDoRefillBCJob();
//        submitDoRefillNCJob();
//        submitDoBundleRenewJob();
//
//        test.get().info("Step 14 : Submit Bill Run Job");
//        submitDraftBillRun();

        test.get().info("Step 15 : Verify One Invoice Generated With Issue Date Of Today");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        verifyOneInvoiceGeneratedWithIssueDateOfToday();
        verifyInvoiceDetail();


        test.get().info("Step 16 : Validate the server log in public/trust server");
        verifyTrustServerLog();

    }

    private void verifyGetOrderRequestAreCorrect(Xml xml){
        String localTime = Common.getCurrentLocalTime();
        String actualFile = Common.saveXmlFile(customerNumber + localTime +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xml.toString())));
        String file =  Common.readFile("src\\test\\resources\\xml\\ocs\\TC5469_Get_Order_Response.xml")
                .replace("$orderId$", orderId)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$subNo1$", subNo1)
                .replace("$dateTime$", Parser.parseDateFormate(TimeStamp.Today(),"yyyy-MM-dd"));

        String expectedResponseFile = Common.saveXmlFile(customerNumber + localTime +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(39, size);
    }


    private void verifyContractInformation(){
        OrderConfirmationPage orderConfirmationPage = OrderConfirmationPage.getInstance();
        Assert.assertEquals(orderConfirmationPage.getOrderIdConfirmation(), String.format("Order #%s\n(15 Order Complete)", orderId));

        String fileName = orderConfirmationPage.saveContractPdfFile(customerNumber);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Pdf.getInstance().getText(localFile, 1,1);
        Assert.assertEquals(pdfList.get(2), String.format("%s %s £0.00", Parser.parseDateFormate(TimeStamp.Today(), "dd MMMM yyyy"), orderId));
    }

    private void verifyServiceOrdersAreCreatedCorrectly(){
        //Service Order Grid
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        List<List<String>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList("Completed Task", "Order Task", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT))));
        ServiceOrdersContentPage serviceOrders = ServiceOrdersContentPage.getInstance();
        Assert.assertEquals(Common.compareLists(serviceOrders.getAllValueOfServiceOrder(), lists), 1);

        //Sales Order
        serviceOrders.clickServiceOrderByType("Sales Order");
        TasksContentPage.TaskPage.EventsGridSectionPage eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        Assert.assertEquals(TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals(eventsGridSectionPage.getRowNumberOfEventGird(),20);

        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(detailsPage.getMasterMpn(), inactiveSubNo1 + " Mobile 1(Inactive)");
        Assert.assertEquals(detailsPage.getSalesChannel(), "Onlines");
        Assert.assertEquals(detailsPage.getOrderType(), "New Order");
        Assert.assertEquals(detailsPage.getRootBuid(), customerNumber);

        //Upgrade Tariff
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrders.clickServiceOrderByType("Upgrade Tariff");

        TasksContentPage.TaskSummarySectionPage taskSummarySectionPage = TasksContentPage.TaskSummarySectionPage.getInstance();
        Assert.assertEquals(taskSummarySectionPage.getStatus(),"Completed Task");
        Assert.assertEquals(taskSummarySectionPage.getDescription(),"Upgrade Tariff");

        detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(detailsPage.getSubscriptionNumber(), inactiveSubNo1 + " Mobile 1 (Inactive)");
        Assert.assertEquals(detailsPage.getSubscriptionNumber2(), subNo1 + " Upgrade Mobile");

        eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        List<List<String>> EventsLists = new ArrayList<>();
        EventsLists.add(new ArrayList<>(Arrays.asList("OCS createSubscriber:Request completed", "Completed Task")));
        EventsLists.add(new ArrayList<>(Arrays.asList("OCS changePayRelation:Request completed", "Completed Task")));
        Assert.assertEquals(Common.compareLists(eventsGridSectionPage.getAllValueOfEvents(), EventsLists), 2);

        //Upgrade Order
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrders.clickServiceOrderByType("Upgrade Order");

        taskSummarySectionPage = TasksContentPage.TaskSummarySectionPage.getInstance();
        Assert.assertEquals(taskSummarySectionPage.getStatus(),"Completed Task");
        Assert.assertEquals(taskSummarySectionPage.getDescription(),"Upgrade Order");

        detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        Assert.assertEquals(detailsPage.getSalesChannel(), "Onlines");
        Assert.assertEquals(detailsPage.getServiceNo(), subNo1);
        Assert.assertEquals(detailsPage.getTariffProductCode(), "FC36-1000-100");

        eventsGridSectionPage = TasksContentPage.TaskPage.EventsGridSectionPage.getInstance();
        EventsLists = new ArrayList<>();
        EventsLists.add(new ArrayList<>(Arrays.asList("Terms And Conditions Accepted", "Completed Task")));
        EventsLists.add(new ArrayList<>(Arrays.asList("Upgrade sales order sent to billing", "Completed Task")));
        EventsLists.add(new ArrayList<>(Arrays.asList("Created - Upgrade Tariff SO", "Completed Task")));
        EventsLists.add(new ArrayList<>(Arrays.asList("Upgrade Tariff SO created successfully as TransactionId: " + serviceOrderId, "Completed Task")));
        EventsLists.add(new ArrayList<>(Arrays.asList("Order Complete", "Completed Task")));
        EventsLists.add(new ArrayList<>(Arrays.asList("Upgrade sales order sent to billing", "Completed Task")));
        Assert.assertEquals(Common.compareLists(eventsGridSectionPage.getAllValueOfEvents(), EventsLists), 6);

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

    private void verifyOcsKeyOfSubscription(){
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subNo1 + " Upgrade Mobile");
        SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage generalSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance();
        discountGroupCode = generalSectionPage.getDiscountGroupCode();
        verifyOcsSubscriptionDetails("OCS", discountGroupCode + "S", discountGroupCode + "A");
    }

    private void verifyOneInvoiceGeneratedWithIssueDateOfToday(){
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(1, grid.getRowNumberOfInvoiceTable());
        grid.clickInvoiceNumberByIndex(1);
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), grid.getIssued());
    }

    private void verifyInvoiceDetail(){
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().clickViewPDFBtn();
        String fileName = String.format("%s_%s.pdf", RandomCharacter.getRandomNumericString(9), customerNumber);
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().savePDFFile(fileName);

        List<String> listInvoiceContent = InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getListInvoiceContent(fileName,1);
        Assert.assertEquals(listInvoiceContent.get(71), String.format("User charges for %s  Upgrade Mobile (£10 Tariff 36 Month Contract)", subNo1));
        Assert.assertEquals(listInvoiceContent.get(74), String.format("Monthly subscription %s %s 10.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT4), Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT4)));
        Assert.assertEquals(listInvoiceContent.get(75), String.format("Total charges for %s 10.00", subNo1));
        Assert.assertEquals(listInvoiceContent.get(76), "Total user charges 30.00");
        Assert.assertEquals(listInvoiceContent.get(85), "Total Adjustments, charges & credits 230.00");
        Assert.assertEquals(listInvoiceContent.get(88), "%s Online/Telesales -15.00", Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT4));
        Assert.assertEquals(listInvoiceContent.get(89), "Total Payments -15.00");
    }

}
