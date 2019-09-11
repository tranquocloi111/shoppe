package suite.shakeout;

import framework.utils.Xml;
import javafx.util.Pair;
import logic.business.db.billing.CommonActions;
import logic.business.entities.CreditAgreementPaymentsEntiy;
import logic.business.entities.CreditAgreementsGridEntity;
import logic.business.entities.EventEntity;
import logic.business.entities.SelfCareProductSummaryEntity;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CreditAgreementsContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.pages.selfcare.MyPersonalInformationPage;
import logic.pages.selfcare.OrderConfirmationPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;
import suite.regression.selfcare.SelfCareTestBase;

public class TC34669_A_New_Customer_New_Order_Multiple_deals_MAX_number_of_deals_with_CCA extends BaseTest {

    @Test(enabled = false, description = "TC34669 A New Customer New Order Multiple deals MAX number of deals with CCA", groups = "Smoke")
    public void TC34669_A_New_Customer_New_Order_Multiple_deals_MAX_number_of_deals_with_CCA() {
        test.get().info("Step 1 : Create a customer with FC and device");
        OWSActions owsActions = new OWSActions();
        owsActions.submitAddNewTariffRequest();

        test.get().info("Step 2 : Verify Agreement Is Signed In DB");
        String orDerId = owsActions.orderIdNo;
        Assert.assertEquals(4, CommonActions.getAgreementIsSigned(orDerId));

        test.get().info("Step 3 : Load customer in hub net");
        String customerNumber = owsActions.customerNo;
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Step 4 : Verify 4 Subscriptions And 4 Credit Agreements");
        String [] ccaArr = Verify4SubscriptionsAnd4CreditAgreements(owsActions, orDerId);

        test.get().info("Step 5 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 6 : Open service orders content for customer");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Sales Order");
        Pair<String, String> event = EventEntity.setEvents("Description", "MPNs Assigned");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "Terms And Conditions Requested for CA");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", "Agreement Accepted");
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));

        Assert.assertEquals("New Order", TasksContentPage.TaskPage.DetailsPage.getInstance().getOrderType());
        Assert.assertEquals("Completed Task", TasksContentPage.TaskPage.DetailsPage.getInstance().getTransactionStatus());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), TasksContentPage.TaskPage.DetailsPage.getInstance().getReceiptDate());

        test.get().info("Step 7 : Back to customer");
        CareTestBase.page().reLoadCustomerInHubNet( customerNumber);

        test.get().info("Step 8 : Open service orders content for customer");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();

        test.get().info("Step 9 : Verify order task details");
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Order Task");
        Assert.assertEquals("Order Task", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getDescription());
        Assert.assertEquals("Completed Task", TasksContentPage.TaskPage.TaskSummarySectionPage.getInstance().getStatus());
        Assert.assertEquals(6, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getRowNumberOfEventGird());

        event = EventEntity.setEvents("Description", String.format("Refill subscription has been requested. (Order id: %s)", orDerId));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", String.format("Activate subscription has been requested. (Order id: %s)", orDerId));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        event = EventEntity.setEvents("Description", String.format("Inventory successfully created in HUB. Inventory created for orderId: %s. (Order id: %s)", orDerId,orDerId));
        Assert.assertEquals(1, TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getNumberOfEvents(event));
        Assert.assertTrue(TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(2).contains("Products Released. Confirm Response: Supplier [Hansen HUB]") || TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(3).contains("Products Released. Confirm Response: Supplier [Hansen HUB]"));
        Assert.assertTrue( TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(2).contains("Products Released. Confirm Response: Supplier [Oakwood]") || TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(3).contains("Products Released. Confirm Response: Supplier [Oakwood]"));
        Assert.assertTrue( TasksContentPage.TaskPage.EventsGridSectionPage.getInstance().getDescriptionByIndex(5).contains("Receipt Created. Receipt(s) created:"));

        test.get().info("Step 10 : Login to self care");
        SelfCareTestBase.page().LoginIntoSelfCarePage(owsActions.username, owsActions.password, customerNumber);

        test.get().info("Step 11 : Verify new agreement details in self care");
        MyPersonalInformationPage.MyPreviousOrdersPage.getInstance().clickViewByIndex(1);

        String [] productSummary = SelfCareProductSummaryEntity.setProductSummary("1 x","Credit Agreement","£16.00");
        Assert.assertEquals(4, OrderConfirmationPage.ProductSummary.getInstance().getNumberOfProductSummary(productSummary));
        productSummary = SelfCareProductSummaryEntity.setProductSummary("","-  HTC Desire HD","£99.00");
        Assert.assertEquals(4, OrderConfirmationPage.ProductSummary.getInstance().getNumberOfProductSummary(productSummary));
        productSummary = SelfCareProductSummaryEntity.setProductSummary("4 x","Insurance Band A £4/month (collected by separate payment)","£0.00");
        Assert.assertEquals(1, OrderConfirmationPage.ProductSummary.getInstance().getNumberOfProductSummary(productSummary));

        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().countRowOfDetailItemsByFirstRow("Mobile 1"));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().countRowOfDetailItemsByFirstRow("Mobile 3"));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().countRowOfDetailItemsByFirstRow("Mobile 4"));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().countRowOfDetailItemsByFirstRow("Mobile 5"));

        Assert.assertEquals(4, OrderConfirmationPage.OrderDetails.getInstance().getItemsCountByRowText("Credit Agreement"));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().getItemsCountByRowText(ccaArr[0]));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().getItemsCountByRowText(ccaArr[1]));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().getItemsCountByRowText(ccaArr[2]));
        Assert.assertEquals(1, OrderConfirmationPage.OrderDetails.getInstance().getItemsCountByRowText(ccaArr[3]));
    }

    private String [] Verify4SubscriptionsAnd4CreditAgreements(OWSActions owsActions, String orderId){
        Xml response = owsActions.getOrder(orderId);

        String ccaNo1 = response.getTextByXpath("//orderItem[1]//device//agreement//number");
        String ccaNo3 = response.getTextByXpath("//orderItem[2]//device//agreement//number");
        String ccaNo4 = response.getTextByXpath("//orderItem[3]//device//agreement//number");
        String ccaNo5 = response.getTextByXpath("//orderItem[4]//device//agreement//number");


        String subNo1 = response.getTextByXpath("//orderItem[1]//serviceRef");
        String subNo3 = response.getTextByXpath("//orderItem[2]//serviceRef");
        String subNo4 = response.getTextByXpath("//orderItem[3]//serviceRef");
        String subNo5 = response.getTextByXpath("//orderItem[4]//serviceRef");

        MenuPage.LeftMenuPage.getInstance().clickCreditAgreementsItem();
        Assert.assertEquals(4, CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getRowOfCreditAgreementsGrid());
        Assert.assertEquals(4, CreditAgreementsContentPage.CreditAgreementPaymentsGrid.getInstance().getRowOfCreditAgreementPaymentsGrid());

        verifyAgreementDetails(subNo1, ccaNo1);
        verifyAgreementDetails(subNo3, ccaNo3);
        verifyAgreementDetails(subNo4, ccaNo4);
        verifyAgreementDetails(subNo5, ccaNo5);

        return new String[]{ccaNo1,ccaNo3,ccaNo4,ccaNo5};

    }

    private void verifyAgreementDetails(String subscriptionNumber, String agreementNumber){
        Assert.assertEquals(1, CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCreditAgreement(CreditAgreementsGridEntity.getCreditAgreementGird(subscriptionNumber)).size());
        CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().clickExpandButtonOfCABySubscription(subscriptionNumber);

        CreditAgreementsContentPage.CreditAgreementsGridPage.CADetailClass caDetailClass = CreditAgreementsContentPage.CreditAgreementsGridPage.getInstance().getCADetailBySubscription(subscriptionNumber);
        Assert.assertEquals("AU Standard", caDetailClass.dealType());
        Assert.assertEquals("1", caDetailClass.ccaPhases());
        Assert.assertEquals("AU Standard", caDetailClass.dealType());
        Assert.assertEquals("24", caDetailClass.ccaFirstPhaseMonths());
        Assert.assertEquals("£16.00", caDetailClass.ccaFirstPhaseMonthlyCharge());
        Assert.assertEquals("0", caDetailClass.ccaSecondPhaseMonths());
        Assert.assertEquals("£0.00", caDetailClass.ccaSecondPhaseMonthlyCharge());
        Assert.assertEquals("24", caDetailClass.ccaTotalDurationMonths());
        Assert.assertEquals("£384.00", caDetailClass.contractValue());
        Assert.assertEquals("£16.00", caDetailClass.monthlyChargePaid());
        Assert.assertEquals("£0.00", caDetailClass.otherPayments());
        Assert.assertEquals(agreementNumber, caDetailClass.agreementNumber());
        Assert.assertEquals("HTC Desire HD", caDetailClass.device());

        Assert.assertEquals(1, CreditAgreementsContentPage.CreditAgreementPaymentsGrid.getInstance().getNumberOfCreditAgreementPayments(CreditAgreementPaymentsEntiy.getCreditAgreementPaymentWithInitialPayment(subscriptionNumber)));
    }
}
