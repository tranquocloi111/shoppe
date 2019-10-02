package suite.regression.selfcare.viewsubscription;
import framework.config.Config;
import framework.utils.Log;
import framework.utils.RandomCharacter;
import logic.business.db.billing.CommonActions;
import logic.business.entities.CreditAgreementPaymentsEntiy;
import logic.business.helper.FTPHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CreditAgreementsContentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class TC33958_CCA_Annual_Statement_PDF_Document extends BaseTest {
    String customerNumber ;
    String agreementNO;

    @Test(enabled = true, description = "TC33958 CCA annual statement PDF document", groups = "SelfCare")
    public void TC33958_CCA_Annual_Statement_PDF_Document() {
        test.get().info("Step 6:run credit agreement annual statement job");
        RemoteJobHelper.getInstance().runCreditAgreementAnnualStatement();
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
        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinusDayAndMonth(5,14));

        test.get().info("Step 6: load user in to hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 6:run credit agreement annual statement job");
        RemoteJobHelper.getInstance().runCreditAgreementAnnualStatement();

        test.get().info("Step 7: verify agreement is created");
        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();

        HashMap<String, String> creditAgreement = CreditAgreementPaymentsEntiy.getCreditAgreement(sub, "Credit Agreement", TimeStamp.TodayMinusDayAndMonth(5,14));
        Assert.assertEquals(CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCreditAgreement(creditAgreement).size(), 1);
        CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().clickExpandButtonOfCABySubscription(sub);
        CreditAgreementsContentPage.CreditAgreementsGridPage.CADetailClass agreementNo= CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(sub);
        agreementNO = agreementNo.agreementNumber();

        test.get().info("Step 8: login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        verifyStatementToDatePDF();
        verifyPDFCacheFileIsCreated(customerNumber);
    }

    private void verifyStatementToDatePDF() {
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile 1").setCreditAgreementSelectByVisibleText("Your annual statement");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile 1").clickViewAgreementButton(0);
        String fileName = String.format("%s_%s_%s_mobile1.pdf", "33958", RandomCharacter.getRandomNumericString(9), customerNumber);
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile 1").savePDFFile(fileName);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);
        Assert.assertEquals("Statement of account for fixed sum credit", pdfList.get(0));
        Assert.assertEquals("(Section 77B of the Customer Credit Act 1974)", pdfList.get(1));
        Assert.assertEquals("Agreement Number: " + agreementNO, pdfList.get(2));
        Assert.assertEquals("Start date " + Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT_IN_PDF), pdfList.get(4));
        Assert.assertEquals("Term in months 24", pdfList.get(5));
        Assert.assertEquals("APR 0", pdfList.get(6));
        Assert.assertEquals("Total paid to date £16.00", pdfList.get(7));
        Assert.assertEquals("Balance including arrears £368.00", pdfList.get(8));
        Assert.assertEquals("Arrears including charges £0.00", pdfList.get(9));
        Assert.assertEquals("Instalments payable", pdfList.get(10));
        Assert.assertEquals("Repayment Date Amount Due Capital Interest", pdfList.get(11));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus10Days(), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(12));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 1), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(13));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 2), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(14));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 3), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(15));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 4), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(16));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 5), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(17));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 6), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(18));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 7), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(19));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 8), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(20));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 9), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(21));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 10), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(22));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 11), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(23));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 12), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(24));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 13), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(25));

        Assert.assertEquals("Page 1", pdfList.get(26));
        Assert.assertEquals("Tesco Mobile Limited is authorised and regulated by the Financial Conduct Authority; No. 723698.  Registered in England No.", pdfList.get(27).trim());
        Assert.assertEquals("4780736.  Registered Office: Tesco House, Shire Park, Kestrel Way, Welwyn Garden City, AL7 1GA. VAT No. 815384524.", pdfList.get(28).trim());

        Assert.assertEquals("Repayment Date Amount Due Capital Interest", pdfList.get(29));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 14), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(30));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 15), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(31));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 16), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(32));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 17), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(33));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 18), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(34));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 19), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(35));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 20), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(36));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 21), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(37));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth(10, 22), TimeStamp.DATE_FORMAT_IN_PDF) + " £16.00 £16.00 £0.00", pdfList.get(38));
        Assert.assertEquals("Total £368.00 £368.00 £0.00", pdfList.get(39));


        Assert.assertEquals("Page 2", pdfList.get(40));
        Assert.assertEquals("Tesco Mobile Limited is authorised and regulated by the Financial Conduct Authority; No. 723698.  Registered in England No.", pdfList.get(41).trim());
        Assert.assertEquals("4780736.  Registered Office: Tesco House, Shire Park, Kestrel Way, Welwyn Garden City, AL7 1GA. VAT No. 815384524.", pdfList.get(42).trim());

    }

    private void verifyPDFCacheFileIsCreated(String customerNumber) {

        String ftpFile = Config.getProp("cdrFolder").replace("Feed/a2aInterface/fileinbox", "Agreements/" + SFTPHelper.getInstance().generateDownLoadFile(customerNumber)+"/");
        String localFile = Common.getFolderLogFilePath()+ "PDF_document_" + Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT5) + RandomCharacter.getRandomNumericString(9);
        new File(localFile).mkdirs();
        List<String> allFileName= FTPHelper.getInstance().getAllFileName(ftpFile);
        FTPHelper.getInstance().downLoadFromDisk(ftpFile,allFileName.get(0),localFile );
        Log.info("pdf document cache file was downloaded to:" + localFile);

        File file=new File(localFile);
        Assert.assertEquals(file.list().length,1);
        File[] listOfFiles = file.listFiles();
        Assert.assertTrue(listOfFiles[0].getName().contains("CCD_STATEMENT.cache"));

    }


}
