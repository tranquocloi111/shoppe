package logic.business.ws.sws;

import framework.utils.Log;
import framework.utils.Soap;
import framework.utils.Xml;
import logic.business.ws.BaseWs;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import suite.regression.care.CareTestBase;

import java.io.File;
import java.sql.Date;

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

    public Xml buildSimpleAccountSummaryResponseData(String file, Date startDate, String customerNumber, String subscriptionNumber){
        Xml response = new Xml(new File(file));

        String sStartDate =  Parser.parseDateFormate(startDate, TimeStamp.DateFormatXml());
        String SNextBillDate = Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DateFormatXml());

        String accountName = "Mr " + CareTestBase.getCustomerName();

        response.setTextByTagName("accountNumber", customerNumber);
        response.setTextByTagName("accountName",accountName);
        response.setTextByTagName("startDate",sStartDate);
        response.setTextByTagName("nextBillDate", SNextBillDate);
        response.setTextByTagName("endDate", "");
        response.setTextByTagName("subscriptionNumber", subscriptionNumber);

        response.setAttributeTextAllNodesByXpath("tariff", "startDate", sStartDate);

        return response;
    }


}
