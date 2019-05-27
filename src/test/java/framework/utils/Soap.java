package framework.utils;

import framework.utils.Xml;
import javax.xml.soap.*;

public class Soap {
    /**
     * @param endpointUrl
     * @param request
     * @return
     */
    public static Xml sendSoapRequestXml(String endpointUrl, SOAPMessage request) {
        try {
            // Send HTTP SOAP request and get response
            SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            SOAPMessage response = soapConnection.call(request, endpointUrl);
            // Close connection
            //soapConnection.close();
            return new Xml(response);
        } catch (SOAPException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
