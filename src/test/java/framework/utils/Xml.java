package framework.utils;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Xml {
    private Document doc;

    public Xml(File file) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            this.doc = docBuilder.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            this.doc = null;
        }
    }

    public Xml (String xmlString){
        try{
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            this.doc = docBuilder.parse(new ByteArrayInputStream(xmlString.getBytes()));
        }catch (ParserConfigurationException | SAXException | IOException e){
            e.printStackTrace();
            this.doc = null;
        }
    }

    public Xml(SOAPMessage mess) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mess.writeTo(outputStream);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            this.doc = docBuilder.parse(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (SOAPException | IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            this.doc = null;
        }
    }

    public NodeList getElementsByTagName(String tagName) {
        return this.doc.getElementsByTagName(tagName);
    }

    public NodeList getElementsByXpath(String xpathExp) {
        try {
            XPathFactory xpathfactory = XPathFactory.newInstance();
            XPath xpath = xpathfactory.newXPath();
            XPathExpression expr = xpath.compile(xpathExp);
            return (NodeList) expr.evaluate(this.doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setTextByTagName(String tagName, String value) {
        NodeList nodes = getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            nodes.item(i).setTextContent(value);
        }
    }

    public void setTextByTagName(Map<String, String> mods) {
        mods.forEach((k, v) -> setTextByTagName(k, v));
    }

    public void setTextByXpath(String xpathExp, String value) {
        NodeList nodes = getElementsByXpath(xpathExp);
        for (int i = 0; i < nodes.getLength(); i++) {
            nodes.item(i).setTextContent(value);
        }
    }

    public void setTextByXpath(Map<String, String> mods) {
        mods.forEach((k, v) -> setTextByXpath(k, v));
    }

    public List<String> getTextsByTagName(String tagName) {
        NodeList nodes = getElementsByTagName(tagName);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add(nodes.item(i).getTextContent());
        }
        return list;
    }

    public String getTextByTagName(String tagName) {
        NodeList nodes = getElementsByTagName(tagName);
        return nodes.item(0).getTextContent();
    }

    public List<String> getTextsByXpath(String tagName) {
        NodeList nodes = getElementsByXpath(tagName);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add(nodes.item(i).getTextContent());
        }
        return list;
    }

    public String getTextByXpath(String tagName) {
        NodeList nodes = getElementsByXpath(tagName);
        return nodes.item(0).getTextContent();
    }

    public String getTextByXpath(String tagName, int index) {
        NodeList nodes = getElementsByXpath(tagName);
        return nodes.item(index).getTextContent();
    }

    public String getNodeValueByXpath(String tagName, String attributeName) {
        NodeList nodes = getElementsByXpath(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.hasAttributes()) {
                Attr attr = (Attr) node.getAttributes().getNamedItem(attributeName);
                if (attr != null) {
                    return attr.getValue();
                }
            }
        }
        return null;
    }

    public void setAttributeTextAllNodesByTagName(String tagName, String attributeName, String value){
        NodeList nodes = getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            ((Element)node).setAttribute(attributeName, value);
        }
    }

    public void setAttributeTextByXpath(String tagName, String attributeName, String value){
        NodeList nodes = getElementsByXpath(tagName);
        Node node = nodes.item(0);
        ((Element)node).setAttribute(attributeName, value);
    }

    public String toString() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter sw = new StringWriter();
            transformer.transform(new DOMSource(this.doc), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return "";
    }

    public SOAPMessage toSOAPMessage() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Result outputTarget = new StreamResult(outputStream);
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(this.doc), outputTarget);

            return MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (TransformerException | SOAPException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Element getChildNodeByTagName(Element parent, String tagName){

        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()){
            if (child instanceof Element && tagName.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }

    public String getAttributeTextByXpath(String xpath, String attributeName, int index){
        NodeList nodes = getElementsByXpath(xpath);
        return nodes.item(index).getAttributes().getNamedItem(attributeName).getNodeValue();
    }

    public Node getParentNodeByXpath(String xpathExp) {
        Node parentNode = null;
        NodeList nodes = getElementsByXpath(xpathExp);
        parentNode = nodes.item(0).getParentNode();
        return parentNode;
    }

    public List<Element> getListChildNodeByTagName(Element parent, String tagName){
        List<Element> listChildNode = new ArrayList<>();

        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()){
            if (child instanceof Element && tagName.equals(child.getNodeName())) {
                listChildNode.add((Element) child);
            }
        }
        return listChildNode;
    }

    public String getAttributeTextByXpath(String xpath, String attributeName) {
        NodeList nodes = getElementsByXpath(xpath);
        return nodes.item(0).getAttributes().getNamedItem(attributeName).getNodeValue();
    }

}
