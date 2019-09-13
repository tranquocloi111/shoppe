package logic.business.ws.ows;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.RandomCharacter;
import framework.utils.Soap;
import framework.utils.Xml;
import logic.business.ws.BaseWs;
import logic.pages.agreement.AgreementSigningContractPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;

public class OWSActions extends BaseWs {
    public String customerNo;
    public String orderIdNo;
    public String fullName;
    public String username;
    public String serviceRef;
    public String firstName;
    public String lastName;
    public String password;
    public String email;
    public String orderRef;
    public Xml requestForNextStep;
    public Xml responseForNextStep;
    public String subscriptionNumber;

    //region XML files
    public static final String EXAMPLE_ORDER = "src\\test\\resources\\xml\\example.xml";
    public static final String FAMILY_PERK_BUNDLE_ORDER = "src\\test\\resources\\xml\\ows\\Family_Perk_Bundle_Order.xml";
    public static final String TC29699_CREATE_ORDER = "src\\test\\resources\\xml\\ows\\TC29699_createOrder.xml";
    private static final String TC32533_CREATE_ORDER = "src\\test\\resources\\xml\\ows\\TC32533_createOrder.xml";
    private static final String TC30432_CREATE_ORDER = "src\\test\\resources\\xml\\ows\\TC30432_createOrder.xml";
    private static final String TC34669_CREATE_ORDER = "src\\test\\resources\\xml\\ows\\CreditAgreementIframe\\TC34669_createOrderRequest.xml";
    private static final String GET_ORDER = "src\\test\\resources\\xml\\commonrequest\\GetOrder.xml";
    private static final String TC1358_CREATE_ORDER = "src\\test\\resources\\xml\\ows\\TC1358_createOrder.xml";
    private static final String GET_CONTRACT = "src\\test\\resources\\xml\\commonrequest\\GetContractRequest.xml";
    private static final String Online_CC_CUSTOMER_WITH_FC_2_BUNDLES_AND_NK2720 = "src\\test\\resources\\xml\\ows\\onlines_CC_customer_with_FC_2_bundles_and_NK2720.xml";
    private static final String CUSTOMER_WITH_2_SUBSCRIPTIONS = "src\\test\\resources\\xml\\sws\\getaccount\\TC32125_createOrder.xml";
    private static final String ONLINES_CC_CUSTOMER_WITH_FC_1_BUNDLE_AND_NK2720 = "src\\test\\resources\\xml\\ows\\onlines_CC_customer_with_FC_1_bundle_and_NK2720.xml";
    private static final String CUSTOMER_WITH_FC_1_BUNDLE_AND_SIMONLY = "src\\test\\resources\\xml\\ows\\customer_with_FC_1_bundle_and_simonly.xml";
    private static final String ONLINES_CC_customer_with_2_FC_family_perk_NK2720 = "src\\test\\resources\\xml\\commonrequest\\onlines_CC_customer_with_2_FC_family_perk_NK2720.xml";
    private static final String CUSTOMER_WITH_PERMITTED_BUNDEL = "src\\test\\resources\\xml\\ows\\customer_with_permitted_bundle.xml";
    private static final String CUSTOMER_WITH_ONE_OFF_BUNDEL = "src\\test\\resources\\xml\\ows\\customer_with_one_off_bundle.xml";
    private static final String CUSTOMER_WITH_FC_3_BUNDLE_AND_SIMONLY = "src\\test\\resources\\xml\\ows\\onlines_CC_customer_with_FC_3_bundles_and_NK2720.xml";
    private static final String CUSTOMER_WITH_3_SUBSCRIPTION = "src\\test\\resources\\xml\\ows\\care_CSA_customer_with_3_subscriptions.xml";
    private static final String ONLINE_CC_CUSTOMER_WITH_2_FC_1_BUNDLE_AND_SIMONLY = "src\\test\\resources\\xml\\ows\\onlines_CC_customer_with_2_FC_1_bundles_and_simonly.xml";
    private static final String ONLINE_CC_CUSTOMER_WITH_FC_1_BUNDLE_OF_SB_AND_SIMONLY = "src\\test\\resources\\xml\\ows\\onlines_CC_customer_with_FC_1_bundle_of_SB_and_sim_only.xml";
    //endregion


    public OWSActions() {
        super();
        putCommonModMap();
    }

    //region SET
    private void putCommonModMap() {
        this.commonModMap.put("firstName", "first" + RandomCharacter.getRandomNumericString(9));
        this.commonModMap.put("lastName", "last" + RandomCharacter.getRandomNumericString(9));
        this.commonModMap.put("username", String.format("un%s@hsntech.com", RandomCharacter.getRandomNumericString(9)));
        this.commonModMap.put("emailAddress", String.format("mail%s@hsntch.com", RandomCharacter.getRandomNumericString(9)));
    }

    private void setCustomerNo() {
        customerNo = response.getTextByTagName("accountNumber");
    }

    private void setOrderIdNo() {
        orderIdNo = response.getTextByTagName("orderId");
    }

    private void setUsername() {
        username = request.getTextByTagName("username");
    }

    private void setPassword() {
        password = request.getTextByTagName("password");
    }

    private void setFullName() {
        fullName = request.getTextByTagName("firstName") + " " + request.getTextByTagName("lastName");
    }

    private void setFirstName() {
        firstName = request.getTextByTagName("firstName");
    }

    private void setLastName() {
        lastName = request.getTextByTagName("lastName");
    }

    private void setServiceRef(String reference) {
        serviceRef = response.getTextByXpath(String.format("//orderItem/serviceRef[@reference='%s']", reference));
    }

    private void setOrderRef() {
        orderRef = request.getTextByXpath("//orderDetail//@orderRef");
    }
    private void setEmail()
    {
        email= request.getTextByTagName("emailAddress");
    }
    //endregion

    //region GET
    public Xml getOrder(String orderIdNo) {
        request = new Xml(new File(GET_ORDER));
        request.setTextByTagName("orderId", orderIdNo);

        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;
    }

    public Xml getContract(String orderIdNo) {
        request = new Xml(new File(GET_CONTRACT));
        request.setTextByTagName("orderId", orderIdNo);

        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;
    }



    public Xml getSubscription(String orderIdNo, String reference) {
        request = new Xml(new File(GET_ORDER));
        request.setTextByTagName("orderId", orderIdNo);
        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        setServiceRef(reference);
        Log.info("Response: " + response.toString());

        return response;
    }

    public String getOrderMpnByReference(int index) {
        return response.getTextByXpath("//orderItem//serviceRef", index - 1);
    }

    public String getOrderMpnByReference(String reference) {
        return response.getTextByXpath(String.format("//orderItem//serviceRef[@reference='%s']", reference));
    }

    public String getCreditAgreementNumberByReference(String reference) {
        Node parentNode = response.getParentNodeByXpath(String.format("//orderItem//serviceRef[@reference='%s']", reference));
        Element deviceElement = response.getChildNodeByTagName((Element) parentNode, "device");
        Element agreement = response.getChildNodeByTagName(deviceElement, "agreement");
        Element number = response.getChildNodeByTagName(agreement, "number");
        String agreementNumber = number.getTextContent();

        return agreementNumber;
    }

    public String getPeripheralCreditAgreementNumberByReference(String reference) {
        Node parentNode = response.getParentNodeByXpath(String.format("//orderItem//serviceRef[@reference='%s']", reference));
        Element deviceElement = response.getChildNodeByTagName((Element) parentNode, "peripheral");
        Element agreement = response.getChildNodeByTagName(deviceElement, "agreement");
        Element number = response.getChildNodeByTagName(agreement, "number");
        String agreementNumber = number.getTextContent();

        return agreementNumber;
    }

    //endregion
    public void submitAddNewTariffRequest() {
        request = new Xml(new File(TC34669_CREATE_ORDER));
        request.setTextByTagName(commonModMap);

        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());

        String agreementSigningUrl = response.getTextByTagName("URL");
        AgreementSigningContractPage agreementSigningContractPage = new AgreementSigningContractPage();
        agreementSigningContractPage.signAgreementViaUI(agreementSigningUrl, 4);

        request.setTextByXpath("//createOrder//@correlationId", response.getTextByXpath("//createOrderResponse//@correlationId"));
        request.setTextByXpath("//verification//@termsAndConditionsAccepted", "true");
        request.setTextByXpath("//verification//@acceptAgreement", "true");
        request.setAttributeTextByXpath("//orderDetail", "orderId", response.getTextByTagName("orderId"));

        Log.info("Request: " + request.toString());
        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        setCustomerNo();
        Log.info("Account number:" + customerNo);
        setOrderIdNo();
        Log.info("OrderId number:" + orderIdNo);
        setUsername();
        setPassword();
        checkAsyncProcessIsCompleted(orderIdNo);
    }

    public Xml acceptOrderForCustomer() {
        requestForNextStep.setTextByXpath("//createOrder//@correlationId", responseForNextStep.getTextByXpath("//createOrderResponse//@correlationId"));
        requestForNextStep.setAttributeTextByXpath("//orderDetail", "orderId", responseForNextStep.getTextByTagName("orderId"));
        requestForNextStep.setTextByXpath("//verification//@termsAndConditionsAccepted", "true");
        if (requestForNextStep.getTextByXpath("//receipt//@dateTime").isEmpty()) {
            requestForNextStep.setTextByXpath(requestForNextStep.getTextByXpath("//receipt//@dateTime"), Parser.parseDateFormate(TimeStamp.Today(), "yyyy-MM-dd") + "T00:00:00");
        }

        Log.info("Request: " + requestForNextStep.toString());
        response = Soap.sendSoapRequestXml(this.owsUrl, requestForNextStep.toSOAPMessage());
        setCustomerNo();
        Log.info("Account number:" + customerNo);
        setOrderIdNo();
        Log.info("OrderId number:" + orderIdNo);
        checkAsyncProcessIsCompleted(orderIdNo);

        return response;
    }

    //region Create Order

    public void createOrderHavingFamilyPerkBundle() {
        request = new Xml(new File(TC32533_CREATE_ORDER));
        request.setTextByTagName(commonModMap);

        Log.info("Request:  " + request.toString());
        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        setCustomerNo();
        Log.info("Account number:" + customerNo);
        setOrderIdNo();
        Log.info("OrderId number:" + orderIdNo);
        checkAsyncProcessIsCompleted(orderIdNo);
    }

    public void createOrderAndSignAgreementByUI(){
        request = new Xml(new File(TC29699_CREATE_ORDER));
        request.setTextByTagName(commonModMap);
        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());

        String agreementSigningUrl = response.getTextByTagName("URL");
        AgreementSigningContractPage agreementSigningContractPage = new AgreementSigningContractPage();
        agreementSigningContractPage.signAgreementViaUI(agreementSigningUrl, 1);

        request.setTextByXpath("//createOrder//@correlationId", response.getTextByXpath("//createOrderResponse//@correlationId"));
        request.setAttributeTextByXpath("//orderDetail","orderId",response.getTextByTagName("orderId"));
        request.setTextByXpath("//verification//@termsAndConditionsAccepted", "true");
        request.setTextByXpath("//verification//@acceptAgreement", "true");

        Log.info("Request: " + request.toString());
        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        setCustomerNo();
        Log.info("Account number:" + customerNo);
        setOrderIdNo();
        Log.info("OrderId number:" + orderIdNo);
        checkAsyncProcessIsCompleted(orderIdNo);
    }

    public void createOrderAndSignAgreementByUI(String filePath, int agreementCount) {
        request = new Xml(new File(filePath));
        request.setTextByTagName(commonModMap);
        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());

        String agreementSigningUrl = response.getTextByTagName("URL");
        AgreementSigningContractPage agreementSigningContractPage = new AgreementSigningContractPage();
        agreementSigningContractPage.signAgreementViaUI(agreementSigningUrl, agreementCount);

        request.setTextByXpath("//createOrder//@correlationId", response.getTextByXpath("//createOrderResponse//@correlationId"));
        request.setAttributeTextByXpath("//orderDetail", "orderId", response.getTextByTagName("orderId"));
        request.setTextByXpath("//verification//@termsAndConditionsAccepted", "true");
        request.setTextByXpath("//verification//@acceptAgreement", "true");

        Log.info("Request: " + request.toString());
        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        setCustomerNo();
        Log.info("Account number:" + customerNo);
        setOrderIdNo();
        Log.info("OrderId number:" + orderIdNo);
        checkAsyncProcessIsCompleted(orderIdNo);
    }

    public void createCustomerWithFCAndDevice() {
        request = new Xml(new File(TC30432_CREATE_ORDER));
        request.setTextByTagName(commonModMap);

        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());

        String agreementSigningUrl = response.getTextByTagName("URL");
        String correlation = response.getTextByXpath("//createOrderResponse//@correlationId");
        AgreementSigningContractPage agreementSigningContractPage = new AgreementSigningContractPage();
        agreementSigningContractPage.signAgreementViaUI(agreementSigningUrl, 2);

        request.setTextByXpath("//createOrder//@correlationId", correlation);
        request.setTextByXpath("//verification//@termsAndConditionsAccepted", "true");
        request.setTextByXpath("//verification//@acceptAgreement", "true");

        Log.info("Request: " + request.toString());
        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        setCustomerNo();
        Log.info("Account number:" + customerNo);
        setOrderIdNo();
        Log.info("OrderId number:" + orderIdNo);
        setFullName();
        checkAsyncProcessIsCompleted(orderIdNo);
    }

    public Xml createACCCustomerWithViaCareInhandMasterCard() {
        subscriptionNumber = "0" + RandomCharacter.getRandomNumericString(9) + "0";
        request = new Xml(new File(TC1358_CREATE_ORDER));
        request.setTextByTagName(commonModMap);
        request.setTextByXpath("//orderItem//serviceRef", subscriptionNumber);

        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());
        setOrderRef();
        setOrderIdNo();
        setFullName();

        requestForNextStep = request;
        responseForNextStep = response;
        return response;
    }

    public void createGeneralCustomerOrder(String path) {
        request = new Xml(new File(path));
        request.setTextByTagName(commonModMap);
        request.setTextByTagName("billGroupId", "2");
        request.setTextByTagName("password", "password1");

        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());
        setCustomerNo();
        Log.info("Account number:" + customerNo);
        setOrderIdNo();
        Log.info("OrderId number:" + orderIdNo);
        setUsername();
        setFirstName();
        setLastName();
        setPassword();
        setFullName();
        setEmail();
        checkAsyncProcessIsCompleted(orderIdNo);
    }

    public void createGeneralCustomerOrderForChangePassword(String path) {
        String email = Config.getProp("emailUsername");
        request = new Xml(new File(path));
        request.setTextByTagName(commonModMap);
        request.setTextByTagName("emailAddress", email);
        request.setTextByTagName("password", "password1");


        response = Soap.sendSoapRequestXml(this.owsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());
        setCustomerNo();
        Log.info("Account number:" + customerNo);
        setOrderIdNo();
        Log.info("OrderId number:" + orderIdNo);
        setUsername();
        setPassword();
        setFirstName();
        setLastName();
        setFullName();
        checkAsyncProcessIsCompleted(orderIdNo);
    }

    public void createAnOnlinesCCCustomerWithFC2BundlesAndNK2720() {
        createGeneralCustomerOrder(Online_CC_CUSTOMER_WITH_FC_2_BUNDLES_AND_NK2720);
    }

    public void createACCCustomerWith2SubscriptionOrder() {
        createGeneralCustomerOrder(CUSTOMER_WITH_2_SUBSCRIPTIONS);
    }

    public void createAnOnlinesCCCustomerWithFC1BundleAndNK2720() {
        createGeneralCustomerOrder(ONLINES_CC_CUSTOMER_WITH_FC_1_BUNDLE_AND_NK2720);
    }

    public void createAnOnlinesCCCustomerWithFC1BundleAndSimOnly() {
        createGeneralCustomerOrder(CUSTOMER_WITH_FC_1_BUNDLE_AND_SIMONLY);
    }

    public void createAnOnlinesCCCustomerWith2FCFamilyPerkAndNK2720() {
        createGeneralCustomerOrder(ONLINES_CC_customer_with_2_FC_family_perk_NK2720);
    }

    public void createACustomerWithOneOffBundle() {
        createGeneralCustomerOrder(CUSTOMER_WITH_ONE_OFF_BUNDEL);
    }

    public void createACustomerWithPermittedBundle() {
        createGeneralCustomerOrder(CUSTOMER_WITH_PERMITTED_BUNDEL);
    }

    public void createAnOnlinesCCCustomerWithFC3BundleAndSimOnly() {
        createGeneralCustomerOrder(CUSTOMER_WITH_FC_3_BUNDLE_AND_SIMONLY);
    }

    public void createACustomerWith3Subscriptions() {
        createGeneralCustomerOrder(CUSTOMER_WITH_3_SUBSCRIPTION);
    }

    public void createAnOnlinesCCCustomerWith2FC1BundleAndSimOnly() {
        createGeneralCustomerOrder(ONLINE_CC_CUSTOMER_WITH_2_FC_1_BUNDLE_AND_SIMONLY);
    }

    public void createAnOnlineCCCustomerWithFC1BundleOfSBAndSimonly() {
        createGeneralCustomerOrder(ONLINE_CC_CUSTOMER_WITH_FC_1_BUNDLE_OF_SB_AND_SIMONLY);
    }

    //endregion

}
