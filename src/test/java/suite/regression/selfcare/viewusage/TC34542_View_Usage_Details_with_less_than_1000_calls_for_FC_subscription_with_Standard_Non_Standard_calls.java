package suite.regression.selfcare.viewusage;

import framework.config.Config;
import framework.utils.RandomCharacter;
import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.entities.TariffSearchCriteriaEnity;
import logic.business.entities.selfcare.UsageDetailsEnity;
import logic.business.helper.FTPHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.TariffSearchPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.pages.selfcare.UsageDetailsSinceMyLastBillPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Time;
import java.util.HashMap;

public class TC34542_View_Usage_Details_with_less_than_1000_calls_for_FC_subscription_with_Standard_Non_Standard_calls extends BaseTest {

    String sub = "07982331280";

    @Test(enabled = true, description = "TC34542 view usage details with less than 1000 calls for FC subscription with standard non standard calls", groups = "SelfCare")
    public void TC34542_View_Usage_Details_with_less_than_1000_calls_for_FC_subscription_with_Standard_Non_Standard_calls() {
//        test.get().info("Step 1: Create a CC customer");
//        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_of_SB_and_sim_only";
//        OWSActions owsActions = new OWSActions();
//        owsActions.createGeneralCustomerOrder(path);
//        String customerNumber = owsActions.customerNo;
//        owsActions.getSubscription(owsActions.orderIdNo, "Mobile Ref 1");
//        sub = owsActions.serviceRef;
//
//        test.get().info("Step 2: create new billing group");
//        createNewBillingGroup();
//        test.get().info("Step 3: update bill group payment collection date to 10 day later ");
//        updateBillGroupPaymentCollectionDateTo10DaysLater();
//        test.get().info("Step 4: set bill group for customer");
//        setBillGroupForCustomer(customerNumber);
//        test.get().info("Step 5: update start date for customer");
//        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus20Days());
//
//        test.get().info("Step 6: load user in to hub net");
//        CareTestBase.page().loadCustomerInHubNet(customerNumber);
//
//        test.get().info("Step 7: Select change Tariff from RHS actions");
//        MenuPage.RightMenuPage.getInstance().clickChangeTariffLink();
//
//        test.get().info("Step 8: Select subscription number from drop down");
//        ServiceOrdersPage.ChangeBundle.getInstance().clickNextButton();
//
//        test.get().info("Step 5: open the search tariff window");
//        String title = ServiceOrdersContentPage.getInstance().getTitle();
//        ServiceOrdersContentPage.getInstance().clicknewTariffSearchBtn();
//        ServiceOrdersContentPage.getInstance().switchWindow("Tariff Search", false);
//
//        test.get().info("Step 6: Search tariff by specified criteria");
//        TariffSearchCriteriaEnity tariffSearchCriteriaEnity = new TariffSearchCriteriaEnity();
//        tariffSearchCriteriaEnity.setBillingType("Flexible Cap");
//        tariffSearchCriteriaEnity.setStaffTariff("No");
//        tariffSearchCriteriaEnity.setMonthlyRental("10-20");
//        tariffSearchCriteriaEnity.setContractPeriod("12");
//        TariffSearchPage.getInstance().searchTariffByCriteria(tariffSearchCriteriaEnity);
//
//
//        test.get().info("Step 7: Select Tariff by code then click next button");
//        TariffSearchPage.getInstance().clickTariffByTariffCode("FC12-2000-750");
//        ServiceOrdersContentPage.getInstance().switchWindow(title, false);
//
//        test.get().info("Step 8: click next button on change tariff wizad");
//        ServiceOrdersContentPage.getInstance().clickNextBtn();
//
//
//        test.get().info("Step 9: select specified bundles on change bundle screen then click then next button");
//        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName("£10 safety buffer");
//        ServiceOrdersContentPage.getInstance().clickNextBtn();
//
//        test.get().info("Step 10:Select recuring bundle to add immdediately");
//        ServiceOrdersContentPage.getInstance().clickNextBtn();
//        ServiceOrdersContentPage.getInstance().clickReturnToCustomer();
//
//
//        test.get().info("Step 10: verify change tariff so created");
//        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
//        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
//        String serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Provision Wait");
//        HashMap<String, String> serviceOrderEnity = ServiceOrderEntity.dataServiceOrder(sub, "Change Tariff", "Provision Wait");
//        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(serviceOrderEnity), 1);
//
//        test.get().info("Step 10: prepare CDR file with sample TM DRAS CDR file and wait for load CDR file compplete");
//        prepareTopUpFileFor2SubscriptionsThenUploadToServer();

        test.get().info("Step 10: Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage("un629105260@hsntech.com", "password1", "11330");

        test.get().info("Step 10: click view my usage details since my last bill link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewMyUsageDetailsSinceMyLastBillLink();

        test.get().info("Step 10: verify usage detail");
        verifyUsageDetail();
    }

    private void verifyUsageDetail() {
        UsageDetailsSinceMyLastBillPage.getInstance().selectSubscriptionForUsage(sub + " Mobile Ref 1");
        UsageDetailsSinceMyLastBillPage.getInstance().clickUsageViewBtn();
        UsageDetailsSinceMyLastBillPage.getInstance().clickMonthlyChargeExpandBtn();
        HashMap<String, String> expectedEnity = UsageDetailsEnity.getMonthlyChargesEnity("CURRENT", "£10 Tariff 12 Month Contract", TimeStamp.TodayMinus20Days(), TimeStamp.TodayPlus1MonthMinus1Day(), "£10.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Monthly Charges", expectedEnity), 1);
        expectedEnity = UsageDetailsEnity.getMonthlyChargesEnity("PENDING", "£20 Tariff 12 Month Contract", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "£20.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Monthly Charges", expectedEnity), 1);

        UsageDetailsSinceMyLastBillPage.getInstance().clickBundleChargeExpandBtn();
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(),  String.format("£10 safety buffer for %s Mobile Ref 1", sub), "£10.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayMinus20Days(), TimeStamp.TodayPlus1MonthMinus1Day(),  String.format("£20 safety buffer for %s", sub), "£0.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);

        UsageDetailsSinceMyLastBillPage.getInstance().clickUsageChargeExpandBtn();
        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("Data","4184","£0.00", "£0.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("Free Calls","18","£0.00", "£0.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("Non-standard Calls", "725", "£413.00", "£413.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("Roaming in the EU", "38", "£0.00", "£0.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("SMS", "50", "£0.00", "£0.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("UK Calls", "369", "£0.00", "£0.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("Video Calls", "7762", "£0.00", "£0.00");
        Assert.assertEquals(UsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);


        UsageDetailsSinceMyLastBillPage.getInstance().clickAdjustmentChargesAndCreditsChargeExpandBtn();
        UsageDetailsSinceMyLastBillPage.getInstance().clickSubscriptionPaymentsExpandBtn();



        String key = "* Call or text to a favourite number\n^ Call or text outside of monthly allowance\n+ Call charged from your Family Perk\n**" +
                " The total cost of this call is our access charge (25p a minute with a minimum charge of 25p) + the service charge (the amount the" +
                " company you called charged for the call)";
        Assert.assertEquals(key,UsageDetailsSinceMyLastBillPage.getInstance().getKey());

    }

    public void prepareTopUpFileFor2SubscriptionsThenUploadToServer() {
        String path = "src\\test\\resources\\txt\\selfcare\\viewusage\\TM_DRAS_CDR_20150520112842";
        String topupsTemplate = Common.readFile(path);
        String random = TimeStamp.TodayMinus1HourReturnFullFormat();
        String fileName = "TM_DRAS_CDR_" + random + ".txt";
        topupsTemplate = topupsTemplate
                .replace("201520112842", random)
                .replace("07925796290", sub)
                .replace("11/11/2015", Parser.parseDateFormate(TimeStamp.TodayMinusDayAndMonth(8, 0), TimeStamp.DATE_FORMAT_IN_PDF));
        String remotePath = Config.getProp("CDRSFTPFolder");
        String localPath = Common.getFolderLogFilePath() + fileName;
        Common.writeFile(topupsTemplate, localPath);
        SFTPHelper.getInstance().upFileFromLocalToRemoteServer(localPath, remotePath);
        RemoteJobHelper.getInstance().waitLoadCDRJobComplete();
    }


}
