package suite.regression.selfcarews.getlivebillestimate;

import framework.config.Config;
import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.LiveBillEstimateContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ChangeSubscriptionNumberPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.bouncycastle.util.encoders.Base64;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;

/**
 * User: Nhi Dinh
 * Date: 1/10/2019
 */
public class TC32161_Alternate_Path_2a_Get_Bill_Estimate_for_Subscription_Active_after_Change_of_MPN extends BaseTest {
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    private String subscriptionNumber;
    private String subscriptionNumberValue;
    private String newSubscriptionNumber;
    private String discountGroupCode;
    private String estimateOfUsageCharges;
    private String receiptID;


    @Test(enabled = true, description = "TC32161_Alternate_Path_2a_Get_Bill_Estimate_for_Subscription_Active_after_Change_of_MPN", groups = "SelfCareWS.GetLiveBillEstimate")
    public void TC32161_Alternate_Path_2a_Get_Bill_Estimate_for_Subscription_Active_after_Change_of_MPN() {
        test.get().info("Step 1 : Create a CC Customer with FC 1 bundle and NK2720");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC1BundleAndNK2720();
        customerNumber = owsActions.customerNo;

        test.get().info("2. Prepare CDR file with sample TM DRAS CDR file");
        prepareCDRFileWithSampleTMDRASCDRFile();

        test.get().info("3. Wait for CDR job to complete");
        RemoteJobHelper.getInstance().waitLoadCDRJobComplete();

        test.get().info("4. Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("5. Update bill group payment collection date to 10 days later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("6. Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("7. Update the start date of customer");
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);
        //===========================================================================
        test.get().info("8. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("9. Change MPN");
        changeMPN();

        //===========================================================================
        ////Adding 9.1 step to record expected data
        test.get().info("Record Data");
        recordData();
        //===========================================================================
        test.get().info("10. Submit get bill estimate request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetBillEstimateRequest(customerNumber);
        String actualBillEstimateResponse = getActualBillEstimateResponse(response);

        test.get().info("11. Build expected response");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getbillestimate\\TC32161_response.xml";
        String expectedResponseFile = buildExpectedResponseData(sampleResponseFile);

        test.get().info("12. Verify get bill estimate response");
        verifyGetBillEstimateResponse(actualBillEstimateResponse, expectedResponseFile);
    }

    private void verifyGetBillEstimateResponse(String actualBillEstimate, String expectedBillEstimate) {
        int size = Common.compareFile(actualBillEstimate, expectedBillEstimate).size();
        Assert.assertEquals(1, size);
    }

    private String getActualBillEstimateResponse(Xml response) {
        String billEstimateStr = response.getTextByTagName("billEstimate");
        byte[] billEstimateByte = Base64.decode(billEstimateStr);
        String billEstimateXmlString = new String(billEstimateByte);

        return Common.saveXmlFile(customerNumber + "_Actual_BillEstimate.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(billEstimateXmlString)));
    }

    private String buildExpectedResponseData(String sampleFile) {
        String sNextInvoiceDate = Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT4);
        String SNextInvoiceDueDate = Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 1), TimeStamp.DATE_FORMAT4);
        String sDateFrom = Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT4);
        String sDateTo = Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT4);
        String sDateFrom2 = Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT4);
        String sDateTo2 = Parser.parseDateFormate(TimeStamp.TodayPlus2MonthMinus1Day(), TimeStamp.DATE_FORMAT4);


        String file = Common.readFile(sampleFile)
                .replace("$CustomerNumber$", customerNumber)
                .replace("$estimateOfUsageCharges$", estimateOfUsageCharges)
                .replace("$nextInvoiceDate$", sNextInvoiceDate)
                .replace("$nextInvoiceDueDate$", SNextInvoiceDueDate)
                .replace("$dateFrom$", sDateFrom)
                .replace("$dateTo$", sDateTo)
                .replace("$invid$", discountGroupCode)
                .replace("$subscriptionNumberValue$", newSubscriptionNumber + "  Mobile Ref 1")
                .replace("$subscriptionNumber$", newSubscriptionNumber)
                .replace("$dateFrom2$", sDateFrom2)
                .replace("$dateTo2$", sDateTo2)
                .replace("$reference$", receiptID);

        return Common.saveXmlFile(customerNumber + "_Expected_BillEstimate.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
    }

    private void recordData() {
        //Record discount group code of subscription
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(subscriptionNumber + " Mobile Ref 1");
        discountGroupCode = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();

        // Record Estimate of Usage Charges is current as of
        MenuPage.LeftMenuPage.getInstance().clickLiveBillEstimateItem();
        LiveBillEstimateContentPage.LiveBillEstimate.BillingInformation billingInformation = new LiveBillEstimateContentPage.LiveBillEstimate.BillingInformation();
        estimateOfUsageCharges = billingInformation.getEstimateOfUsageCharges();

        //Record reference of Account Payments and Vouchers
        LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.AccountPaymentsAndVouchers accountPaymentsAndVouchers = new LiveBillEstimateContentPage.LiveBillEstimate.ChargesToDate.AccountPaymentsAndVouchers();
        accountPaymentsAndVouchers.expand();
        receiptID = accountPaymentsAndVouchers.getReferenceByIndex(1);
    }


    private void changeMPN() {
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscriptionNumberValue = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(1);
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        subscriptionNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();

        MenuPage.RightMenuPage.getInstance().clickChangeSubscriptionNumberLink();
        ChangeSubscriptionNumberPage.ChangeSubscriptionNumber content = ChangeSubscriptionNumberPage.ChangeSubscriptionNumber.getInstance();
        subscriptionNumber = content.getCurrentSubscriptionNumber().split(" ")[0];
        newSubscriptionNumber = CareTestBase.page().updateTheSubscriptionNumberAndClickNextButton();

        ChangeSubscriptionNumberPage.ConfirmChangingSubscriptionNumber.getInstance().clickNextButton();
        ServiceOrdersPage.ServiceOrderComplete.getInstance().clickReturnToCustomer();
    }

    private void prepareCDRFileWithSampleTMDRASCDRFile() {
        String cdrTemplate = "src\\test\\resources\\txt\\selfcare\\viewaccount\\TM_DRAS_CDR_20150114150001";
        String cdrFileString = Common.readFile(cdrTemplate);
        String fileName = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT2) + RandomCharacter.getRandomNumericString(6);
        fileName = Common.getFolderLogFilePath() + "TM_DRAS_CDR_" + fileName + ".txt";

        Common.writeFile(cdrFileString, fileName);

        String remotePath = Config.getProp("CDRSFTPFolder");
        SFTPHelper.getInstance().upFileFromLocalToRemoteServer(fileName, remotePath);
    }
}
