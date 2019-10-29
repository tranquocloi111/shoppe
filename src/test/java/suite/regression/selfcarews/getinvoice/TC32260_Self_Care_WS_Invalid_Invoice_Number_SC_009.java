package suite.regression.selfcarews.getinvoice;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.ErrorResponseEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 8/10/2019
 */
public class TC32260_Self_Care_WS_Invalid_Invoice_Number_SC_009 extends BaseTest {
    String invoiceNumber;
    private Date newStartDate = TimeStamp.TodayMinus20Days();

    @Test(enabled = true, description = "TC32260_Self_Care_WS_Invalid_Invoice_Number_SC_009", groups = "SelfCareWS.GetInvoice")
    public void TC32260_Self_Care_WS_Invalid_Invoice_Number_SC_009() {
        test.get().info("1. Create an onlines CC customer with FC no bundle but has sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFCNoBundleButHasSimOnly();
        String customerNumber = owsActions.customerNo;

        test.get().info("2. Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("3. Update bill group payment collection date to 10 days later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("4. Set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("5. Update the start date of customer");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //===========================================================================
        test.get().info("6. Submit do refill bc job");
        submitDoRefillBCJob();

        test.get().info("7. Submit do refill nc job");
        submitDoRefillNCJob();

        test.get().info("8. Submit do bundle renew job");
        submitDoBundleRenewJob();

        test.get().info("9. draft bill run");
        submitDraftBillRun();

        test.get().info("10. Submit confirm bill run");
        submitConfirmBillRun();

        test.get().info("11. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("12. Verify customer has 1 invoice generated");
        invoiceNumber = CareTestBase.page().verifyCustomerHas1InvoiceGenerated();
        //===========================================================================
        test.get().info("13. Submit get invoice request with invalid Invoice Number");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetInvoiceWithAccountNoAndInvoiceNoRequest(customerNumber, "0");

        test.get().info("14. Verify self care WS fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, buildFaultResponse());
    }

    private ErrorResponseEntity buildFaultResponse() {
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("SC_009");
        falseResponse.setCodeAttribute("SC_009");
        falseResponse.setTypeAttribute("ERROR");
        falseResponse.setFaultString("Invoice not found");
        falseResponse.setDescription("Invoice not found");
        falseResponse.setExceptionMsg("Invoice not found");
        falseResponse.setExceptionCauseMsg("Invoice not found");

        return falseResponse;
    }
}