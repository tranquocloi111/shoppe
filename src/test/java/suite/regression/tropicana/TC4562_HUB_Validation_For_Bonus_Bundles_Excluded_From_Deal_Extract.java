package suite.regression.tropicana;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.Xml;
import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.helper.FTPHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.BasePage;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.utils.Common;
import logic.utils.TimeStamp;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Date;
import java.util.List;

public class TC4562_HUB_Validation_For_Bonus_Bundles_Excluded_From_Deal_Extract extends BaseTest {
    String customerNumber;
    Date newStartDate;
    String subscription1;

    @Test(enabled = true, description = "TC 4562 HUB - Validation for Bonus bundles excluded from Deal Extract", groups = "Tropicana")
    public void TC4562_HUB_Validation_For_Bonus_Bundles_Excluded_From_Deal_Extract() {
        test.get().info("Step 1 : Create a customer with NC and device");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlinesCCCustomerWith2FCFamilyPerkAndNK2720();

        test.get().info("Step 2 : Create New Billing Group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3 : Update Bill Group Payment Collection Date To 10 Days Later");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update Customer Start Date");
        newStartDate = TimeStamp.TodayMinus15Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Get Subscription Number");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscription1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("FC Mobile 1");

        test.get().info("Step 7 : Submit DoDealXMLExtract and DealCatalogueExtract Job");
        RemoteJobHelper.getInstance().runDoDealXMLExtractJob();
        RemoteJobHelper.getInstance().runDealCatalogueExtractJob();

        test.get().info("Step 8 : Open extracted XML file and validate the existence of  bundles under Permitted Bundle Group. ");
        VerifyDealXMLExtractFile(false, "productCode=\"bonus\"");
        VerifyDealXMLExtractFile(true, "productCode=\"NC24-4500-3000-IP-S\"");
        VerifyDealCatalogueExtractFile(false, "productCode=\"bonus\"");
        VerifyDealCatalogueExtractFile(true, "productCode=\"NC24-4500-3000-IP-S\"");

        test.get().info("Step 9 : Add Bonus Bundle to Subscription");
        SWSActions swsActions = new SWSActions();
        String path = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC4682_request.xml";
        swsActions.submitMaintainBundleRequest(path, customerNumber, subscription1);

        test.get().info("Step 10 : Submit Provision job");
        BaseTest.updateThePDateAndBillDateForSO("");
        RemoteJobHelper.getInstance().runProvisionSevicesJob();

        test.get().info("Step 7 : Submit DoDealXMLExtract and DealCatalogueExtract Job");
        RemoteJobHelper.getInstance().runDoDealXMLExtractJob();
        RemoteJobHelper.getInstance().runDealCatalogueExtractJob();

        test.get().info("Step 12 : Open extracted XML file and validate the existence of  bundles under Permitted Bundle Group.");
        VerifyDealXMLExtractFile(false, "productCode=\"bonus\"");
        VerifyDealXMLExtractFile(true, "productCode=\"NC24-4500-3000-IP-S\"");
        VerifyDealCatalogueExtractFile(false, "productCode=\"bonus\"");
        VerifyDealCatalogueExtractFile(true, "productCode=\"NC24-4500-3000-IP-S\"");
    }

    private String getXmlFile(int jobId, String jobDescr) {
        String sql = "select text from remoteoutput where jobid=" + jobId;
        try {
            String text = String.valueOf(OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "text"));
            List<String> lines = IOUtils.readLines(new StringReader(text));

            String ftpFile = Config.getProp("cdrFolder").replace("Feed/a2aInterface/fileinbox", "ftp/insight/fileoutbox");
            String localFile = Common.getFolderLogFilePath();
            BaseTest.downLoadFile(ftpFile, "", localFile);
            Log.info("TM_HUB_DEAL_Onlines.zip file:" + localFile);

            String unzipFile = localFile;
            return Common.unzip(localFile, unzipFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getCSVFile(int jobId, String jobDescr) {
        String sql = "select text from remoteoutput where jobid=" + jobId;
        try {
            String text = String.valueOf(OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "text"));
            List<String> lines = IOUtils.readLines(new StringReader(text));

            String ftpFile = Config.getProp("cdrFolder").replace("Feed/a2aInterface/fileinbox", "ftp/insight/fileoutbox");
            String localFile = Common.getFolderLogFilePath();
            BaseTest.downLoadFile(ftpFile, "", localFile);
            Log.info("TM_HUB_DEAL_Onlines.zip file:" + localFile);

            String unzipFile = localFile;
            return Common.unzip(localFile, unzipFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void VerifyDealXMLExtractFile(boolean isExist, String... bundleCode) {
        int remoteJobId = RemoteJobHelper.remoteJobId;
        String xmlFile = getXmlFile(remoteJobId, "Deal XML Extract");
        Xml xml = new Xml(new File(xmlFile));
        if (isExist) {
            for (int i = 0; i < bundleCode.length; i++) {
                Assert.assertTrue(xml.toString().contains(bundleCode[i]));
            }
        } else {
            for (int i = 0; i < bundleCode.length; i++) {
                Assert.assertFalse(xml.toString().contains(bundleCode[i]));
            }
        }
    }

    private void VerifyDealCatalogueExtractFile(boolean isExist, String... bundleCode) {
        int remoteJobId = RemoteJobHelper.remoteJobId;
        String xmlFile = getCSVFile(remoteJobId, "Deal XML Extract");
        Xml xml = new Xml(new File(xmlFile));
        if (isExist) {
            for (int i = 0; i < bundleCode.length; i++) {
                Assert.assertTrue(xml.toString().contains(bundleCode[i]));
            }
        } else {
            for (int i = 0; i < bundleCode.length; i++) {
                Assert.assertFalse(xml.toString().contains(bundleCode[i]));
            }
        }


    }

}
