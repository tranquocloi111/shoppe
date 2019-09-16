package suite.regression.selfcare.modifysubscription;

import framework.config.Config;
import framework.utils.RandomCharacter;
import framework.utils.SFTP;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.helper.FTPHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.selfcare.MakeAOneOffPaymentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
/*Tran Quoc Loi
SelfCareTestBase
MyTariffDetailsPage
ServiceOrdersPage
 */
public class TC31892_Service_has_been_restricted_all_bars_are_visible extends BaseTest {

     List<String> bar = Arrays.asList("Customer","Fraud","Treatment","HighUsage");
    @Test(enabled = true, description = "TC331892 service has been restricted all bars are visible", groups = "SelfCare")
    public void TC31892_Service_has_been_restricted_all_bars_are_visible()
    {
        test.get().info("Create a CC customer with no bundle and sim only");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_NC_no_bundle_and_sim_only";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getSubscription(owsActions.orderIdNo,"Mobile NC 1");
        String customerNumber= owsActions.customerNo;
        String subNo1 = owsActions.serviceRef;

        test.get().info("Create new billing group from to day minus 20 days");
        createNewBillingGroupToMinus20days();

        test.get().info("update bill group payment collection date to 10 dáy later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("set billing group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("update customer start date");
        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus15Days());

        test.get().info("load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("generate the cdr file then upload to server");
        generateCDRFileFromTemplateThenUploadToServer(subNo1);
        BaseTest.waitLoadCDRJobComplete();

        test.get().info("Turn on all bars in Barring and Roaming options");
        turnOnAllBarsInBarringAndRoamingOptions();

        test.get().info("Open the first subscription details content for first subscription");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);

        test.get().info("Verify all bars are turned on");
        String actualStatus=SubscriptionContentPage.SubscriptionDetailsPage.SubscriptionFeatureSectionPage.getInstance().getBarringStatus();
        Assert.assertEquals(actualStatus,"Capped Excess=OFF, Fraud=ON, Treatment=ON, Customer=ON, HighUsage=ON");

        test.get().info("Login to self care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username,owsActions.password,customerNumber);

        test.get().info("Verify the message customers service has been restricted is displayed");
        MyPersonalInformationPage.myAlertSection.getInstance().isMssgDisplayed("Your service has been restricted. Click here for more options.");
        MyPersonalInformationPage.myAlertSection.getInstance().clickAlertMessageByText("Your service has been restricted. Click here for more options.");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Verify all bar messages in my tariff page");
        verifyAllBarMessageInMyTariffPage();

        test.get().info("click 40 data cap abroad link");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").clickDataCapAbroad();

        test.get().info("verify accept data charges of more than 40 while abroad page open");
        SelfCareTestBase.page().verifyAcceptDataChargesOfMoreThan40WhileAbroadPageOpen();
        SelfCareTestBase.page().clickBackBtn();

        test.get().info("verify unpaid link available");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").clickUnPaidLink();
        SelfCareTestBase.page().verifyMakeAOneOffPayment();

    }
    public void generateCDRFileFromTemplateThenUploadToServer(String subNo1){
        String filePath = "src\\test\\resources\\txt\\care\\TM_DRAS_CDR_20150106170007";
        String cdrFileString = Common.readFile(filePath);
        String fileName= Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DATE_FORMAT2)+ RandomCharacter.getRandomNumericString(6);
        fileName= Common.getFolderLogFilePath()+"TM_DRAS_CDR_" + fileName + ".txt";
        cdrFileString=cdrFileString.replace("20150106170007",fileName)
                        .replace("07847469610",subNo1)
                        .replace("04/01/2015",Parser.parseDateFormate(TimeStamp.TodayMinus2Days(),TimeStamp.DATE_FORMAT4));
        Common.writeFile(cdrFileString,fileName);
        String remotePath= Config.getProp("CDRSFTPFolder");
        SFTPHelper.getInstance().upFileFromLocalToRemoteServer(fileName,remotePath);
    }

    public void turnOnAllBarsInBarringAndRoamingOptions(){
        for(int i=0;i<bar.size();i++) {
            MenuPage.RightMenuPage.getInstance().clickConfigureBarRoamingonLink();
            ServiceOrdersPage.SelectSubscription.getInstance().clickNextButton();
            ServiceOrdersPage.ConfigureSubscription.getInstance().selectSubscriptionBarring("Outbound");
            ServiceOrdersPage.ConfigureSubscription.getInstance().selectSubscriptionRoaming("Barred");
            ServiceOrdersPage.ConfigureSubscription.getInstance().enterNote("Turn on bar");
            ServiceOrdersPage.ConfigureSubscription.getInstance().selectSubscriptionBarReason(bar.get(i));
            ServiceOrdersPage.ConfigureSubscription.getInstance().clickNextButton();

            ServiceOrdersPage.ConfigureSubscription.getInstance().clickReturnToCustomer();
        }
    }

    public void verifyAllBarMessageInMyTariffPage(){
        Assert.assertEquals("Call Customer Care team on 0345 301 4455 to make a top-up", MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getHighUsage());
        Assert.assertEquals("Call Customer Care team for more details",MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getCustomer());
        Assert.assertEquals("Click here to make a payment", MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getUnpaidBill());
        Assert.assertEquals("Call Customer Care team on 0800 022 4030 for more details", MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getFraud());
        Assert.assertEquals("Click here to accept data charges of more than £40 while abroad & to remove the cap on data usage from your subscription",MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile NC 1").getDataCapAbroad().trim());
    }



}
