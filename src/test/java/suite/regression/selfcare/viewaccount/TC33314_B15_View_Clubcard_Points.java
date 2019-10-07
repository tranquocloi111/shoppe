package suite.regression.selfcare.viewaccount;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.RandomCharacter;
import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.CardDetailsEntity;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.selfcare.ClubCarDTransactionEnity;
import logic.business.helper.DateTimeHelper;
import logic.business.helper.FTPHelper;
import logic.business.helper.RemoteJobHelper;
import logic.business.helper.SFTPHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.selfcare.MakeAOneOffPaymentPage;
import logic.pages.selfcare.MyClubCardPointPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TC33314_B15_View_Clubcard_Points extends BaseTest {

    String sub = "07644567890";
    String subFC = "07759195580";
    String discountGroup = "14335";
    String reference;

    @Test(enabled = true, description = "TC33314 B15 view clubcard points", groups = "SelfCare")
    public void TC33314_B15_View_Clubcard_Points() {
        test.get().info("Step 1: Create a CC customer ");
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_2_subscriptions";
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getOrder(owsActions.orderIdNo);
        subFC = owsActions.getOrderMpnByReference(1);
        sub = owsActions.getOrderMpnByReference(2);
        String customerNumber = owsActions.customerNo;

        test.get().info("Step 2: create new billing group");
        createNewBillingGroup();
        test.get().info("Step 3: update bill group payment collection date to 10 day later ");
        updateBillGroupPaymentCollectionDateTo10DaysLater();
        test.get().info("Step 4: set bill group for customer");
        setBillGroupForCustomer(customerNumber);
        test.get().info("Step 5: update start date for customer");
        CommonActions.updateCustomerStartDate(customerNumber, TimeStamp.TodayMinus20Days());

        test.get().info("Step 6: prepare top up file for 2 subscriptions then upload to the server");
        prepareTopUpFileFor2SubscriptionsThenUploadToServer();

        test.get().info("Step 7: submit do refill bc Job");
        RemoteJobHelper.getInstance().submitDoRefillBcJob(TimeStamp.Today());
        test.get().info("Step 8: submit do refill NC Job");
        RemoteJobHelper.getInstance().submitDoRefillNcJob(TimeStamp.Today());
        test.get().info("Step 9: submit do bundle renew Job");
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(TimeStamp.Today());
        test.get().info("Step 10: submit do drap bill Job");
        RemoteJobHelper.getInstance().submitDraftBillRun();
        test.get().info("Step 11: submit do comfirm bill Job");
        RemoteJobHelper.getInstance().submitConfirmBillRun();

        test.get().info("Step 12: load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);


        test.get().info("Step 13: Record discount group code of Mobile NC");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(sub + " Mobile NC");
        discountGroup = SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getDiscountGroupCode();

        test.get().info("Step 14: Verify invoice status is confirmed");
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        Assert.assertEquals(InvoicesContentPage.getInstance().getRowNumberOfInvoiceTable(), 1);
        Assert.assertEquals(InvoicesContentPage.getInstance().getStatusByIndex(1), "Confirmed");

        test.get().info("Step 15: Login in to selfcare");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 16:open the my club card points screen");
        MyPersonalInformationPage.MyBillsAndPaymentsSection.getInstance().clickViewDetailsOfMyCLubCardPoints();


        test.get().info("Step 17:verify the my clubcard points screen displayed with correct data");
        verifyMyClubCardPointsScreenDisplayedWithCorrectData();


        test.get().info("Step 18:click back button");
        MyClubCardPointPage.getInstance().clickBackBtn();
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 19:click one off payment link");
        SelfCareTestBase.page().clickMakeAOneOfPayment();
        SelfCareTestBase.page().verifyMakeAOneOffPayment();

        test.get().info("Step 20: make a partial payment");
        String paymentAmount = "5.23";
        CardDetailsEntity cardDetails = new CardDetailsEntity();
        cardDetails.setCardType("MasterCard");
        cardDetails.setCardNumber("5105105105105100");
        cardDetails.setCardHolderName("Mr Card Holder");
        cardDetails.setCardSecurityCode("123");
        cardDetails.setCardExpiryMonth("01");
        cardDetails.setCardExpiryYear(TimeStamp.TodayPlus5Year().toString().substring(2, 4));

        MakeAOneOffPaymentPage.CardDetailSection.getInstance().enterPaymentAmount("10");
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().inputCardDetail(cardDetails);
        MakeAOneOffPaymentPage.PesonalDetailsSection.getInstance().clickSubmitBtn();

        test.get().info("Step 21:open the my club card points screen");
        MyPersonalInformationPage.MyBillsAndPaymentsSection.getInstance().clickViewDetailsOfMyCLubCardPoints();

        test.get().info("Step 22: verify my clubcard point is not change");
        verifyMyClubCardPointsScreenDisplayedWithCorrectData();


        test.get().info("Step 23: make a payment with enough money");
        MyClubCardPointPage.getInstance().clickBackBtn();
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        SelfCareTestBase.page().clickMakeAOneOfPayment();
        SelfCareTestBase.page().verifyMakeAOneOffPayment();

        MakeAOneOffPaymentPage.CardDetailSection.getInstance().enterPaymentAmount("20");
        MakeAOneOffPaymentPage.CardDetailSection.getInstance().inputCardDetail(cardDetails);
        MakeAOneOffPaymentPage.PesonalDetailsSection.getInstance().clickSubmitBtn();

        test.get().info("Step 24: submit payment allocation batch job");
        RemoteJobHelper.getInstance().submitPaymentAllocationBatchJobRun();

        test.get().info("Step 25:open the my club card points screen");
        MyPersonalInformationPage.MyBillsAndPaymentsSection.getInstance().clickViewDetailsOfMyCLubCardPoints();

        test.get().info("Step 24: verify my club card point screen displayed with changed data");
        verifyMyClubCardPointsScreenDisplayedWithChangedData();


        test.get().info("Step 26:Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 27:verify invoice status changed to fully paid");
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        Assert.assertEquals("Fully Paid", InvoicesContentPage.getInstance().getStatusByIndex(1));
        //issue 2307. This check point will fail before this issue is fixed.
        //Remove the check point with defect 2307
        test.get().info("Step 27: verify invoice number are equal to reference number of lastesst clubcard point transaction ");
//        String invoiceNumber= InvoicesContentPage.getInstance().getInvoiceNumber();
//        Assert.assertEquals(invoiceNumber,reference);
    }

    public void prepareTopUpFileFor2SubscriptionsThenUploadToServer() {
        String path = "src\\test\\resources\\txt\\selfcare\\viewaccount\\TM_SMP_TOPUPS_20150327114604";
        String topupsTemplate = Common.readFile(path);
        String timeStamp = TimeStamp.TodayMinus10DatsReturnFullFormat();
        String fileName = "TM_SMP_TOPUPS_" + timeStamp + ".txt";
        topupsTemplate = topupsTemplate
                .replace("20150327114603", timeStamp)
                .replace("FCMPN", subFC)
                .replace("NCMPN", sub)
                //.Replace("BCMPN", ServiceRefOf3rdSubscription)
                .replace("22/03/2015", Parser.parseDateFormate(TimeStamp.TodayMinus5Days(), TimeStamp.DATE_FORMAT_IN_PDF));
        String remotePath = Config.getProp("CDRSFTPFolder");
        String localPath = Common.getFolderLogFilePath() + fileName;
        Common.writeFile(topupsTemplate, localPath);
        SFTPHelper.getInstance().upFileFromLocalToRemoteServer(localPath, remotePath);
        RemoteJobHelper.getInstance().waitForTopUpFile(fileName);
    }

    private void verifyMyClubCardPointsScreenDisplayedWithCorrectData() {

        Assert.assertEquals("Clubcard number: **************2784", MyClubCardPointPage.getInstance().getClubCardNumber());
        Assert.assertEquals("Please note, any Clubcard points collected before 19th September 2012 won't be shown." +
                " See tescomobile.com/clubcard for full details on how to collect Clubcard " +
                "points with us.", MyClubCardPointPage.getInstance().getClubCardNote());
        Assert.assertEquals("4 items found, displaying all items.", MyClubCardPointPage.getInstance().getPageBanner());
        Assert.assertEquals("1", MyClubCardPointPage.getInstance().getPageLinkText());

        Assert.assertEquals(4, MyClubCardPointPage.getInstance().getNumberRowExceptHeader());


        Assert.assertEquals("tescomobile.com/clubcard", MyClubCardPointPage.getInstance().getClubCardLinkText());
        Assert.assertEquals("_blank", MyClubCardPointPage.getInstance().getClubCardNumberTagert());
        Assert.assertEquals("http://tescomobile.com/clubcard", MyClubCardPointPage.getInstance().getClubCardNumberLinkHref());


        try {
            List<String> receiptIdList = new ArrayList<>();
            String sql = "select receiptid from receiptproperty where propertykey='DISCGRPCD' and propvalchar=" + discountGroup;
            ResultSet rs = OracleDB.SetToNonOEDatabase().executeQuery(sql);
            while (rs.next()) {
                receiptIdList.add(rs.getString(1));
            }

            Assert.assertEquals(4, receiptIdList.size());

        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }


        HashMap<String, String> expectedEnity = ClubCarDTransactionEnity.getClubCardTransactionEnity(TimeStamp.Today(),
                String.format("Top Up: Top-up with Tesco - stored debit/credit via 4444 (%s)", sub), "£ 3.00", "9");
        Assert.assertEquals(1, MyClubCardPointPage.getInstance().findRowInTable(expectedEnity));
        expectedEnity = ClubCarDTransactionEnity.getClubCardTransactionEnity(TimeStamp.Today(),
                String.format("Top Up: Top-up with Tesco - top-up debit/credit via 4444 (%s)", sub), "£ 6.00", "18");
        Assert.assertEquals(1, MyClubCardPointPage.getInstance().findRowInTable(expectedEnity));
        expectedEnity = ClubCarDTransactionEnity.getClubCardTransactionEnity(TimeStamp.Today(),
                String.format("Top Up: Top-up with Tesco - top-up voucher via 4444 (%s)", sub), "£ 5.00", "15");
        Assert.assertEquals(1, MyClubCardPointPage.getInstance().findRowInTable(expectedEnity));
        expectedEnity = ClubCarDTransactionEnity.getClubCardTransactionEnity(TimeStamp.Today(),
                String.format("Top Up: (%s)", sub), "£ 4.00", "0");
        Assert.assertEquals(1, MyClubCardPointPage.getInstance().findRowInTable(expectedEnity));

    }

    private void verifyMyClubCardPointsScreenDisplayedWithChangedData(){

        Assert.assertEquals(5, MyClubCardPointPage.getInstance().getNumberRowExceptHeader());

        HashMap<String, String> expectedEnity = ClubCarDTransactionEnity.getClubCardTransactionEnity(TimeStamp.Today(),
                "Invoice", "£ 37.00", "120");
        Assert.assertEquals(1, MyClubCardPointPage.getInstance().findRowInTable(expectedEnity));
        reference=MyClubCardPointPage.getInstance().getLastReferenceNumber();

    }


}
