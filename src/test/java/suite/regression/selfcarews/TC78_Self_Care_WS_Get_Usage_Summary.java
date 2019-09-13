package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.io.File;
import java.sql.Date;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 29/08/2019
 */
public class TC78_Self_Care_WS_Get_Usage_Summary extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    private List<String> subscriptionNumberList;

    @Test(enabled = true, description = "TC78 Self Care WS Get Usage Summary", groups = "SelfCareWS")
    public void TC78_Self_Care_WS_Get_Usage_Summary(){
        test.get().info("Create Customer with 3 Subscriptions");
        OWSActions owsActions = new OWSActions();
        owsActions.createACustomerWith3Subscriptions();
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
        test.get().info("Login to HUBNet then search Customer by customer number then open Customer Summary");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Get All Subscriptions Number");
        subscriptionNumberList = CareTestBase.getAllSubscription();

        test.get().info("Verify Customer Start Date and Billing Group are updated successfully");
        CareTestBase.page().verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(newStartDate);
        //==============================================================================
        test.get().info("Submit Get Account Summary Request to SelfCare WS");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetUsageSummaryRequest(customerNumber);

        test.get().info("Build Expected Account Summary Response Data");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getusage\\TC78_Response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String expectedResponseFile = selfCareWSTestBase.buildResponseData(sampleResponseFile, newStartDate, TimeStamp.TodayMinus15DaysAdd1Month(), customerNumber, subscriptionNumberList);

        test.get().info("Verify Get Account Summary Response");
        verifyGetAccountSummaryResponse(expectedResponseFile, response);
    }

    private Boolean foundSubscription = false;
    private Boolean foundAllowance = false;

    private void verifyGetAccountSummaryResponse(String expectedResponseFile, Xml response){
        Xml expectedResponse = new Xml(new File(expectedResponseFile));
        NodeList expectSubNodeList = expectedResponse.getElementsByTagName("subscription");
        NodeList actualSubNodeList = response.getElementsByTagName("subscription");

        Assert.assertEquals(expectedResponse.getTextByTagName("accountNumber"), response.getTextByTagName("accountNumber"));
        Assert.assertEquals(expectSubNodeList.getLength(), actualSubNodeList.getLength());
        Assert.assertEquals(expectedResponse.getTextByTagName("lastCDRLoaded"), response.getTextByTagName("lastCDRLoaded"));


        for(int i=0; i<expectSubNodeList.getLength(); i++){
            for(int j = 0; j < actualSubNodeList.getLength(); j++){
                Node item1 =  expectSubNodeList.item(i);
                Node item2 = actualSubNodeList.item(j);

                String subscriptionNumber1 = expectedResponse.getChildNodeByTagName((Element)item1, "subscriptionNumber").getTextContent();
                String subscriptionNumber2 = response.getChildNodeByTagName((Element)item2, "subscriptionNumber").getTextContent();

                if(!subscriptionNumber1.equals(subscriptionNumber2)){
                    continue;
                }
                foundSubscription =true;

                Assert.assertEquals(subscriptionNumber1, subscriptionNumber2);
                Assert.assertEquals(expectedResponse.getChildNodeByTagName((Element)item1, "startDate").getTextContent(),
                        response.getChildNodeByTagName((Element)item2, "startDate").getTextContent());
                Assert.assertEquals(expectedResponse.getChildNodeByTagName((Element)item1, "endDate").getTextContent(),
                        response.getChildNodeByTagName((Element)item2, "endDate").getTextContent());

                List<Element> allowances_1 = expectedResponse.getListChildNodeByTagName((Element)item1, "allowances");
                List<Element> allowances_2 = expectedResponse.getListChildNodeByTagName((Element)item2, "allowances");

                for(int k =0; k<allowances_1.size(); k++){
                    for(Element actualE:allowances_2){
                        if(!getAllowanceElementsText(allowances_1.get(k), "allowanceType").equals(getAllowanceElementsText(actualE,"allowanceType"))){
                            continue;
                        }
                        foundAllowance = true;

                        Assert.assertEquals(getAllowanceElementsText(allowances_1.get(k), "allowanceType"),getAllowanceElementsText(actualE,"allowanceType"));

                        if(getAllowanceElementsText(actualE,"allowanceType").equals("TOPUP") || getAllowanceElementsText(actualE,"allowanceType").equals("GOODWILL")){
                            if (getAllowanceElementsText(allowances_1.get(k), "totalRemaining") != null) {
                            Assert.assertEquals(getAllowanceElementsText(allowances_1.get(k), "totalRemaining"), getAllowanceElementsText(actualE, "totalRemaining"));
                            }
                        }else {

                            if (getAllowanceElementsText(allowances_1.get(k), "totalUsed") != null && getAllowanceElementsText(allowances_1.get(k), "totalRemaining") != null) {
                                Assert.assertEquals(Parser.parseToInt(getAllowanceElementsText(allowances_1.get(k), "totalAllowance")),
                                        Parser.parseToInt(getAllowanceElementsText(actualE, "totalUsed")) + Parser.parseToInt(getAllowanceElementsText(actualE, "totalRemaining")));
                            }

                            if (getAllowanceElementsText(allowances_1.get(k), "totalUsed") == null && getAllowanceElementsText(allowances_1.get(k), "totalRemaining") == null) {
                                Assert.assertNotNull(getAllowanceElementsText(actualE, "totalRemaining"));
                            }

                            if (getAllowanceElementsText(allowances_1.get(k), "startDate") != null) {
                            Assert.assertEquals(getAllowanceElementsText(allowances_1.get(k), "startDate"), getAllowanceElementsText(actualE, "startDate"));
                            }

                            if (getAllowanceElementsText(allowances_1.get(k), "endDate") != null) {
                            Assert.assertEquals(getAllowanceElementsText(allowances_1.get(k), "endDate"), getAllowanceElementsText(actualE, "endDate"));
                            }

                            if (getAllowanceElementsText(allowances_1.get(k), "nextScheduledRefill") != null) {
                                Assert.assertEquals(getAllowanceElementsText(allowances_1.get(k), "nextScheduledRefill"), getAllowanceElementsText(actualE, "nextScheduledRefill"));
                            }
                        }
                    }
                    Assert.assertTrue(foundAllowance,  String.format("Not found allowance: %s", getAllowanceElementsText(allowances_1.get(k), "allowanceType")));
                }
                Assert.assertTrue(foundSubscription,  String.format("Not found subscription number: %s", subscriptionNumber1));
            }
        }
    }

    private String getAllowanceElementsText(Element allowances, String tagName){
        return allowances.getElementsByTagName(tagName).item(0).getTextContent();
    }
}
