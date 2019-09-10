package logic.business.ws.sws;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.entities.MaintainBundleEntity;
import logic.business.ws.BaseWs;
import logic.pages.care.MenuPage;
import logic.pages.care.options.DeactivateSubscriptionPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;
import org.w3c.dom.NodeList;
import suite.regression.care.CareTestBase;

import java.sql.Date;
import java.util.List;

public class SelfCareWSTestBase extends BaseWs {
    public void verifyNormalMaintainBundleResponse(Xml response) {
        Assert.assertEquals(MaintainBundleEntity.getNormalMaintainBundle().getTypeAttribute(), response.getTextByXpath("//message//@type"));
        Assert.assertEquals(MaintainBundleEntity.getNormalMaintainBundle().getCode(), response.getTextByXpath("//message//code"));
        Assert.assertEquals(MaintainBundleEntity.getNormalMaintainBundle().getDescription(), response.getTextByXpath("//message//description"));
    }

    public void verifyTheResponseOfRequestIsCorrect(String userNumber, String expectedResponseFile, Xml response) {
        String actualFile = Common.saveXmlFile(userNumber + "_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(response.toString())));
        Assert.assertEquals(1, Common.compareFile(expectedResponseFile, actualFile).size());
    }

    public void verifyGetBundleResponseAreCorrect(String expectedResponse, Xml response, String customerId, String subscriptionNumber, String serviceOrderId, Date newStartDate) {
        String sStartDate = Parser.parseDateFormate(newStartDate, TimeStamp.DateFormatXml());
        String sNextScheduledRefill = Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DateFormatXml());

        String actualFile = Common.saveXmlFile(customerId + "_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(response.toString())));
        String file = Common.readFile(expectedResponse)
                .replace("$accountNumber$", customerId)
                .replace("$subscriptionNumber$", subscriptionNumber)
                .replace("$startDateOneOffBundle$", Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DateFormatXml()))
                .replace("$startDate$", sStartDate)
                .replace("$nextScheduledRefill$", sNextScheduledRefill)
                .replace("$SOId$", serviceOrderId);
        String expectedResponseFile = Common.saveXmlFile(customerId + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(1, size);
    }

    public void deactivateAccountInFutureAndReturnToCustomer() {
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().deactivateLastActiveSubscription();
    }

    public void verifyImmediateMaintainBundleResponse(Xml response) {
        Assert.assertEquals(MaintainBundleEntity.getImmediateMaintainBundleData().getTypeAttribute(), response.getTextByXpath("//message//@type"));
        Assert.assertEquals(MaintainBundleEntity.getImmediateMaintainBundleData().getCode(), response.getTextByXpath("//message//code"));
        Assert.assertEquals(MaintainBundleEntity.getImmediateMaintainBundleData().getDescription(), response.getTextByXpath("//message//description"));
    }

    public void verifySelfCareWSFaultResponse(Xml response, ErrorResponseEntity falseResponseData) {
        Assert.assertEquals(response.getTextByTagName("faultcode"), falseResponseData.getFaultCode());
        Assert.assertEquals(response.getTextByTagName("faultstring"), falseResponseData.getFaultString());

        if (falseResponseData.getCodeAttribute() != null) {
            Assert.assertEquals(falseResponseData.getCodeAttribute(), response.getTextByTagName("codeattribute"));
        }
        if (falseResponseData.getTypeAttribute() != null) {
            Assert.assertEquals(falseResponseData.getTypeAttribute(), response.getTextByTagName("typeattribute"));
        }
        if (falseResponseData.getDescription() != null) {
            Assert.assertEquals(falseResponseData.getDescription(), response.getTextByTagName("description"));
        }
        if (falseResponseData.getSCSMultiExceptionMessages() != null) {
            NodeList element = response.getElementsByTagName("selfcareServiceMultiException");

            Assert.assertEquals(element.getLength(), falseResponseData.getSCSMultiExceptionMessages().size());

            for (int i = 0; i < falseResponseData.getSCSMultiExceptionMessages().size(); i++) {
                Assert.assertEquals(response.getAttributeTextByXpath("//message", "type", i), falseResponseData.getSCSMultiExceptionMessages().get(i).getTypeAttribute());
                Assert.assertEquals(response.getTextByXpath("//message//code", i), falseResponseData.getSCSMultiExceptionMessages().get(i).getCode());
                Assert.assertEquals(response.getTextByXpath("//message//description", i), falseResponseData.getSCSMultiExceptionMessages().get(i).getDescription());
            }
        }

    }

    public String buildResponseData(String sampleFile, Date startDate,Date nextBillDate, String customerNumber, String subscriptionNumber){
        String sStartDate = Parser.parseDateFormate(startDate, TimeStamp.DateFormatXml());
        String sNextBillDate = Parser.parseDateFormate(nextBillDate, TimeStamp.DateFormatXml());

        String accountName = "Mr " + CareTestBase.getCustomerName();

        String file = Common.readFile(sampleFile).replace("$accountNumber$", customerNumber)
                .replace("$accountName$", accountName)
                .replace("$startDate$", sStartDate)
                .replace("$nextBillDate$", sNextBillDate)
                .replace("$subscriptionNumber$", subscriptionNumber);

        return Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
    }

    public String buildResponseData(String sampleFile, Date startDate, Date nextBillDate, String customerNumber, List<String> subscriptionNumberList){
        String sStartDate = Parser.parseDateFormate(startDate, TimeStamp.DateFormatXml());
        String sSubStartDate = Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT_XML);
        String sBufStartDate = "";
        String sEndDate = Parser.parseDateFormate(TimeStamp.TodayMinus16DaysAdd1Month(), TimeStamp.DateFormatXml());
        String sNextBillDate = Parser.parseDateFormate(nextBillDate, TimeStamp.DateFormatXml());
        String SNextScheduledRefill = Parser.parseDateFormate(TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.DateFormatXml());
        String subscriptionFC = "INVALID";
        String subscriptionNC = "INVALID";
        String subscription3 = "INVALID";
        String accountName = "Mr " + CareTestBase.getCustomerName();


        for (String subscription : subscriptionNumberList) {
            if (subscription.contains("Mobile FC")) {
                subscriptionFC = subscription.split(" ")[0];
            } else if (subscription.contains("Mobile NC")) {
                subscriptionNC = subscription.split(" ")[0];
            }else{
                subscription3 = subscription.split(" ")[0];
            }
        }

        String file = Common.readFile(sampleFile).replace("$accountNumber$", customerNumber)
                .replace("$accountName$", accountName)
                .replace("$subscriptionNumberFC$", subscriptionFC)
                .replace("$startDate$", sStartDate)
                .replace("$endDate$", sEndDate)
                .replace("$nextBillDate$", sNextBillDate)
                .replace("$nextScheduledRefill$", SNextScheduledRefill)
                .replace("$subscriptionNumberNC$", subscriptionNC)
                .replace("$subscriptionNumber3$", subscription3)
                .replace("$subStartDate$", sSubStartDate)
                .replace("$timeZone$", TimeStamp.TimeZone());


        return Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
    }
}
