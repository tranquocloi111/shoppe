package suite.regression.selfcarews.maintainpayment;

import framework.utils.Xml;
import logic.business.entities.FinancialTransactionEnity;
import logic.business.entities.selfcare.MaintainPaymentResponseData;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.FinancialTransactionPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.util.HashMap;

public class TC32710_Basic_Path_Adhoc_Payment_Non_account_holder_Account_Number_Existing_MasterCard extends BaseTest {
    String customerNumber;
    String financialTransactionContentRef;

    @Test(enabled = true, description = "TC32710 basic path adhoc payment non account holder account number existing Mastercard", groups = "SelfCareWS.Payment")
    public void TC32710_Basic_Path_Adhoc_Payment_Non_account_holder_Account_Number_Existing_MasterCard() {
        //-----------------------------------------
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_of_SB_and_sim_only";
        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : load customer in hub net ");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3 : Build maintain payment detail request ");
        path = "src\\test\\resources\\xml\\sws\\maintainpayment\\TC32710_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildPaymentDetailRequest( customerNumber, path);

        test.get().info("Step 4  submit the request to webservice");
        Xml response = swsActions.submitTheRequest();

        test.get().info("Step 5  verify maintain payment response");
        MaintainPaymentResponseData maintainPaymentResponseData =new MaintainPaymentResponseData();
        maintainPaymentResponseData.setAccountNumber(customerNumber);
        maintainPaymentResponseData.setAction("ADHOC_PAYMENT_NON_ACCOUNT_HOLDER");
        maintainPaymentResponseData.setResponseCode("0");
        maintainPaymentResponseData.setMessage("Payment was successful");
        maintainPaymentResponseData.setReference("True");
        maintainPaymentResponseData.setDateTime(Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT_XML));

        SelfCareWSTestBase.verifyMaintainPaymentResponseByTagName(maintainPaymentResponseData,response);

        test.get().info("Step 6  refresh current customer data in hub net");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();


        test.get().info("Step 7  access financial transaction content for customer");
        MenuPage.LeftMenuPage.getInstance().clickFinancialTransactionLink();

        test.get().info("Step 8: verify 1 ad hoc payment generation");
        HashMap<String, String> financialTransaction = FinancialTransactionEnity.dataFinancialTransactionForMakeAOneOffPayment("Ad Hoc Payment", "£10.00");
        Assert.assertEquals(FinancialTransactionPage.FinancialTransactionGrid.getInstance().getNumberOfFinancialTransaction(financialTransaction), 1);
        

    }


}




