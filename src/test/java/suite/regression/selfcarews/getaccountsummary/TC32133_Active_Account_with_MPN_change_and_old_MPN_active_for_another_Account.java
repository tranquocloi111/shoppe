package suite.regression.selfcarews.getaccountsummary;

import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.entities.SubscriptionEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ChangeSubscriptionNumberPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.io.File;
import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 1/10/2019
 */
public class TC32133_Active_Account_with_MPN_change_and_old_MPN_active_for_another_Account extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    String subscriptionNumber;
    String subscriptionNumberValue;
    String newSubscriptionNumber;
    String orderWebServiceId;

    @Test(enabled = true, description = "TC32133_Active_Account_with_MPN_change_and_old_MPN_active_for_another_Account", groups = "SelfCareWS.GetAccountSummary")
    public void TC32133_Active_Account_with_MPN_change_and_old_MPN_active_for_another_Account(){
        test.get().info("1. Create an onlines CC Customer with FC 1 bundle of SB and sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
        customerNumber = owsActions.customerNo;

        test.get().info("2. Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("3. Update bill group payment collection date to 10 days later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("4. Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("5. Update the start date of customer");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("6. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("7. Record latest subscription for customer");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscriptionNumberValue = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(1);
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        subscriptionNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();

        test.get().info("8. Start change subscription number wizard");
        startChangeSubscriptionNumberWizard();

        test.get().info("9. Verify subscriptions in summary");
        verifySubscriptionsInSummary();

        test.get().info("10. Create another customer using the old MPN");
        CommonActions.updateCustomerEndDate(customerNumber, TimeStamp.TodayMinus1Day());
        createAnotherCustomerUsingTheOldMPN();

        test.get().info("11. Submit Get Account Summary Request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountSummaryWithSubsRequest(newSubscriptionNumber);

        test.get().info("12. Build Expected Account Summary Response Data");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getaccount\\TC32133_response.xml";
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String expectedResponseFile = buildResponseData(sampleResponseFile);

        test.get().info("13. Verify Get Account Summary Response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponseFile, response);

    }
    private String buildResponseData(String sampleFile){
        String sStartDate = Parser.parseDateFormate(newStartDate, TimeStamp.DateFormatXml());
        String sNextBillDate = Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DateFormatXml());
        String sNewStartDate = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DateFormatXml());
        String sEndDate = Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DateFormatXml());

        String accountName = "Mr " + CareTestBase.getCustomerName();

        String file = Common.readFile(sampleFile).replace("$accountNumber$", customerNumber)
                .replace("$accountName$", accountName)
                .replace("$startDate$", sStartDate)
                .replace("$endDate$", sEndDate)
                .replace("$nextBillDate$", sNextBillDate)
                .replace("$subscriptionNumber$", newSubscriptionNumber)
                .replace("$newStartDate$", sNewStartDate);

        return Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
    }

    private void createAnotherCustomerUsingTheOldMPN(){
        String requestFile = "src\\test\\resources\\xml\\ows\\TC32133_createOrder.xml";
        Xml file =  new Xml(new File(requestFile));
        file.setTextByTagName("serviceRef", subscriptionNumber);

        Common.writeFile(file.toString(), requestFile);

        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(requestFile);

    }
    private void verifySubscriptionsInSummary(){
        Assert.assertEquals(1, CommonContentPage.SubscriptionsGridSectionPage.getInstance().getNumberOfSubscription(
                SubscriptionEntity.dataForActiveSubscriptions((newSubscriptionNumber + " Mobile Ref 1"))));
    }
    private void startChangeSubscriptionNumberWizard(){
        MenuPage.RightMenuPage.getInstance().clickChangeSubscriptionNumberLink();
        ChangeSubscriptionNumberPage.ChangeSubscriptionNumber content = ChangeSubscriptionNumberPage.ChangeSubscriptionNumber.getInstance();
        subscriptionNumber = content.getCurrentSubscriptionNumber().split(" ")[0];
        newSubscriptionNumber = CareTestBase.page().updateTheSubscriptionNumberAndClickNextButton();

        ChangeSubscriptionNumberPage.ConfirmChangingSubscriptionNumber.getInstance().clickNextButton();
        ServiceOrdersPage.ServiceOrderComplete.getInstance().clickReturnToCustomer();
    }
}
