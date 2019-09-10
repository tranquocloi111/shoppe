package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 6/09/2019
 */
public class TC37408_Basic_Path_Subscription_Active_Device_and_Peripherals_with_no_Agreement extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus20Days();
    private String subscriptionNumber;
    private String deviceAgreementNumber;
    private String peripheralsAgreementNumber;

    @Test(enabled = true, description = "TC37408_Basic Path Subscription Active Device and Peripherals with no Agreement", groups = "SelfCareWS")
    public void TC37408_Basic_Path_Subscription_Active_Device_and_Peripherals_with_no_Agreement(){
        test.get().info("Create an Online CC Customer with FC Oakwook None Peripheral");
        OWSActions owsActions = new OWSActions();
        String TC37408_CreateOrderRequest = "src\\test\\resources\\xml\\ows\\onlines_CC_customer_with_FC_Oakwood_None_CCA_peripheral.xml";
        owsActions.createGeneralCustomerOrder(TC37408_CreateOrderRequest);
        customerNumber = owsActions.customerNo;

        test.get().info("Create new billing group");
        createNewBillingGroup();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Update customer start date to Minus 20 days");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //=======================================================================
        test.get().info("Record subscription number from order");
        recordSubscriptionNumberFromOrder(owsActions);

        test.get().info("Submit get contract summary request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetContractSummaryRequest(subscriptionNumber);

        test.get().info("Build expected response");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String sampleFile = "src\\test\\resources\\xml\\sws\\getcontract\\TC37048_response.xml";
        String expectedResponse = buildResponseData(sampleFile);

        test.get().info("Verify get Contract summary response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponse, response);
    }

    private String buildResponseData(String sampleFile){
        String sStartDate = Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT_XML);
        String sEndDate = Parser.parseDateFormate(TimeStamp.TodayPlusMonth(11), TimeStamp.DATE_FORMAT_XML);
        String timeZone = TimeStamp.TimeZone();

        String accountName = "Mr " + CareTestBase.getCustomerName();

        String file = Common.readFile(sampleFile).replace("$accountNumber$", customerNumber)
                .replace("$accountName$", accountName)
                .replace("$startDate$", sStartDate)
                .replace("$endDate$", sEndDate)
                .replace("$subscriptionNumber$", subscriptionNumber)
                .replace("$timeZone$", timeZone);

        return Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
    }

    private void recordSubscriptionNumberFromOrder(OWSActions owsActions){
        owsActions.getOrder(owsActions.orderIdNo);
        subscriptionNumber = owsActions.getOrderMpnByReference("Mobile Apple Watch");
    }
}
