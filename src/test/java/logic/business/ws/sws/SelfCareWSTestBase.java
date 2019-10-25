package logic.business.ws.sws;

import framework.utils.Xml;
import logic.business.entities.ErrorResponseEntity;
import logic.business.entities.MaintainBundleEntity;
import logic.business.ws.BaseWs;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.bouncycastle.util.encoders.Base64;
import org.testng.Assert;
import org.w3c.dom.NodeList;
import suite.BaseTest;
import suite.regression.care.CareTestBase;

import java.io.File;
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



    public void verifyImmediateMaintainBundleResponse(Xml response) {
        Assert.assertEquals(MaintainBundleEntity.getImmediateMaintainBundleData().getTypeAttribute(), response.getTextByXpath("//message//@type"));
        Assert.assertEquals(MaintainBundleEntity.getImmediateMaintainBundleData().getCode(), response.getTextByXpath("//message//code"));
        Assert.assertEquals(MaintainBundleEntity.getImmediateMaintainBundleData().getDescription(), response.getTextByXpath("//message//description"));
    }

    public void verifySelfCareWSFaultResponse(Xml response, ErrorResponseEntity falseResponseData) {
        Assert.assertEquals(response.getTextByTagName("faultcode"), falseResponseData.getFaultCode());
        Assert.assertEquals(response.getTextByTagName("faultstring"), falseResponseData.getFaultString());

        if (falseResponseData.getCodeAttribute() != null) {
            Assert.assertEquals(falseResponseData.getCodeAttribute(), response.getAttributeTextByXpath("//selfcareServiceException","code", 0));
        }
        if (falseResponseData.getTypeAttribute() != null) {
            Assert.assertEquals(falseResponseData.getTypeAttribute(), response.getAttributeTextByXpath("//selfcareServiceException", "type", 0));
        }
        if (falseResponseData.getDescription() != null) {
            Assert.assertEquals(falseResponseData.getDescription(), response.getTextByTagName("description"));
        }
        if (falseResponseData.getSCSMultiExceptionMessages() != null) {
            NodeList element = response.getElementsByXpath("//selfcareServiceMultiException//message");

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
        String sNewStartDate = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DateFormatXml());
        String sEndDate = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DateFormatXml());

        String accountName = "Mr " + CareTestBase.getCustomerName();

        String file = Common.readFile(sampleFile).replace("$accountNumber$", customerNumber)
                .replace("$accountName$", accountName)
                .replace("$startDate$", sStartDate)
                .replace("$endDate$", sEndDate)
                .replace("$nextBillDate$", sNextBillDate)
                .replace("$subscriptionNumber$", subscriptionNumber)
                .replace("$newStartDate$", sNewStartDate);

        return Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
    }

    public String buildResponseData(String sampleFile, Date startDate, Date nextBillDate, String customerNumber, List<String> subscriptionNumberList){
        String sStartDate = Parser.parseDateFormate(startDate, TimeStamp.DateFormatXml());
        String sSubStartDate = Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT_XML);
        String sBufStartDate = "";
//        String sEndDate = Parser.parseDateFormate(TimeStamp.TodayMinus16DaysAdd2Months(), TimeStamp.DateFormatXml());
        String sNextBillDate = Parser.parseDateFormate(nextBillDate, TimeStamp.DateFormatXml());
        String SNextScheduledRefill = Parser.parseDateFormate(TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.DateFormatXml());
        String subscriptionFC = "INVALID";
        String subscriptionNC = "INVALID";
        String subscription3 = "INVALID";
        String accountName = "Mr " + CareTestBase.getCustomerName();


        for (String subscription : subscriptionNumberList) {
            if (subscription.endsWith("Mobile FC")) {
                subscriptionFC = subscription.split(" ")[0];
            } else if (subscription.endsWith("Mobile NC")) {
                subscriptionNC = subscription.split(" ")[0];
            }else{
                subscription3 = subscription.split(" ")[0];
            }
        }

        String XmlValue = Common.readFile(sampleFile).replace("$accountNumber$", customerNumber)
                .replace("$accountName$", accountName)
                .replace("$subscriptionNumberFC$", subscriptionFC)
                .replace("$startDate$", sStartDate)
                .replace("$nextBillDate$", sNextBillDate)
                .replace("$nextScheduledRefill$", SNextScheduledRefill)
                .replace("$subscriptionNumberNC$", subscriptionNC)
                .replace("$subscriptionNumber3$", subscription3)
                    .replace("$subStartDate$", sSubStartDate)
                .replace("$timeZone$", TimeStamp.TimeZone());

        return Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(XmlValue)));
    }

    public String buildCustomerDetailsResponseData(String tempFilePath, String customerNumber, String addressee,String MPNNumber,String email, String billingGroupName){
        String firstName = addressee.split(" ")[0];
        String lastName = addressee.split(" ")[1];

        String XmlValue = Common.readFile(tempFilePath).replace("$accountNumber$", customerNumber)
                .replace("$firstName$", firstName)
                .replace("$lastName$", lastName)
                .replace("$mainSubscription$", MPNNumber)
                .replace("$email$", email)
                .replace("$BillingGroup$", billingGroupName)
                .replace("$cardName$", addressee);

        return Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(XmlValue)));
    }

    public String buildResponseData(String sampleFile, Date startDate, Date endDate, Date nextBillDate, String customerNumber, List<String> subscriptionNumberList){
        String sStartDate = Parser.parseDateFormate(startDate, TimeStamp.DateFormatXml());
        String sEndDate = Parser.parseDateFormate(endDate, TimeStamp.DateFormatXml());
        String sSubStartDate = Parser.parseDateFormate(startDate, TimeStamp.DATE_FORMAT_XML);
        String sBufStartDate = "";
//        String sEndDate = Parser.parseDateFormate(TimeStamp.TodayMinus16DaysAdd2Months(), TimeStamp.DateFormatXml());
        String sNextBillDate = Parser.parseDateFormate(nextBillDate, TimeStamp.DateFormatXml());
        String SNextScheduledRefill = Parser.parseDateFormate(TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.DateFormatXml());
        String subscriptionFC = "INVALID";
        String subscriptionNC = "INVALID";
        String subscription3 = "INVALID";
        String accountName = "Mr " + CareTestBase.getCustomerName();


        for (String subscription : subscriptionNumberList) {
            if (subscription.endsWith("Mobile FC")) {
                subscriptionFC = subscription.split(" ")[0];
            } else if (subscription.endsWith("Mobile NC")) {
                subscriptionNC = subscription.split(" ")[0];
            }else{
                subscription3 = subscription.split(" ")[0];
            }
        }

        String XmlValue = Common.readFile(sampleFile).replace("$accountNumber$", customerNumber)
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

        return Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(XmlValue)));
    }

    public void verifyGetInvoiceResponse(Xml response, String customerNumber, String subscriptionValue, Date newStartDate) {
        Xml actualMainInvoiceContentResponse = getActualMainInvoiceContentResponse(response, customerNumber);
        String sDateFrom = Parser.parseDateFormate(newStartDate, TimeStamp.DATE_FORMAT_XINVOICE).toUpperCase();
        String sDateTo = Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DATE_FORMAT_XINVOICE).toUpperCase();
        String sDateIssue = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_XINVOICE).toUpperCase();

        //Verify MainInvoiceContent of get invoice response
        //Verify XINVOICE Attribute data
        Assert.assertEquals(customerNumber, actualMainInvoiceContentResponse.getAttributeTextByXpath("//XINVOICE", "customerNo"));
        Assert.assertEquals(sDateFrom, actualMainInvoiceContentResponse.getAttributeTextByXpath("//XINVOICE", "dateFrom"));
        Assert.assertEquals(sDateTo, actualMainInvoiceContentResponse.getAttributeTextByXpath("//XINVOICE", "dateTo"));
        Assert.assertEquals(sDateIssue, actualMainInvoiceContentResponse.getAttributeTextByXpath("//XINVOICE", "dateIssue"));
        //=============================================================
        //Verify Account Data
        String accountName = CareTestBase.getCustomerName();
        String actualAccName = actualMainInvoiceContentResponse.getAttributeTextByXpath("//ACCT_ROW", "accountName");
        Assert.assertEquals(accountName, actualAccName, "Verify account name is not matched");
        //====================================================
        //Verify USUMM ROW
        Assert.assertEquals(subscriptionValue, actualMainInvoiceContentResponse.getAttributeTextByXpath("//USUMM//USUMM_ROW", "reference"));

        //Verify each of refNo of CDR equal Subscription Number
        Xml actualCDRXml = getActualCDRInvoiceContentResponse(response, customerNumber);
        int numberOfCDR = actualCDRXml.getElementsByXpath("//CDRS/CDR").getLength();
        for (int i = 0; i < numberOfCDR; i++) {
            Assert.assertEquals(subscriptionValue.split(" ")[0], getActualCDRInvoiceContentResponse(response, customerNumber).getAttributeTextByXpath("//CDRS/CDR", "refNo", i));
        }

    }

    public Xml getActualMainInvoiceContentResponse(Xml response, String customerNumber) {
        String mainInvoiceContentStr = response.getTextByXpath("//invoiceContent//MAIN");
        byte[] mainInvoiceContentByte = Base64.decode(mainInvoiceContentStr);
        String mainInvoiceContentXmlString = new String(mainInvoiceContentByte);

        String filePath = Common.saveXmlFile(customerNumber + "_Actual_mainInvoiceContent.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(mainInvoiceContentXmlString)));
        Xml MainInvoiceContentXml = new Xml(new File(filePath));

        return MainInvoiceContentXml;
    }

    public Xml getActualCDRInvoiceContentResponse(Xml response, String customerNumber) {
        String CDRInvoiceContentStr = response.getTextByXpath("//invoiceContent//CDR");
        byte[] CDRInvoiceContentByte = Base64.decode(CDRInvoiceContentStr);
        String CDRInvoiceContentXmlString = new String(CDRInvoiceContentByte);
        String fileName = customerNumber + "_Actual_CDRInvoiceContent.txt";
        String filePath = Common.saveXmlFile(fileName, XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(CDRInvoiceContentXmlString)));

        Xml CRDInvoiceContentXml = new Xml(new File(filePath));

        return CRDInvoiceContentXml;
    }

    public void verifyGetInvoiceDetailResponse(Xml response, String tempFile, String customerNumber, Date newStartDate, String subscriptionNumber, String invoiceNumber) {
        String actualFile = Common.saveXmlFile(customerNumber + "_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(response.toString())));
        String chargeId = response.getTextByXpath("//chargeId", 0);
        String chargeId2 = response.getTextByXpath("//chargeId", 1);

        String invoiceStatus = response.getTextByTagName("invoiceStatus");
        Date invoiceDueDate = null;
        String XmlValue;
        if (invoiceStatus.equals("CONFIRMED")) {
            invoiceDueDate = BaseTest.paymentCollectionDateEscapeNonWorkDay(10);
        } else if (invoiceStatus.equals("DDPENDING") || invoiceStatus.equals("FULLYPAID")) {
            invoiceDueDate = TimeStamp.Today();
        }

        String sDateFrom = Parser.parseDateFormate(newStartDate, TimeStamp.DateFormatXml());
        String sDateTo = Parser.parseDateFormate(TimeStamp.TodayMinus1Day(), TimeStamp.DateFormatXml());
        String sDateFrom2 = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DateFormatXml());
        String sDateTo2 = Parser.parseDateFormate(TimeStamp.TodayPlus1MonthMinus1Day(), TimeStamp.DateFormatXml());
        String sDateIssued = Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DateFormatXml());
        String sDateDue = Parser.parseDateFormate(invoiceDueDate, TimeStamp.DateFormatXml());

        XmlValue = Common.readFile(tempFile).replace("$accountNumber$", customerNumber)
                .replace("$subscriptionNumber$", subscriptionNumber)
                .replace("$invoiceNumber$", invoiceNumber)
                .replace("$dateFrom$", sDateFrom)
                .replace("$dateTo$", sDateTo)
                .replace("$dateFrom2$", sDateFrom2)
                .replace("$dateTo2$", sDateTo2)
                .replace("$dateIssued$", sDateIssued)
                .replace("$dateDue$", sDateDue)
                .replace("$chargeId$", chargeId)
                .replace("$chargeId2$", chargeId2);

        String expectedResponse = Common.saveXmlFile(customerNumber + "_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(XmlValue)));

        Assert.assertEquals(1, Common.compareFile(expectedResponse, actualFile).size());
    }

}
