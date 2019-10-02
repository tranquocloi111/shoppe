package suite.regression.selfcare.viewaccount;

import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.helper.DateTimeHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.Date;

public class TC33340_Self_Care_An_alert_is_shown_when_there_is_an_Overdue_invoice_on_the_account extends BaseTest {


    @Test(enabled = true, description = "TC33340 self care an alert is shown when there is an overdue invoice on the account", groups = "SelfCare")
    public void TC33340_Self_Care_An_alert_is_shown_when_there_is_an_Overdue_invoice_on_the_account() {
        test.get().info("Step 1: Create a CC customer ");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 2: create new billing group");
        createNewBillingGroup();
        test.get().info("Step 3: update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();
        test.get().info("Step 4: set bill group for customer");
        setBillGroupForCustomer(owsActions.customerNo);
        test.get().info("Step 5: update start date for customer");
        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus20Days());
        test.get().info("Step 6: Load customer in hub net ");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 7: submit do refill bc Job");
        RemoteJobHelper.getInstance().submitDoRefillBcJob(TimeStamp.Today());
        test.get().info("Step 8: submit do refill NC Job");
        RemoteJobHelper.getInstance().submitDoRefillNcJob(TimeStamp.Today());
        test.get().info("Step 9: submit do  do bundle renew Job");
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(TimeStamp.Today());
        test.get().info("Step 10: submit do drap bill Job");
        RemoteJobHelper.getInstance().submitDraftBillRun();
        test.get().info("Step 12: submit do comfirm bill Job");
        RemoteJobHelper.getInstance().submitConfirmBillRun();
        test.get().info("Step 13: submit do payment allocation batch Job");
        RemoteJobHelper.getInstance().submitPaymentAllocationBatchJobRun();

        test.get().info("Step 15: get invoice due date escape work day");
        Date dueDate = super.paymentCollectionDateEscapeNonWorkDay(10);

        test.get().info("Step 16: verify invoice has been produced with correct dute date");
        verifyInvoiceHasBeenProducedWithCorrectDueDate(dueDate);

        test.get().info("Step 17: update invoice due date to 14 days before");
        BillingActions.updateInvoiceDueDate(customerNumber, TimeStamp.TodayMinus14Days());

        test.get().info("Step 18: login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 19: verify alert");
        String alert = "You currently have an overdue balance of  £10.00.  Click here to make payment.";
        String mssg = MyPersonalInformationPage.myAlertSection.getInstance().getAlertMessagebForOverDuePayment();
        Assert.assertEquals(alert,mssg);

    }

    private void verifyInvoiceHasBeenProducedWithCorrectDueDate(Date duteDate) {

        String duteD = DateTimeHelper.getInstance().changeformatDate(duteDate, TimeStamp.DATE_FORMAT);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);
        Assert.assertEquals(duteD, InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getDueDate());


    }

}
