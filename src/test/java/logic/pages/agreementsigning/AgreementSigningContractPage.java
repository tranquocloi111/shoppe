package logic.pages.agreementsigning;

import framework.wdm.WdManager;
import logic.pages.BasePage;
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


    public  void signAgreementViaUI(String agreementSigningUrl){
        try {
            WdManager.get().get(agreementSigningUrl);
            waitUntilElementVisible(ckAcceptTAndCs);
            scrollToElement(ckAcceptTAndCs);

            ckAcceptTAndCs.click();
            waitUntilElementVisible(btnContinue);

            btnContinue.click();
            waitUntilElementVisible(lbUniqueCodeImage);

            String signatureCode = lbUniqueCodeImage.getAttribute("src");
            txtSignatureCode.click();
            txtSignatureCode.sendKeys(stripNonDigits(splitSignatureCode(signatureCode)));
            waitUntilElementVisible(btnSubmitCode);

            btnSubmitCode.click();
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }

    }
}
