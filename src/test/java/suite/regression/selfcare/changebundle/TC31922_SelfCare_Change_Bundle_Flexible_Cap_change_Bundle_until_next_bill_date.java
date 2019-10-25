package suite.regression.selfcare.changebundle;

import framework.utils.RandomCharacter;
import logic.business.db.billing.BillingActions;
import logic.business.db.billing.CommonActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.EventEntity;
import logic.business.entities.OtherProductEntiy;
import logic.business.entities.ServiceOrderEntity;
import logic.business.helper.RemoteJobHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.ChangeMySafetyBufferPage;
import logic.pages.selfcare.MonthlyBundlesAddChangeOrRemovePage;
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
import java.util.HashMap;
import java.util.List;

public class TC31922_SelfCare_Change_Bundle_Flexible_Cap_change_Bundle_until_next_bill_date extends BaseTest {

    String serviceRefOf1stSubscription;
    String serviceOrderID;
    Date newStartDate;
    String customerNumber;

     @Test(enabled = true, description = "TC31922 SelfCare change bundle Flexible Cap change bundle until next bill date", groups = "SelfCare")
    public void TC31922_SelfCare_Change_Bundle_Flexible_Cap_change_Bundle_until_next_bill_date() {
        String path = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_FC_1_bundle_of_SB_and_sim_only";
        test.get().info("Step 1 : Create a customer with FC 1 bundle of sb and sim only");
        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerOrder(path);
        owsActions.getSubscription(owsActions.orderIdNo, "Mobile Ref 1");
        serviceRefOf1stSubscription = owsActions.serviceRef;
        customerNumber = owsActions.customerNo;

        test.get().info("Step 2 : Create the new billing group");
        BaseTest.createNewBillingGroup();

        test.get().info("Step 3: Update the payment collection date is 10");
        BaseTest.updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Step 4: set bill group for customer");
        BaseTest.setBillGroupForCustomer(customerNumber);

        test.get().info("Step 5: Update the start date of customer");
        newStartDate = TimeStamp.TodayMinus20Days();
        CommonActions.updateCustomerStartDate(customerNumber, newStartDate);

        test.get().info("Step 6 : Login to Self Care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);
        SelfCareTestBase.page().verifyMyPersonalInformationPageIsDisplayed();

        test.get().info("Step 7 : Click view or change my tariff detail links");
        MyPersonalInformationPage.MyTariffPage.getInstance().clickViewOrChangeMyTariffDetailsLink();
        SelfCareTestBase.page().verifyMyTariffDetailsPageIsDisplayed();

        test.get().info("Step 8 : verify safety buffer is 20");
        Assert.assertEquals("£20 safety buffer    ACTIVE  as of  " + Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT_IN_PDF),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getSafetyBuffer());

        test.get().info("Step 9: click change my safety buffer button");
        MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").clickChangeMySafetyBufferBtn();
        SelfCareTestBase.page().verifyChangeMySafetyBufferPage();

        test.get().info("Step 10: verify detail screen");
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().IsSaftyBufferSelected("£20 safety buffer"));

        test.get().info("Step 11: select 40 safety buffer and change it now but only until my next bill date");
        ChangeMySafetyBufferPage.getInstance().selectSafetyBuffer("£40 safety buffer");
        ChangeMySafetyBufferPage.getInstance().selectWhenWouldLikeToChangeMethod("Change it now but only until my next bill date");

        test.get().info("Step 12 : verify confirming your changes displays correct result");
        Assert.assertEquals("£20.00", ChangeMySafetyBufferPage.getInstance().getPreviousSafetyBuffer());
        Assert.assertEquals("£40.00", ChangeMySafetyBufferPage.getInstance().getNewSafetyBuffer());

        String message = String.format("Your safety buffer will change now and go back to £20.00 on %s.\r\n"
                + "While our systems update you might still get texts about your original safety buffer. To find out your balance, just call 4488 free from your Tesco Mobile phone or go to "
                + "our iPhone and Android app.",Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),TimeStamp.DATE_FORMAT6));

        Assert.assertEquals(message, ChangeMySafetyBufferPage.getInstance().getComfirmMessage());
        Assert.assertTrue(ChangeMySafetyBufferPage.getInstance().IsWhenWouldYouLikeYourSafetyBufferToChangeBlockDisplayed());

        test.get().info("Step 13 : click save button");
        MonthlyBundlesAddChangeOrRemovePage.getInstance().clickSaveBtn();

        test.get().info("Step 14 :verify my tariff details page displayed with successful alert");
        List<String> alert = SelfCareTestBase.page().successfulMessageStack();
        Assert.assertEquals(1, alert.size());
        Assert.assertEquals("You’ve successfully changed your safety buffer.", alert.get(0));
        Assert.assertEquals(String.format("Your safety buffer has been increased to £40.00 until %s", Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF)),
                MyPersonalInformationPage.MyTariffPage.MyTariffDetailsPage.getInstance("Mobile Ref 1").getSafetyBuffer());


        test.get().info("Step 15 : load user in hub net for customer");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);


        test.get().info("Step 16 : Open details for customer 1st subscription");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByCellValue(serviceRefOf1stSubscription + " Mobile Ref 1");

        test.get().info("Step 17: Verify the one off bundle just added is listed in other products grid");
        HashMap<String, String> otherProducts = OtherProductEntiy.dataForOtherBundleProductNoEndDate("FLEXCAP - [02000-SB-A]", "Bundle", newStartDate);
        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage otherProductsGridSectionPage = SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance();
        Assert.assertEquals(1, otherProductsGridSectionPage.getNumberOfOtherProductsByProduct(otherProducts));


        test.get().info("Step 18 : Verify safety buffer details flexible cap amount");
         SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().clickProductCodeByProductCode("FLEXCAP - [02000-SB-A]");
        String exectedResult = String.format("Date Effective Record(s): Flexible Cap Amount\n20 %s to ...\n40 %s to %s\n20 %s to %s",
                Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT),
                Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT),
                Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT),
                Parser.parseDateFormate(TimeStamp.TodayMinus20Days(), TimeStamp.DATE_FORMAT),
                Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT));
        Assert.assertEquals(exectedResult, SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getFlexibleCapToolTip());

        test.get().info("Step 19 : Open service orders page in hub net for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 20 : Verify customer has 1 expected change bundle SO record");
        HashMap<String, String> temp = ServiceOrderEntity.dataServiceOrder(serviceRefOf1stSubscription, "Change Bundle", "Completed Task");
        int size = ServiceOrdersContentPage.getInstance().getNumberOfServiceOrdersByOrderService(temp);
        Assert.assertEquals(1, size);
        serviceOrderID = ServiceOrdersContentPage.getInstance().getServiceOrderidByType("Change Bundle");

        test.get().info("Step 21 : Open details screen for change bundle SO");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Change Bundle");

        test.get().info("Step 22 : Verify SO details data are updated");
        Assert.assertEquals("£40 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesAdded());
        Assert.assertEquals("£20 safety buffer;", TasksContentPage.TaskPage.DetailsPage.getInstance().getBundlesRemoved());

        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("Refill Amount: £20.00 - Completed", "Completed Task", "Batch")));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("Bonus Money reset to zero", "In Progress", "Batch")));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEventsByEvent(EventEntity.dataForEventServiceOrder("Service Order Completed", "Completed Task", "Batch")));


        test.get().info("Step 23 : submit the do refill BC Job");
        BaseTest.submitDoRefillBCJob();
        test.get().info("Step 24 : submit the do refill NC Job");
        BaseTest.submitDoRefillNCJob();
        test.get().info("Step 25 : submit bundle review Job");
        BaseTest.submitDoBundleRenewJob();

        test.get().info("Step 26 : submit the draft bill run");
        submitDraftBillRun();

        test.get().info("Step 27 : submit the confirm bill run");
        submitConfirmBillRun();

        test.get().info("Step 28 :verify PDF invoice the safety buffer in next period is still 20");
        verifyPDFInvoiceTheSafeTyBufferInNextPeriodIsStill20();
    }

    private void verifyPDFInvoiceTheSafeTyBufferInNextPeriodIsStill20() {
        CareTestBase.page().reLoadCustomerInHubNet(customerNumber);
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);

        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().clickViewPDFBtn();
        String fileName = String.format("%s_%s_%s.pdf", "TC31922", customerNumber, RandomCharacter.getRandomNumericString(9));
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().savePDFFile(fileName);

        String adjustmentsChargesCredits = String.format("%s %s £20 safety buffer for %s 0.00", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DATE_FORMAT_IN_PDF), serviceRefOf1stSubscription);
        String localFile = Common.getFolderLogFilePath() + fileName;
        List<String> pdfList = Common.readPDFFileToString(localFile);
        Boolean flag = false;
        for (int i = 0; i < pdfList.size(); i++) {
            if (pdfList.get(i).contains(adjustmentsChargesCredits))
                flag = true;
        }
        Assert.assertTrue(flag);
    }


}
