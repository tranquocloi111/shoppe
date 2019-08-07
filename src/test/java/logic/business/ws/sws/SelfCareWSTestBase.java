package logic.business.ws.sws;

import framework.utils.Xml;
import logic.business.entities.NormalMaintainBundleEntity;
import logic.business.ws.BaseWs;
import logic.pages.care.MenuPage;
import logic.pages.care.find.DetailsContentPage;
import logic.utils.Common;
import logic.utils.XmlUtils;
import org.testng.Assert;

public class SelfCareWSTestBase extends BaseWs {
    public  void verifyNormalMaintainBundleResponse(Xml response){
        Assert.assertEquals(NormalMaintainBundleEntity.getNormalMaintainBundle().getTypeAttribute(), response.getTextByXpath("//message//@type"));
        Assert.assertEquals(NormalMaintainBundleEntity.getNormalMaintainBundle().getCode(), response.getTextByXpath("//message//code"));
        Assert.assertEquals(NormalMaintainBundleEntity.getNormalMaintainBundle().getDescription(), response.getTextByXpath("//message//description"));
    }

    public void verifyGetAccountSummaryResponse(String userNumber, Xml expectedResponse, Xml response) {
        String expectedFile = Common.saveXmlFile(userNumber +"_ExpectedResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(expectedResponse.toString())));
        String actualFile = Common.saveXmlFile(userNumber+ "_ActualResponse.txt", XmlUtils.prettyFormat(XmlUtils.toCanonicalXml(response.toString())));

        Assert.assertEquals(1, Common.compareFile(expectedFile, actualFile).size());
    }


    public String getCustomerName(){
        if(MenuPage.LeftMenuPage.getInstance().verifyLinkIsNotSelected("Details"))
        {
            MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        }
        return DetailsContentPage.AddressInformationPage.getInstance().getAddressee();
    }

    public String getClubCardNumber(){
        if(MenuPage.LeftMenuPage.getInstance().verifyLinkIsNotSelected("Details"))
        {
            MenuPage.LeftMenuPage.getInstance().clickDetailsLink();
        }
        return DetailsContentPage.CreditInformationPage.getInstance().getClubCardNumber();
    }
}
