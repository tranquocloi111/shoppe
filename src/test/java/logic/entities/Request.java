package logic.entities;

import logic.utils.Xml;

import javax.xml.soap.SOAPMessage;
import java.util.ArrayList;
import java.util.List;

public class Request {
    private static Xml xml;
    private static Address address;
    //Order tag name
    public static String nodeRootTagNameOrder = "ord1:createOrder";

    public static String attrCorrelationIdOrder = "correlationId";

    public static String nodeFirstNameOrder = "firstName";

    public static String nodeLastNameOrder = "lastName";

    public static String nodeEmailAddressOrder = "emailAddress";

    public static String nodeUserNameOrder = "username";

    public static String nodePassWordOrder = "password";

    public static String nodeVerificationOrder = "verification";

    public static String attrTermsAndConditionsAcceptedOrder = "termsAndConditionsAccepted";

    public static String attrAcceptAgreementOrder = "acceptAgreement";

    public static String nodeBillGroupIdOrder = "billGroupId";

    public static String nodeCardExpiryDateOrder = "cardExpiryDate";

    public static String nodeCardNumberOrder = "cardNumber";

    public static String nodeCardTypeOrder = "cardType";

    public static String nodeCVVOrder = "cvv";

    public static String nodeOrderDetailOrder = "orderDetail";

    public static String attrOrderIdOrder = "orderId";

    public static String nodeAgreementNumberOrder = "number";

    public static String nodeServiceRefOrder = "serviceRef";

    public static String nodeReceiptOrder = "receipt";

    public static String nodeBuildingIdentifierOrder = "buildingIdentifier";

    public static String nodeAddressLine1Order = "addressLine1";

    public static String nodeAddressLine2Order = "addressLine2";

    public static String nodeTownOrder = "town";

    public static String nodeCountryOrder = "country";

    public static String nodePostcodeOrder = "postcode";

    public static String nodePtcAbsCodeOrder = "ptc_abs_code";


    //FindAddres
    public static String nodeRootTagNameFA = "order:findAddressResponse";

    public static String nodeAddressFA = "address";

    public static String nodeBuildingIdentifierFA = "buildingIdentifier";

    public static String nodeAddressLine1FA = "addressLine1";

    public static String nodeAddressLine2FA = "addressLine2";

    public static String nodeTownFA = "town";

    public static String nodeCountryFA = "country";

    public static String nodePostcodeFA = "postcode";

    public static String nodePtcAbsCodeFA = "ptc_abs_code";

    public static String nodematchCodeFA = "matchCode";


    public static void setBillingAddress(Address address) {
        xml = new Xml();
        xml.modifyNodeValueByTag(Request.nodeBuildingIdentifierOrder, address.getBuildingIdentifier());
        xml.modifyNodeValueByTag(Request.nodeAddressLine1Order, address.getAddressLine1());
        xml.modifyNodeValueByTag(Request.nodeTownOrder, address.getTown());
        xml.modifyNodeValueByTag(Request.nodeCountryOrder, address.getCountry());
        xml.modifyNodeValueByTag(Request.nodePostcodeOrder, address.getPostcode());
        xml.modifyNodeValueByTag(Request.nodePtcAbsCodeOrder, address.getPtcAbsCode());
    }

    public static List<Address> addresses(SOAPMessage soapMessage) {
        xml = new Xml(soapMessage);
        List<Address> list = new ArrayList<Address>();
        for (int i = 0; i < xml.getNodesByTag(nodeAddressFA).getLength(); i++) {
            address = new Address();

            address.setBuildingIdentifier(xml.getNodeByTag(nodeBuildingIdentifierFA, i).getFirstChild().getNodeValue());
            address.setAddressLine1(xml.getNodeByTag(nodeAddressLine1FA, i).getFirstChild().getNodeValue());
            if (xml.getNodeByTag(nodeAddressLine2FA, i) != null) {
                address.setAddressLine2(xml.getNodeByTag(nodeAddressLine2FA, i).getFirstChild().getNodeValue());
            }
            address.setTown(xml.getNodeByTag(nodeTownFA, i).getFirstChild().getNodeValue());
            address.setCountry(xml.getNodeByTag(nodeCountryFA, i).getFirstChild().getNodeValue());
            address.setPostcode(xml.getNodeByTag(nodePostcodeFA, i).getFirstChild().getNodeValue());
            address.setPtcAbsCode(xml.getNodeByTag(nodePtcAbsCodeFA, i).getFirstChild().getNodeValue());

            list.add(address);
        }

        return list;
    }

}
