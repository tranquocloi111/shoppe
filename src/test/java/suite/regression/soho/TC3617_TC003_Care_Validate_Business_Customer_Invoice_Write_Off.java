package suite.regression.soho;

import framework.config.Config;
import framework.utils.RandomCharacter;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TC3617_TC003_Care_Validate_Business_Customer_Invoice_Write_Off extends BaseTest {
    private String customerNumber;
    private Date newStartDate;
    Date firstRunDate;
    String invoiceId;
    String subNo1;
    String subNo2;

    @Test(enabled = true, description = "TC3617_TC003_Care_Validate_Business_Customer_Invoice_Write_Off", groups = "SOHO")
    public void TC3617_TC003_Care_Validate_Business_Customer_Invoice_Write_Off() {
        test.get().info("Step 1 : Create a Customer with business type with CC method");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC3617_TC003_write_off_request.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Create new billing group");
        createNewBillingGroupToMinusMonth(2);

        test.get().info("Step 3 : Update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4 : Set bill group for customer");
        customerNumber = owsActions.customerNo;
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5 : Update start date for customer");
        firstRunDate = Date.valueOf(TimeStamp.Today().toLocalDate().minusMonths(2));
        newStartDate = Date.valueOf(TimeStamp.Today().toLocalDate().minusMonths(2).minusDays(20));
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Get Subscription Number");
        updateReadWriteAccessBusinessCustomers();
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subNo1 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 1");
        subNo2 = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue("Mobile Ref 2");

        test.get().info("Step 7 : Generate the cdr file then upload to server");
        generateCDRFileFromTemplateThenUploadToServer(subNo1);
        BaseTest.waitLoadCDRJobComplete();

        test.get().info("Step 7 : Run refill job");
        submitDoRefillBCJob();
        submitDoRefillNCJob();
        submitDoBundleRenewJob();

        test.get().info("Step 8 : Submit draft bill run");
        submitDraftBillRun();

        test.get().info("Step 9 : Submit confirm bill run");
        submitConfirmBillRun();

        test.get().info("Step 10 : Verify One Invoice Generated With Issue Date Of Today");
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        verifyOneInvoiceGeneratedWithIssueDateOfToday();

    }

    private void verifyOneInvoiceGeneratedWithIssueDateOfToday(){
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.InvoiceDetailsContentPage grid = InvoicesContentPage.InvoiceDetailsContentPage.getInstance();
        Assert.assertEquals(1, grid.getRowNumberOfInvoiceTable());
        invoiceId = grid.getInvoiceNumber();

        grid.clickInvoiceNumberByIndex(1);
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), grid.getIssued());
    }

    public void generateCDRFileFromTemplateThenUploadToServer(String subNo1){
        String filePath = "src\\test\\resources\\txt\\care\\TM_DRAS_CDR_20150106170007";
        String cdrFileString = Common.readFile(filePath);
        String file = Parser.parseDateFormate(TimeStamp.Today(),"yyyyMMddHHmm")+ RandomCharacter.getRandomNumericString(6);
        String fileName = Common.getFolderLogFilePath() + "TM_DRAS_CDR_" + file + ".txt";
        cdrFileString = cdrFileString.replace("20150106170007", file)
                .replace("07847469610",subNo1)
                .replace("04/01/2015",Parser.parseDateFormate(TimeStamp.TodayMinus2Days(), TimeStamp.DATE_FORMAT4));
        Common.writeFile(cdrFileString,fileName);
        String remotePath = Config.getProp("CDRSFTPFolder");
        SFTPHelper.getInstance().upFileFromLocalToRemoteServer(fileName , remotePath);
        String remoteFile = Config.getProp("cdrFolder").replace("Feed/a2aInterface/fileinbox","Feed/TMPP/"+"TM_DRAS_CDR_" + file + ".txt");
        RemoteJobHelper.getInstance().waitForLoadFile(remoteFile);
    }

}
