package logic.business.ws;

import logic.entities.Address;
import logic.entities.Request;
import logic.entities.Response;
import logic.pages.agreementsigning.AgreementSigningContractPage;
import logic.utils.Log;
import logic.utils.Xml;
import org.w3c.dom.Document;
import suite.TestBase;

import javax.xml.soap.SOAPMessage;

public class BaseWS {

    private static BaseWS baseWS = new BaseWS();
    public SOAPMessage xmlReponse;
    Xml xml;
    OrderingWS ows;

    public static BaseWS page() {
        if (baseWS == null)
            return new BaseWS();
        return baseWS;
    }

    public void createOrderAndSignAgreementByUI(String requestFile) {
        ows = new OrderingWS();
        xml = new Xml();

        xml.LoadXml(requestFile, true);
        xml.xmlResponse = ows.createOrder(xml);

        String agreementSigningUrl = xml.getNodeValueByTag(xml.xmlResponse, Response.nodeUrlOrder);
        AgreementSigningContractPage agreementSigningContractPage = new AgreementSigningContractPage();
        agreementSigningContractPage.signAgreementViaUI(agreementSigningUrl);

        xml.modifyAttributeValueByTag(Request.nodeRootTagNameOrder, Request.attrCorrelationIdOrder, xml.getAttribute(xml.xmlResponse, Response.nodeRootTagNameOrder, Response.attrCorrelationIdOrder));
        xml.modifyAttributeValueByTag(Request.nodeOrderDetailOrder, Request.attrOrderIdOrder, xml.getNodeValueByTag(xml.xmlResponse, Response.nodeOrderIdOrder));
        xml.modifyAttributeValueByTag(Request.nodeVerificationOrder, Request.attrTermsAndConditionsAcceptedOrder, "true");
        xml.modifyAttributeValueByTag(Request.nodeVerificationOrder, Request.attrAcceptAgreementOrder, "true");

        xml.xmlResponse = ows.acceptOrder(xml);
        String customerNumber = xml.getNodeValueByTag(xml.xmlResponse, Response.nodeAccountNumberOrder);
        Log.info("Account number:" + customerNumber);
        String orderID = xml.getNodeValueByTag(xml.xmlResponse, Response.nodeOrderIdOrder);
        System.out.println(orderID);
        Log.info("orderID:" + orderID);
        TestBase.waitForAsyncProcessComplete(orderID);
        xmlReponse = xml.xmlResponse;
    }

}
