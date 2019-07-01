package logic.pages.agreement;

import framework.utils.Log;
import framework.wdm.WdManager;
import logic.pages.BasePage;
import logic.utils.Common;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


public class AgreementSigningContractPage extends BasePage {

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


    public  void signAgreementViaUI(String agreementSigningUrl, int numberOfAgreements){
        try {
            getDriver().get(agreementSigningUrl);
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
