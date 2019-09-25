package suite.regression.selfcare.modifysubscription;

import framework.utils.RandomCharacter;
import logic.business.db.billing.CommonActions;
import logic.business.helper.MiscHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.agreement.AgreementWrapperPage;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CreditAgreementsContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.DeactivateSubscriptionPage;
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
import java.util.List;

public class TC31967_View_Credit_Agreement_Documents_CCA_Document_Statement_of_Account extends BaseTest {
    String customerNumber;
    String subno1;
    String subno2;
    String subno3;
    String subno4;
    String cCANo3;
    String cCANo1;
    String cCANo2;
    String cCANo4;

    @Test(enabled = true, description = "TC331967 view credit agreement documents CCA document statement of account", groups = "SelfCare")
    public void TC31967_View_Credit_Agreement_Documents_CCA_Document_Statement_of_Account() {
        test.get().info("Create a CC customer");
        String path = "src\\test\\resources\\xml\\selfcare\\modifysubscription\\TC31927_createOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createOrderAndSignAgreementByUI(path, 4);

        test.get().info("create new billing group");
        createNewBillingGroup();

        test.get().info("update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("set bill group for customer");
        customerNumber = owsActions.customerNo;
        setBillGroupForCustomer(customerNumber);

        test.get().info("update start date for customer");
        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus1MonthMinus20Day());

        test.get().info("get all FC subscription number and ccaNO");
        owsActions.getOrder(owsActions.orderIdNo);
        subno1 = owsActions.getOrderMpnByReference(1);
        subno2 = owsActions.getOrderMpnByReference(2);
        subno3 = owsActions.getOrderMpnByReference(3);
        subno4 = owsActions.getOrderMpnByReference(4);

        cCANo1 = owsActions.getCreditAgreementNumberByReference("Mobile FC 1");
        cCANo2 = owsActions.getCreditAgreementNumberByReference("Mobile FC 2");
        cCANo4 = owsActions.getCreditAgreementNumberByReference("Mobile FC 4");

        test.get().info("Upgrade FC 3 and accept upgrade");
        owsActions.upgradeFC3AndAcceptUpgrade(customerNumber, subno3);

        test.get().info("Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("get CCA  no for new upgraded credit agreement of mobile FC3");
        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();

        CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().clickExpandButtonOfCABySubscription(subno3);
        CreditAgreementsContentPage.CreditAgreementsGridPage.CADetailClass caDetailNo3 = CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(subno3);
        cCANo3 = caDetailNo3.agreementNumber();

        test.get().info("Deactivate FC 4 Subscription");
        deactiveFC4Subscription();

        test.get().info("Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("verify the statement to date pdf of FC1");
        verifyStatementToDatePDFOfFC1();

        test.get().info("verify the statement to date pdf of FC2");
        verifyStatementToDatePDFOfFC2();

        test.get().info("verify the statement to date pdf of FC3");
        verifyStatementToDatePDFOfFC3();

        test.get().info("verify the statement to date pdf of FC4");
        verifyStatementToDatePDFOfFC4();
    }

    public void deactiveFC4Subscription() {
        MenuPage.RightMenuPage.getInstance().clickDeactivateSubscriptionLink();
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().selectDeactiveBySubscription(subno4);
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().clickNextButton();

        ServiceOrdersPage.ReturnsAndEtcPage.getInstance().selectWaiveETCReasonByIndexAndValue(0, "Goodwill Gesture");
        ServiceOrdersPage.ReturnsAndEtcPage.getInstance().selectWaiveETCReasonByIndexAndValue(1, "Goodwill Gesture");
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().clickNextButton();

        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().clickNextButton();
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().clickReturnToCustomer();

    }

    private void verifyStatementToDatePDFOfFC1() {
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 1").setCreditAgreementSelectByVisibleText("Your statement to date");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 1").clickViewAgreementButton(0);
        String fileName = String.format("%s_%s_%s_mobile1.pdf", "31927", RandomCharacter.getRandomNumericString(9), customerNumber);
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 1").savePDFFile(fileName);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);

        Assert.assertEquals("Agreement Number: " + cCANo1, pdfList.get(1));
        Assert.assertEquals("Start date " + Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), TimeStamp.DATE_FORMAT_IN_PDF), pdfList.get(3));
        Assert.assertEquals("Term in months 24", pdfList.get(4));
        Assert.assertEquals("Total paid to date £16.00", pdfList.get(5));
        Assert.assertEquals("Balance including arrears £368.00", pdfList.get(6));
        Assert.assertEquals("Arrears including charges £0.00", pdfList.get(7));
        Assert.assertEquals("Repayment Date Account Repayment Balance", pdfList.get(8));
        Assert.assertEquals("Amount", pdfList.get(9));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), TimeStamp.DATE_FORMAT_IN_PDF) + " Advance £384.00 £384.00", pdfList.get(10));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), TimeStamp.DATE_FORMAT_IN_PDF) + " Payment -£16.00 £368.00", pdfList.get(11));
        Assert.assertEquals("Page 1", pdfList.get(12));
        Assert.assertEquals("Tesco Mobile Limited is authorised and regulated by the Financial Conduct Authority; No. 723698.  Registered in England No.", pdfList.get(13).trim());
        Assert.assertEquals("4780736.  Registered Office: Tesco House, Shire Park, Kestrel Way, Welwyn Garden City, AL7 1GA. VAT No. 815384524.", pdfList.get(14).trim());
    }

    private void verifyStatementToDatePDFOfFC2() {
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 2").setCreditAgreementSelectByVisibleText("Your statement to date");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 2").clickViewAgreementButton(1);
        String fileName = String.format("%s_%s_%s_mobile2.pdf", "31927", RandomCharacter.getRandomNumericString(9), customerNumber);
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 2").savePDFFile(fileName);

        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);
        Assert.assertEquals("Agreement Number: " + cCANo2, pdfList.get(1));
        Assert.assertEquals("Start date " + Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), TimeStamp.DATE_FORMAT_IN_PDF), pdfList.get(3));
        Assert.assertEquals("Term in months 1", pdfList.get(4));
        Assert.assertEquals("Total paid to date £16.00", pdfList.get(5));
        Assert.assertEquals("Balance including arrears £0.00", pdfList.get(6));
        Assert.assertEquals("Arrears including charges £0.00", pdfList.get(7));
        Assert.assertEquals("Repayment Date Account Repayment Balance", pdfList.get(8));
        Assert.assertEquals("Amount", pdfList.get(9));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), TimeStamp.DATE_FORMAT_IN_PDF) + " Advance £16.00 £16.00", pdfList.get(10));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), TimeStamp.DATE_FORMAT_IN_PDF) + " Payment -£16.00 £0.00", pdfList.get(11));
        Assert.assertEquals("Page 1", pdfList.get(12));
        Assert.assertEquals("Tesco Mobile Limited is authorised and regulated by the Financial Conduct Authority; No. 723698.  Registered in England No.", pdfList.get(13).trim());
        Assert.assertEquals("4780736.  Registered Office: Tesco House, Shire Park, Kestrel Way, Welwyn Garden City, AL7 1GA. VAT No. 815384524.", pdfList.get(14).trim());
    }

    private void verifyStatementToDatePDFOfFC3() {
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 3").setCreditAgreementSelectByVisibleText("Your statement to date");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 3").clickViewAgreementButton(2);
        String fileName = String.format("%s_%s_%s_mobile3.pdf", "31927", RandomCharacter.getRandomNumericString(9), customerNumber);
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 3").savePDFFile(fileName);

        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);
        Assert.assertEquals("Agreement Number: " + cCANo3, pdfList.get(1));
        Assert.assertEquals("Start date " + Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), pdfList.get(3));
        Assert.assertEquals("Term in months 24", pdfList.get(4));
        Assert.assertEquals("Total paid to date £0.00", pdfList.get(5));
        Assert.assertEquals("Balance including arrears £384.00", pdfList.get(6));
        Assert.assertEquals("Arrears including charges £0.00", pdfList.get(7));
        Assert.assertEquals("Repayment Date Account Repayment Balance", pdfList.get(8));
        Assert.assertEquals("Amount", pdfList.get(9));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF) + " Advance £384.00 £384.00", pdfList.get(10));
        Assert.assertEquals("Page 1", pdfList.get(11));
        Assert.assertEquals("Tesco Mobile Limited is authorised and regulated by the Financial Conduct Authority; No. 723698.  Registered in England No.", pdfList.get(12).trim());
        Assert.assertEquals("4780736.  Registered Office: Tesco House, Shire Park, Kestrel Way, Welwyn Garden City, AL7 1GA. VAT No. 815384524.", pdfList.get(13).trim());
    }

    private void verifyStatementToDatePDFOfFC4() {
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 3").setCreditAgreementSelectByVisibleTextForInactiveSubscription("Your statement to date", 3);
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 3").clickViewAgreementButton(3);
        String fileName = String.format("%s_%s_%s_mobile4.pdf", "31927", RandomCharacter.getRandomNumericString(9), customerNumber);
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 3").savePDFFile(fileName);

        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);
        Assert.assertEquals("Agreement Number: " + cCANo4, pdfList.get(1));
        Assert.assertEquals("Start date " + Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), TimeStamp.DATE_FORMAT_IN_PDF), pdfList.get(3));
        Assert.assertEquals("Term in months 24", pdfList.get(4));
        Assert.assertEquals("Total paid to date £16.00", pdfList.get(5));
        Assert.assertEquals("Balance including arrears £368.00", pdfList.get(6));
        Assert.assertEquals("Arrears including charges £0.00", pdfList.get(7));
        Assert.assertEquals("Repayment Date Account Repayment Balance", pdfList.get(8));
        Assert.assertEquals("Amount", pdfList.get(9));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), TimeStamp.DATE_FORMAT_IN_PDF) + " Advance £384.00 £384.00", pdfList.get(10));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayMinus1MonthMinus20Day(), TimeStamp.DATE_FORMAT_IN_PDF) + " Payment -£16.00 £368.00", pdfList.get(11));
        Assert.assertEquals("Page 1", pdfList.get(12));
        Assert.assertEquals("Tesco Mobile Limited is authorised and regulated by the Financial Conduct Authority; No. 723698.  Registered in England No.", pdfList.get(13).trim());
        Assert.assertEquals("4780736.  Registered Office: Tesco House, Shire Park, Kestrel Way, Welwyn Garden City, AL7 1GA. VAT No. 815384524.", pdfList.get(14).trim());
    }

}
