package logic.business.ws.sws;

import framework.utils.Log;
import framework.utils.Soap;
import framework.utils.Xml;
import logic.business.ws.BaseWs;
import logic.utils.Common;
import logic.utils.XmlUtils;
import org.testng.Assert;

import java.io.File;

public class SWSActions extends BaseWs {
    //region XML files
    public static final String MAINTAIN_BUNDLE_REQUEST = "src\\test\\resources\\xml\\sws\\TC32533_request.xml";
    public static final String GET_BUNDLE_REQUEST = "src\\test\\resources\\xml\\sws\\getbundle\\Get_bundle_request.xml";
    public static final String GETACCOUNTSUMMARYREQUEST = "src\\test\\resources\\xml\\sws\\getaccount\\Get_Account_Summary_Request.xml";

    //endregion

    public SWSActions() {
        super();
    }

    public Xml submitMaintainBundleRequest(String customerNumber, String subscriptionNumber){
        request = new Xml(new File(MAINTAIN_BUNDLE_REQUEST));
        request.setTextByTagName("sel:accountNumber", customerNumber);
        request.setTextByTagName("sel:subscriptionNumber", subscriptionNumber);

        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response : " + response.toString());
        return response;
    }

    public Xml submitMaintainBundleRequest(String customerNumber, String subscriptionNumber, String newStartDate, String nextScheduledRefill, String serviceOrderId){
        request = new Xml(new File(MAINTAIN_BUNDLE_REQUEST));
        request.setTextByTagName("sel:accountNumber", customerNumber);
        request.setTextByTagName("sel:subscriptionNumber", subscriptionNumber);


        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response : " + response.toString());
        return response;
    }

    public Xml submitGetBundleRequest(String customerNumber, String subscriptionNumber){
        request = new Xml(new File(GET_BUNDLE_REQUEST));
        request.setTextByTagName("sel:accountNumber", customerNumber);
        request.setTextByTagName("sel:subscriptionNumber", subscriptionNumber);

        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response : " + response.toString());
        return response;
    }

    public Xml submitGetAccountSummaryRequestToSelfCareWS(String customerNumber){
        request = new Xml(new File(GETACCOUNTSUMMARYREQUEST));
        request.setTextByTagName("sel:accountNumber", customerNumber);

        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;
    }

    public void verifyGetAccountSummaryResponse(Xml expectedResponse, Xml response) {
        String expectedFile = Common.saveXmlFile("ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(expectedResponse.toString())));
        String actualFile = Common.saveXmlFile("ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(response.toString())));

        Assert.assertEquals(1, Common.compareFile(expectedFile, actualFile).size());
    }
}
