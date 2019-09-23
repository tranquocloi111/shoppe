package suite.regression.selfcare.modifysubscription;

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
import logic.utils.TimeStamp;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.io.File;

public class TC31967_View_Credit_Agreement_Documents_CCA_Document_Statement_of_Account extends BaseTest {
    String customerNumber="8627";
    String subno1;
    String subno2;
    String subno3;
    String subno4;
    String cCANo3="0031909000010519";
    String cCANo1="0031909000010517";
    String cCANo2="0031909000010518";
    String cCANo4="0031909000010520";

    @Test(enabled = true, description = "TC331967 view credit agreement documents CCA document statement of account", groups = "SelfCare")
    public void TC31967_View_Credit_Agreement_Documents_CCA_Document_Statement_of_Account() {
        test.get().info("Create a CC customer");
        String path = "src\\test\\resources\\xml\\selfcare\\modifysubscription\\TC31927_createOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createOrderAndSignAgreementByUI(path, 4);

        customerNumber = owsActions.customerNo;

        test.get().info("create new billing group");
        createNewBillingGroup();
        test.get().info("update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();
        test.get().info("set bill group for customer");
        setBillGroupForCustomer(customerNumber);
        test.get().info("update start date for customer");
        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus1MonthMinus20Day());

        test.get().info("get all FC subscription number and ccaNO");
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile FC 1");
        subno1 = owsActions.serviceRef;
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile FC 2");
        subno2 = owsActions.serviceRef;
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile FC 3");
        subno3 = owsActions.serviceRef;
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile FC 4");
        subno4 = owsActions.serviceRef;

         cCANo1 = owsActions.getCreditAgreementNumberByReference("Mobile FC 1");
         cCANo2 = owsActions.getCreditAgreementNumberByReference("Mobile FC 2");
         cCANo4 = owsActions.getCreditAgreementNumberByReference("Mobile FC 4");

        test.get().info("Upgrade FC 3 and accept upgrade");
       owsActions.upgradeFC3AndAcceptUpgrade(customerNumber,subno3);

        test.get().info("Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("get CCA  no for new upgraded credit agreement of mobile FC3");
        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();

        CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().clickExpandButtonOfCABySubscription(subno3);
        CreditAgreementsContentPage.CreditAgreementsGridPage.CADetailClass caDetailNo3 = CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(subno3);
         cCANo3 = caDetailNo3.agreementNumber();

        test.get().info("deactive FC 4 Subscription");
        deactiveFC4Subscription();

        test.get().info("Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage("un292108730@hsntech.com","Password10",customerNumber);
        verifyStatementToDatePDFOfFC1();
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
    private void verifyStatementToDatePDFOfFC1()
    {
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 1").setCreditAgreementSelectByVisibleText("Your statement to date");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC 1").savePDFFile(cCANo1,"Your statement to date",customerNumber);
    }

}
