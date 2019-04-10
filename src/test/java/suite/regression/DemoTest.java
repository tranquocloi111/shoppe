package suite.regression;

import logic.pages.care.CareTestBase;
import logic.pages.care.ServiceOrdersContentPage;
import logic.pages.care.sidebar.LeftMenuPage;
import logic.utils.TimeStamp;
import logic.utils.Xml;
import org.testng.annotations.Test;
import suite.BaseTest;

import java.sql.Date;

public class DemoTest extends BaseTest {
    Xml xml;
    //@Test
    public void createOrderTest() {
//        String requestPath = "";
//        OrderingWS ows = new OrderingWS(requestPath);
       // ows.createOrder();
    }

    @Test(description = "TC29701_Deactivate_Account_with_contracted_subscription_within_28_days_with_a_delay_return_and_immediate_refund", groups = "Care")
    public void TC29701_Deactivate_Account_with_contracted_subscription_within_28_days_with_a_delay_return_and_immediate_refund(){
        String requestPath = "C:\\GIT\\TM\\hub_testauto\\src\\test\\resources\\xml\\TC29699_createOrder.xml";
       // BaseWS.page().createOrderAndSignAgreementByUI(requestPath);

        //TestBase.createNewBillingGroup();

        //TestBase.updateBillGroupPaymentCollectionDateTo10DaysLater();

        //String customerId = new Xml(BaseWS.page().xmlReponse).getNodeValueByTag(Response.nodeAccountNumberOrder);
        //TestBase.setBillGroupForCustomer(customerId);

        Date newStartDate = TimeStamp.TodayMinus15Days();

        //TestBase.updateCustomerStartDate(customerId, newStartDate);

        CareTestBase.page().loadCustomerInHubNet("8877");
        //OptionsMenuPage.page().clickDeactivateAccountLink();

        //ServiceOrdersPage serviceOrdersPage = new ServiceOrdersPage();
        //ServiceOrdersPage.DeactivateSubscriptionPage deactivateSubscriptionPage = serviceOrdersPage.new DeactivateSubscriptionPage();
        //deactivateSubscriptionPage.deactivateAccountWithADelayReturnAndImmediateRefund();

        ServiceOrdersContentPage serviceOrdersContentPage = new ServiceOrdersContentPage();
        serviceOrdersContentPage.verifyDeactivateAccountSOIsInProvisionWait();



    }

}
