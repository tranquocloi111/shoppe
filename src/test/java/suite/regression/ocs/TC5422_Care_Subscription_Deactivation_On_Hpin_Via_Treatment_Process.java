package suite.regression.ocs;

import framework.utils.RandomCharacter;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.*;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class TC5422_Care_Subscription_Deactivation_On_Hpin_Via_Treatment_Process extends BaseTest {
    private String customerNumber = "47759541";
    private String orderId = "8701696";
    private String subNo1;
    private String subNo2 = "07647064770";
    private OWSActions owsActions;
    private Date newStartDate;
    private  Date firstRunDate;
    private DateTime datetime;
    private String nextTreatmentDate;
    private String firstName = "first952016109";
    private String lastName = "last076869566";

    @Test(enabled = true, description = "TC5422_Care_Subscription_Deactivation_On_HPIN_Via_Treatment_Process", groups = "OCS")
    public void TC5422_Care_Subscription_Deactivation_On_HPIN_Via_Treatment_Process() {
        test.get().info("Step 1 : Create a Consumer customer with single deal account, subscription is on HPIN within trial period, device having CCA");
        CommonActions.updateHubProvisionSystem("H");
        owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\ocs\\TC5422_HPIN_3_FC_SB_And_Sim_Only_Type_Request.xml";
        owsActions.createOcsCustomerRequest(path, true, "");

        test.get().info("Step 2 : Verify Create Ocs Account async task is not displayed");
        customerNumber = owsActions.customerNo;
        orderId = owsActions.orderIdNo;
        firstName = owsActions.firstName;
        lastName = owsActions.lastName;
        checkCreateOcsAccountCommand();

        test.get().info("Step 3 : Create new billing group");
        createNewBillingGroup();

        test.get().info("Step 4 : Update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 5 : Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 6 : Update start date for customer");
        newStartDate = TimeStamp.TodayMinus1MonthMinus20Day();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 8 : Run Inclusive Spend Refill for Billing Capped and Network Capped. Run Discount Bundle Renewal job.");
        submitDoRefillBCJob();
        submitDoRefillNCJob();
        submitDoBundleRenewJob();

        test.get().info("Step 9 : Submit Bill Run Job");
        submitDraftBillRun();
        submitConfirmBillRun();

        test.get().info("Step 10 : Load Care screen");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();

        test.get().info("Step 11 : Get First Treatment Date");
        getFirstTreatmentDate();

        test.get().info("Step 12 : Submit Treatment job");
        RemoteJobHelper.getInstance().submitTreatmentJob(nextTreatmentDate);
    }

    private void checkCreateOcsAccountCommand(){
        boolean isExist = false;
        List asyncCommand =  CommonActions.getAsynccommand(orderId);
        for (int i = 0; i < asyncCommand.size(); i++) {
            if (((HashMap) asyncCommand.get(i)).containsValue("CREATE_OCS_ACCOUNT")) {
                isExist = true;
                break;
            }
        }
        Assert.assertFalse(isExist);
    }

    private void getFirstTreatmentDate() {
        openInvoiceDetailsScreen();
        datetime = DateTime.parse(InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getNextTreatmentDate(), DateTimeFormat.forPattern("dd MMM yyyy"));
        nextTreatmentDate = Parser.parseDateTimeFormat(datetime, "yyyyMMdd");
    }
}
