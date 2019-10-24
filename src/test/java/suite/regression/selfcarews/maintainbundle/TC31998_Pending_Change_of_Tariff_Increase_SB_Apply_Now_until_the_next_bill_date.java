package suite.regression.selfcarews.maintainbundle;

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
import logic.pages.care.options.ChangeTariffPage;
import logic.pages.care.options.TariffSearchPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 13/09/2019
 */
public class TC31998_Pending_Change_of_Tariff_Increase_SB_Apply_Now_until_the_next_bill_date extends BaseTest {
    String customerNumber;
    String subscriptionNumber;
    String subscriptionNumberValue;
    String tariffCode;
    String serviceOrderId;

    @Test(enabled = true, description = "TC31998_Pending_Change_of_Tariff_Increase_SB_Apply_Now_until_the_next_bill_date", groups = "SelfCareWS.MaintainBundle")
    public void TC31998_Pending_Change_of_Tariff_Increase_SB_Apply_Now_until_the_next_bill_date(){
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

        test.get().info("Open change tariff page for customer");
        openChangeTariffPageForCustomer();

        test.get().info("Open Tariff Search Window");
        openTariffSearchPage();

        test.get().info("Select Tariff by Code then click next button");
        tariffCode = "FC12-1500-100SO";
        selectTariffByCodeThenClickNextButton(tariffCode);

        test.get().info("Select SB bundle and finish change tariff process");
        selectSBBundleAndFinishChangeTariffProcess();

        test.get().info("Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Verify Change tariff is in provision wait status");
        verifyChangeTariffIsInProvisionWaitStatus();
        //===============================================================================
        test.get().info("Build and submit maintain bundle request only customer number subscription number");
        SWSActions swsActions = new SWSActions();
        String filePath = "src\\test\\resources\\xml\\sws\\maintainbundle\\TC31998_request.xml";
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
                String.format("Pending service order error (Change Tariff (Service Order ID %s))", serviceOrderId),
                "ERROR");
        sCSMultiExceptionList.add(sCSMultiException);

        falseResponse.setSCSMultiExceptionMessages(sCSMultiExceptionList);

        return falseResponse;
    }

    private void verifyChangeTariffIsInProvisionWaitStatus(){
        serviceOrderId = ServiceOrdersContentPage.getInstance().getServiceOrderIdByIndex(2);
        HashMap<String, String> serviceOrder = ServiceOrderEntity.dataServiceOrder(subscriptionNumber, "Change Tariff", "Provision Wait");
        Assert.assertEquals(1, ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(serviceOrder));
    }

    private void selectSBBundleAndFinishChangeTariffProcess(){
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName(BundlesToSelectEntity.getSafetyBuffersAToSelect(),"£10 safety buffer");
        ServiceOrdersPage.ChangeBundle.getInstance().clickNextButton();
        ServiceOrdersPage.ConfirmChangeBundle.getInstance().clickNextButton();
        ServiceOrdersPage.ServiceOrderComplete.getInstance().clickReturnToCustomer();
    }

    private void selectTariffByCodeThenClickNextButton(String tariffCode){
        TariffSearchPage.getInstance().selectTariffByCode(tariffCode);
        ChangeTariffPage.ChangeTariff.getInstance().clickNextButton();
    }

    private void openTariffSearchPage(){
        ChangeTariffPage.ChangeTariff.getInstance().clickButtonNewTariff();
    }

    private void openChangeTariffPageForCustomer(){
        MenuPage.RightMenuPage.getInstance().clickChangeTariffLink();
        ServiceOrdersPage.SelectSubscription.getInstance().clickNextButton();
        ChangeTariffPage.ChangeTariff.getInstance().clickButtonNewTariff();
    }
}
