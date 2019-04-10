package logic.pages.care;

import framework.wdm.WdManager;
import logic.entities.CardDetails;
import logic.pages.BasePage;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import logic.utils.Xml;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import java.util.List;

public class ServiceOrdersPage extends BasePage {

    public class DeactivateSubscriptionPage extends ServiceOrdersPage{

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Next Bill Date:')]/following-sibling::td//input")
        WebElement nextBillDate;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Customers email address:')]/following-sibling::td//input")
        WebElement customersEmailAddress;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Deactivation Date:')]/following-sibling::td//input")
        WebElement deactivationDate;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Deactivation Time:')]/following-sibling::td//input")
        WebElement deactivationTime;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Deactivation Reason:')]/following-sibling::td//input")
        public WebElement deactivationReason;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Notes:')]/following-sibling::td//input")
        public WebElement deactivationNotes;

        @FindBy(xpath = "//input[@name='PostCmdBtn_NEXT']")
        WebElement btnNext;

        @FindBy(xpath = "//input[@name='PostCmdBtn_CANCEL']")
        WebElement btnDelete;

        public void deactivateAccountWithADelayReturnAndImmediateRefund()  {
            enterValueByLabel(deactivationDate, Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT_IN_PDF));
            click(btnNext);

            DeactivateSubscriptionRefundDetailsPage deactivateSubscriptionRefundDetailsPage = new DeactivateSubscriptionRefundDetailsPage();
            String currentWindow = WdManager.get().getWindowHandle();
            deactivateSubscriptionRefundDetailsPage.clickNewCardDetailsButton();
            switchWindow("Update Card Details", false);

            CardDetails cardDetails = new CardDetails();
            cardDetails.setCardType("Visa");
            cardDetails.setCardNumber("4111111111111111");
            cardDetails.setCardHolderName("Mr Card Holder");
            cardDetails.setCardSecurityCode("123");
            cardDetails.setCardExpiryMonth("12");
            cardDetails.setCardExpiryYear("2020");
            new UpdateCardDetailsPage().inputCardDetail(cardDetails);
            switchWindow(currentWindow, true);

            deactivateSubscriptionRefundDetailsPage.clickDeactivateSubscriptionNextButton();
            deactivateSubscriptionRefundDetailsPage.clickDeactivateSubscriptionNextButton();

            ReturnsAndEtcPage returnsAndEtcPage = new ReturnsAndEtcPage();
            Assert.assertNotNull(returnsAndEtcPage.getIMEI());

            enterValueByLabel(returnsAndEtcPage.dateReturnedCtl, Parser.parseDateFormate(TimeStamp.TodayMinus3Days(), TimeStamp.DATE_FORMAT_IN_PDF));
            selectByVisibleText(returnsAndEtcPage.assessmentGradeCtl, "Grade 1 - full credit. Phone works or has confirmed manufacture fault. No visible damage. All components included. Box reasonable wear.");
            clickNextButton();

            clickNextButton();
            clickReturnToCustomer();
        }
    }

    public class UpdateCardDetailsPage extends ServiceOrdersPage{

        @FindBy(id = "PropFld_CDTP")
        WebElement cardTypeCtl;

        @FindBy(id = "PropFld_CDNO")
        WebElement cardNumberCtl;

        @FindBy(id = "PropFld_CCNM")
        WebElement cardHolderNameCtl;

        @FindBy(id = "PropFld_CVC2")
        WebElement cardSecurityCodeCtl;

        @FindBy(id = "PropFld_CCEM")
        WebElement cardExpiryMonthCtl;

        @FindBy(id = "PropFld_CCEY")
        WebElement cardExpiryYearCtl;

        @FindBy(id = "PropFld_CCSD")
        WebElement cardStartDateCtl;

        @FindBy(id = "PropFld_CCISSUENO")
        WebElement cardIssueNumberCtl;

        @FindBy(id = "btnApply")
        WebElement btnApply;

        @FindBy(id = "btnCancel")
        WebElement btnCancel;

        public void inputCardDetail(CardDetails cardDetails) {
            selectByVisibleText(cardTypeCtl, cardDetails.getCardType());
            enterValueByLabel(cardNumberCtl, cardDetails.getCardNumber());
            enterValueByLabel(cardHolderNameCtl, cardDetails.getCardHolderName());
            enterValueByLabel(cardSecurityCodeCtl, cardDetails.getCardSecurityCode());
            selectByVisibleText(cardExpiryMonthCtl, cardDetails.getCardExpiryMonth());
            selectByVisibleText(cardExpiryYearCtl, cardDetails.getCardExpiryYear());
            btnApply.click();
        }

    }

    public class DeactivateSubscriptionRefundDetailsPage extends ServiceOrdersPage{

        @FindBy(xpath = "//input[@value='New Card Details']")
        private WebElement btnNewCardDetails;

        @FindBy(xpath = "//input[@name='PostCmdBtn_NEXT']")
        private WebElement btnNextCardDetails;

        public void clickNewCardDetailsButton() {
            btnNewCardDetails.click();
        }

        public void clickDeactivateSubscriptionNextButton() {
            btnNextCardDetails.click();
        }
    }

    public class ReturnsAndEtcPage extends ServiceOrdersPage{

        @FindBy(xpath = "//td[@class='pagedesc']")
        WebElement MPN;

        @FindBy(xpath = "//td[@class='instuctionalTextHighLight']")
        WebElement IMEI;

        @FindBy(className = "PanelList")
        WebElement productTable;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Date Returned:')]/following-sibling::td//input")
        WebElement dateReturnedCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Assessment Grade:')]/following-sibling::td//select")
        WebElement assessmentGradeCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Non-Return Charge Amount:')]/following-sibling::td//input")
        WebElement nonReturnChargeAmountCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Return Reference Number:')]/following-sibling::td//input")
        WebElement returnReferenceNoCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Agent:')]/following-sibling::td//input")
        WebElement agentCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Notes:')]/following-sibling::td//input")
        WebElement notesCtl;

        public String getMPN() {
            return MPN.getText().split(" ")[4];
        }

        public String getIMEI() {
            return IMEI.getText().split(":")[1];
        }


        public String GetInitialPurchasePriceByProduct(String product) {
            List<WebElement> rows = productTable.findElements(By.tagName("tr"));
            for (WebElement row : rows) {
                List<WebElement> tds = row.findElements(By.tagName("td"));
                if (tds.get(0).getText().trim() == product)
                    return tds.get(1).getText().trim();
            }
            return null;
        }

        public String GetNonReturnChargeByProduct(String product) {
            List<WebElement> rows = productTable.findElements(By.tagName("tr"));
            for (WebElement row : rows) {
                List<WebElement> tds = row.findElements(By.tagName("td"));
                if (tds.get(0).getText().trim() == product)
                    return tds.get(2).getText().trim();
            }
            return null;
        }
    }

}
