package suite.regression.selfcarews;

import framework.utils.Log;
import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.entities.MaintainBundleEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ChangeBillCyclePage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 10/09/2019
 */
public class TC31983_Pending_Change_of_Bill_Cycle_Increase_SB_Apply_Now_untill_next_bill_date extends BaseTest {
    String customerNumber;
    String subscriptionNumber;
    String subscriptionNumberValue;
    String orderWebServiceId;

    @Test(enabled = true, description = "TC31983_Pending_Change_of_Bill_Cycle_Increase_SB_Apply_Now_untill_next_bill_date", groups = "SelfCareWS")
    public void TC31983_Pending_Change_of_Bill_Cycle_Increase_SB_Apply_Now_untill_next_bill_date(){
        test.get().info("Create an online CC customer with FC 1 bundle of SB and simonly");
        OWSActions owsActions = new OWSActions();
        String TC31983_CreateOrderRequest = "src\\test\\resources\\xml\\ows\\onlines_CC_customer_with_FC_1_bundle_of_SB_and_sim_only.xml";
        owsActions.createGeneralCustomerOrder(TC31983_CreateOrderRequest);
        customerNumber = owsActions.customerNo;

        test.get().info("Login to HUBNet then search Customer by customer number then open Customer Summary");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Record latest subscription number for customer");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscriptionNumberValue = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(1);
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        subscriptionNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();

        test.get().info("Pending change bill cycle");
        pendingChangeBillCycle();

        test.get().info("Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Verify change bill cycle is in progress");
        verifyChangeBillCycleIsInProgress();
        //============================================================
        test.get().info("Build maintain bundle request only customer number and subscription number");
        SWSActions swsActions = new SWSActions();
        String filePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC31983_request.xml";
        Xml response = swsActions.submitMaintainBundleRequest(filePath, customerNumber, subscriptionNumber);
        Log.info("Response: " + response);

        test.get().info("Build fault response data");
        ErrorResponseEntity falseResponse = buildFalseResponse();

        test.get().info("Verify self care ws fault response");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        selfCareWSTestBase.verifySelfCareWSFaultResponse(response, falseResponse);
    }

    private ErrorResponseEntity buildFalseResponse(){
        ErrorResponseEntity falseResponse = new ErrorResponseEntity();
        falseResponse.setFaultCode("ERROR");
        falseResponse.setFaultString("Validation Errors");
        falseResponse.setExceptionMsg("Validation Errors");
        falseResponse.setExceptionCauseMsg("Validation Errors");

        List<ErrorResponseEntity.SelfCareServiceMultiExceptionEntity> sCSMultiExceptionList = new ArrayList<>();
        ErrorResponseEntity.SelfCareServiceMultiExceptionEntity sCSMultiException = new ErrorResponseEntity.SelfCareServiceMultiExceptionEntity(
                "UBE_001",
                String.format("Pending service order error (Change Bill Cycle (Service Order ID %s))", orderWebServiceId),
                "ERROR");
        sCSMultiExceptionList.add(sCSMultiException);

        falseResponse.setSCSMultiExceptionMessages(sCSMultiExceptionList);

        return falseResponse;
    }


    private void verifyChangeBillCycleIsInProgress(){
        orderWebServiceId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByIndex(1);
        HashMap<String, String> serviceOrder = ServiceOrderEntity.dataServiceOrder("", "Change Bill Cycle", "In Progress");
        Assert.assertEquals(1, ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(serviceOrder));
    }

    private void pendingChangeBillCycle(){
        MenuPage.RightMenuPage.getInstance().clickChangeBillCycle();
        ChangeBillCyclePage.ChangeBillCycle changeBillCycle = new ChangeBillCyclePage.ChangeBillCycle();
        String currentBillCycle = changeBillCycle.getCurrentBillCycle();

        switch (currentBillCycle) {
            case "1st of month (14th bill run)":
                changeBillCycle.selectNewBillCycle("22nd of month (9th bill run)");
                break;
            case "8th of month (23rd bill run)":
                changeBillCycle.selectNewBillCycle("22nd of month (9th bill run)");
                break;
            case "22nd of month (9th bill run)":
                changeBillCycle.selectNewBillCycle("1st of month (14th bill run)");
                break;
            default:
                changeBillCycle.selectNewBillCycle("22nd of month (9th bill run)");
                break;
        }

        changeBillCycle.clickNextButton();
        ServiceOrdersPage.ServiceOrderComplete.getInstance().clickReturnToCustomer();
    }
}
