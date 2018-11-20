package logic.utils;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class Soap {
    public static SOAPMessage sendSoapRequest(String endpointUrl, SOAPMessage request) {
        try {
            // Send HTTP SOAP request and get response
            SOAPConnection soapConnection
                    = SOAPConnectionFactory.newInstance().createConnection();
            SOAPMessage response = soapConnection.call(request, endpointUrl);
            // Close connection
            soapConnection.close();
            return response;
        } catch (SOAPException ex) {
            Log.error(ex.getStackTrace().toString());
        }
        return null;
    }


}
