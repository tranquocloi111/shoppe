package logic.business.ws.sws;

import framework.utils.Xml;
import logic.business.entities.NormalMaintainBundleEntity;
import logic.business.ws.BaseWs;
import logic.pages.care.find.DetailsContentPage;
import org.testng.Assert;

public class SelfCareWSTestBase extends BaseWs {
    public  void verifyNormalMaintainBundleResponse(Xml response){
        Assert.assertEquals(NormalMaintainBundleEntity.getNormalMaintainBundle().getTypeAttribute(), response.getTextByXpath("//message//@type"));
        Assert.assertEquals(NormalMaintainBundleEntity.getNormalMaintainBundle().getCode(), response.getTextByXpath("//message//code"));
        Assert.assertEquals(NormalMaintainBundleEntity.getNormalMaintainBundle().getDescription(), response.getTextByXpath("//message//description"));
    }

    public String getCustomerName(){
        return DetailsContentPage.AddressInformationPage.getInstance().getAddressee();
    }

    public String getClubCardNumber(){
        return DetailsContentPage.CreditInformationPage.getInstance().getClubCardNumber();
    }
}
