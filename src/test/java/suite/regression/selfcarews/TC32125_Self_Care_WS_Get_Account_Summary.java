package suite.regression.selfcarews;

import framework.utils.Log;
import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.find.CommonContentPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 29/07/2019
 */
public class TC32125_Self_Care_WS_Get_Account_Summary extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    private List<String> subscriptionNumberList = new ArrayList<>();

    @Test(enabled = true, description = "TC32125 Self Care WS Get Account Summary", groups = "SelfCareWS")
    public void TC32125_Self_Care_WS_Get_Account_Summary(){
        test.get().info("Step 1 : Create a CC Customer with 2 subscriptions order");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWith2SubscriptionOrder();
        customerNumber = owsActions.customerNo;

        test.get().info("Create new billing group start from today minus 15 days");
        createNewBillingGroupToMinus15days();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //=============================================================================

        test.get().info("Login to HUBNet then search Customer by customer number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Get All Subscriptions Number");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        subscriptionNumberList = selfCareWSTestBase.getAllSubscription(2);

        test.get().info("Verify Customer Start Date and Billing Group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);
        //==============================================================================

        test.get().info("Submit Get Account Summary Request to SelfCare WS");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountSummaryRequestToSelfCareWS(customerNumber);

        test.get().info("Build Expected Account Summary Response Data");
        Xml expectedResponse = buildAccountSummaryResponseData(newStartDate);

        test.get().info("Verify Get Account Summary Response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponse, response);
    }

    private String getMobileFCSubscriptionNumber(){
        String number = "";
        for(String subNo:subscriptionNumberList ){
            if (subNo.contains("Mobile FC")){
                number = subNo.split(" ")[0];
            }
        }
        return number;
    }

    private String getMobileNCSubscriptionNumber(){
        String number = "";
        for(String subNo:subscriptionNumberList ){
            if (subNo.contains("Mobile NC")){
                number = subNo.split(" ")[0];
            }
        }
        return number;
    }

    private Xml buildAccountSummaryResponseData(Date startDate){
        Xml response = new Xml(new File("src\\test\\resources\\xml\\sws\\getaccount\\TC32125_response"));

        String sStartDate =  Parser.parseDateFormate(startDate, TimeStamp.DateFormatXml());
        String SNextBillDate = Parser.parseDateFormate(TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.DateFormatXml());

        String accountName = "Mr " + CareTestBase.getCustomerName();

        response.setTextByTagName("accountNumber", customerNumber);
        response.setTextByTagName("accountName",accountName);
        response.setTextByTagName("startDate",sStartDate);
        response.setTextByTagName("nextBillDate", SNextBillDate);
        response.setTextByTagName("endDate", "");

        NodeList nodes = response.getElementsByTagName("subscriptionDetail");
        Log.info(nodes.toString());
        for (int i = 0; i < nodes.getLength(); i++) {
            Element parentNode = (Element)nodes.item(i);;
            Element child = response.getChildNodeByTagName(parentNode, "subscriptionDescription");
            if (child.getTextContent().equals("Mobile FC")) {
                Element subNumber = response.getChildNodeByTagName(parentNode, "subscriptionNumber");
                subNumber.setTextContent(getMobileFCSubscriptionNumber());
            } else {
                Element subNumber = response.getChildNodeByTagName(parentNode, "subscriptionNumber");
                subNumber.setTextContent(getMobileNCSubscriptionNumber());
            }

        }
        response.setAttributeTextAllNodesByXpath("tariff", "startDate", sStartDate);

        return response;
    }
}