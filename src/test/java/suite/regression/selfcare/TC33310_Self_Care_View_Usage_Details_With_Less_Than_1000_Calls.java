package suite.regression.selfcare;

import framework.utils.Log;
import logic.business.db.billing.CommonActions;
import logic.business.helper.FTPHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;

import java.sql.Date;

public class TC33310_Self_Care_View_Usage_Details_With_Less_Than_1000_Calls extends BaseTest {

    String serviceRefOf1stSubscription;
    String serviceRefOf2ndSubscription;
    String serviceRefOf3rdSubscription;
    int remoteJobId;


    @Test(enabled = true, description = "TC33310 Self Care View Usage Details with less than 1000 calls", groups = "SelfCare")
    public void TC33310_Self_Care_View_Usage_Details_with_less_than_1000_calls() {
        test.get().info("Step 1 : Create a CC customer with 3 subscriptions");
        String path = "src\\test\\resources\\xml\\selfcare\\viewaccount\\TC65_create_order.xml";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        String customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 4 : Update Customer Start Date");
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 5 : Record MPN of 3 subscriptions");
        owsActions.getOrder(owsActions.orderIdNo);
        serviceRefOf1stSubscription = owsActions.getOrderMpnByReference(1);
        serviceRefOf2ndSubscription = owsActions.getOrderMpnByReference(2);
        serviceRefOf3rdSubscription = owsActions.getOrderMpnByReference(3);

        test.get().info("Step 6 : Generate CDR file from template then upload to server");
        generateCDRFileFromTemplateThenUploadToServer();

    }

    private void generateCDRFileFromTemplateThenUploadToServer() {
        String cdrTemplate = Common.readFile("src\\test\\resources\\xml\\selfcare\\viewaccount\\TM_DRAS_CDR_20150105140001.txt");
        String fileName = Parser.parseDateFormate(TimeStamp.TodayMinus1Hour(), "yyyyMMddHHmm") + Common.getRandomNumber(10, 59);
        cdrTemplate = cdrTemplate
                .replace("20150105140001", fileName)
                .replace("FC-MPN", serviceRefOf1stSubscription)
                .replace("BC-MPN", serviceRefOf2ndSubscription)
                .replace("NC-MPN", serviceRefOf3rdSubscription)
                .replace("01/01/2015", Parser.parseDateFormate(TimeStamp.TodayMinus5Days(), TimeStamp.DATE_FORMAT_IN_PDF));

        String cdrFile = Common.getFolderLogFilePath() + "TM_DRAS_CDR_" + fileName + ".txt";
        Common.writeFile(cdrTemplate, cdrFile);
        Log.info("CDR file:" + cdrFile);
        remoteJobId = RemoteJobHelper.getInstance().getMaxRemoteJobId();
        FTPHelper.getInstance().upLoadFromDisk(cdrFile, "TM_DRAS_CDR_" + fileName + ".txt");
    }
}
