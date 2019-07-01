package logic.pages.care.main;

import framework.wdm.Browser;
import framework.wdm.WdManager;
import logic.business.entities.CardDetailsEntity;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import java.util.HashMap;
import java.util.List;

public class ServiceOrdersPage extends BasePage {

    @FindBy(xpath = "//input[@name='PostCmdBtn_NEXT']")
    WebElement btnNext;

    @FindBy(xpath = "//input[@name='PostCmdBtn_CANCEL']")
    WebElement btnDelete;

    public static class DeactivateSubscriptionPage extends ServiceOrdersPage {
        private static DeactivateSubscriptionPage instance;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Next Bill Date:')]/following-sibling::td//input")
        WebElement nextBillDate;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Customers email address:')]/following-sibling::td//input")
        WebElement customersEmailAddress;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Deactivation Date:')]/following-sibling::td//input")
        WebElement deactivationDate;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Deactivation Time:')]/following-sibling::td//input")
        WebElement deactivationTime;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Deactivation Reason:')]/following-sibling::td//input")
        WebElement deactivationReason;
        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Notes:')]/following-sibling::td//textarea")
        WebElement deactivationNotes;
        @FindBy(xpath = "//input[@type='checkbox' and not (@disabled)]")
        WebElement ckSubscription;

        public static DeactivateSubscriptionPage getInstance() {
            if (instance == null)
                return new DeactivateSubscriptionPage();
            return instance;
        }

        public void deactivateAccountWithADelayReturnAndImmediateRefund() {
            enterValueByLabel(deactivationDate, Parser.parseDateFormate(TimeStamp.TodayPlus1Day(), TimeStamp.DATE_FORMAT_IN_PDF));
            click(btnNext);

            DeactivateSubscriptionRefundDetailsPage deactivateSubscriptionRefundDetailsPage = new DeactivateSubscriptionRefundDetailsPage();
            String currentWindow = getDriver().getWindowHandle();
            deactivateSubscriptionRefundDetailsPage.clickNewCardDetailsButton();
            switchWindow("Update Card Details", false);

            CardDetailsEntity cardDetails = new CardDetailsEntity();
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

        public void deactivateSubscription() {
            click(ckSubscription);
            enterValueByLabel(deactivationNotes, "Regression Automation");
            clickNextButton();
            clickNextButton();
            clickNextButton();

            ReturnsAndEtcPage returnsAndEtcPage = new ReturnsAndEtcPage();
            enterValueByLabel(returnsAndEtcPage.dateReturnedCtl, Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF));
            selectByVisibleText(returnsAndEtcPage.assessmentGradeCtl, "Grade 1 - full credit. Phone works or has confirmed manufacture fault. No visible damage. All components included. Box reasonable wear.");
            enterValueByLabel(returnsAndEtcPage.returnReferenceNoCtl, "1234567890");
            clickNextButton();

            clickNextButton();
            clickReturnToCustomer();
        }


    }

    public static class UpdateCardDetailsPage extends ServiceOrdersPage {
        private static UpdateCardDetailsPage instance;
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

        public static UpdateCardDetailsPage getInstance() {
            if (instance == null)
                return new UpdateCardDetailsPage();
            return instance;
        }

        public void inputCardDetail(CardDetailsEntity cardDetails) {
            selectByVisibleText(cardTypeCtl, cardDetails.getCardType());
            enterValueByLabel(cardNumberCtl, cardDetails.getCardNumber());
            enterValueByLabel(cardHolderNameCtl, cardDetails.getCardHolderName());
            enterValueByLabel(cardSecurityCodeCtl, cardDetails.getCardSecurityCode());
            selectByVisibleText(cardExpiryMonthCtl, cardDetails.getCardExpiryMonth());
            selectByVisibleText(cardExpiryYearCtl, cardDetails.getCardExpiryYear());
            click(btnApply);
        }

    }

    public static class DeactivateSubscriptionRefundDetailsPage extends ServiceOrdersPage {

        private static DeactivateSubscriptionRefundDetailsPage instance;
        @FindBy(xpath = "//input[@value='New Card Details']")
        WebElement btnNewCardDetails;
        @FindBy(xpath = "//input[@name='PostCmdBtn_NEXT']")
        WebElement btnNextCardDetails;

        public static DeactivateSubscriptionRefundDetailsPage getInstance() {
            if (instance == null)
                return new DeactivateSubscriptionRefundDetailsPage();
            return instance;
        }

        public void clickNewCardDetailsButton() {
            click(btnNewCardDetails);
        }

        public void clickDeactivateSubscriptionNextButton() {
            click(btnNextCardDetails);
        }
    }

    public static class ReturnsAndEtcPage extends ServiceOrdersPage {

        private static ReturnsAndEtcPage instance;
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

        public static ReturnsAndEtcPage getInstance() {
            if (instance == null)
                return new ReturnsAndEtcPage();
            return instance;
        }

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

    public static class AccountSummaryAndSelectAction extends ServiceOrdersPage {
        private static AccountSummaryAndSelectAction instance;
        @FindBy(xpath = "//select[@name='PropFld_FINTXNTYP']")
        WebElement ddChooseAction;

        public static AccountSummaryAndSelectAction getInstance() {
            if (instance == null)
                return new AccountSummaryAndSelectAction();
            return instance;
        }

        public void selectChooseAction() {
            selectByVisibleText(ddChooseAction, "Take a Payment");
            click(btnNext);
        }
    }

    public static class InputPaymentDetails extends ServiceOrdersPage {
        private static InputPaymentDetails instance;
        @FindBy(xpath = "//input[@name='PropFld_DBAMT']")
        WebElement txtPaymentAmount;
        @FindBy(xpath = "//textarea[@name='PropFld_NOTES']")
        WebElement txtNotes;

        public static InputPaymentDetails getInstance() {
            if (instance == null)
                return new InputPaymentDetails();
            return instance;
        }

        public void inputPaymentDetail(String paymentAmount, String notes) {
            enterValueByLabel(txtPaymentAmount, paymentAmount);
            enterValueByLabel(txtNotes, notes);
            click(btnNext);
        }
    }

    public static class TransferExistingFunds extends ServiceOrdersPage {
        private static TransferExistingFunds instance;
        @FindBy(xpath = "//td[contains(text(),'Enter the required amount against each MPN')]/..//following-sibling::tr//input")
        WebElement txtAmountAgainstSubscription;

        public static TransferExistingFunds getInstance() {
            if (instance == null)
                return new TransferExistingFunds();
            return instance;
        }

        public void inputAmountAgainstSubscription(String amount) {
            enterValueByLabel(txtAmountAgainstSubscription, amount);
            click(btnNext);
        }

    }

    public static class ConfirmFundsTransfer extends ServiceOrdersPage {
        private static ConfirmFundsTransfer instance;
        @FindBy(xpath = "//td[contains(text(),'Payment Amount:')]//following-sibling::td//span")
        WebElement paymentAmount;
        @FindBy(xpath = "//td[contains(text(),'Notes:')]//following-sibling::td//span")
        WebElement notes;
        @FindBy(xpath = "//td[contains(text(),'Amount allocated to overdue invoice/s:')]//following-sibling::td//span")
        WebElement amountAllocatedToOverdueInvoices;
        @FindBy(xpath = "//td[contains(text(),'Amount allocated to top-ups')]/..//following-sibling::tr[1]//td[2]//span")
        WebElement amountAllocatedToTopUps;
        @FindBy(xpath = "//td[contains(text(),'Amount allocated to Credit Agreements')]/..//following-sibling::tr[1]//td[2]//span")
        WebElement amountAllocatedToCreditAgreement;
        @FindBy(xpath = "//td[contains(text(),'Card Type:')]//following-sibling::td//span")
        WebElement cardType;
        @FindBy(xpath = "//td[contains(text(),'Card Number:')]//following-sibling::td//span")
        WebElement cardNumber;
        @FindBy(xpath = "//td[contains(text(),'Card Holder Name:')]//following-sibling::td//span")
        WebElement cardHolderName;
        @FindBy(xpath = "//td[contains(text(),'Card Expiry Month:')]//following-sibling::td//span")
        WebElement cardExpiryMonth;
        @FindBy(xpath = "//td[contains(text(),'Card Expiry Year:')]//following-sibling::td//span")
        WebElement cardExpiryYear;

        public static ConfirmFundsTransfer getInstance() {
            if (instance == null)
                return new ConfirmFundsTransfer();
            return instance;
        }

        public HashMap<String, String> getConfirmFundsTransfer() {
            HashMap<String, String> confirmFundsTransfer = new HashMap<>();
            confirmFundsTransfer.put("PaymentAmount", getTextOfElement(paymentAmount));
            confirmFundsTransfer.put("Notes", getTextOfElement(notes));
            confirmFundsTransfer.put("OverdueInvoices", getTextOfElement(amountAllocatedToOverdueInvoices));
            confirmFundsTransfer.put("TopUps", getTextOfElement(amountAllocatedToTopUps));
            confirmFundsTransfer.put("CreditAgreement", getTextOfElement(amountAllocatedToCreditAgreement));
            confirmFundsTransfer.put("CardType", getTextOfElement(cardType));
            confirmFundsTransfer.put("CardNumber", getTextOfElement(cardNumber));
            confirmFundsTransfer.put("CardHolderName", getTextOfElement(cardHolderName));
            confirmFundsTransfer.put("CardExpiryMonth", getTextOfElement(cardExpiryMonth));
            confirmFundsTransfer.put("CardExpiryYear", getTextOfElement(cardExpiryYear));

            click(btnNext);

            return confirmFundsTransfer;
        }
    }

    public static class FundsTransferResults extends ServiceOrdersPage {
        private static FundsTransferResults instance;
        @FindBy(xpath = "//td[contains(text(),'Payment Amount:')]//following-sibling::td//span")
        WebElement paymentAmount;
        @FindBy(xpath = "//td[contains(text(),'Notes:')]//following-sibling::td//span")
        WebElement notes;
        @FindBy(xpath = "//td[contains(text(),'Amount Allocated to overdue invoices:')]//following-sibling::td//span")
        WebElement amountAllocatedToOverdueInvoices;
        @FindBy(xpath = "//td[contains(text(),'Amount allocated to top-ups')]/..//following-sibling::tr[1]//table[@class='PanelList']")
        WebElement tblAmountTopUps;
        @FindBy(xpath = "//td[contains(text(),'Amount allocated to Credit Agreements')]/..//following-sibling::tr[1]//table[@class='PanelList']")
        WebElement tblCreditAgreements;
        @FindBy(xpath = "//td[contains(text(),'Amount Allocated as unallocate payment:')]//following-sibling::td//span")
        WebElement unAllocatePayment;
        @FindBy(xpath = "//td[contains(text(),'Card Type:')]//following-sibling::td//span")
        WebElement cardType;
        @FindBy(xpath = "//td[contains(text(),'Card Number:')]//following-sibling::td//span")
        WebElement cardNumber;
        @FindBy(xpath = "//td[contains(text(),'Card Holder Name:')]//following-sibling::td//span")
        WebElement cardHolderName;
        @FindBy(xpath = "//td[contains(text(),'Card Expiry Month:')]//following-sibling::td//span")
        WebElement cardExpiryMonth;
        @FindBy(xpath = "//td[contains(text(),'Card Expiry Year:')]//following-sibling::td//span")
        WebElement cardExpiryYear;
        @FindBy(xpath = "//td[contains(text(),'ReDS Status Authorisation:')]//following-sibling::td//span")
        WebElement reDSStatusAuthorisation;

        public static FundsTransferResults getInstance() {
            if (instance == null)
                return new FundsTransferResults();
            return instance;
        }

        public HashMap<String, String> getFundsTransferResults() {
            HashMap<String, String> confirmFundsTransfer = new HashMap<>();
            confirmFundsTransfer.put("PaymentAmount", getTextOfElement(paymentAmount));
            confirmFundsTransfer.put("Notes", getTextOfElement(notes));
            confirmFundsTransfer.put("OverdueInvoices", getTextOfElement(amountAllocatedToOverdueInvoices));
            confirmFundsTransfer.put("AmountAllocatedToTopupsGrid", String.valueOf(getRowOfTopUps()));
            confirmFundsTransfer.put("AmountAllocatedToCreditAgreementsGrid", String.valueOf(getRowOfAgreementsGrid()));
            confirmFundsTransfer.put("AmountAllocatedAsUnallocatePayment", getTextOfElement(unAllocatePayment));
            confirmFundsTransfer.put("CardType", getTextOfElement(cardType));
            confirmFundsTransfer.put("CardNumber", getTextOfElement(cardNumber));
            confirmFundsTransfer.put("CardHolderName", getTextOfElement(cardHolderName));
            confirmFundsTransfer.put("CardExpiryMonth", getTextOfElement(cardExpiryMonth));
            confirmFundsTransfer.put("CardExpiryYear", getTextOfElement(cardExpiryYear));
            confirmFundsTransfer.put("ReDSStatusAuthorisation", getTextOfElement(reDSStatusAuthorisation));

            click(btnNext);
            clickReturnToCustomer();

            return confirmFundsTransfer;
        }

        private int getRowOfTopUps() {
            return tblAmountTopUps.findElements(By.xpath(".//tr")).size() - 1;
        }

        private int getRowOfAgreementsGrid() {
            return tblCreditAgreements.findElements(By.xpath(".//tr")).size() - 1;
        }

    }

    public static class SelectSubscription extends ServiceOrdersPage {
        private static SelectSubscription instance;
        @FindBy(name = "PropFld_SNO1")
        WebElement ddSubscriptionNumber;
        @FindBy(name = "PropFld_BDACT")
        WebElement ddAction;

        public static SelectSubscription getInstance() {
            if (instance == null)
                return new SelectSubscription();
            return instance;
        }

        public void selectSubscription(String subNo, String action) {
            selectByVisibleText(ddSubscriptionNumber, subNo);
            selectByVisibleText(ddAction, action);
            clickNextButton();
        }
    }

    public static class ChangeBundle extends ServiceOrdersPage {
        private static ChangeBundle instance;
        @FindBy(xpath = "//td[normalize-space(text())='Available Bundle(s)']//ancestor::form[1]")
        WebElement form;
        @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]/following-sibling::td")
        WebElement lblSubNumber;
        @FindBy(xpath = "//td[contains(text(),'Next Bill Date for this Account:')]/following-sibling::td")
        WebElement lblNextBillDateForThisAccount;
        @FindBy(xpath = "//td[contains(text(),'Current Tariff:')]/following-sibling::td")
        WebElement lblCurrentTariff;
        @FindBy(xpath = "//td[contains(text(),'Current Tariff:')]/following-sibling::td")
        WebElement lblPackagedBundle;
        @FindBy(xpath = "//td[contains(text(),'Info:')]/following-sibling::td")
        WebElement lblInfo;
        @FindBy(xpath = "//td[contains(text(),'When to apply change?:')]/following-sibling::td//span")
        WebElement lblWhenToApplyChangeText;

        public static ChangeBundle getInstance() {
            if (instance == null)
                return new ChangeBundle();
            return instance;
        }

        public String getSubscriptionNumber() {
            return getTextOfElement(lblSubNumber);
        }

        public String getNextBillDateForThisAccount() {
            return getTextOfElement(lblNextBillDateForThisAccount);
        }

        public String getCurrentTariff() {
            return getTextOfElement(lblCurrentTariff);
        }

        public String getPackagedBundle() {
            return getTextOfElement(lblPackagedBundle);
        }

        public String getWhenToApplyChangeText() {
            return getTextOfElement(lblNextBillDateForThisAccount);
        }

        public String getInfo() {
            return getTextOfElement(lblInfo);
        }

        public Boolean bundleExists(String[] bundles) {
            int matchCount = 0;
            for (String bundle : bundles) {
                List<WebElement> tdCells = form.findElements(By.xpath(".//td"));
                for (WebElement cell : tdCells) {
                    if (cell.getText().trim() == bundle)
                        matchCount++;
                }
            }
            if (bundles.length == matchCount)
                return true;
            else
                return false;
        }

        public String bundleToolTip(String bundle) {
            List<WebElement> tds = form.findElements((By.xpath(".//td")));
            try {
                for (WebElement td : tds) {
                    if (td.getText() == bundle) {
                        WebElement image = td.findElement(By.tagName("img"));
                        String js = image.getAttribute("onmouseover");
                        clickByJs(js);
                        Thread.sleep(1000);
                        WebElement div = getDriver().findElement(By.xpath(".//body/div[last()]"));
                        return div.getText();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void clickNextButton(){
            clickNextButton();
        }

        public void selectBundlesByName(String[] names){
            for (String name : names){
                WebElement tdCell = form.findElement(By.xpath(String.format(".//td[normalize-space(text())='{0}']", name)));
                WebElement checkbox = tdCell.findElement(By.xpath(".//input[@type='checkbox']"));
                if (checkbox.getAttribute("checked") != "true"){
                    click(checkbox);
                }
            }
        }
    }

}
