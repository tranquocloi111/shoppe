package suite.regression.selfcare.modifysubscription;

import framework.utils.Log;
import logic.business.db.OracleDB;
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

import java.sql.Date;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

public class TC53291_Change_SB_temporary_pending_change_tariff_so_new_so_allowed extends BaseTest {
    String sub = "07209466820";
    String inventoryID;
    String serviceOrder="16606";

    @Test(enabled = true, description = "TC53286 chaneg SB Immdiate pending change Tariff SO new SO rejected", groups = "SelfCare")
    public void TC53286_Change_SB_Immediate_Pending_Change_Tariff_SO_New_SO_rejected() {

        test.get().info("Step 1: Create a CC customer");
        String path = "src\\test\\resources\\xml\\selfcare\\modifysubscription\\TC53291_createOrder";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        String customerNumber=owsActions.customerNo;
        owsActions.getSubscription(owsActions.orderIdNo,"Mobile FC");
        sub=owsActions.serviceRef;
        serviceOrder=owsActions.orderIdNo;

        //Change a customer tariff amd the Service Order Status is PWAIT
        test.get().info("Step 2: load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 3: select change tariff from RHS actions");
        MenuPage.RightMenuPage.getInstance().clickChangeTariffLink();

        test.get().info("Step 4: select subscription from drop down");
        ServiceOrdersContentPage.getInstance().clickNextBtn();

        test.get().info("Step 5: open the search tariff window");
        ServiceOrdersContentPage.getInstance().clicknewTariffSearchBtn();

        test.get().info("Step 6: Build Search tariFff by specified criteria Entity");
        TariffSearchCriteriaEnity tariffSearchCriteriaEnity=new TariffSearchCriteriaEnity();
        tariffSearchCriteriaEnity.setBillingType("Flexible Cap");
        tariffSearchCriteriaEnity.setContractPeriod("24");

        test.get().info("Step 7: Select Tariff by code then click next button");
        TariffSearchPage.getInstance().searchAndSelectTariffByCode(tariffSearchCriteriaEnity, "FC24-2000-500");

        test.get().info("Step 8: click next button on change tariff wizad");
        ServiceOrdersContentPage.getInstance().clickNextBtn();

        test.get().info("Step 9: select specified bundles on change bundle screen then click then next button");
        ServiceOrdersPage.ChangeBundle.getInstance().selectBundlesByName("£30 safety buffer" );
        ServiceOrdersContentPage.getInstance().clickNextBtn();

        test.get().info("Step 10: finish change bundle wizard then back to customer");
        ServiceOrdersContentPage.getInstance().clickNextBtn();
        ServiceOrdersContentPage.getInstance().clickReturnToCustomer();

        test.get().info("Step 11: Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username,owsActions.password,customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 12: access my tariff detail screen");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 13: click safety buffer ");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile FC").clickChangeMySafetyBufferBtn();
        SelfCareTestBase.page().verifyChangeMySafetyBufferPage();

        test.get().info("Step 14: verify A Panel With A Message Regarding The FC SB Option");
        verifyAPanelWithAMessageRegardingTheFCSBOption();

        test.get().info("Step 15: verify a panel for selecting FC amount");
        verifyThePanelForSelectingFCAmount();

        test.get().info("Step 16: verify a panel for selecting when to apply the change");
        verifyThePanelForSelectingWhenToApplyToChange();

        test.get().info("Step 17: select a safety buffer is more than existing SB");
        ChangeMySafetyBufferPage.getInstance().selectSafetyBuffer("£40 safety buffer");

        test.get().info("Step 18: verify a panel for selecting when to apply the change");
        verifyThePanelForSelectingWhenToApplyToChangeAgain();

        test.get().info("Step 19: verify confirm message");
        verifyConfirmMssg();

        test.get().info("Step 20: click save changes in change my safety buffer page");
        ChangeMySafetyBufferPage.getInstance().clickSaveBtn();

        test.get().info("Step 21: My tariff detail page displayed with successfull message");
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();
        List<String> listMssg= SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(listMssg.get(0),"You’ve successfully changed your safety buffer.");

        test.get().info("Step 22: load user in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();

        test.get().info("Step 23: open the service order content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 24: verify add safety buffer service order status is completed");
        HashMap<String, String> expectedServiceOrder = ServiceOrderEntity.dataServiceOrderForChangePassword("Change Bundle", "Completed Task");
        Assert.assertEquals(ServiceOrdersContentPage.getInstance().getNumberOfServiceOrders(expectedServiceOrder), 1);

        test.get().info("Step 25: verify service order detail page");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");
        Assert.assertEquals(sub +" Mobile FC", TasksContentPage.TaskPage.DetailsPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals("£40 safety buffer;",TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("£20 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());
        Assert.assertEquals("Yes", TasksContentPage.TaskPage.DetailsPage.getInstance().getTemporaryChangeFlag());
        serviceOrder= TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getSoID();

        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);

        test.get().info("Step 26: open the subscription content for customer");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 27: open the first subscription");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);

        test.get().info("Step 28:open  FC safety buffer detail page");
        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().clickProductCodeByProductCode("FLEXCAP - [02000-SB-A]");

        test.get().info("Step 29: verify inventory id detail page");
        verifyInventoryidDetailPage();

        test.get().info("Step 30: verify invproperty table for the new flexible cap bundle");
        verifyInvpropertyTableForTheNewFlexibleCapBundle();

        test.get().info("Step 31: verify CLOB and all details for thí CPIT transaction");
        verifyCLOBAndAllDetailsForThisCPITransaction();
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


    private void verifyAPanelWithAMessageRegardingTheFCSBOption() {

        Assert.assertEquals(sub + " - Mobile FC", ChangeMySafetyBufferPage.getInstance().getMobilePhone());
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().calculateNextAllowanceDate(), ChangeMySafetyBufferPage.getInstance().getNextAllowanceDate());
        Assert.assertEquals("£20.00", ChangeMySafetyBufferPage.getInstance().getMonthlySafetyBuffer());
        String expectedFlexbleCapMsg = "Your safety buffer is £20.00."
                + " This is the maximum you’ve allowed yourself to spend on things outside your monthly allowance,"
                + " so your bill will never be higher than you expect.";
        Assert.assertEquals(ChangeMySafetyBufferPage.getInstance().getFlexibleCapMsg(), expectedFlexbleCapMsg);
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

    private void verifyThePanelForSelectingWhenToApplyToChangeAgain() {
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodEnable("Change it now but only until my next bill date"));
        Assert.assertFalse(ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodEnable("Change it now and keep it at this amount"));
        Assert.assertFalse(ChangeMySafetyBufferPage.getInstance().isWhenWouldLikeToChangeMethodEnable("Change it from my next bill date"));
    }

    private void verifyInventoryidDetailPage() {
        inventoryID = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getHUbInternalId();
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getStartDate());
        Assert.assertEquals(String.format("20 ( %s )", getNextBillDate()), SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getFlexibleCapAmount());

        String expectedToolTip = String.format("Date Effective Record(s): Flexible Cap Amount\n" +
                        "20 %s to ...\n" +
                        "40 %s to %s\n" +
                        "20 %s to %s", getNextBillDate(), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), Parser.parseDateFormate(getNextBillASATDate(), TimeStamp.DATE_FORMAT), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT)
                , Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        String toolTip = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getFlexibleCapToolTip();
        Assert.assertEquals(toolTip, expectedToolTip);
    }

    private void verifyInvpropertyTableForTheNewFlexibleCapBundle() {
        String sql = String.format("select propertykey,propvalnumber,datestart,dateend from invproperty ip where ip.inventoryid = %s and propvalnumber=40 and ip.propertykey ='FCAMT'",
                inventoryID);
        boolean flag = false;
        try {
            ResultSet rs = OracleDB.SetToNonOEDatabase().executeQuery(sql);
            while (rs.next()) {
                Assert.assertTrue(rs.getString(1).equalsIgnoreCase("FCAMT"));
                String provalnumber = rs.getString(2);
                Assert.assertEquals("40", provalnumber);
                Assert.assertEquals(Parser.parseDateFormate(rs.getDate(3), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF));
                Assert.assertEquals(Parser.parseDateFormate(rs.getDate(4), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(getNextBillASATDate(), TimeStamp.DATE_FORMAT_IN_PDF));
                flag = true;

            }
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        Assert.assertTrue(flag);

    }

    private void verifyCLOBAndAllDetailsForThisCPITransaction() {
        String sql = String.format("select contextinfo from hitransactionevent e where e.hitransactionid = %s and e.hieventtype= 'REFREQ'",serviceOrder );
        String outPut= OracleDB.SetToNonOEDatabase().executeQueryReturnListString(sql).get(0);
        Assert.assertTrue(outPut.contains("<CPITransaction>"));
        Assert.assertTrue(outPut.contains("<UpliftAmount>2000</UpliftAmount>"));
        Assert.assertTrue(outPut.contains("<TPST_CODE>SUCCESS</TPST_CODE></SuccessfulCredit></CPITransaction>"));
    }
}
