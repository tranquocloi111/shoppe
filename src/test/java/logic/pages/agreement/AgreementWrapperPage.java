package logic.pages.agreement;

import framework.config.Config;
import framework.utils.Log;
import logic.pages.BasePage;
import logic.utils.Common;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AgreementWrapperPage extends BasePage {

    public static AgreementWrapperPage getInstance(){
        return new AgreementWrapperPage();
    }

    @FindBy(id = "agreement_sign_app")
    WebElement txtAgreementSigningURL;

    @FindBy(id = "submit")
    WebElement btnSubmit;

    @FindBy(id = "ccaform:acceptTAndCs")
    WebElement ckAcceptTAndCs;

    @FindBy(id = "ccaform:continue")
    WebElement btnContinue;

    @FindBy(id = "ccaform:uniqueCodeImage")
    WebElement lbUniqueCodeImage;

    @FindBy(id = "ccaform:signatureCode")
    WebElement txtSignatureCode;

    @FindBy(id = "ccaform:submit-code")
    WebElement btnSubmitCode;

    @FindBy(id = "ccaform:confirm-continue")
    WebElement btnNextAgmtPopupDialog;

    @FindBy(id = "contractPages")
    WebElement frameContractPages;

    public void openAgreementSigningMainPage(String agreementSigningURL){
        getDriver().get(Config.getProp("agreementwrapper"));
        waitForPageLoadComplete(90);

        waitUntilElementVisible(txtAgreementSigningURL);
        enterValueByLabel(txtAgreementSigningURL, agreementSigningURL);
        click(btnSubmit);
    }

    public  void signAgreementViaUI(int numberOfAgreements){
        switchFrameByName(frameContractPages);
        try {
            for (int i = 0; i < numberOfAgreements; i++) {
                waitUntilElementVisible(ckAcceptTAndCs);
                scrollToElement(ckAcceptTAndCs);

                click(ckAcceptTAndCs);
                waitUntilElementVisible(btnContinue);

                click(btnContinue);
                waitUntilElementVisible(lbUniqueCodeImage);

                String signatureCodeNumber = Common.stripNonDigits(Common.splitSignatureCode(lbUniqueCodeImage.getAttribute("src")));
                enterValueByLabel(txtSignatureCode, signatureCodeNumber);
                waitUntilElementVisible(btnSubmitCode);

                click(btnSubmitCode);
                if (isElementPresent(btnNextAgmtPopupDialog))
                    click(btnNextAgmtPopupDialog);
            }

        }catch(Exception ex){
            Log.error(ex.getMessage());
        }

    }

}
