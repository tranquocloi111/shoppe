package suite.regression.selfcarews;

import framework.utils.Xml;
import logic.business.entities.BundlesToSelectEntity;
import logic.business.entities.ErrorResponseEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ChangeSafetyBufferPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 6/09/2019
 */
public class TC31993_Pending_Change_of_SB_Decrease_SB_Apply_From_next_bill_date extends BaseTest {
    String customerNumber;
    String subscriptionNumber;
    String subscriptionNumberValue;
    String serviceOrderId;

    @Test(enabled = true, description = "TC31993_Pending Change of SB Decrease SB Apply From next bill date", groups = "SelfCareWS")
    public void TC31993_Pending_Change_of_SB_Decrease_SB_Apply_From_next_bill_date(){
        test.get().info("Create an online CC customer with FC 1 bundle of SB and simonly");
        OWSActions owsActions = new OWSActions();
        owsActions.createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly();
        customerNumber = owsActions.customerNo;

        test.get().info("Login to HUBNet then search Customer by customer number then open Customer Summary");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Record latest subscription number for customer");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        subscriptionNumberValue = CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(1);
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        subscriptionNumber = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber();

        test.get().info("Change safety buffer from next bill date");
        changeSafetyBufferFromNextBillDate();

        test.get().info("Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Verify Change bundle is in provision wait status");
        verifyChangeBundleIsInProvisionWaitStatus();
        //===============================================================================
        test.get().info("Build and submit maintain bundle request only customer number subscription number");
        SWSActions swsActions = new SWSActions();
        String filePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC31993_request.xml";
        Xml response = swsActions.submitMaintainBundleRequest(filePath, customerNumber, subscriptionNumber);

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
                String.format("Pending service order error (Change Bundle (%s  Mobile Ref 1) (Service Order ID %s))", subscriptionNumber, serviceOrderId),
                "ERROR");
        sCSMultiExceptionList.add(sCSMultiException);

        falseResponse.setSCSMultiExceptionMessages(sCSMultiExceptionList);

        return falseResponse;
    }
    private void verifyChangeBundleIsInProvisionWaitStatus(){
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByIndex(2);
        HashMap<String, String> serviceOrder = ServiceOrderEntity.dataServiceOrder(subscriptionNumber, "Change Bundle", "Provision Wait");
        Assert.assertEquals(1, ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(serviceOrder));
    }

    private void changeSafetyBufferFromNextBillDate(){
        MenuPage.RightMenuPage.getInstance().clickChangeBundleLink();
        ServiceOrdersPage.SelectSubscription.getInstance().selectSubscription(subscriptionNumberValue, "Change Safety Buffer");
        ChangeSafetyBufferPage.ChangeSafetyBuffer changeSafetyBuffer = new ChangeSafetyBufferPage.ChangeSafetyBuffer();
        changeSafetyBuffer.unSelectBundlesByName(BundlesToSelectEntity.getSafetyBuffersAToSelect(),"£20 safety buffer");
        changeSafetyBuffer.selectBundlesByName(BundlesToSelectEntity.getSafetyBuffersAToSelect(), "£10 safety buffer");
        changeSafetyBuffer.selectWhenToApplyChangeText("From next bill date (permanent)");
        changeSafetyBuffer.clickNextButton();
        ServiceOrdersPage.ConfirmChangeBundle.getInstance().clickNextButton();
        ServiceOrdersPage.ServiceOrderComplete.getInstance().clickReturnToCustomer();
    }
}
