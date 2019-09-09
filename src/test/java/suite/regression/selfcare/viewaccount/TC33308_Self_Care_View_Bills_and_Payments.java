package suite.regression.selfcare.viewaccount;

import logic.business.entities.selfcare.MyBillAndPaymentEnity;
import logic.business.ws.ows.OWSActions;
import logic.pages.selfcare.MyBillsAndPaymentsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.HashMap;

public class TC33308_Self_Care_View_Bills_and_Payments extends BaseTest {


    @Test(enabled = true, description = "TC33308 self care view bills and payments", groups = "SelfCare")
    public void TC33308_Self_Care_View_Bills_and_Payments() {
        test.get().info("Create a CC customer with no bundle and sim only");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_NC_no_bundle_and_sim_only";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;



        test.get().info("Login in to selfcare page");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Access view details of my bills and payments link");
        MyPersonalInformationPage.MyBillsAndPaymentsSection.getInstance().clickViewDetailsOfMyBillsAndPayments();
        MyPersonalInformationPage.MyBillsAndPaymentsSection.getInstance().verifyTheMyBillsAndPaymentsPage();

        test.get().info("verify my bills and payments details page result display correctly");
        verifyMyBillsAndPaymentsDetailsPageResultIsDisplayedCorrectly();



    }
    private void verifyMyBillsAndPaymentsDetailsPageResultIsDisplayedCorrectly()
    {
        HashMap<String,String> myBillsAndPaymentsEnity= MyBillAndPaymentEnity.dataForMyBillsAndPayment("Online Payment","£-17.50","£-17.50");
        Assert.assertEquals(MyBillsAndPaymentsPage.getInstance().getNumberPaymentByEnity(myBillsAndPaymentsEnity),1);
        Assert.assertEquals(MyBillsAndPaymentsPage.getInstance().getTotalNumberPayment(),1);
    }


}
