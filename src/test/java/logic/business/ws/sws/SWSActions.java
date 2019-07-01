package logic.business.ws.sws;

import framework.utils.Log;
import framework.utils.Soap;
import framework.utils.Xml;
import logic.business.ws.BaseWs;

import java.io.File;

public class SWSActions extends BaseWs {
    //region XML files
    public static final String MAINTAIN_BUNDLE_REQUEST = "src\\test\\resources\\xml\\sws\\TC32533_request.xml";
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
}
