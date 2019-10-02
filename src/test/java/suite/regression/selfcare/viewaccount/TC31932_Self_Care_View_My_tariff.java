package suite.regression.selfcare.viewaccount;

import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.selfcare.MyAccountDetailsPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

public class TC31932_Self_Care_View_My_tariff extends BaseTest {


    String subNo;
    String userName;

    @Test(enabled = true, description = "TC31932 self care view my tariff", groups = "SelfCare")
    public void TC31932_Self_Care_View_My_tariff() {
        test.get().info("Step 1: Create a CC customer with no bundle and sim only");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_NC_no_bundle_and_sim_only";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile NC 1");
        String customerNumber = owsActions.customerNo;
        userName = owsActions.username;

        test.get().info("Step 2: Load user in the hub net");
        CareTestBase.page().loadCustomerInHubNet(owsActions.customerNo);
        subNo = CommonContentPage.CustomerSummarySectionPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberByIndex(1);


        test.get().info("Step 3: Login in to selfcare page");
        SelfCareTestBase.page().LoginIntoSelfCarePage(userName, owsActions.password, customerNumber);

        test.get().info("Step 4: Access the view or change my tariff detail link");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 5: Verify my tariff details other information is correct");
        verifyMyTariffDetailsOtherInformationIsCorrect();

        test.get().info("Step 6: Verify page tool tip is correct");
        verifyPageToolTip();


    }

    @DataProvider(name = "browsername")
    public Object[][] dataProviderMethod() {
        return new Object[][]{{"gc"}, {"ff"}, {"ie"}};
    }

    public void verifyMyTariffDetailsOtherInformationIsCorrect() {
        String actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getDescription();
        Assert.assertEquals(actualResult, "Mobile NC 1");

        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getMobilePhoneNumber();
        Assert.assertEquals(actualResult, subNo);

        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getTariff();
        Assert.assertEquals(actualResult, "£10 Tariff 12 Month Contract");

        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getStatus();
        String expectedStatus = String.format("ACTIVE   as of   %s   ", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT4));
        Assert.assertEquals(actualResult.trim(), expectedStatus.trim());

        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getMonthlyAllowance();
        String expectedAllowanceBundle = String.format("100 mins, 5000 texts (Capped)");
        Assert.assertEquals(actualResult.trim(), expectedAllowanceBundle.trim());

        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getMonthlyBundles();
        String expectedMonthlyBundle = String.format("Monthly data bundle - 1GB (Capped)   ACTIVE  as of  %s ", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT4));
        Assert.assertEquals(actualResult.trim(), expectedMonthlyBundle.trim());

        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").hasAddOrChangeABundleButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").hasAddOrChangeAFamilyPerkButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").hasAddOrViewOneoffBundlesButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").hasSaveButton());
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").hasUpdateButton());

        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getRoaming();
        Assert.assertEquals(actualResult.trim(), "On");
        Assert.assertTrue(MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").isDataCapAbroadRed());
        actualResult = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getDataCapAbroad();
        Assert.assertEquals(actualResult.trim(), "Click here to accept data charges of more than £40 while abroad & to remove the cap on data usage from your subscription");
    }

    public void verifyPageToolTip() {

        String expectedMssg = "This bundle is included with your tariff and may not be changed unless you switch tariff.";
        String actualMssg = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getHelpIntructionByIndex(2);
        Assert.assertEquals(actualMssg, expectedMssg);

        expectedMssg = "You can change your data bundle any time. Choose a bundle from 500MB to 30GB and you'll get your new allowance on your next bill date.\nYou can use all of this data in the UK and in Europe with Home From Home. Read our fair usage policy.";
        actualMssg = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getHelpIntructionByIndex(4);
        Assert.assertEquals(actualMssg, expectedMssg);

        expectedMssg = "You can change your data bundle any time. Choose a bundle from 500MB to 30GB and you'll get your new allowance on your next bill date.\nYou can use all of this data in the UK and in Europe with Home From Home. Read our fair usage policy.";
        actualMssg = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getHelpIntructionByIndex(5);
        Assert.assertEquals(actualMssg, expectedMssg);

        expectedMssg = "You can buy a one-off bundle if you're running out of data before the end of the month. It's better value than paying our standard rates for data until your allowance renews. You can use the data bundle straight away and you'll be charged for it on your next bill. Choose from 250MB for £3.00, 500MB for £5.00 or 1GB for £7.50.\nYou can use all of this data in the UK and Europe with Home From Home. Read our fair usage policy.";
        actualMssg = MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getHelpIntructionByIndex(7);
        Assert.assertEquals(actualMssg, expectedMssg);


    }


}
