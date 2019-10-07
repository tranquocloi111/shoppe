package suite.regression.selfcare.viewaccount;

import framework.config.Config;
import framework.utils.Log;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.find.UnbilledSumaryPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.pages.selfcare.MyUsageSinceMyLastBillPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.selfcare.SelfCareTestBase;

public class TC35529_Self_Care_View_Usage_Details_with_less_than_1000_calls extends BaseTest {

    String serviceRefOf1stSubscription;
    String serviceRefOf2ndSubscription;
    String serviceRefOf3rdSubscription;
    String customerNumber;

    @Test(enabled = true, description = "TC35529 selfcare view usage details with less than 1000 calls", groups = "SelfCare")
    public void TC35529_Self_Care_View_Usage_Details_with_less_than_1000_calls() {
        test.get().info("Step 1: Create a  customer with 3 subscription");
        String path = "src\\test\\resources\\xml\\selfcare\\viewaccount\\TC65_create_order.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo, "FC Mobile 1");
        serviceRefOf1stSubscription = owsActions.serviceRef;
        serviceRefOf2ndSubscription = owsActions.getOrderMpnByReference("FC Mobile 2");
        serviceRefOf3rdSubscription = owsActions.getOrderMpnByReference("NC Mobile 3");

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
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewMyUsageSinceMyLastBillLink();

        test.get().info("Step 9: view usage for FC mobile 1");
        MyUsageSinceMyLastBillPage.getInstance().setSubscriptionSelect(serviceRefOf1stSubscription + " FC Mobile 1");

        test.get().info("Step 10: verify an image was displayed for FC1");
        verifyAnImageWasDisplayedForFC1();

        test.get().info("Step 11: view usage for FC mobile 2");
        MyUsageSinceMyLastBillPage.getInstance().setSubscriptionSelect(serviceRefOf1stSubscription + " FC Mobile 2");

        test.get().info("Step 12: verify an image was displayed for FC2");
        verifyAnImageWasDisplayedForFC2();

        test.get().info("Step 13: view usage for FC mobile 1");
        MyUsageSinceMyLastBillPage.getInstance().setSubscriptionSelect(serviceRefOf1stSubscription + " NC Mobile 3");

        test.get().info("Step 14: verify an image was displayed for NC");
        verifyAnImageWasDisplayedForNC();
    }

    public void verifyAnImageWasDisplayedForFC1() {
        String imgFile = String.format("UsageImg_%s_%s_HubNet.jpg",TimeStamp.getMillisecond(), customerNumber);
        MyUsageSinceMyLastBillPage.getInstance().saveFileFromWebRequest(imgFile);
        Log.info("Usage image of FC 1 from SelfCare saved to:" + Common.getFolderLogFilePath() + imgFile);
    }
    public void verifyAnImageWasDisplayedForFC2() {
        String imgFile = String.format("UsageImg_%s_%s_HubNet.jpg",TimeStamp.getMillisecond(), customerNumber);
        MyUsageSinceMyLastBillPage.getInstance().saveFileFromWebRequest(imgFile);
        Log.info("Usage image of FC 2 from SelfCare saved to:" + Common.getFolderLogFilePath() + imgFile);
    }
    public void verifyAnImageWasDisplayedForNC() {
        String imgFile = String.format("UsageImg_%s_%s_HubNet.jpg",TimeStamp.getMillisecond(), customerNumber);
        MyUsageSinceMyLastBillPage.getInstance().saveFileFromWebRequest(imgFile);
        Log.info("Usage image of NC from SelfCare saved to:" + Common.getFolderLogFilePath() + imgFile);
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


}
