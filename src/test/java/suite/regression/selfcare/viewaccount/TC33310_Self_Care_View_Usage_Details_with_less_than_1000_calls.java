package suite.regression.selfcare.viewaccount;

import framework.config.Config;
import framework.utils.Log;
import logic.business.db.billing.CommonActions;
import logic.business.entities.selfcare.MyBillAndPaymentEnity;
import logic.business.entities.selfcare.UsageDetailsEnity;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.selfcare.MyBillsAndPaymentsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.pages.selfcare.MyUsageDetailsSinceMyLastBillPage;
import logic.pages.selfcare.MyUsageSinceMyLastBillPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Time;
import java.util.HashMap;

public class TC33310_Self_Care_View_Usage_Details_with_less_than_1000_calls extends BaseTest {

    String serviceRefOf1stSubscription;
    String serviceRefOf2ndSubscription;
    String serviceRefOf3rdSubscription;
    String customerNumber = "9330";

    @Test(enabled = true, description = "TC33310 self care view usage details with less than 1000 calls", groups = "SelfCare")
    public void TC33310_Self_Care_View_Usage_Details_with_less_than_1000_calls() {
        test.get().info("Step 1: Create a  customer with 3 subscription");
        String path = "src\\test\\resources\\xml\\selfcare\\viewaccount\\TC65_create_order.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo, "FC Mobile 1");
        serviceRefOf1stSubscription = owsActions.serviceRef;
        serviceRefOf2ndSubscription = owsActions.getOrderMpnByReference(2);
        serviceRefOf3rdSubscription = owsActions.getOrderMpnByReference(3);

        test.get().info("Step 2: create new billing group");
        createNewBillingGroup();
        test.get().info("Step 3: update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();
        test.get().info("Step 4: set bill group for customer");
        setBillGroupForCustomer(customerNumber);
        test.get().info("Step 5: update start date for customer");
        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus10Days());

        test.get().info("Step 6: prepare CDR file with sample TM DRAS CDR file and wait for load CDR file compplete");
        prepareTopUpFileFor2SubscriptionsThenUploadToServer();

        test.get().info("Step 7: Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 8: click view my usage  since my last bill link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewMyUsageDetailsSinceMyLastBillLink();

        test.get().info("Step 9: view usage for FC mobile 1");
        MyUsageDetailsSinceMyLastBillPage.getInstance().selectSubscriptionForUsage(serviceRefOf1stSubscription + " FC Mobile 1");
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickUsageViewBtn();

        test.get().info("Step 10: verify FC mobile 1 usage data are correct");
        verifyFCMobile1UsageDetail();
        verifySearchCallDetailRecordsByUsageTypeFeature();

        test.get().info("Step 11: view usage for FC mobile 2");
        MyUsageDetailsSinceMyLastBillPage.getInstance().selectSubscriptionForUsage(serviceRefOf2ndSubscription + " FC Mobile 2");
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickUsageViewBtn();

        test.get().info("Step 12: verify FC mobile 2 usage data are correct");
        verifyFCMobile2UsageDetail();
        verifySearchCallDetailRecordsByUsageTypeFeature();

        test.get().info("Step 13: view usage for  Mobile NC 3");
        MyUsageDetailsSinceMyLastBillPage.getInstance().selectSubscriptionForUsage(serviceRefOf3rdSubscription + " NC Mobile 3");
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickUsageViewBtn();

        test.get().info("Step 14: verify usage for  Mobile NC 3");
        verifyNCMobile3UsageDetail();
        verifySearchCallDetailRecordsByUsageTypeFeature();

    }


    public void prepareTopUpFileFor2SubscriptionsThenUploadToServer() {
        String path = "src\\test\\resources\\txt\\selfcare\\viewaccount\\TM_DRAS_CDR_20150105140001.txt";
        String topupsTemplate = Common.readFile(path);
        String random = TimeStamp.TodayMinus1HourReturnFullFormat();
        String fileName = "TM_DRAS_CDR_" + random + ".txt";
        topupsTemplate = topupsTemplate
                .replace("20150105140001", random)
                .replace("FC-MPN", serviceRefOf1stSubscription)
                .replace("BC-MPN", serviceRefOf2ndSubscription)
                .replace("NC-MPN", serviceRefOf3rdSubscription)
                .replace("01/01/2015", Parser.parseDateFormate(TimeStamp.TodayMinusDayAndMonth(5, 0), TimeStamp.DATE_FORMAT_IN_PDF));
        String remotePath = Config.getProp("CDRSFTPFolder");
        String localPath = Common.getFolderLogFilePath() + fileName;
        Common.writeFile(topupsTemplate, localPath);
        SFTPHelper.getInstance().upFileFromLocalToRemoteServer(localPath, remotePath);
        RemoteJobHelper.getInstance().waitLoadCDRJobComplete();
    }


    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }

    private void verifyFCMobile2UsageDetail() {
        Assert.assertEquals("£20.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("FC Mobile 2"));

        MyUsageDetailsSinceMyLastBillPage.getInstance().clickMonthlyChargeExpandBtn();
        Assert.assertEquals("£20.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Monthly Charge"));
        HashMap<String, String> expectedEnity = UsageDetailsEnity.getMonthlyChargesEnity("CURRENT", "£10 Tariff 12 Month Contract", TimeStamp.TodayMinus10Days(), TimeStamp.TodayPlus1MonthMinus1Day(), "£10.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Monthly Charges", expectedEnity), 1);
        expectedEnity = UsageDetailsEnity.getMonthlyChargesEnity("CURRENT", "£10 Tariff 12 Month Contract", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "£10.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Monthly Charges", expectedEnity), 1);

        Assert.assertEquals("£0.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Bundle Charge"));
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickBundleChargeExpandBtn();
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayMinus10Days(), TimeStamp.TodayPlus1MonthMinus1Day(), String.format("£20 safety buffer for %s  ", serviceRefOf2ndSubscription), "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), String.format("£20 safety buffer for %s  ", serviceRefOf2ndSubscription), "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayMinus10Days(), TimeStamp.TodayPlus1MonthMinus1Day(), String.format("Family perk - 150 Mins per month for  %s", serviceRefOf2ndSubscription), "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), String.format("Family perk - 150 Mins per month for  %s", serviceRefOf2ndSubscription), "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);

        Assert.assertEquals("£0.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Usage Charges"));
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickUsageChargeExpandBtn();
        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("Data", "76800", "£0.00", "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("SMS", "5", "£0.00", "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("UK Calls", "3000", "£0.00", "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("MMS", "5", "£0.00", "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        Assert.assertEquals("£0.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Adjustments, Charges and Credits"));
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickAdjustmentChargesAndCreditsChargeExpandBtn();
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayMinus10Days(), "Nokia 2720", "£0.00");
        Assert.assertEquals(1, MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Adjustments, Charges and Credits", expectedEnity));

        Assert.assertEquals("£0.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Subscription Payments"));
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickSubscriptionPaymentsExpandBtn();
        Assert.assertEquals("No data available for Subscription Payments", MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDownByIndex("Subscription Payments", 0).getText());

        Assert.assertEquals("5 items found, displaying all items.", MyUsageDetailsSinceMyLastBillPage.getInstance().getToTalCountText());
        Assert.assertEquals("1", MyUsageDetailsSinceMyLastBillPage.getInstance().getPageLinks());
        Assert.assertEquals(5, MyUsageDetailsSinceMyLastBillPage.getInstance().getCountRowInCallDetailTable());

    }

    private void verifyFCMobile1UsageDetail() {

        Assert.assertEquals("£41.75", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("FC Mobile 1"));
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickMonthlyChargeExpandBtn();
        Assert.assertEquals("£20.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Monthly Charges"));
        HashMap<String, String> expectedEnity = UsageDetailsEnity.getMonthlyChargesEnity("CURRENT", "£10 Tariff 12 Month Contract", TimeStamp.TodayMinus10Days(), TimeStamp.TodayPlus1MonthMinus1Day(), "£10.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Monthly Charges", expectedEnity), 1);
        expectedEnity = UsageDetailsEnity.getMonthlyChargesEnity("CURRENT", "£10 Tariff 12 Month Contract", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "£10.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Monthly Charges", expectedEnity), 1);

        MyUsageDetailsSinceMyLastBillPage.getInstance().clickBundleChargeExpandBtn();
        Assert.assertEquals("£0.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Bundle Charges"));
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayMinus10Days(), TimeStamp.TodayPlus1MonthMinus1Day(), String.format("£20 safety buffer for %s ", serviceRefOf1stSubscription), "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), String.format("£20 safety buffer for %s  ", serviceRefOf1stSubscription), "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayMinus10Days(), TimeStamp.TodayPlus1MonthMinus1Day(), String.format("Family perk - 150 Mins per month for  %s", serviceRefOf1stSubscription), "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), String.format("Family perk - 150 Mins per month for  %s", serviceRefOf1stSubscription), "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);

        MyUsageDetailsSinceMyLastBillPage.getInstance().clickUsageChargeExpandBtn();
        Assert.assertEquals("£21.75", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Usage Charges"));
        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("Data", "76800", "£7.50", "£7.50");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("SMS", "5", "£0.50", "£0.50");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("UK Calls", "3000", "£12.50", "£12.50");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("MMS", "5", "£1.25", "£1.25");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        Assert.assertEquals("£0.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Adjustments, Charges and Credits"));
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickAdjustmentChargesAndCreditsChargeExpandBtn();
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayMinus10Days(), "Nokia 2720", "£0.00");
        Assert.assertEquals(1, MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Adjustments, Charges and Credits", expectedEnity));
        Assert.assertEquals("£0.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Subscription Payments"));
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickSubscriptionPaymentsExpandBtn();
        Assert.assertEquals("No data available for Subscription Payments", MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDownByIndex("Subscription Payments", 0).getText());


        Assert.assertEquals("20 items found, displaying all items.", MyUsageDetailsSinceMyLastBillPage.getInstance().getToTalCountText());
        Assert.assertEquals("1", MyUsageDetailsSinceMyLastBillPage.getInstance().getPageLinks());
        Assert.assertEquals(20, MyUsageDetailsSinceMyLastBillPage.getInstance().getCountRowInCallDetailTable());

    }

    private void verifyNCMobile3UsageDetail() {
        Assert.assertEquals("£35.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("NC Mobile 3"));
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickMonthlyChargeExpandBtn();
        Assert.assertEquals("£20.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Monthly Charges"));
        HashMap<String, String> expectedEnity = UsageDetailsEnity.getMonthlyChargesEnity("CURRENT", "£10 SIM Only Tariff", TimeStamp.TodayMinus10Days(), TimeStamp.TodayPlus1MonthMinus1Day(), "£10.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Monthly Charges", expectedEnity), 1);
        expectedEnity = UsageDetailsEnity.getMonthlyChargesEnity("CURRENT", "£10 SIM Only Tariff", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), "£10.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Monthly Charges", expectedEnity), 1);

        MyUsageDetailsSinceMyLastBillPage.getInstance().clickBundleChargeExpandBtn();
        Assert.assertEquals("£15.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Bundle Charges"));
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayMinus10Days(), TimeStamp.TodayPlus1MonthMinus1Day(), String.format("Monthly data bundle - 1GB (Capped) for   %s", serviceRefOf3rdSubscription), "£7.50");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), String.format("Monthly data bundle - 1GB (Capped) for   %s", serviceRefOf3rdSubscription), "£7.50");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Bundle Charges", expectedEnity), 1);

        MyUsageDetailsSinceMyLastBillPage.getInstance().clickUsageChargeExpandBtn();
        Assert.assertEquals("£0.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Usage Charges"));
        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("Data", "76800", "£5.00", "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("SMS", "5", "£0.00", "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("UK Calls", "3000", "£6.25", "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        expectedEnity = UsageDetailsEnity.getUsageChargesEnity("MMS", "5", "£1.25", "£0.00");
        Assert.assertEquals(MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Usage Charges", expectedEnity), 1);

        Assert.assertEquals("£0.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Adjustments, Charges and Credits"));
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickAdjustmentChargesAndCreditsChargeExpandBtn();
        expectedEnity = UsageDetailsEnity.getBundleChargesEnity(TimeStamp.TodayMinus10Days(), "Nokia 2720", "£0.00");
        Assert.assertEquals(1, MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDown("Adjustments, Charges and Credits", expectedEnity));

        Assert.assertEquals("£0.00", MyUsageDetailsSinceMyLastBillPage.getInstance().getTotalInHeader("Subscription Payments"));
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickSubscriptionPaymentsExpandBtn();
        Assert.assertEquals("No data available for Subscription Payments", MyUsageDetailsSinceMyLastBillPage.getInstance().getRowInDropDownByIndex("Subscription Payments", 0).getText());

        MyUsageDetailsSinceMyLastBillPage.getInstance().selectUsageType("All");
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickSearchButton();
        Assert.assertEquals("20 items found, displaying all items.", MyUsageDetailsSinceMyLastBillPage.getInstance().getToTalCountText());
        Assert.assertEquals("1", MyUsageDetailsSinceMyLastBillPage.getInstance().getPageLinks());
        Assert.assertEquals(20, MyUsageDetailsSinceMyLastBillPage.getInstance().getCountRowInCallDetailTable());
    }

    private void verify5CDRSLoadedInGrid(String callType) {
        MyUsageDetailsSinceMyLastBillPage.getInstance().selectUsageType(callType);
        MyUsageDetailsSinceMyLastBillPage.getInstance().clickSearchButton();
        Assert.assertEquals("5 items found, displaying all items.", MyUsageDetailsSinceMyLastBillPage.getInstance().getToTalCountText());
        Assert.assertEquals("1", MyUsageDetailsSinceMyLastBillPage.getInstance().getPageLinks());
        Assert.assertEquals(5, MyUsageDetailsSinceMyLastBillPage.getInstance().getCountRowInCallDetailTable());
        for (String text : MyUsageDetailsSinceMyLastBillPage.getInstance().getAllStringColumnInGrid(callType)) {
            Assert.assertEquals(5, MyUsageDetailsSinceMyLastBillPage.getInstance().getAllStringColumnInGrid(callType).size());
            Assert.assertEquals(callType, text);
        }

    }

    private void verifySearchCallDetailRecordsByUsageTypeFeature() {
        verify5CDRSLoadedInGrid("Data");

        verify5CDRSLoadedInGrid("SMS");

        verify5CDRSLoadedInGrid("MMS");

        verify5CDRSLoadedInGrid("UK Calls");
    }
}

