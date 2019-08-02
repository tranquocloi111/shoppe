package suite.regression.SelfCareWS;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.io.File;
import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 2/08/2019
 */
public class TC32131_Basic_Path_Active_Account_with_Clubcard extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    private String latestSubscriptionNumber;
    private String clubCardNumber;

    @Test
    public void TC32131_Basic_Path_Active_Account_with_Clubcard(){
        test.get().info("Step 1 : Create a CC Customer with 2 subscriptions order");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC1BundleAndNK2720();
        customerNumber = owsActions.customerNo;

        test.get().info("Create new billing group start from today minus 15 days");
        createNewBillingGroupStartFromTodayMinus15Days();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Update Customer Start Date");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //=============================================================================

        test.get().info("Login to HUBNet then search Customer by customer number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Record latest subsciption number for customer");
        latestSubscriptionNumber = CareTestBase.page().recordLatestSubscriptionNumberForCustomer();

        test.get().info("Record account name and club card number");
        recordAccountNameAndClubCardNumber();
        //=============================================================================
        test.get().info("Submit Get Account Summary Request To SelfCare WebService");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountSummaryRequestToSelfCareWS(customerNumber);

        test.get().info("Build Expected Account Summary Response Data");
        Xml expectedResponse = buildAccountSummaryResponseData(newStartDate);

        test.get().info("Verify Get Account Summary Response");
        swsActions.verifyGetAccountSummaryResponse(expectedResponse, response);
    }

    private void recordAccountNameAndClubCardNumber(){
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        clubCardNumber = selfCareWSTestBase.getClubCardNumber().split(" ")[0];
    }


    private Xml buildAccountSummaryResponseData(Date startDate){
        Xml response = new Xml(new File("src\\test\\resources\\xml\\sws\\getaccount\\TC32131_response.xml"));

        String sStartDate =  Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT_XML)+"+08:00";
        String SNextBillDate = Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus15Days(), TimeStamp.DATE_FORMAT_XML)+"+08:00";

        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String accountName = "Mr " + selfCareWSTestBase.getCustomerName();

        response.setTextByTagName("accountNumber", customerNumber);
        response.setTextByTagName("accountName",accountName);
        response.setTextByTagName("startDate",sStartDate);
        response.setTextByTagName("nextBillDate", SNextBillDate);
        response.setTextByTagName("endDate", "");
        response.setTextByTagName("subscriptionNumber", latestSubscriptionNumber);

        response.setAttributeTextAllNodesByXpath("tariff", "startDate", sStartDate);

        return response;
    }

}
