package logic.utils;

import javax.xml.soap.*;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;

public class Soap {

    public static SOAPMessage readSoapMessage(String requestPath) {
        SOAPMessage message = null;
        try {
            message = MessageFactory.newInstance().createMessage();

            SOAPPart soapPart = message.getSOAPPart();
            soapPart.setContent(new StreamSource(new FileInputStream(requestPath)));

            message.saveChanges();
            return  message;
        }catch (Exception ex){
            try{
                SOAPPart soapPart = message.getSOAPPart();
                soapPart.setContent(new StreamSource(new StringReader(requestPath)));

                message.saveChanges();
                return  message;
            }catch (Exception ex1) {
                Log.error(ex1.getStackTrace().toString());
            }
        }

        return null;
    }

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
