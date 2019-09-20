package logic.business.ws.sws;

import framework.utils.Log;
import framework.utils.Soap;
import framework.utils.Xml;
import logic.business.ws.BaseWs;

import java.io.File;

public class SWSActions extends BaseWs {
    //region XML files
    private static final String MAINTAIN_BUNDLE_REQUEST = "src\\test\\resources\\xml\\sws\\TC32533_request.xml";
    private static final String GET_BUNDLE_REQUEST = "src\\test\\resources\\xml\\sws\\getbundle\\Get_bundle_request.xml";
    private static final String GETACCOUNTSUMMARYREQUEST = "src\\test\\resources\\xml\\sws\\getaccount\\Get_Account_Summary_Request.xml";
    private static final String GETSUBSCRIPTIONSUMMARYREQUEST = "src\\test\\resources\\xml\\sws\\getsubscription\\Get_Subscription_Summary_Request.xml";
    private static final String GET_SUBSCRIPTION_SUMMARY_BY_SUBSCRIPTION_NUMBER_REQUEST = "src\\test\\resources\\xml\\sws\\getsubscription\\Get_Subscription_Summary_SubscriptionNumber_Request.xml";
    private static final String GET_INVALID_SUBSCRIPTION_SUMMARY__REQUEST = "src\\test\\resources\\xml\\sws\\getsubscription\\Get_Subscription_Summary_Request.xml";
    private static final String GET_USAGE_SUMMARY_REQUEST = "src\\test\\resources\\xml\\sws\\getusage\\Get_Usage_Summary_Request.xml";
    private static final String GET_CONTRACT_SUMMARY_REQUEST = "src\\test\\resources\\xml\\sws\\getcontract\\Get_Contract_Summary_Request.xml";
    private static final String GET_ACCOUNT_DETAIL_REQUEST = "src\\test\\resources\\xml\\sws\\getaccountdetails\\Get_Account_Detail_Request.xml";

    //endregion

    public SWSActions() {
        super();
    }

    public Xml submitMaintainBundleRequest(String customerNumber, String subscriptionNumber){
        return submitGetByCustomerAndSubscriptionNumbersRequest(MAINTAIN_BUNDLE_REQUEST, customerNumber, subscriptionNumber);
    }

    public Xml submitGetBundleRequest(String customerNumber, String subscriptionNumber){
        return submitGetByCustomerAndSubscriptionNumbersRequest(GET_BUNDLE_REQUEST, customerNumber, subscriptionNumber);
    }

    public Xml submitGetAccountSummaryRequest(String customerNumber){
        request = new Xml(new File(GETACCOUNTSUMMARYREQUEST));
        request.setTextByTagName("sel:accountNumber", customerNumber);

        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;
    }

    public Xml submitMaintainBundleRequest(String path, String customerNumber, String subscriptionNumber){
        request = new Xml(new File(path));
        request.setTextByTagName("sel:accountNumber", customerNumber);
        request.setTextByTagName("sel:subscriptionNumber", subscriptionNumber);

        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response : " + response.toString());
        return response;
    }

    public Xml submitGetSubscriptionSummaryRequestBySubNumber(String subscriptionNumber, boolean isFlag){
        request = new Xml(new File(GET_SUBSCRIPTION_SUMMARY_BY_SUBSCRIPTION_NUMBER_REQUEST));
        request.setTextByTagName("sel:subscriptionNumber", subscriptionNumber);
        if (isFlag)
            request.setTextByTagName("sel:includeInactiveSubscriptionFlag", "true");
        else
            request.setTextByTagName("sel:includeInactiveSubscriptionFlag", "false");

        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;
    }

    public Xml submitGetSubscriptionSummaryRequestByCusNumber(String customerNumber, boolean isFlag){
        request = new Xml(new File(GETSUBSCRIPTIONSUMMARYREQUEST));
        request.setTextByTagName("sel:accountNumber", customerNumber);
        if (isFlag)
            request.setTextByTagName("sel:includeInactiveSubscriptionFlag", "true");
        else
            request.setTextByTagName("sel:includeInactiveSubscriptionFlag", "false");

        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;
    }

    public Xml submitGetUsageSummaryRequest(String customerNumber){
        return submitGetByCustomerNumberRequest(GET_USAGE_SUMMARY_REQUEST, customerNumber);
    }

    public Xml submitGetByCustomerNumberRequest(String filePath, String customerNumber){
        request = new Xml(new File(filePath));
        request.setTextByTagName("sel:accountNumber", customerNumber);

        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;
    }

    public Xml submitGetBySubscriptionNumberRequest(String filePath, String subscriptionNumber){
        request = new Xml(new File(filePath));
        request.setTextByTagName("sel:subscriptionNumber", subscriptionNumber);
        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;

    }
    public Xml submitGetByCustomerAndSubscriptionNumbersRequest(String filePath, String customerNumber, String subscriptionNumber){
        request = new Xml(new File(filePath));
        request.setTextByTagName("sel:accountNumber", customerNumber);
        request.setTextByTagName("sel:subscriptionNumber", subscriptionNumber);
        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;
    }

    public Xml submitGetContractSummaryRequest(String subscriptionNumber){
        return submitGetBySubscriptionNumberRequest(GET_CONTRACT_SUMMARY_REQUEST, subscriptionNumber);
    }

    public Xml submitGetAccountAuthorityRequest(String filePath,String username, String password){
        request = new Xml(new File(filePath));
        request.setTextByTagName("sel:username", username);
        request.setTextByTagName("sel:password", password);
        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;
    }

    public Xml submitGetAccountDetailsRequest(String accountNumber){
        request = new Xml(new File(GET_ACCOUNT_DETAIL_REQUEST));
        request.setTextByTagName("sel:accountNumber", accountNumber);
        response = Soap.sendSoapRequestXml(this.swsUrl, request.toSOAPMessage());
        Log.info("Response: " + response.toString());

        return response;
    }
}

