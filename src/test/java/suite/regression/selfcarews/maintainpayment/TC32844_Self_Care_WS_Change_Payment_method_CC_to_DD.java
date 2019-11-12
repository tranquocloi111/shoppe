package suite.regression.selfcarews.maintainpayment;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.ErrorResponseEntity;
import logic.business.entities.MaintainPaymentResponseData;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

public class TC32844_Self_Care_WS_Change_Payment_method_CC_to_DD extends BaseTest {
    String customerNumber;

    @Test(enabled = true, description = "TC32744 Selfcare ws change payment method cc to DD", groups = "SelfCareWS.Payment")
    public void TC32844_Self_Care_WS_Change_Payment_method_CC_to_DD() {
        //-----------------------------------------
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWithOrder();
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2: Update the start date of customer");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 3: load customer in hub net ");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 4: verify payment method start date is correct");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(String.format("Credit Card ( %s )", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT)),
                DetailsContentPage.PaymentInformationPage.getInstance().getPaymentMethod());

        test.get().info("Step 5: back date the payment method start date to today minus 1 day");
        BaseTest.backDateThePaymentMethodStartDateToTodayMinus1Day(customerNumber);

        test.get().info("Step 6: verify  payment method start date has been back dated");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(String.format("Credit Card ( %s )", Parser.parseDateFormate(TimeStamp.TodayMinus1Day(),TimeStamp.DATE_FORMAT)),
                DetailsContentPage.PaymentInformationPage.getInstance().getPaymentMethod());
        test.get().info("Step 7: Build maintain payment detail request ");
        String path = "src\\test\\resources\\xml\\sws\\maintainpayment\\TC68_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildPaymentDetailRequestByIndex(customerNumber, path);

        test.get().info("Step 8:  submit the request to webservice");
        Xml response = swsActions.submitTheRequest();

        test.get().info("Step 9: verify maintain payment response");
        logic.business.entities.selfcare.MaintainPaymentResponseData maintainPaymentResponseData = new logic.business.entities.selfcare.MaintainPaymentResponseData();
        maintainPaymentResponseData.setAccountNumber(customerNumber);
        maintainPaymentResponseData.setResponseCode("0");
        maintainPaymentResponseData.setAction("UPDATE_DIRECT_DEBIT");
        Assert.assertEquals(maintainPaymentResponseData.getAccountNumber(),response.getTextByTagName("accountNumber"));
        Assert.assertEquals(maintainPaymentResponseData.getAction(),response.getTextByTagName("action"));
        Assert.assertEquals(maintainPaymentResponseData.getResponseCode(),response.getTextByTagName("responseCode"));

        test.get().info("Step 10:  verify payment information have been updated");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals( String.format("Direct Debit ( %s )",Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT))
                ,DetailsContentPage.PaymentInformationPage.getInstance().getPaymentMethod());
        Assert.assertEquals("Natewst",DetailsContentPage.PaymentInformationPage.getInstance().getBankName());
        Assert.assertEquals("***999", DetailsContentPage.PaymentInformationPage.getInstance().getBankSortCode());
        Assert.assertEquals( "66374958",DetailsContentPage.PaymentInformationPage.getInstance().getBankAccountNumber());

        test.get().info("Step 11:  submit send DDI request job");
        RemoteJobHelper.getInstance().submitSendDDIRequestJob();

        test.get().info("Step 12: verify DDI status changed to Inactive");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("Inactive",DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus());

        test.get().info("Step 13: open service order details for send DDI to BACS item");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Send DDI to BACS");


        test.get().info("Step 14: verify service order status is send and DDI reference has value");
        Assert.assertEquals("Sent", TasksContentPage.TaskPage.DetailsPage.getInstance().getServiceOrderStatus());
        Assert.assertFalse( TasksContentPage.TaskPage.DetailsPage.getInstance().getDDIReference()=="");

        test.get().info("Step 15: Update customer DDI details in database");
        TasksContentPage.TaskPage.DetailsPage detailsPage = TasksContentPage.TaskPage.DetailsPage.getInstance();
        CommonActions.updateCustomerDDIDetailsInDatabase(Parser.parseDateFormate(newStartDate, "dd/MMM/YY"),
                detailsPage.getHierarchyMbr(), detailsPage.getDDIReference());

        test.get().info("Step 16: find customer then open the details content");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 17: verify DDI information is correct");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("Active",DetailsContentPage.PaymentInformationPage.getInstance().getDDIStatus());
        String expectedDDI=DetailsContentPage.PaymentInformationPage.getInstance().getDDIReference().split(" ")[0];
        Assert.assertEquals(String.format("%s ( %s )",expectedDDI, Parser.parseDateFormate(newStartDate,TimeStamp.DATE_FORMAT)),DetailsContentPage.PaymentInformationPage.getInstance().getDDIReference());
    }


}




