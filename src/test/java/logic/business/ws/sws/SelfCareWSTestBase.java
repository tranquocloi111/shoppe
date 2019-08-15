package logic.business.ws.sws;

import framework.utils.Xml;
import logic.business.entities.NormalMaintainBundleEntity;
import logic.business.ws.BaseWs;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.options.DeactivateSubscriptionPage;
import logic.utils.Common;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.XmlUtils;
import org.testng.Assert;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class SelfCareWSTestBase extends BaseWs {
    public  void verifyNormalMaintainBundleResponse(Xml response){
        Assert.assertEquals(NormalMaintainBundleEntity.getNormalMaintainBundle().getTypeAttribute(), response.getTextByXpath("//message//@type"));
        Assert.assertEquals(NormalMaintainBundleEntity.getNormalMaintainBundle().getCode(), response.getTextByXpath("//message//code"));
        Assert.assertEquals(NormalMaintainBundleEntity.getNormalMaintainBundle().getDescription(), response.getTextByXpath("//message//description"));
    }

    public void verifyTheResponseOfRequestIsCorrect(String userNumber, Xml expectedResponse, Xml response) {
        String expectedFile = Common.saveXmlFile(userNumber +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(expectedResponse.toString())));
        String actualFile = Common.saveXmlFile(userNumber+ "_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(response.toString())));

        Assert.assertEquals(1, Common.compareFile(expectedFile, actualFile).size());
    }

    public void verifyGetBundleResponseAreCorrect(String expectedResponse, Xml response, String customerId, String subscriptionNumber, String serviceOrderId, Date newStartDate){
        String sStartDate =  Parser.parseDateFormate(newStartDate, TimeStamp.DateFormatXml());
        String sNextScheduledRefill = Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DateFormatXml());

        String actualFile = Common.saveXmlFile(customerId +"_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(response.toString())));
        String file =  Common.readFile(expectedResponse)
                .replace("$accountNumber$", customerId)
                .replace("$subscriptionNumber$", subscriptionNumber)
                .replace("$startDateOneOffBundle$", Parser.parseDateFormate(TimeStamp.Today(),TimeStamp.DateFormatXml()))
                .replace("$startDate$", sStartDate)
                .replace("$nextScheduledRefill$", sNextScheduledRefill)
                .replace("$SOId$", serviceOrderId);
        String expectedResponseFile = Common.saveXmlFile(customerId +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(file)));
        int size = Common.compareFile(actualFile, expectedResponseFile).size();
        Assert.assertEquals(1, size);
    }

    public void deactivateAccountInFutureAndReturnToCustomer(){
        MenuPage.RightMenuPage.getInstance().clickDeactivateAccountLink();
        DeactivateSubscriptionPage.DeactivateSubscription.getInstance().deactivateLastActiveSubscription();
    }

    public List<String> getAllSubscription(int numberSubscriptions){
        ArrayList<String> subscriptionNumberList = new ArrayList<String>();
        for(int i = 1; i<=numberSubscriptions; i++){
            String subscriptionNumber = CommonContentPage.SubscriptionsGirdSectionPage.getInstance().getSubscriptionNumberAndNameByIndex(i);
            subscriptionNumberList.add(subscriptionNumber);
        }
        return subscriptionNumberList;
    }

}
