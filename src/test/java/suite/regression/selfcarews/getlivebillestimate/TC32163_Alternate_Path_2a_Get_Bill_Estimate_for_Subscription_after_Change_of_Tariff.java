package suite.regression.selfcarews.getlivebillestimate;

import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.BundlesToSelectEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.entities.TariffSearchCriteriaEnity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.LiveBillEstimateContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ChangeTariffPage;
import logic.pages.care.options.TariffSearchPage;
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
import java.util.HashMap;

/**
 * User: Nhi Dinh
 * Date: 1/10/2019
 */
public class TC32163_Alternate_Path_2a_Get_Bill_Estimate_for_Subscription_after_Change_of_Tariff extends BaseTest {
    String subscriptionNumber;
    String subscriptionNumberValue;
    String serviceOrderId;
    private String customerNumber;
    private Date newStartDate = TimeStamp.TodayMinus10Days();
    private String estimateOfUsageCharges;
    private String discountGroupCode;
    private String receiptID;

    @Test(enabled = true, description = "TC32163_Alternate_Path_2a_Get_Bill_Estimate_for_Subscription_after_Change_of_Tariff", groups = "SelfCareWS.GetLiveBillEstimate")
    public void TC32163_Alternate_Path_2a_Get_Bill_Estimate_for_Subscription_after_Change_of_Tariff() {
        test.get().info("Step 1 : Create a CC Customer with FC 1 bundle and NK2720");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWithFC1BundleAndNK2720();
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
        //===========================================================================
        test.get().info("6. Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("7. Change Tariff");
        changeTariff();

        test.get().info("8. Verify change tariff so created");
        HashMap<String, String> serviceOrderEntity = ServiceOrderEntity.dataServiceOrder("Provision Wait", "Change Tariff");
        verifyChangeTariffSOCreated(serviceOrderEntity);

        test.get().info("9. Update the PDATE and BILLDATE for provision wait SO");
        BillingActions.getInstance().updateThePDateAndBillDateForChangeBundle(serviceOrderId);

        test.get().info("10. Run Provision Service Job");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("11. Refresh current customer data in hub net");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("12. Submit get bill estimate request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetBillEstimateRequest(customerNumber);
        String actualBillEstimateResponse = getActualBillEstimateResponse(response);
        //===========================================================================
        ////Adding 12.1 step to Record Data
        test.get().info("Record Data");
        recordData();
        //===========================================================================
        test.get().info("12. Build expected response");
        String sampleResponseFile = "src\\test\\resources\\xml\\sws\\getbillestimate\\TC32163_response.xml";
        String expectedResponseFile = buildExpectedResponseData(sampleResponseFile);

        test.get().info("13. Verify get bill estimate response");
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
        String sDateFrom0 = Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT4);
        String sDateTo0 = Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT4);
        String sDateFrom1 = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT4);
        String sDateTo1 = Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT4);
        String sDateFrom2 = Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT4);
        String sDateTo2 = Parser.parseDateFormate(TimeStamp.TodayPlus2MonthMinus1Day(), TimeStamp.DATE_FORMAT4);

        String file = Common.readFile(sampleFile)
                .replace("$CustomerNumber$", customerNumber)
                .replace("$estimateOfUsageCharges$", estimateOfUsageCharges)
                .replace("$nextInvoiceDate$", sNextInvoiceDate)
                .replace("$nextInvoiceDueDate$", SNextInvoiceDueDate)
                .replace("$dateFrom0$", sDateFrom0)
                .replace("$dateTo0$", sDateTo0)
                .replace("$dateFrom1$", sDateFrom1)
                .replace("$dateTo1$", sDateTo1)
                .replace("$dateFrom2$", sDateFrom2)
                .replace("$dateTo2$", sDateTo2)
                .replace("$invid$", discountGroupCode)
                .replace("$subscriptionNumberValue$", subscriptionNumber + "  Mobile Ref 1")
                .replace("$subscriptionNumber$", subscriptionNumber)
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

    private void verifyChangeTariffSOCreated(HashMap<String, String> serviceOrderEntity) {
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderidByType(serviceOrderEntity.get("Type"));
        Assert.assertEquals(1, ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(serviceOrderEntity));
    }

    private void changeTariff() {
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscriptionNumberValue = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(1);
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        subscriptionNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();

        MenuPage.RightMenuPage.getInstance().clickChangeTariffLink();
        ServiceOrdersPage.SelectSubscription.getInstance().clickNextButton();
        ChangeTariffPage.ChangeTariff.getInstance().clickButtonNewTariff();

        ChangeTariffPage.ChangeTariff.getInstance().clickButtonNewTariff();
        TariffSearchCriteriaEnity tariffSearchCriteriaEnity = new TariffSearchCriteriaEnity();
        tariffSearchCriteriaEnity.setStaffTariff("No");
        tariffSearchCriteriaEnity.setBillingType("Flexible Cap");
        tariffSearchCriteriaEnity.setTariffType("");
        tariffSearchCriteriaEnity.setContractPeriod("12");
        tariffSearchCriteriaEnity.setMonthlyRental("10-20");
        tariffSearchCriteriaEnity.setEarlyTerminationCharge("");

        TariffSearchPage.getInstance().searchTariffByCriteria(tariffSearchCriteriaEnity);

        String tariffCode = "FC12-2000-750";
        TariffSearchPage.getInstance().selectTariffByCode(tariffCode);
        ChangeTariffPage.ChangeTariff.getInstance().clickNextButton();

        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName(BundlesToSelectEntity.getSafetyBuffersAToSelect(), "£10 safety buffer");
        ServiceOrdersPage.ChangeBundle.getInstance().clickNextButton();
        ServiceOrdersPage.ConfirmChangeBundle.getInstance().clickNextButton();
        ServiceOrdersPage.ServiceOrderComplete.getInstance().clickReturnToCustomer();
    }
}
