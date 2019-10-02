package suite.regression.selfcare.viewaccount;

import logic.business.db.billing.CommonActions;
import logic.business.entities.selfcare.MyBillAndPaymentEnity;
import logic.business.ws.ows.OWSActions;
import logic.pages.selfcare.MyBillsAndPaymentsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.HashMap;

public class TC33310_Self_Care_View_Usage_Details_with_less_than_1000_calls extends BaseTest {


    @Test(enabled = true, description = "TC33310 self care view usage details with less than 1000 calls", groups = "SelfCare")
    public void TC33310_Self_Care_View_Usage_Details_with_less_than_1000_calls() {
        test.get().info("Step 1: Create a CC customer with no bundle and sim only");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_NC_no_bundle_and_sim_only";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 2: create new group billing");
        createNewBillingGroup();

        test.get().info("Step 3: update bill group payment collection date to 10 dáy later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set billing group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5:update customer start date");
        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus10Days());


    }
    private void verifyMyBillsAndPaymentsDetailsPageResultIsDisplayedCorrectly()
    {
        HashMap<String,String> myBillsAndPaymentsEnity= MyBillAndPaymentEnity.dataForMyBillsAndPayment("Online Payment","£-17.50","£-17.50");
        Assert.assertEquals(MyBillsAndPaymentsPage.getInstance().getNumberPaymentByEnity(myBillsAndPaymentsEnity),1);
        Assert.assertEquals(MyBillsAndPaymentsPage.getInstance().getTotalNumberPayment(),1);
    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }


}
