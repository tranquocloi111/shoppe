package suite.regression.selfcarews.maintaincontact;

import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.entities.MaintainContactResponseData;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.SelfCareSettingContentPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

public class TC32630_Self_Care_WS_Change_a_Self_Care_Username extends BaseTest {
  String customerNumber;


    @Test(enabled = true, description = "TC32630 selfcare ws maintain change a self care username", groups = "SelfCare")
    public void TC32630_Self_Care_WS_Change_a_Self_Care_Username() {

        test.get().info("Step 1 : Create a customer ");
        OWSActions owsActions = new OWSActions();
        owsActions.createACCCustomerWithOrder();
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2: Load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3:verify customer email address and username");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(owsActions.email, DetailsContentPage.AddressInformationPage.getInstance().getEmail());
        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();
        Assert.assertEquals(SelfCareSettingContentPage.SelfCareSettingSection.getInstance().getUserName(),owsActions.username);

        test.get().info("Step 4:verify bill type is summary");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("Summary", DetailsContentPage.BillingInformationSectionPage.getInstance().getBillStyle());

        test.get().info("Step 5 : Build maintain contact detail request ");
        String path = "src\\test\\resources\\xml\\sws\\maintaincontact\\TC70_request";
        SWSActions swsActions = new SWSActions();
        String newUserName= String.format("NewName%s@hsntech.com", RandomCharacter.getRandomNumericString(9));
        String newEmailAddress =  String.format("NewEmail%s@hsntech.com", RandomCharacter.getRandomNumericString(9));
        swsActions.buildContactDetailRequest(owsActions.username,newUserName,newEmailAddress,customerNumber, path);

        test.get().info("Step 6: submit the request to webservice");
        Xml response= swsActions.submitTheRequest();


        test.get().info("Step 7:  verify selfcare ws fault response");
        Assert.assertEquals(customerNumber,response.getTextByTagName("accountNumber"));
        Assert.assertEquals("0",response.getTextByTagName("responseCode"));
        Assert.assertEquals("",response.getTextByTagName("message"));

        test.get().info("Step 8: verify customer email addres and user name are updated successfully");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(newEmailAddress,DetailsContentPage.AddressInformationPage.getInstance().getEmail());

        MenuPage.LeftMenuPage.getInstance().clickSelfCareSetting();
        Assert.assertEquals(newUserName,SelfCareSettingContentPage.SelfCareSettingSection.getInstance().getUserName());
    }




}


