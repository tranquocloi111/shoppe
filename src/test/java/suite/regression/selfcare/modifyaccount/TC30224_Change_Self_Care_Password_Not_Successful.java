package suite.regression.selfcare.modifyaccount;


import framework.config.Config;
import framework.utils.Log;
import logic.business.db.OracleDB;
import logic.business.db.billing.CommonActions;
import logic.business.entities.EventEntity;
import logic.business.helper.EmailHelper;
import logic.business.helper.FTPHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.SelfCareSettingContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.MyPasswordPage;
import logic.utils.Common;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.io.File;
import java.util.List;

public class TC30224_Change_Self_Care_Password_Not_Successful extends BaseTest {
/*
Author: Tran Quoc Loi
 */

    @Test(enabled = true, description = "TC30223 Change password by selfcare", groups = "SelfCare")
    public void TC30223_Change_Self_Care_Password() {

        test.get().info("Step 1 : Create a General customer ");
        String path = "src\\test\\resources\\xml\\SelfCare\\viewaccount\\TC30222_CreateOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);


        test.get().info("Step 4: Access detail screen");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();

        test.get().info("Step 5: Change Email and change bill notification ");
        String email = Config.getProp("emailUsername");
        CareTestBase.page().clickEditBtnBySection("Address Information");
        DetailsContentPage.AddressInformationPage.getInstance().changeEmail(email);
        CareTestBase.page().clickApplyBtn();

        CareTestBase.page().clickEditBtnBySection("Billing Information");
        DetailsContentPage.BillingInformationSectionPage.getInstance().changeBillNotification("SMS");
        CareTestBase.page().clickApplyBtn();

        test.get().info("Step 6: go to self care setting  ");
        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();
        SelfCareSettingContentPage.SelfCareSettingSection.getInstance().clickFirstRow();

        test.get().info("Step 7: Click reset password ");
        SelfCareSettingContentPage.SelfCareSettingSection.getInstance().clickResetPasswordBtn();
        SelfCareSettingContentPage.SelfCareSettingSection.getInstance().acceptComfirmDialog();

        test.get().info("Step 8 :Verify the error message ");
        String  expectedErrorMsg = "[SMS_SYS_39-S] HUB does not send an SMS confirmation on password resets done in Care.  (Error: -20199)";
        Assert.assertEquals(CareTestBase.page().errorMessageList().get(0),expectedErrorMsg);

        test.get().info("Step 9: Login to the selfcare screen ");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username,owsActions.password,owsActions.customerNo);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();


    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }


}
