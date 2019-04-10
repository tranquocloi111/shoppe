package logic.utils;

import logic.entities.Request;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class Xml {
    public SOAPMessage xmlRequest;
    public SOAPMessage xmlResponse;
    private String requestPath;
    private Document xmlDoc;
    private String firstName;
    private String lastName;
    private String emailAddress;
    public String userName;
    private Node rootNode;
    private String xml;

    public Xml() {
        firstName = "first" + RandomCharacter.getRandomNumericString(9);
        lastName = "last" + RandomCharacter.getRandomNumericString(9);
        emailAddress = String.format("mail%s@hsntch.com", RandomCharacter.getRandomNumericString(9));
        userName = String.format("un%s@hsntech.com", RandomCharacter.getRandomNumericString(9));
    }

    public Xml(SOAPMessage soapMessage) {
        firstName = "first" + RandomCharacter.getRandomNumericString(9);
        lastName = "last" + RandomCharacter.getRandomNumericString(9);
        emailAddress = String.format("mail%s@hsntch.com", RandomCharacter.getRandomNumericString(9));
        userName = String.format("un%s@hsntech.com", RandomCharacter.getRandomNumericString(9));
        xmlDoc = convertStringtoDocument(convertDocumentToXmlString(soapMessage));
    }

    public static Document convertStringToXMLDocument(String xml) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            return document;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Document convertStringtoDocument(final String xml) {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            Document document = null;

            db = dbf.newDocumentBuilder();
            document = db.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));

            return document;
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }

        return null;
    }

    public String convertDocumentToXmlString(final Document document) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
        final DOMSource domSource = new DOMSource(document);
        final StringWriter writer = new StringWriter();
        final StreamResult result = new StreamResult(writer);
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(domSource, result);
        return writer.toString();
    }

    public String convertDocumentToXmlString(final SOAPMessage message) {
        String result = null;
        if (message != null) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                result = baos.toString();
            } catch (Exception e) {
            } finally {
                if (baos != null) {
                    try {
                        baos.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
        return result;
    }

    public String getNodeValueByTag(String tag) {
        return xmlDoc.getElementsByTagName(tag).item(0).getFirstChild().getNodeValue();
    }

    public String getNodeValueByTag(SOAPMessage soapMessage, String tag) {
        String xml = convertDocumentToXmlString(soapMessage);
        Document document = convertStringToXMLDocument(xml);
        Element element = (Element) document.getElementsByTagName(tag).item(0);
        return element.getFirstChild().getNodeValue();
    }


//    public static void modifyNodeValueByTag(String tag, String value) {
//        try {
//            xmlDoc.getElementsByTagName(tag).item(0).setTextContent(value);
//        } catch (Exception ex) {
//            Log.error(ex.getStackTrace().toString());
//        }
//    }

    public void modifyNodeValueByTag(String tag, String value) {
        try {
            String xml = convertDocumentToXmlString(xmlRequest);
            Document document = convertStringToXMLDocument(xml);
            document.getElementsByTagName(tag).item(0).setTextContent(value);
            xmlRequest = convertStringToSoapMessage(convertDocumentToXmlString(document));
        } catch (Exception ex) {
            Log.error(ex.getStackTrace().toString());
        }
    }

    public String getAttribute(String tag, String attrName) {
        Element element = (Element) xmlDoc.getElementsByTagName(tag).item(0);
        return element.getAttribute(attrName);
    }

    public String getAttribute(SOAPMessage soapMessage, String tag, String attrName) {
        String xml = convertDocumentToXmlString(soapMessage);
        Document document = convertStringToXMLDocument(xml);
        Element element = (Element) document.getElementsByTagName(tag).item(0);
        return element.getAttribute(attrName);
    }

//    public  void modifyAttributeValueByTag(String tag, String attrName, String value) {
//        try {
//            Element element = (Element) xmlDoc.getElementsByTagName(tag).item(0);
//            element.setAttribute(attrName, value);
//        } catch (Exception ex) {
//            Log.error(ex.getStackTrace().toString());
//        }
//    }

    public void modifyAttributeValueByTag(String tag, String attrName, String value) {
        try {
            String xml = convertDocumentToXmlString(xmlRequest);
            Document document = convertStringToXMLDocument(xml);
            Element element = (Element) document.getElementsByTagName(tag).item(0);
            element.setAttribute(attrName, value);
            xmlRequest = convertStringToSoapMessage(convertDocumentToXmlString(document));

        } catch (Exception ex) {
            Log.error(ex.getStackTrace().toString());
        }
    }

    public SOAPMessage convertStringToSoapMessage(String resMessage) {
        try {
            // Create SoapMessage
            MessageFactory msgFactory = MessageFactory.newInstance();
            SOAPMessage message = msgFactory.createMessage();
            SOAPPart soapPart = message.getSOAPPart();

            // Load the SOAP text into a stream source
            byte[] buffer = resMessage.getBytes();
            ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
            StreamSource source = new StreamSource(stream);

            // Set contents of message
            soapPart.setContent(source);
            message.saveChanges();

            return message;
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return null;
    }

    public Node getNodeByTag(String tag) {
        return xmlDoc.getElementsByTagName(tag).item(0);
    }

    public Node getNodeByTag(String tag, int index) {
        return xmlDoc.getElementsByTagName(tag).item(index);
    }

    public NodeList getNodesByTag(String tag) {
        return xmlDoc.getElementsByTagName(tag);
    }

    public void LoadXml(String xml, boolean firstOrder) {
        this.xml = xml;
        xmlDoc = convertStringToXMLDocument(convertDocumentToXmlString(Soap.readSoapMessage(xml)));
        init(firstOrder);
        initAgreementNumber();
    }

    private void init(Boolean firstOrder) {
        rootNode = xmlDoc.getElementsByTagName("ord1:createOrder").item(0);
        if (firstOrder) {
            firstName = "first" + RandomCharacter.getRandomNumericString(9);
            lastName = "last" + RandomCharacter.getRandomNumericString(9);
            emailAddress = String.format("mail%s@hsntch.com", RandomCharacter.getRandomNumericString(9));
            userName = String.format("un%s@hsntech.com", RandomCharacter.getRandomNumericString(9));
        }
    }

    private void initAgreementNumber() {
        if (getNodesByTag("").getLength() > 0) {
            for (int i = 0; i < getNodesByTag(Request.nodeAgreementNumberOrder).getLength(); i++) {
                getNodesByTag(Request.nodeAgreementNumberOrder).item(0).setTextContent("CATE" + RandomCharacter.getRandomNumericString(9));
            }
        }
    }

    public SOAPMessage getXmlRequest() {
        try {
            xmlRequest = Soap.readSoapMessage(convertDocumentToXmlString(xmlDoc));
            return xmlRequest;
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

}






