package suite.regression.soho;

import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SummaryContentsPage;
import logic.pages.care.main.HeaderContentPage;
import logic.pages.care.options.ChangeCustomerTypePage;
import logic.pages.care.options.ConfirmNewCustomerTypePage;
import logic.pages.selfcare.MyAccountDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TC3796_005_Existing_Customer_Convert_From_Consumer_To_Business_Account_And_Add_Subscriptions_In_Ows extends BaseTest {
    private String customerNumber = "10170";

    @Test(enabled = true, description = "TC3796_005_Existing_Customer_Convert_From_Consumer_To_Business_Account_And_Add_Subscriptions_In_Ows", groups = "SOHO")
    public void TC3796_005_Existing_Customer_Convert_From_Consumer_To_Business_Account_And_Add_Subscriptions_In_Ows() {
        test.get().info("Step 1 : Create a Customer with RESIDENTIAL type");
        OWSActions owsActions = new OWSActions();
        String path = "src\\test\\resources\\xml\\soho\\TC3796_002_request_residential_type.xml";
        owsActions.createGeneralCustomerOrder(path);

        test.get().info("Step 2 : Add More Than 1 Subscriptions To Customer with Business type");
        path = "src\\test\\resources\\xml\\soho\\TC3796_005_request_business_type.xml";
        Xml xml = owsActions.createNewOrderWithExistCustomer(path, customerNumber);

        test.get().info("Step 3 : Verify the system does not allow to add new business customer type");
        Assert.assertEquals(xml.getTextByTagName("errorCode"), "CO_060");
        Assert.assertEquals(xml.getTextByTagName("errorDescription"), "Max Subscriptions Per Account exceeded");

    }


}
