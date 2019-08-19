package suite.regression.selfcare.modifyaccount;


import framework.utils.RandomCharacter;
import logic.business.entities.EventEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.EmailHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.MyPasswordPage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.util.HashMap;

public class TC33317_Self_Care_Change_Password extends BaseTest {


    @Test(enabled = true, description = "TC33317 Self Care change password", groups = "SelfCare")
    public void TC33317_Self_Care_Change_Password() {

        test.get().info("Step 1 : Create a general customer");
        String path = "src\\test\\resources\\xml\\SelfCare\\viewaccount\\TC30222_CreateOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrderForChangePassword(path);
        EmailHelper.getInstance().deleteAllEmailByFolderNameAndEmailSubject("TescoMobilePayMonthly","");

        test.get().info("Step 2: Login to Self Care  ");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password,owsActions.customerNo);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 3: Click reset password ");
        SelfCareTestBase.page().clickChangeMyAccountPassword();

        test.get().info("Step 4: Input the new pass and click continue button");
        String newPasswd = "NewPsw" + RandomCharacter.getRandomAlphaNumericString(9);
        MyPasswordPage.getInstance().updateNewPassword(owsActions.password,newPasswd);

        test.get().info("Step 5: Click save button");
        String message = "Your password has been successfully changed.";
        Assert.assertEquals(SelfCareTestBase.page().successfulMessageStack().get(0),message);

        test.get().info("Step 6: Login selfcare by new password");
        SelfCareTestBase.page().reLoginIntoSelfCarePage(owsActions.username, newPasswd,owsActions.customerNo);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 7: Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        String serviceRefOf1stSubscription = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberValue("Mobile NC 1");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 8: Verify expected SO is generated for customer");
        HashMap<String,String> expectedServiceOrder = ServiceOrderEntity.dataServiceOrderForChangePassword("Ad-hoc SMS Messages","Created");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(expectedServiceOrder),1);

        test.get().info("Step 9: Access the service order detail");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Ad-hoc SMS Messages");

        test.get().info("Step 10: Verify the event of SO is generated correctly");
        String description=String.format("Password Change (%s)",serviceRefOf1stSubscription);
        HashMap<String,String> expectedEvent = EventEntity.dataForEventChangePassword(description,"Created","Batch");
        Assert.assertEquals(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(expectedEvent),1);

        test.get().info("Step 11: Verify change password successfully mail sent to customer");
        Assert.assertTrue(EmailHelper.getInstance().findStringInEmail("TescoMobilePayMonthly", " Tesco Mobile password", "Thank you for your request.  Your password has now been changed."));

    }


    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }
}
