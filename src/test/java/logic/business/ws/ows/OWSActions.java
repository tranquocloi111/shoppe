package logic.business.ws.ows;

import logic.business.ws.BaseWs;
import logic.pages.agreement.AgreementSigningContractPage;
import framework.utils.Log;
import framework.utils.RandomCharacter;
import framework.utils.Soap;
import framework.utils.Xml;
import suite.TestBase;

import java.io.File;

public class OWSActions extends BaseWs {
    public String customerNo;
    public String orderIdNo;

    //region XML files
    public static final String EXAMPLE_ORDER = "src\\test\\resources\\xml\\example.xml";
    public static final String FAMILY_PERK_BUNDLE_ORDER = "src\\test\\resources\\xml\\ows\\Family_Perk_Bundle_Order.xml";
    public static final String NC_ORDER = "C:\\GIT\\TM\\hub_testauto\\src\\test\\resources\\xml\\ows\\TC29699_createOrder.xml";
    //endregion


    public OWSActions() {
        super();
        putCommonModMap();
    }

    private void putCommonModMap() {
        this.commonModMap.put("firstName",  "first" + RandomCharacter.getRandomNumericString(9));
        this.commonModMap.put("lastName", "last" + RandomCharacter.getRandomNumericString(9));
        this.commonModMap.put("username", String.format("un%s@hsntech.com", RandomCharacter.getRandomNumericString(9)));
        this.commonModMap.put("emailAddress", String.format("mail%s@hsntch.com", RandomCharacter.getRandomNumericString(9)));
    }
    private void setCustomerNo(){
        customerNo = response.getTextByTagName("accountNumber");
    }

    private void setOrderIdNo(){
        orderIdNo = response.getTextByTagName("orderId");
    }


    public void createOrderHavingFamilyPerkBundle() {
        request = new Xml(new File(FAMILY_PERK_BUNDLE_ORDER));
        request.setTextByTagName(commonModMap);

        Log.info("Request: \n" + request.toString());
        response = Soap.sendSoapRequestXml(this.url, request.toSOAPMessage());
        Log.info("Response: \n" + response.toString());
        setCustomerNo();

    }

    public void createOrderAndSignAgreementByUI(){
        request = new Xml(new File(NC_ORDER));
        request.setTextByTagName(commonModMap);
        response = Soap.sendSoapRequestXml(this.url, request.toSOAPMessage());

        String agreementSigningUrl = response.getTextByTagName("URL");
        AgreementSigningContractPage agreementSigningContractPage = new AgreementSigningContractPage();
        agreementSigningContractPage.signAgreementViaUI(agreementSigningUrl);

        request.setTextByXpath("//createOrder//@correlationId", response.getTextByXpath("//createOrderResponse//@correlationId"));
        request.setAttributeTextByXpath("//orderDetail","orderId",response.getTextByTagName("orderId"));
        request.setTextByXpath("//verification//@termsAndConditionsAccepted", "true");
        request.setTextByXpath("//verification//@acceptAgreement", "true");

        Log.info("Request: " + request.toString());
        response = Soap.sendSoapRequestXml(this.url, request.toSOAPMessage());
        setCustomerNo();
        Log.info("Account number:" + customerNo);
        setOrderIdNo();
        Log.info("OrderId number:" + orderIdNo);
        TestBase.waitForAsyncProcessComplete(orderIdNo);
    }


    public static void main(String[] args) {

     OWSActions owsActions = new OWSActions();
     owsActions.createOrderAndSignAgreementByUI();

    }
}
