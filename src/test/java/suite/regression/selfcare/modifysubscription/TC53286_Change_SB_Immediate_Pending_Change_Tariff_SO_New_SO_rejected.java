package suite.regression.selfcare.modifysubscription;

import framework.utils.Log;
import logic.business.db.OracleDB;
import logic.business.db.billing.CommonActions;
import logic.business.entities.BundlesToSelectEntity;
import logic.business.entities.ServiceOrderEntity;
import logic.business.entities.TariffSearchCriteriaEnity;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.care.options.TariffSearchPage;
import logic.pages.selfcare.ChangeMySafetyBufferPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.ResultSet;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;

public class TC53286_Change_SB_Immediate_Pending_Change_Tariff_SO_New_SO_rejected extends BaseTest {
    String sub = "07209466820";
    String inventoryID;
    String serviceOrder;

    @Test(enabled = true, description = "TC53286 chaneg SB Immdiate pending change Tariff SO new SO rejected", groups = "SelfCare")
    public void TC53286_Change_SB_Immediate_Pending_Change_Tariff_SO_New_SO_rejected() {

        test.get().info("Create a CC customer");
        String path = "src\\test\\resources\\xml\\selfcare\\modifysubscription\\TC53286_createOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber=owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo,"Mobile FC");
        sub=owsActions.serviceRef;
        serviceOrder=owsActions.orderIdNo;


        //Change a customer tariff amd the Service Order Status is PWAIT
        test.get().info("load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);


        test.get().info("select change tariff from RHS actions");
        MenuPage.RightMenuPage.getInstance().clickChangeTariffLink();

        test.get().info("select subscription from drop down");
        ServiceOrdersContentPage.getInstance().clickNextBtn();

        test.get().info("open the search tariff window");
        String title= ServiceOrdersContentPage.getInstance().getTitle();
        ServiceOrdersContentPage.getInstance().clicknewTariffSearchBtn();
        ServiceOrdersContentPage.getInstance().switchWindow("Tariff Search",false);

        test.get().info("Search tariff by specified criteria");
        TariffSearchCriteriaEnity tariffSearchCriteriaEnity=new TariffSearchCriteriaEnity();
        tariffSearchCriteriaEnity.setBillingType("Flexible Cap");
        tariffSearchCriteriaEnity.setContractPeriod("24");
        TariffSearchPage.getInstance().searchTariffByCriteria(tariffSearchCriteriaEnity);

        test.get().info("Select Tariff by code then click next button");
        TariffSearchPage.getInstance().clickTariffByTariffCode("FC24-2000-500");
        ServiceOrdersContentPage.getInstance().switchWindow(title,false);

        test.get().info("click next button on change tariff wizad");
        ServiceOrdersContentPage.getInstance().clickNextBtn();

        test.get().info("select specified bundles on change bundle screen then click then next button");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName("£30 safety buffer" );
        ServiceOrdersContentPage.getInstance().clickNextBtn();

        test.get().info("finish change bundle wizard then back to customer");
        ServiceOrdersContentPage.getInstance().clickNextBtn();
        ServiceOrdersContentPage.getInstance().clickReturnToCustomer();

        test.get().info("Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username,owsActions.password,customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("access my tariff detail screen");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("click safety buffer ");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC").clickChangeMySafetyBufferBtn();
        SelfCareTestBase.page().verifyChangeMySafetyBufferPage();

        test.get().info("verify A Panel With A Message Regarding The FC SB Option");
        verifyWarningMessageIsDisplayedInChangeSafetyBuffer();

        test.get().info("verify a panel for selecting FC amount");
        verifyThePanelForSelectingFCAmount();


        test.get().info("select a safety buffer is more than existing SB");
        ChangeMySafetyBufferPage.getInstance().selectSafetyBuffer("£40 safety buffer");

        test.get().info("verify only change it now but only until next bill date option is available");
        verifyOnlyCHangeItNowButOnlyUntilNextBillDateOptionIsavalaible();




    }

    private void verifyConfirmMssg() {
        Assert.assertEquals("£20.00", ChangeMySafetyBufferPage.getInstance().getPreviousSafetyBuffer());
        Assert.assertEquals("£40.00", ChangeMySafetyBufferPage.getInstance().getNewSafetyBuffer());
        String actualConfirmingMsg = ChangeMySafetyBufferPage.getInstance().getComfirmMessage();
        String expectedConfirmingMsg = String.format("Your safety buffer will change now and go back to £20.00 on %s.\r\n"
                + "While our systems update you might still get texts about your original safety buffer."
                + " To find out your balance, just call 4488 free from your Tesco Mobile phone or go to our iPhone and Android app.", getNextBillMonth());
        Assert.assertEquals(actualConfirmingMsg, expectedConfirmingMsg);
    }


    private void verifyWarningMessageIsDisplayedInChangeSafetyBuffer() {


        String expectedWarningMsg = String.format("Your safety buffer and tariff are set to change from your next bill date on %s."
                + " You can make a change to your safety buffer now but it will only last until %s."
                + " If you need to make another change, please call us on 4455 from your Tesco Mobile Phone.", getNextBillMonth(), getNextBillMonth());
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().getDescreaseWarningMessage(), expectedWarningMsg);
        Assert.assertEquals("Find out more about safety buffers.", ChangeMySafetyBufferPage.getInstance().getLinkText());
    }

    private String getNextBillMonth() {
        String date = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF);
        int day = Integer.parseInt(date.substring(0, 2));
        if (day >= 23) {
            return "23/" + Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF3).substring(0, 2);
        } else {
            return "23/" + Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT_IN_PDF3).substring(0, 2);
        }
    }

    private String getNextBillDate() {
        String date = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF);
        int day = Integer.parseInt(date.substring(0, 2));
        if (day <= 23) {
            return Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth((23 - day), 0), TimeStamp.DATE_FORMAT);
        } else {
            return Parser.parseDateFormate(TimeStamp.TodayPlusDayAndMonth((23 - day), 1), TimeStamp.DATE_FORMAT);
        }
    }

    private Date getNextBillASATDate() {
        String date = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF);
        int day = Integer.parseInt(date.substring(0, 2));
        if (day <= 23) {
            return TimeStamp.TodayPlusDayAndMonth((23 - day - 1), 0);
        } else {
            return TimeStamp.TodayPlusDayAndMonth((23 - day - 1), 1);
        }
    }

    private void verifyThePanelForSelectingFCAmount() {
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().IsSaftyBufferSelected("£20 safety buffer"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().IsSaftyBufferExisted("No safety buffer"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().IsSaftyBufferExisted("£20 safety buffer"));
    }

    private void verifyThePanelForSelectingWhenToApplyToChange() {
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodExist("Change it now but only until my next bill date"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodExist("Change it now and keep it at this amount"));
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodExist("Change it from my next bill date"));
    }

    private void verifyOnlyCHangeItNowButOnlyUntilNextBillDateOptionIsavalaible() {
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodEnable("Change it now but only until my next bill date"));
        Assert.assertFalse(ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodEnable("Change it now and keep it at this amount"));
        Assert.assertFalse(ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodEnable("Change it from my next bill date"));
    }


    private void verifyCLOBAndAllDetailsForThisCPITransaction() {
        String sql = String.format("select contextinfo from hitransactionevent e where e.hitransactionid = %s and e.hieventtype= 'REFREQ'",serviceOrder );
        String outPut= OracleDB.SetToNonOEDatabase().executeQueryReturnListString(sql).get(0);
        Assert.assertTrue(outPut.contains("<responseCode>901</responseCode>"));
        Assert.assertTrue(outPut.contains("<responseCodeDescription>4G already present</responseCodeDescription>"));
    }
}
