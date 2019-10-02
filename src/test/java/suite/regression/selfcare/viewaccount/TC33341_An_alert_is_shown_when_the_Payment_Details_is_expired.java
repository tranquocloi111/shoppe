package suite.regression.selfcare.viewaccount;

import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.helper.DateTimeHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.Date;

public class TC33341_An_alert_is_shown_when_the_Payment_Details_is_expired extends BaseTest {

    String customerNumber;

    @Test(enabled = true, description = "TC33340 self care an alert is shown when there is an overdue invoice on the account", groups = "SelfCare")
    public void TC33340_Self_Care_An_alert_is_shown_when_there_is_an_Overdue_invoice_on_the_account() {
        test.get().info("Step 1: Create a CC customer ");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2: Update credit card expiry date");
        updateCreditCardExpiryDate();

        test.get().info("Step 3: Load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 4: verify the payment detail is expired");
        goTodDetailsToVerifyThePaymentIsExpired();

        test.get().info("Step 5: Login to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 6: Verify the alert message");
        String mssg = "Your payment card has expired. Click here to update your card details";
        Assert.assertEquals(mssg, MyPersonalInformationPage.myAlertSection.getInstance().getAlertMessageByText("Your payment card has expired. Click here to update your card details"));

    }

    private void updateCreditCardExpiryDate() {
        updatePrpovaldate();
        updateProvalNumberValue("CCEY", 2013);
        updateProvalNumberValue("CCEM", 1);
    }

    private void updatePrpovaldate() {
        String sql = String.format("update hmbrproperty set propvaldate = to_date('2013-01-31','yyyy-mm-dd') where hmbrid in (select hmbrid from hierarchymbr hm, hierarchy h where h.rootbuid in (%s)  and h.hid = hm.hid and hm.hmbrtype = 'BP') and propertykey in ('TKEXPDATE')", customerNumber);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    private void updateProvalNumberValue(String propertyKey, double value) {
        String sql = String.format("update hmbrproperty set PROPVALNUMBER = %s where hmbrid in (select hmbrid from hierarchymbr hm, hierarchy h where h.rootbuid in (%s)  and h.hid = hm.hid and hm.hmbrtype = 'BP') and propertykey in ('%s')", value, customerNumber, propertyKey);

        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public void goTodDetailsToVerifyThePaymentIsExpired() {
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("2013", DetailsContentPage.PaymentInformationPage.getInstance().getCardExpireYear());
        Assert.assertEquals("01", DetailsContentPage.PaymentInformationPage.getInstance().getCardExpireMonth());
    }
}
