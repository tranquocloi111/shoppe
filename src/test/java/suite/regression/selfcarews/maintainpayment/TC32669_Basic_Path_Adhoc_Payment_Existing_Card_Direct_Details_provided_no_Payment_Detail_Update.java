package suite.regression.selfcarews.maintainpayment;

import framework.utils.Xml;
import logic.business.entities.FinancialTransactionEnity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.entities.selfcare.MaintainPaymentResponseData;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.FinancialTransactionPage;
import logic.pages.care.find.PaymentDetailPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.util.HashMap;

public class TC32669_Basic_Path_Adhoc_Payment_Existing_Card_Direct_Details_provided_no_Payment_Detail_Update extends BaseTest {
    String customerNumber;
    String financialTransactionContentRef;

    @Test(enabled = true, description = "TC32669 basic path adhoc payment existing card direct details provided no payment detail update", groups = "SelfCareWS.Payment")
    public void TC32669_Basic_Path_Adhoc_Payment_Existing_Card_Direct_Details_provided_no_Payment_Detail_Update() {
        //-----------------------------------------
        String path = "src\\test\\resources\\xml\\sws\\maintainpayment\\TC32669";
        test.get().info("Step 1 : Create a custome ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : Build maintain payment detail request ");
        path = "src\\test\\resources\\xml\\sws\\maintainpayment\\TC32669_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildPaymentDetailRequest( customerNumber, path);

        test.get().info("Step 3:  submit the request to webservice");
        Xml response = swsActions.submitTheRequest();

        test.get().info("Step 4:  verify maintain payment response");
        MaintainPaymentResponseData maintainPaymentResponseData =new MaintainPaymentResponseData();
        maintainPaymentResponseData.setAccountNumber(customerNumber);
        maintainPaymentResponseData.setAction("ADHOC_PAYMENT");
        maintainPaymentResponseData.setMessage("Payment was successful");
        maintainPaymentResponseData.setResponseCode("0");
        maintainPaymentResponseData.setReference("True");
        maintainPaymentResponseData.setDateTime(Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_XML));
        SelfCareWSTestBase.verifyMaintainPaymentResponseByTagName(maintainPaymentResponseData,response);

        test.get().info("Step 5 : load customer in hub net ");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 6  refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 7  access financial transaction content for customer");
        MenuPage.LeftMenuPage.getInstance().clickFinancialTransactionLink();

        test.get().info("Step 8: verify 1 ad hoc payment generation");
        HashMap<String, String> financialTransaction = FinancialTransactionEnity.dataFinancialTransactionForMakeAOneOffPayment("Ad Hoc Payment", "£22.51");
        Assert.assertEquals(FinancialTransactionPage.FinancialTransactionGrid.getInstance().getNumberOfFinancialTransaction(financialTransaction), 1);

        test.get().info("Step 8: verify the adhoc payment transaction detail");
        financialTransactionContentRef = FinancialTransactionPage.FinancialTransactionGrid.getInstance().getRefNumberByDetail("Ad Hoc Payment");
        FinancialTransactionPage.FinancialTransactionGrid.getInstance().clickFinancialTransactionByDetail("Ad Hoc Payment");
        verifyAdHocPaymentTransactionDetail();

        test.get().info("Step 9 :Open the service order content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        HashMap<String, String> serviceOrder = ServiceOrderEntity.dataServiceOrderFinancialTransaction();
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(serviceOrder), 1);

        test.get().info("Step 10 :verify the service order detail content for customer");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Ad-hoc Payment");
        Assert.assertEquals(TasksContentPage.TaskSummarySectionPage.getInstance().getStatus(), "Completed Task");
        Assert.assertEquals("MasterCard", TasksContentPage.TaskPage.DetailsPage.getInstance().getCardType());
        Assert.assertEquals("****************5100", TasksContentPage.TaskPage.DetailsPage.getInstance().getCardNumber());
        Assert.assertEquals("2030", TasksContentPage.TaskPage.DetailsPage.getInstance().getCreditCardExpiryYear());
        Assert.assertEquals("22.51", TasksContentPage.TaskPage.DetailsPage.getInstance().getAmountToBeDebited());
        Assert.assertEquals("12", TasksContentPage.TaskPage.DetailsPage.getInstance().getCreditCardExpiryMonth());


    }

    public void verifyAdHocPaymentTransactionDetail() {
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getReceiptType(), "Ad Hoc Payment");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getReceiptStatus(), "Fully Allocated");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getPaymentAmount(), "£22.51");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getPaymentCurrency(), "Great Britain Pound");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getCardType(), "MasterCard");
        Assert.assertEquals(PaymentDetailPage.ReceiptDetail.getInstance().getCardNumber(), "************5100");
    }

}




