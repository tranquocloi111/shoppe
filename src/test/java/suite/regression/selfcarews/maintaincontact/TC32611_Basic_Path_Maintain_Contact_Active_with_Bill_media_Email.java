package suite.regression.selfcarews.maintaincontact;

import framework.utils.Xml;
import logic.business.db.OracleDB;
import logic.business.entities.ServiceOrderEntity;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.selfcare.MyPasswordPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;
import suite.regression.selfcare.modifyaccount.TC33317_Self_Care_Change_Password;

import java.io.File;
import java.util.HashMap;

public class TC32611_Basic_Path_Maintain_Contact_Active_with_Bill_media_Email extends BaseTest {
    String serviceOrderID;
    String currentPWD;

//    @Test(enabled = true, description = "TC32611 basic path maintain contact active with bill media email", groups = "SelfCare")
    public void TC32611_Basic_Path_Maintain_Contact_Active_with_Bill_media_Email() {

        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_and_NK2720";
        test.get().info("Step 1 : Create a customer with FC 1 bundle and NK270");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 1 : Load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 1 : verify bill notification is email");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(DetailsContentPage.BillingInformationSectionPage.getInstance().getBillNotification(), "Email");

        test.get().info("Step 1 : Build forgotten password request ");
        path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC32611_request";
        SWSActions swsActions = new SWSActions();
        swsActions.buildForgottenPasswordRequest(owsActions.username, path);

        test.get().info("Step 1  submit the request to webservice");
        swsActions.submitTheRequest();

        test.get().info("Step 1 : verify expected SO is generated for customer");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        HashMap<String, String> entiy = ServiceOrderEntity.dataServiceOrderForChangePassword("Ad-hoc SMS Messages", "Created");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(entiy), 1);
        serviceOrderID = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Ad-hoc SMS Messages");

        test.get().info("Step 1 : verify message detail");
        verifySMSMessageDetail();

        test.get().info("Step 1 :Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username,currentPWD,customerNumber);

        test.get().info("Step 1 :input the current password and new password then click continue button");
//        MyPasswordPage.getInstance().

    }

    private void verifySMSMessageDetail() {
        String sql = "select hte.contextinfo from hitransactionevent hte where hte.hitransactionid = " + serviceOrderID;
        String message = null;

        try {
             message =   OracleDB.SetToNonOEDatabase().executeQueryReturnListString(sql).get(0);
        }catch (Exception ex){System.out.println(ex);}
        int start= message.indexOf("<BODY>");
        int end= message.indexOf("</BODY>");
        String subMssg= message.substring(start+6,end);
        Assert.assertTrue(subMssg.startsWith("Tesco Mobile: Your password for your Pay monthly online account has been reset to"));
        currentPWD=subMssg.split(" ")[subMssg.split(" ").length-1];
    }

}


