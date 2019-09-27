package suite.shakeout;

import framework.utils.Pdf;
import framework.utils.Xml;
import logic.business.entities.OtherProductEntiy;
import logic.business.ws.ows.OWSActions;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.ServiceOrdersContentPage;
import logic.pages.care.find.SubscriptionContentPage;
import logic.pages.care.main.TasksContentPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.io.File;
import java.util.List;

public class TC1358_OWS_Basic_Path_New_Customer_New_Order_via_Care_Inhand_Master_Card extends BaseTest {

    @Test(enabled = false, description = "TC1358 OWS Basic Path New Customer New Order via Care Inhand Master Card", groups = "Smoke")
    public void TC1358_OWS_Basic_Path_New_Customer_New_Order_via_Care_Inhand_Master_Card(){
        test.get().info("Step 1 : Create a CC cusotmer with via Care Inhand MasterCard");
        OWSActions owsActions = new OWSActions();
        Xml response =  owsActions.createACCCustomerWithViaCareInhandMasterCard();

        test.get().info("Step 2 : Verify_create_order_response");
        CareTestBase.page().verifyCreateOrderResponse(owsActions, response);

        test.get().info("Step 3 : Call get contract ws method");
        Xml getContractResponseXml =  owsActions.getContract(owsActions.orderIdNo);

        test.get().info("Step 4 : Verify get contract response");
        Assert.assertEquals(owsActions.orderRef, getContractResponseXml.getTextByTagName("orderRef"));

        test.get().info("Step 5 : Verify contract PDF file");
        verifyContractPDFFile(owsActions, getContractResponseXml.getTextByXpath("//contractDetails//contractContent"));

        test.get().info("Step 6 : Accept order for customer");
        response =  owsActions.acceptOrderForCustomer();

        test.get().info("Step 7 : verify accept order response");
        CareTestBase.page().verifyAcceptOrderResponse(owsActions, response);

        test.get().info("Step 8 : Load customer in hub net");
        String customerId = owsActions.customerNo;
        CareTestBase.page().loadCustomerInHubNet(customerId);

        test.get().info("Step 9 : Open subscriptions content for customer");
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();

        test.get().info("Step 10 : Verify HTC WILDFIRE XXX 60 exist in other product and subscription is same as assigned");
        CommonContentPage.SubscriptionsGridSectionPage.getInstance().clickSubscriptionNumberLinkByIndex(1);
        Assert.assertEquals(owsActions.subscriptionNumber, SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getSubscriptionNumber());
        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getRowNumberOfOtherProductsGridTable());
        Assert.assertEquals(3, SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().getNumberOfOtherProducts(OtherProductEntiy.dataForOtherProductHTCWILDFIRE()));

        test.get().info("Step 11 : Verify the details of HTC WILDFIRE XXX 60");
        SubscriptionContentPage.SubscriptionDetailsPage.OtherProductsGridSectionPage.getInstance().clickProductCodeByProductCode("HTC-WILDFIRE-XXX-60");
        Assert.assertTrue(Integer.parseInt(SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getHUbInternalId()) > 0);
        Assert.assertEquals("137695587771463", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getIMEI());
        Assert.assertEquals(owsActions.subscriptionNumber, SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getReference());
        Assert.assertEquals("60", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getInitialPurchasePrice());
        Assert.assertEquals("HTC", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getMake());
        Assert.assertEquals("", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getAgreementNumber());
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT), SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getStartDate());
        Assert.assertTrue(SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getOrderReference().endsWith("-1_DEALINHAND"));
        Assert.assertEquals(Parser.parseDateFormate(TimeStamp.TodayPlus1YearMinus1Day(), TimeStamp.DATE_FORMAT), SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getWarrantyEndDate());
        Assert.assertEquals("1", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getQuantity());
        Assert.assertEquals("60", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getAmountPaid());
        Assert.assertEquals("Wildfire", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getModel());
        Assert.assertEquals("", SubscriptionContentPage.SubscriptionDetailsPage.GeneralSectionPage.getInstance().getUpgradeOrderReference());

        test.get().info("Step 12 : Verify customer card type");
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals("MasterCard", DetailsContentPage.PaymentInformationPage.getInstance().getCardType());

        test.get().info("Step 13 : Verify customer sales channel");
        MenuPage.LeftMenuPage.getInstance().clickServiceOrdersLink();
        ServiceOrdersContentPage.getInstance().clickServiceOrderByType("Sales Order");
        Assert.assertEquals("Telecomm Centre", TasksContentPage.TaskPage.DetailsPage.getInstance().getSalesChannel());
    }

    private void verifyContractPDFFile(OWSActions owsActions, String value){
        String path =  System.getProperty("user.home")+"\\Desktop\\QA_Project\\";
        if(!new File(path).exists())
            Common.createUserDir(path);
        String locationFileName = path + String.format("TC1358Contract_%s.pdf", TimeStamp.Today());
        Pdf.getInstance().saveToPDF(locationFileName, value);

        List<String> pdfList = Pdf.getInstance().getText(locationFileName);
        CareTestBase.page().verifyContractPdfCommonData(pdfList, "£75.00", owsActions);

        Assert.assertEquals("Your device details", pdfList.get(6));
        Assert.assertTrue( pdfList.get(7).startsWith("Number:"));
        Assert.assertEquals("Friendly name chosen: Mobile Ref 1",  pdfList.get(8));
        Assert.assertEquals("Your order details",  pdfList.get(9));
        Assert.assertEquals("Tariff: £10 12Mth SIM Only Tariff 500 Mins 5000 Texts",  pdfList.get(10));
        Assert.assertEquals("Device: HTC Wildfire",  pdfList.get(11));
        Assert.assertEquals("Monthly bundle: Monthly 500MB data allowance",  pdfList.get(12));
        Assert.assertTrue( pdfList.get(13).startsWith("IMEI: 137695587771463"));
        Assert.assertEquals("Any upfront costs", pdfList.get(16));
        Assert.assertEquals("First month's payment: £15.00", pdfList.get(17));
        Assert.assertEquals("Upfront device cost: £60.00", pdfList.get(18));
        Assert.assertEquals("Total upfront cost: £75.00", pdfList.get(19));
        Assert.assertEquals("Your monthly costs", pdfList.get(20));
        Assert.assertEquals("Monthly charges: £15.00", pdfList.get(21));
    }
}
