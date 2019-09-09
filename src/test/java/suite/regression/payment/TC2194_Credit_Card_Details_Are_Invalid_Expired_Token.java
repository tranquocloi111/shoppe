package suite.regression.payment;

import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.HashMap;

public class TC2194_Credit_Card_Details_Are_Invalid_Expired_Token extends BaseTest {
    /*
     * PaymentDetailPage
     * PaymentDPage
     * RemoteJobHelper
     * */
    String customerNumber = null;
    String fullName = null;
    String invoiceNumber=null;

    @Test(enabled = true, description = "TC2194 Credit Card detail are invalid token", groups = "Payment")
    public void TC2194_Credit_Card_Details_Are_Invalid_Expired_Token() {
        test.get().info("Step 1 : create an online cc customer with FC 1 bundle of SB and sim only");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);
        customerNumber =owsActions.customerNo;
        fullName =owsActions.lastName+","+" "+owsActions.firstName;


        test.get().info("Step 2 : create new billing");
        super.createNewBillingGroup();
        updateBillGroupPaymentCollectionDateTo10DaysLater();
        setBillGroupForCustomer(customerNumber);
        Date newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        RemoteJobHelper.getInstance().submitDoRefillBcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoRefillNcJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(TimeStamp.Today());
        RemoteJobHelper.getInstance().submitDraftBillRun();
        RemoteJobHelper.getInstance().submitConfirmBillRun();

        test.get().info("Step 3 : load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        updateCreditCardExpiryDate();

        test.get().info("Step 4 : update invoice due date");
        BillingActions.updateInvoiceDueDate(customerNumber);

        test.get().info("Step 5 : submit payment all location and credit card batch job");
        RemoteJobHelper.getInstance().submitPaymentAllocationBatchJobRun();
        RemoteJobHelper.getInstance().submitCreditCardBatchJobRun();

        test.get().info("Step 6 : refresh user in hub");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 7 : get invoice number");
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        invoiceNumber = InvoicesContentPage.getInstance().getInvoiceNumber();

        test.get().info("Step 8 :  open the service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 9 :  verify the invoice status is direct debit rejected");
        HashMap cardPaymentSOEnity = ServiceOrderEntity.dataServiceOrderCreditCardPayment("Rejected", "Credit Card Failure");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(cardPaymentSOEnity), 1);

        test.get().info("Step 10 :  open the reject service order record");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Credit Card Failure");

        test.get().info("Step 11 :  verify the reject service order details are correct");
        verifyTheRejectServiceOrderDetail();

    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }


    private void updateCreditCardExpiryDate() {
        updatePrpovaldate();
        updateProvalNumberValue("CCEY", 2005);
        updateProvalNumberValue("CCEM", 1);
    }

    private void updatePrpovaldate() {
        String sql = String.format("update hmbrproperty set propvaldate = to_date('2005-01-01','yyyy-mm-dd') where hmbrid in (select hmbrid from hierarchymbr hm, hierarchy h where h.rootbuid in (%s)  and h.hid = hm.hid and hm.hmbrtype = 'BP') and propertykey in ('TKEXPDATE')", customerNumber);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    private void updateProvalNumberValue(String propertyKey, double value) {
        String sql = String.format("update hmbrproperty set PROPVALNUMBER = %s where hmbrid in (select hmbrid from hierarchymbr hm, hierarchy h where h.rootbuid in (%s)  and h.hid = hm.hid and hm.hmbrtype = 'BP') and propertykey in ('%s')", value, customerNumber, propertyKey);

        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public void verifyTheRejectServiceOrderDetail() {
        Assert.assertEquals(fullName, TasksContentPage.TaskPage.DetailsPage.getInstance().getCustomerName());
        Assert.assertEquals(customerNumber, TasksContentPage.TaskPage.DetailsPage.getInstance().getCustomerID());
        int hierachyMbr = Integer.parseInt(TasksContentPage.TaskPage.DetailsPage.getInstance().getHierarchyMbr());
        Assert.assertTrue(hierachyMbr>0);
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getInvoiceNumber(), invoiceNumber);
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getContactDetail(), "By Phone, daytime - 0398403850");
        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getHubRejecttionCode(), "Credit Card expired");

        Assert.assertEquals(TasksContentPage.TaskPage.DetailsPage.getInstance().getReceiptID(), "");
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getReDSStatusAuthorisation());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getReDSResponseDate());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getEndOfWizardMessage());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getRedCode());
        Assert.assertEquals("", TasksContentPage.TaskPage.DetailsPage.getInstance().getRedSAuthorisationNumber());
    }
}