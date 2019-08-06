package suite.regression.care;

import framework.config.Config;
import framework.utils.Xml;
import javafx.util.Pair;
import logic.business.db.billing.BillingActions;
import logic.business.helper.MiscHelper;
import logic.business.ws.ows.OWSActions;
import logic.pages.BasePage;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.DetailsContentPage;
import logic.pages.care.find.FindPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.pages.care.main.LoginPage;
import logic.pages.care.main.ServiceOrdersPage;
import logic.pages.care.options.ChangeSubscriptionNumberPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.testng.Assert;

import java.sql.Date;
import java.util.List;

public class CareTestBase extends BasePage {
    LoginPage loginPage;
    FindPage findPage;
    String userName;
    String passWord;

    private CareTestBase() {
        loginPage = new LoginPage();
        findPage = new FindPage();
        userName = Config.getProp("careUserName");
        passWord = Config.getProp("carePassword");
    }

    public static CareTestBase page() {
        return new CareTestBase();
    }

    public void loadCustomerInHubNet(String customerId) {
        loginPage.navigateToLoginPage();
        loginPage.login(userName, passWord);

        findPage.findCustomer(new Pair<String, String>("Customer Number", customerId));
        findPage.openCustomerByIndex(1);
    }

    public void reLoadCustomerInHubNet(String customerId){
        MenuPage.HeaderMenuPage.getInstance().clickCustomersTab();
        findPage.findCustomer(new Pair<String, String>("Customer Number", customerId));
        findPage.openCustomerByIndex(1);
    }

    public void clickApplyBtn() {
        DetailsContentPage.AddressInformationSection.getInstance().clickApplyBtn();
    }
    public void clickEditBtn()
    {
        DetailsContentPage.AddressInformationSection.getInstance().clickEditBtn();
    }
    public void verifyCreateOrderResponse(OWSActions owsActions, Xml xml){
        Assert.assertNotNull(xml.getTextByXpath("//createOrderResponse//@correlationId"));
        Assert.assertEquals(owsActions.orderRef, xml.getTextByTagName("orderRef"));
        Assert.assertEquals("1", xml.getTextByXpath("//orderProcessResponse//responseCode"));
        Assert.assertEquals("Order pending Confirmation required", xml.getTextByXpath("//orderProcessResponse//responseMessage"));
        Assert.assertTrue(Integer.parseInt(xml.getTextByTagName("orderId")) > 0);
        Assert.assertEquals("CO_280", xml.getTextByXpath("//orderError//errorCode"));
        Assert.assertEquals("ERROR", xml.getTextByXpath("//orderError//errorType"));
        Assert.assertEquals("Confirmation Required – Terms and Conditions must be accepted", xml.getTextByXpath("//orderError//errorDescription"));
    }

    public  void verifyContractPdfCommonData(List<String> reader, String amount, OWSActions owsActions){
        Assert.assertEquals("Your contract", reader.get(0));
        Assert.assertEquals("Order date Order number Order total", reader.get(1));
        Assert.assertEquals(String.format("%s %s %s", Parser.parseDateFormate(TimeStamp.Today(), "dd MMMM yyyy"), owsActions.orderIdNo, amount), reader.get(2));
        Assert.assertEquals("Your details", reader.get(3));
        Assert.assertEquals("Name:" + String.format(" Mr %s", owsActions.fullName), reader.get(4));
        Assert.assertEquals("Address: 6 LUKIN STREET, LONDON E1 0AA", reader.get(5));
    }

    public void verifyAcceptOrderResponse(OWSActions owsActions, Xml response) {
        Assert.assertNotNull(response.getTextByXpath("//createOrderResponse//@correlationId"));
        Assert.assertEquals(owsActions.customerNo, response.getTextByTagName("accountNumber"));
        Assert.assertTrue(Integer.parseInt(response.getTextByTagName("orderId")) > 0);
        Assert.assertEquals(owsActions.orderRef, response.getTextByTagName("orderRef"));
        Assert.assertEquals("0", response.getTextByXpath("//orderProcessResponse//responseCode"));
    }

    public void verifyCustomerStartDateAndBillingGroupAreUpdatedSuccessfully(Date newStartDate) {
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        Assert.assertEquals(Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT), CommonContentPage.CustomerSummarySectionPage.getInstance().getCustomerSummaryStartDate());
        Assert.assertEquals(BillingActions.tempBillingGroupHeader.getValue(), DetailsContentPage.BillingInformationSectionPage.getInstance().getBillingGroup());
    }

    public void checkBundleToolTip(String[] bundles) {
        String expectTooltip;
        String actualTooltip;
        for (String bundle : bundles) {
            int endIndex = bundle.lastIndexOf((" - £"));
            String bundleSubstr = bundle.substring(0, endIndex);
            expectTooltip = String.format("Additional Information\n%s\nFair Usage Warning =\nFair Usage Limit =", bundleSubstr);
            actualTooltip = ServiceOrdersPage.ChangeBundle.getInstance().bundleToolTip(bundle);
            Assert.assertEquals(expectTooltip, actualTooltip);
        }

    }

    public String verifyServiceOrderCompleteScreenHasProvisionWaitMessage(){
        String provisionWaitStatusMessage = String.format("*** Service Order has been set to Status of Provision Wait, and is due to be processed on %s ***", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(),"dd/MM/yyyy"));
        Assert.assertEquals(provisionWaitStatusMessage, ServiceOrdersPage.ServiceOrderComplete.getInstance().getMessage());

        return provisionWaitStatusMessage;
    }

    public void clickNextButton() {
        clickNextBtn();
    }

    public void openInvoiceDetailsScreen(){
        MenuPage.LeftMenuPage.getInstance().clickSummaryLink();
        MenuPage.RightMenuPage.getInstance().clickRefreshLink();
        MenuPage.LeftMenuPage.getInstance().clickInvoicesItem();
        InvoicesContentPage.getInstance().clickInvoiceNumberByIndex(1);
    }

    public  void verifyInvoiceDetailsAreCorrect(String issued, String end, String dueDate, String status){
        Assert.assertEquals(issued, InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getIssued());
        Assert.assertEquals(end, InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getEnd());
        Assert.assertEquals(dueDate, InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getDueDate());
        Assert.assertEquals(status, InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getStatus(1));
    }

    public void openChangeSubscriptionNumberPage(){
        MenuPage.RightMenuPage.getInstance().clickChangeSubscriptionNumberLink();
    }
    public String updateTheSubscriptionNumberAndClickNextButton(){
        ChangeSubscriptionNumberPage.ChangeSubscriptionNumber content = ChangeSubscriptionNumberPage.ChangeSubscriptionNumber.getInstance();
        String newSubscriptionNumber = newSubscriptionNumber();
        content.setNewSubscriptionNumber(newSubscriptionNumber);
        content.setNotes("Change MPN");
        clickNextButton();
        return newSubscriptionNumber;
    }
    private String newSubscriptionNumber(){
        return "0" + MiscHelper.RandomStringF9()+"0";
    }
    public void verifyConfirmChangingSubscriptionNumberMessageIsCorrect(String subscriptionNumber, String newSubscriptionNumber){
        ChangeSubscriptionNumberPage.ConfirmChangingSubscriptionNumber content = ChangeSubscriptionNumberPage.ConfirmChangingSubscriptionNumber.getInstance();
        String expectedMessage = "Subscription number "+subscriptionNumber+" will be changed to "+newSubscriptionNumber+" and this cannot be undone. Are you sure? Select 'Next' to confirm.";
        Assert.assertEquals(expectedMessage, content.getConfirmMessage());
        clickNextButton();
    }

    public void verifyServiceOrderCompletePageAndClickReturnToCustomerButton(){
        String message = ServiceOrdersPage.ServiceOrderComplete.getInstance().getMessage();
        Assert.assertEquals("*** The Change of Subscription has been completed ***", message);

        clickReturnToCustomer();
    }

}
