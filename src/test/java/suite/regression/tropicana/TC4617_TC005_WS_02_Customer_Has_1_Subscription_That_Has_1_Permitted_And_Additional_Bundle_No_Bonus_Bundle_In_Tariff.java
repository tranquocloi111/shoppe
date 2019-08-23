package suite.regression.tropicana;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;

import java.sql.Date;

public class TC4617_TC005_WS_02_Customer_Has_1_Subscription_That_Has_1_Permitted_And_Additional_Bundle_No_Bonus_Bundle_In_Tariff extends BaseTest {
    String subscriptionNumber;
    private String customerNumber = "15758";
    private Date newStartDate;
    private String username;
    private String password;
    private String serviceOrderId;

    @Test(enabled = true, description = "TC4617 SCWS-Get Bundle- Validation for new Bonus Bundle Group and its bundles in response", groups = "Tropicana")
    public void TC4719_SCWS_Get_Bundle_Validation_For_New_Bonus_Bundle_Group_And_Its_Bundles_In_Response() {
        test.get().info("Step 1 : Create a Customer has 1 Subscription that has 1 Permitted/Additional Bundle (NO Bonus Bundle Group associated with tariff)");
        String path = "\\src\\test\\resources\\xml\\tropicana\\TC4617_TC005_request.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Submit get Bundle");
        SWSActions swsActions = new SWSActions();
        Xml xml = swsActions.submitGetBundleRequest(customerNumber, subscriptionNumber);

        test.get().info("Step 8 : Verify get bundle response are correct");
        verifyGetBundleResponseAreCorrect(xml);
    }

    private void verifyGetBundleResponseAreCorrect(Xml xml){
        String actualFile = Common.saveXmlFile(customerNumber +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xml.toString())));
        String file =  Common.readFile("src\\test\\resources\\xml\\sws\\getbundle\\TC3363_response.xml")
                .replace("$accountNumber$", customerNumber)
                .replace("$subscriptionNumber$", subscriptionNumber)
                .replace("$startDate$", Parser.parseDateFormate(newStartDate,"yyyy-MM-dd"))
                .replace("$nextScheduledRefill$", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"yyyy-MM-dd"))
                .replace("$SOId$", serviceOrderId);;
        String expectedResponseFile = Common.saveXmlFile(customerNumber +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(1, size);
    }
}
