package suite.regression.selfcare.viewsubscription;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.RandomCharacter;
import logic.business.db.billing.CommonActions;
import logic.business.entities.CreditAgreementPaymentsEntiy;
import logic.business.entities.CreditAgreementsGridEntity;
import logic.business.helper.FTPHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CreditAgreementsContentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.apache.commons.net.ftp.FTPFile;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.util.HashMap;
import java.util.List;

public class TC33952_CCA_Statement_to_Date_PDF_Document extends BaseTest {
    String customerNumber ;
    String agreementNO;

    @Test(enabled = true, description = "TC33952 CCA statement to Date PDF document", groups = "SelfCare")
    public void TC33952_CCA_Statement_to_Date_PDF_Document() {
        test.get().info("Step 1: Create a CC customer");
        String path = "src\\test\\resources\\xml\\selfcare\\viewsubscription\\TC33952_createOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createOrderAndSignAgreementByUI(path, 1);
        String customerNumber =owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile 1");
        String sub =  owsActions.serviceRef;


        test.get().info("Step 2: create new billing group");
        createNewBillingGroup();
        test.get().info("Step 3: update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();
        test.get().info("Step 4: set bill group for customer");
        setBillGroupForCustomer(customerNumber);
        test.get().info("Step 5: update start date for customer");
        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus20Days());

        test.get().info("Step 6: load user in to hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);


        test.get().info("Step 7: load user in to hub net");
        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();

        HashMap<String, String> creditAgreement = CreditAgreementPaymentsEntiy.getCreditAgreement(sub, "Credit Agreement", TimeStamp.TodayMinus20Days());
        Assert.assertEquals(CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCreditAgreement(creditAgreement).size(), 1);
        CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().clickExpandButtonOfCABySubscription(sub);
        CreditAgreementsContentPage.CreditAgreementsGridPage.CADetailClass agreementNo= CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(sub);
        agreementNO = agreementNo.agreementNumber();

        test.get().info("Step 8: login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 9:  verify statement to date PDF");
        verifyStatementToDatePDF();

        test.get().info("Step 10:  verify PDF cache file is created");
        verifyPDFCacheFileIsCreated(customerNumber);
    }

    private void verifyStatementToDatePDF() {
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile 1").setCreditAgreementSelectByVisibleText("Your statement to date");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile 1").clickViewAgreementButton(0);
        String fileName = String.format("%s_%s_%s_mobile1.pdf", "33952", RandomCharacter.getRandomNumericString(9), customerNumber);
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile 1").savePDFFile(fileName);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);
        Assert.assertEquals("Agreement Number: " + agreementNO, pdfList.get(1));
        Assert.assertEquals("Start date " + Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF), pdfList.get(3));
        Assert.assertEquals("Term in months 24", pdfList.get(4));
        Assert.assertEquals("Total paid to date £16.00", pdfList.get(5));
        Assert.assertEquals("Balance including arrears £368.00", pdfList.get(6));
        Assert.assertEquals("Arrears including charges £0.00", pdfList.get(7));
        Assert.assertEquals("Repayment Date Account Repayment Balance", pdfList.get(8));
        Assert.assertEquals("Amount", pdfList.get(9));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF) + " Advance £384.00 £384.00", pdfList.get(10));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF) + " Payment -£16.00 £368.00", pdfList.get(11));
        Assert.assertEquals("Page 1", pdfList.get(12));
        Assert.assertEquals("Tesco Mobile Limited is authorised and regulated by the Financial Conduct Authority; No. 723698.  Registered in England No.", pdfList.get(13).trim());
        Assert.assertEquals("4780736.  Registered Office: Tesco House, Shire Park, Kestrel Way, Welwyn Garden City, AL7 1GA. VAT No. 815384524.", pdfList.get(14).trim());

    }

    private void verifyPDFCacheFileIsCreated(String customerNumber) {
        String ftpFile = Config.getProp("cdrFolder").replace("Feed/a2aInterface/fileinbox", "Agreements/" + SFTPHelper.getInstance().generateDownLoadFile(customerNumber)+"/");
        String localFile = Common.getFolderLogFilePath() + "PDF_document_" + Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT5) + RandomCharacter.getRandomNumericString(9);
        new File(localFile).mkdirs();
        List<String> allFileName= FTPHelper.getInstance().getAllFileName(ftpFile);
        FTPHelper.getInstance().downLoadFromDisk(ftpFile,allFileName.get(0),localFile );
        Log.info("pdf document cache file was downloaded to:" + localFile);

        File file=new File(localFile);
        Assert.assertEquals(file.list().length,1);
        File[] listOfFiles = file.listFiles();
        Assert.assertTrue(listOfFiles[0].getName().contains("ACCOUNT_STATEMENT.cache"));

    }


}
