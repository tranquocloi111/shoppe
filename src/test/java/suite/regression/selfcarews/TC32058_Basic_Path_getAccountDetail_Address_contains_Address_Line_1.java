package suite.regression.selfcarews;

import framework.utils.RandomCharacter;
import framework.utils.Xml;
import logic.business.db.billing.BillingActions;
import logic.business.ws.ows.OWSActions;
import logic.business.ws.sws.SWSActions;
import logic.business.ws.sws.SelfCareWSTestBase;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.utils.Common;
import logic.utils.XmlUtils;
import org.testng.annotations.Test;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

/**
 * User: Nhi Dinh
 * Date: 17/09/2019
 */
public class TC32058_Basic_Path_getAccountDetail_Address_contains_Address_Line_1 extends BaseTest {
    String customerNumber;
    String billingGroupName;
    String subscriptionNumber;
    String emailAddress;
    String addressLine1;
    String buildingIdentifier;
    String firstName;
    String lastName;
    String username;
    String orderId;

    @Test(enabled = true, description = "TC32058_Basic Path getAccountDetail Address contains Address Line 1", groups = "SelfCareWS")
    public void TC32058_Basic_Path_getAccountDetail_Address_contains_Address_Line_1(){
        test.get().info("Create a CC Customer with address line 1");
        createACCCustomerWithAddressLine1();

        test.get().info("Create new billing group");
        createNewBillingGroup();

        test.get().info("Update bill group payment collection date to 10 days later");
        updateBillGroupPaymentCollectionDateTo10DaysLater();

        test.get().info("Set bill group for customer");
        setBillGroupForCustomer(customerNumber);

        test.get().info("Load customer in hub net");
        CareTestBase.page().loadCustomerInHubNet(customerNumber);

        test.get().info("Record billing related values from details content");
        recordBillingRelatedValuesFromDetailsContent();

        test.get().info("Submit get account detail request");
        SWSActions swsActions = new SWSActions();
        Xml response = swsActions.submitGetAccountDetailsRequest(customerNumber);

        test.get().info("Build expected response data");
        SelfCareWSTestBase selfCareWSTestBase = new SelfCareWSTestBase();
        String expectedResponse = buildCustomerDetailsResponseData();

        test.get().info("Verify get account detail response");
        selfCareWSTestBase.verifyTheResponseOfRequestIsCorrect(customerNumber, expectedResponse, response);
    }

    private String buildCustomerDetailsResponseData(){
        String tempFilePath = "src\\test\\resources\\xml\\sws\\getaccountdetails\\TC32058_response.xml";

        String XmlValue = Common.readFile(tempFilePath).replace("$accountNumber$", customerNumber)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$mainSubscription$", subscriptionNumber)
                .replace("$email$", emailAddress)
                .replace("$BillingGroup$", billingGroupName)
                .replace("$buildingIdentifier$", buildingIdentifier)
                .replace("$addressLine1$", addressLine1)
                .replace("$cardName$", firstName + " " + lastName);

        return Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(XmlValue)));
    }

    private void recordBillingRelatedValuesFromDetailsContent(){
        MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        billingGroupName = BillingActions.tempBillingGroupHeader.getValue();
        subscriptionNumber = DetailsContentPage.BillingInformationSectionPage.getInstance().getMasterMPN().split(" ")[0];
    }

    private void createACCCustomerWithAddressLine1(){
        addressLine1 = "address Line1" + RandomCharacter.getRandomNumericString(8);
        buildingIdentifier = RandomCharacter.getRandomNumericString(8);
        firstName = "first" + RandomCharacter.getRandomNumericString(9);
        lastName = "last" + RandomCharacter.getRandomNumericString(9);
        emailAddress = String.format("mail%s@hsntch.com", RandomCharacter.getRandomNumericString(9));
        username = String.format("un%s@hsntech.com", RandomCharacter.getRandomNumericString(9));

        String tempFilePath = "src\\test\\resources\\xml\\ows\\TC32058_createOrder.xml";
        String xmlRequest = Common.readFile(tempFilePath)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$emailAddress$", emailAddress)
                .replace("$buildingIdentifier$", buildingIdentifier)
                .replace("$addressLine1$", addressLine1)
                .replace("$username$", username);
        String requestFile =  Common.saveXmlFile(username + "_request.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(xmlRequest)));

        OWSActions owsActions = new OWSActions();
        owsActions.createGeneralCustomerWithRequestFile(requestFile);
        customerNumber = owsActions.customerNo;
        orderId = owsActions.orderIdNo;

    }
}
