package suite.regression.selfcare.viewsubscription;

import logic.business.ws.ows.OWSActions;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Timestamp;

public class TC31918_My_tariff_details_Flexible_Cap_FC_SB_active extends BaseTest {

    String subno;

    @Test(enabled = true, description = "TC31918 my tariff details flexible cap FC SB acctive", groups = "SelfCare")
    public void TC31918_My_tariff_details_Flexible_Cap_FC_SB_active() {
        test.get().info("Step 1: Create a CC customer");
        String path = "src\\test\\resources\\xml\\selfcare\\viewsubscription\\TC31918_createOrderRequest";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getSubscription(owsActions.orderIdNo, "FC Mobile 1");
        String customerNumber = owsActions.customerNo;
        subno = owsActions.getOrderMpnByReference("FC Mobile 1");

        test.get().info("Step 2:Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 3: access my tariff details page ");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 4: verify subscription details");
        verifySubscriptionDetails();

    }

    private void verifySubscriptionDetails() {
        String actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").getDescription();
        Assert.assertEquals("FC Mobile 1", actualResult);
       actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").getMobilePhoneNumber();
        Assert.assertEquals(subno, actualResult);
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").hasSaveButton());
        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").getTariff();
        Assert.assertEquals("£10 Tariff 12 Month Contract", actualResult);


        String expectedResult = String.format("ACTIVE   as of   %s    ", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF));
        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").getStatus();
        Assert.assertEquals(expectedResult.trim(), actualResult.trim());


        expectedResult = String.format("£20 safety buffer    ACTIVE  as of  %s   ", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF));
        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").getSafetyBuffer();
        Assert.assertEquals(expectedResult.trim(), actualResult.trim());

        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").getMonthlyAllowance();
        Assert.assertEquals("500 mins, 5000 texts (FC)", actualResult.trim());

        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").hasChangeMySafetyBufferButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").hasAddOrChangeABundleButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").hasAddOrChangeAFamilyPerkButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").hasUpdateButton());

        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("FC Mobile 1").getRoaming();
        Assert.assertEquals("On", actualResult);
    }



    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }
}
