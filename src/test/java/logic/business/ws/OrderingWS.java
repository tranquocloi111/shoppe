package logic.business.ws;

import logic.entities.Address;
import logic.entities.Request;
import logic.utils.Soap;
import logic.utils.Xml;

import javax.xml.soap.SOAPMessage;



public class OrderingWS{
    String url = "http://hsnsha-qadb01.cn.hsntech.int:38089/order/orderService";
    Xml xml ;
    public OrderingWS() {
        // url = Config.getProp("owsUrl");
    }

    public  SOAPMessage createOrder(final Xml xml) {
        xml.xmlRequest = xml.getXmlRequest();
        Address address = Request.addresses(findAddress()).get(0);

        xml.modifyNodeValueByTag(Request.nodeBillGroupIdOrder, "2");

        Request.setBillingAddress(address);

        xml.modifyNodeValueByTag(Request.nodeUserNameOrder, xml.userName);

        return Soap.sendSoapRequest(url, xml.xmlRequest);
    }

    public  SOAPMessage acceptOrder(final Xml xml) {
        return Soap.sendSoapRequest(url, xml.xmlRequest);
    }

    private  SOAPMessage findAddress() {
        return Soap.sendSoapRequest(url, Soap.readSoapMessage("C:\\GIT\\TM\\hub_testauto\\src\\test\\resources\\xml\\commonrequestxml\\FindAddressRequest.xml"));
    }

}
