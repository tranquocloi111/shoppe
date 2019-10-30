package logic.pages.care.main;

import logic.business.entities.CardDetailsEntity;
import logic.pages.BasePage;
import logic.pages.TableControlBase;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import logic.pages.TableControlBase;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

public class ServiceOrdersPage extends BasePage {

    public static ServiceOrdersPage getInstance() {
        return new ServiceOrdersPage();
    }

    @FindBy(xpath = "//input[@name='PostCmdBtn_NEXT']")
    protected WebElement btnNext;

    @FindBy(xpath = "//input[@name='PostCmdBtn_CANCEL']")
    protected WebElement btnDelete;

    @FindBy(name = "PostCmdBtn_BACK")
    protected WebElement btnPre;

    @FindBy(xpath = "//form[@name='wizardstep']//table")
    WebElement wizardstepTable;

    public void clickNextButton() {
        clickNextBtn();
    }

    public void clickDeleteButton() {
        clickWithOutWait(btnDelete);
    }

    public void clickPreButton() {
        click(btnPre);
    }

    public static class DeactivateSubscriptionPage extends ServiceOrdersPage {
        private static DeactivateSubscriptionPage instance = new DeactivateSubscriptionPage();
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
            return new DeactivateSubscriptionPage();
        }

        public void deactivateAccountWithADelayReturnAndImmediateRefund(Date date) {
            enterValueByLabel(deactivationDate, Parser.parseDateFormate(date, TimeStamp.DATE_FORMAT_IN_PDF));
            clickNextBtn();

            DeactivateSubscriptionRefundDetailsPage deactivateSubscriptionRefundDetailsPage = new DeactivateSubscriptionRefundDetailsPage();
            String currentWindow = getDriver().getTitle();
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

            switchWindow(currentWindow, false);

            deactivateSubscriptionRefundDetailsPage.clickDeactivateSubscriptionNextButton();
            deactivateSubscriptionRefundDetailsPage.clickDeactivateSubscriptionNextButton();

            ReturnsAndEtcPage returnsAndEtcPage = new ReturnsAndEtcPage();
            Assert.assertNotNull(returnsAndEtcPage.getIMEI());

            enterValueByLabel(returnsAndEtcPage.dateReturnedCtl, Parser.parseDateFormate(TimeStamp.TodayMinus3Days(), TimeStamp.DATE_FORMAT_IN_PDF));
            selectByVisibleText(returnsAndEtcPage.assessmentGradeCtl, "Grade 1 - full credit. Phone works or has confirmed manufacture fault. No visible damage. All components included. Box reasonable wear.");
            clickNextBtn();

            clickNextBtn();
            clickReturnToCustomer();
        }

        public void deactivateAccountWithOutReturnRefund(Date date) {
            enterValueByLabel(deactivationDate, Parser.parseDateFormate(date, TimeStamp.DATE_FORMAT_IN_PDF));
            clickNextBtn();
            clickNextBtn();
            if (isElementPresent(btnNext))
                clickNextBtn();
            clickReturnToCustomer();
        }

        public void deactivateSubscription(boolean isList) {
            click(ckSubscription);
            enterValueByLabel(deactivationNotes, "Regression Automation");
            clickNextBtn();
            clickNextBtn();
            clickNextBtn();
            ReturnsAndEtcPage returnsAndEtcPage = new ReturnsAndEtcPage();
            if (isList){
                enterValueByLabel(returnsAndEtcPage.dateReturnedCtl, new String[]{Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF), Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF)} );
                selectByVisibleText(returnsAndEtcPage.assessmentGradeCtl, new String[]{"Grade 1 - full credit. Phone works or has confirmed manufacture fault. No visible damage. All components included. Box reasonable wear.","Grade 1 - full credit. Phone works or has confirmed manufacture fault. No visible damage. All components included. Box reasonable wear."});
                enterValueByLabel(returnsAndEtcPage.returnReferenceNoCtl, new String[]{"1234567890", "1234567890"});
            }else {
                enterValueByLabel(returnsAndEtcPage.dateReturnedCtl, Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT_IN_PDF));
                selectByVisibleText(returnsAndEtcPage.assessmentGradeCtl, "Grade 1 - full credit. Phone works or has confirmed manufacture fault. No visible damage. All components included. Box reasonable wear.");
                enterValueByLabel(returnsAndEtcPage.returnReferenceNoCtl, "1234567890");
            }
            clickNextBtn();
            clickNextBtn();
            clickReturnToCustomer(); }

        public void deactivateSubscriptionWithoutEtc() {
            click(ckSubscription);
            enterValueByLabel(deactivationNotes, "Regression Automation");
            clickNextBtn();
            clickNextBtn();
            if (isElementPresent(btnNext))
                clickNextBtn();
            clickReturnToCustomer();
        }
    }

    public static class UpdateCardDetailsPage extends ServiceOrdersPage {
        private static UpdateCardDetailsPage instance = new UpdateCardDetailsPage();
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
            return new UpdateCardDetailsPage();
        }

        public void inputCardDetail(CardDetailsEntity cardDetails) {
            selectByVisibleText(cardTypeCtl, cardDetails.getCardType());
            enterValueByLabel(cardNumberCtl, cardDetails.getCardNumber());
            enterValueByLabel(cardHolderNameCtl, cardDetails.getCardHolderName());
            enterValueByLabel(cardSecurityCodeCtl, cardDetails.getCardSecurityCode());
            selectByVisibleText(cardExpiryMonthCtl, cardDetails.getCardExpiryMonth());
            selectByVisibleText(cardExpiryYearCtl, cardDetails.getCardExpiryYear());
            clickByJs(btnApply);
        }

    }

    public static class DeactivateSubscriptionRefundDetailsPage extends ServiceOrdersPage {

        private static DeactivateSubscriptionRefundDetailsPage instance = new DeactivateSubscriptionRefundDetailsPage();
        @FindBy(xpath = "//input[@value='New Card Details']")
        WebElement btnNewCardDetails;
        @FindBy(xpath = "//input[@name='PostCmdBtn_NEXT']")
        WebElement btnNextCardDetails;

        public static DeactivateSubscriptionRefundDetailsPage getInstance() {
            if (instance == null)
                return new DeactivateSubscriptionRefundDetailsPage();
            return new DeactivateSubscriptionRefundDetailsPage();
        }

        public void clickNewCardDetailsButton() {
            clickByJs(btnNewCardDetails);
        }

        public void clickDeactivateSubscriptionNextButton() {
            click(btnNextCardDetails);
        }
    }

    public boolean getHyperLinkChangeCustomerServiceOrderProgress() {
        boolean flag = false;
        List<WebElement> tds = wizardstepTable.findElements(By.xpath(".//td[@valign='middle']/following-sibling::td"));
        for (WebElement td : tds) {
            try {
                td.findElement(By.tagName("a"));
                flag = true;
            } catch (Exception ex) {
                flag = false;
            }
        }
        return flag;
    }

    public static class AccountSummaryAndSelectAction extends ServiceOrdersPage {
        private static AccountSummaryAndSelectAction instance = new AccountSummaryAndSelectAction();
        @FindBy(xpath = "//select[@name='PropFld_FINTXNTYP']")
        WebElement ddChooseAction;

        public static AccountSummaryAndSelectAction getInstance() {
            if (instance == null)
                return new AccountSummaryAndSelectAction();
            return new AccountSummaryAndSelectAction();
        }

        public void selectChooseAction(String action) {
            selectByVisibleText(ddChooseAction, action);
            click(btnNext);
        }
    }

    public static class InputPaymentDetails extends ServiceOrdersPage {
        private static InputPaymentDetails instance = new InputPaymentDetails();
        @FindBy(xpath = "//input[@name='PropFld_DBAMT']")
        WebElement txtPaymentAmount;
        @FindBy(xpath = "//textarea[@name='PropFld_NOTES']")
        WebElement txtNotes;

        public static InputPaymentDetails getInstance() {
            if (instance == null)
                return new InputPaymentDetails();
            return new InputPaymentDetails();
        }

        public void inputPaymentDetail(String paymentAmount, String notes) {
            enterValueByLabel(txtPaymentAmount, paymentAmount);
            enterValueByLabel(txtNotes, notes);
            click(btnNext);
        }
    }

    public static class TransferExistingFunds extends ServiceOrdersPage {
        private static TransferExistingFunds instance = new TransferExistingFunds();
        @FindBy(xpath = "//td[contains(text(),'Enter the required amount against each MPN')]/..//following-sibling::tr//input")
        WebElement txtAmountAgainstSubscription;

        public static TransferExistingFunds getInstance() {
            if (instance == null)
                return new TransferExistingFunds();
            return new TransferExistingFunds();
        }

        public void inputAmountAgainstSubscription(String amount) {
            enterValueByLabel(txtAmountAgainstSubscription, amount);
            click(btnNext);
        }

    }

    public static class ConfirmFundsTransfer extends ServiceOrdersPage {
        private static ConfirmFundsTransfer instance = new ConfirmFundsTransfer();
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
            return new ConfirmFundsTransfer();
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
        private static FundsTransferResults instance = new FundsTransferResults();
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
            return new FundsTransferResults();
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
        private static SelectSubscription instance = new SelectSubscription();
        @FindBy(name = "PropFld_SNO1")
        WebElement ddSubscriptionNumber;
        @FindBy(name = "PropFld_BDACT")
        WebElement ddAction;

        public static SelectSubscription getInstance() {
            if (instance == null)
                return new SelectSubscription();
            return new SelectSubscription();
        }

        public void selectSubscription(String subNo, String action) {
            selectByVisibleText(ddSubscriptionNumber, subNo);
            selectByVisibleText(ddAction, action);
            clickNextBtn();
        }

        public void selectAction(String action) {
            selectByVisibleText(ddAction, action);
            clickNextBtn();
        }

        public void selectSubscriptionWithouAction(String subNo) {
            selectByVisibleText(ddSubscriptionNumber, subNo);
            clickNextBtn();
        }

        public String getSubscription() {
            return getTextOfSelectedOption(ddSubscriptionNumber);
        }
    }

    public static class ReturnsAndEtcPage extends ServiceOrdersPage {

        private static ReturnsAndEtcPage instance = new ReturnsAndEtcPage();
        @FindBy(xpath = "//td[@class='pagedesc']")
        WebElement MPN;

        @FindBy(xpath = "//td[@class='instuctionalTextHighLight']")
        WebElement IMEI;

        @FindBy(className = "PanelList")
        WebElement productTable;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Date Returned:')]/following-sibling::td//input")
        List<WebElement> dateReturnedCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Assessment Grade:')]/following-sibling::td//select")
        List<WebElement> assessmentGradeCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Non-Return Charge Amount:')]/following-sibling::td//input")
        WebElement nonReturnChargeAmountCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Return Reference Number:')]/following-sibling::td//input")
        List<WebElement> returnReferenceNoCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Agent:')]/following-sibling::td//input")
        WebElement agentCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Notes:')]/following-sibling::td//textarea")
        WebElement notesCtl;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'ETC Amount:')]/following-sibling::td//input")
        WebElement ETCAmountCtl;



        public static ReturnsAndEtcPage getInstance() {
            if (instance == null)
                return new ReturnsAndEtcPage();
            return new ReturnsAndEtcPage();
        }

        public String getMPN() {
            return MPN.getText().split(" ")[4];
        }

        public String getIMEI() {
            return IMEI.getText().split(":")[1];
        }


        public String getInitialPurchasePriceByProduct(String product) {
            List<WebElement> rows = productTable.findElements(By.tagName("tr"));
            for (WebElement row : rows) {
                List<WebElement> tds = row.findElements(By.tagName("td"));
                if (tds.get(0).getText().trim() == product)
                    return tds.get(1).getText().trim();
            }
            return null;
        }

        public String getNonReturnChargeByProduct(String product) {
            List<WebElement> rows = productTable.findElements(By.tagName("tr"));
            for (WebElement row : rows) {
                List<WebElement> tds = row.findElements(By.tagName("td"));
                if (tds.get(0).getText().trim() == product)
                    return tds.get(2).getText().trim();
            }
            return null;
        }

        public void selectWaiveETCReasonByIndexAndValue(int index, String value) {
            List<WebElement> elementList = getDriver().findElements(By.xpath("//td[contains(text(),'Waive ETC Reason:')]"));
            selectByVisibleText(elementList.get(index).findElement(By.xpath("..//following-sibling::td[1]//select")), value);
        }

        public String getETCAmountValue() {
            return ETCAmountCtl.getAttribute("value");
        }

        public void setNotes(String note) {
            notesCtl.sendKeys(note);
        }

    }

    public static class ConfirmChangeBundle extends ServiceOrdersPage {
        private static ConfirmChangeBundle instance = new ConfirmChangeBundle();

        public static ConfirmChangeBundle getInstance() {
            if (instance == null)
                return new ConfirmChangeBundle();
            return new ConfirmChangeBundle();
        }

        @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]/following-sibling::td//span")
        WebElement subscriptionNumber;

        @FindBy(xpath = "//td[contains(text(),'Next Bill Date for this Account:')]/following-sibling::td//span")
        WebElement nextBillDateForThisAccount;

        @FindBy(xpath = "//td[contains(text(),'Current Tariff:')]/following-sibling::td//span")
        WebElement currentTariff;

        @FindBy(xpath = "//td[contains(text(),'Packaged Bundle:')]/following-sibling::td//span")
        WebElement packagedBundle;

        @FindBy(xpath = "//td[contains(text(),'Info:')]/following-sibling::td//span")
        WebElement infoBefore;

        @FindBy(xpath = "//td[contains(text(),'Total Recurring Bundle Charge:')]/following-sibling::td//span")
        WebElement totalRecurringBundleChargeBefore;

        @FindBy(xpath = "//td[contains(text(),'Total Recurring Bundle Charge:')]/following-sibling::td//span")
        WebElement totalRecurringBundleChargeAfter;

        @FindBy(xpath = "//td[contains(text(),'Total Recurring Bundle Charge:')]/following-sibling::td//span")
        List<WebElement> totalRecurringBundleCharge;

        @FindBy(xpath = "//td[contains(text(),'Recurring Bundles Charge Difference:')]/following-sibling::td//span")
        WebElement recurringBundlesChargeDifference;

        @FindBy(xpath = "//td[contains(text(),'Effective:')]/following-sibling::td//span")
        WebElement effective;

        @FindBy(xpath = "//td[@class='descError']")
        WebElement lblErrorEmssage;

        public String getSubscriptionNumber() {
            return getTextOfElement(subscriptionNumber);
        }

        public String getNextBillDateForThisAccount() {
            return getTextOfElement(nextBillDateForThisAccount);
        }

        public String getCurrentTariff() {
            return getTextOfElement(currentTariff);
        }

        public String getPackagedBundle() {
            return getTextOfElement(packagedBundle);
        }

        public String getInfoBefore() {
            return getTextOfElement(infoBefore);
        }

        public String getTotalRecurringBundleChargeBefore() {
            return getTextOfElement(totalRecurringBundleCharge.get(0));
        }

        public String getTotalRecurringBundleChargeAfter() {
            return getTextOfElement(totalRecurringBundleCharge.get(1));
        }

        public String getRecurringBundlesChargeDifference() {
            return getTextOfElement(recurringBundlesChargeDifference);
        }

        public String getEffective() {
            return getTextOfElement(effective);
        }

        public String getBundleInfo(String name) {
            return getTextOfElement(getDriver().findElement(By.xpath("//td[contains(text(),'" + name + "')]/following-sibling::td//span")));
        }

        public String getErrorMessage() {
            return getTextOfElement(lblErrorEmssage);
        }



        @FindBy(xpath = "//td[normalize-space(text())='Safety Buffer Before']//ancestor::tr[1]//following-sibling::tr[1]")
        WebElement safetyBufferBefore;
        @FindBy(xpath = "//td[normalize-space(text())='Safety Buffer After']//ancestor::tr[1]//following-sibling::tr[1]")
        WebElement safetyBufferAfter;
        public String getSafetyBufferBefore() {
            return getTextOfElement(safetyBufferBefore);
        }

        public String getSafetyBufferAfter() {
            return getTextOfElement(safetyBufferAfter);
        }
    }

    public static class ServiceOrderComplete extends ServiceOrdersPage {
        private static ServiceOrderComplete instance = new ServiceOrderComplete();

        public static ServiceOrderComplete getInstance() {
            return new ServiceOrderComplete();
        }

        @FindBy(xpath = ".//td[@class='instuctionalTextHighLight']")
        WebElement message;

        public String getMessage() {
            return getTextOfElement(message);
        }

    }

    public static class AddOneOffBundle extends ServiceOrdersPage {
        final static String BUNDLE_GROUP = "Bundle Group";

        public static AddOneOffBundle getInstance() {
            return new AddOneOffBundle();
        }

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Subscription Number:')]/following-sibling::td")
        WebElement subscriptionNumber;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Next Bill Date for this Account:')]/following-sibling::td")
        WebElement nextBillDateForThisAccount;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Current Tariff:')]/following-sibling::td")
        WebElement currentTariff;

        @FindBy(xpath = "//tr/td[contains(@class, 'label') and contains(text(),'Packaged Bundle:')]/following-sibling::td")
        WebElement packagedBundle;

        @FindBy(xpath = ".//table[@class='PanelList']")
        WebElement currentBundleTable;
        TableControlBase table = new TableControlBase(currentBundleTable);

        public String getSubscriptionNumber() {
            return getTextOfElement(subscriptionNumber);
        }

        public String getNextBillDateForThisAccount() {
            return getTextOfElement(nextBillDateForThisAccount);
        }

        public String getCurrentTariff() {
            return getTextOfElement(currentTariff);
        }

        public String getPackagedBundle() {
            return getTextOfElement(packagedBundle);
        }

        public boolean isBonusBundleDisplayed(String cellValue) {
            try {
                return getTextOfElement(table.getRowByColumnNameAndCellValue(BUNDLE_GROUP, cellValue)).isEmpty();
            }catch (Exception ex){
                return false;
            }
        }

        public String getCellValueByIndex(int index) {
            return table.getRecordByIndex(index).getText();
        }

        public int getNumberOfCurrentBundleByBundleGroup(HashMap<String, String> bundleGroup) {
            return table.findRowsByColumns(bundleGroup).size();
        }
    }

    public static class ConfigureSubscription extends ServiceOrdersPage {
        private static ConfigureSubscription instance = new ConfigureSubscription();

        public static ConfigureSubscription getInstance() {
            if (instance == null)
                return new ConfigureSubscription();
            return new ConfigureSubscription();
        }

        @FindBy(name = "PropFld_SOSNOBAR")
        WebElement subScriptionBarring;
        @FindBy(name = "PropFld_OUTBARFLG")
        WebElement subScriptionBarReason;
        @FindBy(name = "PropFld_SNOROAM")
        WebElement subScriptionRoaming;
        @FindBy(name = "PropFld_NOTES")
        WebElement note;

        public void selectSubscriptionBarring(String barring) {
            selectByVisibleText(subScriptionBarring, barring);
        }

        public void selectSubscriptionBarReason(String barring) {
            selectByVisibleText(subScriptionBarReason, barring);
        }

        public void selectSubscriptionRoaming(String barring) {
            selectByVisibleText(subScriptionRoaming, barring);
        }

        public void enterNote(String noteValue) {
            enterValueByLabel(note, noteValue);
        }
    }

    public static class ChangeBundle extends ServiceOrdersPage {
        private static ChangeBundle instance = new ChangeBundle();
        @FindBy(xpath = "//td[normalize-space(text())='Available Bundle(s)']//ancestor::form[1]")
        WebElement form;
        @FindBy(xpath = "//td[contains(text(),'Subscription Number:')]/following-sibling::td//span")
        WebElement lblSubNumber;
        @FindBy(xpath = "//td[contains(text(),'Next Bill Date for this Account:')]/following-sibling::td//span")
        WebElement lblNextBillDateForThisAccount;
        @FindBy(xpath = "//td[contains(text(),'Current Tariff:')]/following-sibling::td//span")
        WebElement lblCurrentTariff;
        @FindBy(xpath = "//td[contains(text(),'Packaged Bundle:')]/following-sibling::td//span")
        WebElement lblPackagedBundle;
        @FindBy(xpath = "//td[contains(text(),'Info:')]/following-sibling::td//span")
        WebElement lblInfo;
        @FindBy(xpath = "//td[contains(text(),'When to apply change?:')]/following-sibling::td//span")
        WebElement lblWhenToApplyChangeText;

        @FindBy(xpath = "//td[contains(.,'Double Data')]")
        WebElement lblBonusBundle;

        public static ChangeBundle getInstance() {
            if (instance == null)
                return new ChangeBundle();
            return new ChangeBundle();
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
            return getTextOfElement(lblWhenToApplyChangeText);
        }

        public String getInfo() {
            return getTextOfElement(lblInfo);
        }

        public Boolean bundleExists(String[] bundles) {
            int matchCount = 0;
            for (String bundle : bundles) {
                List<WebElement> tdCells = form.findElements(By.xpath(".//td"));
                for (WebElement cell : tdCells) {
                    if (cell.getText().equalsIgnoreCase(bundle))
                        System.out.println("cell.getText().trim() : " + cell.getText().trim());
                    matchCount++;
                    break;
                }
            }
            return bundles.length == matchCount;
        }

        public String bundleToolTip(String bundle) {
            List<WebElement> tds = form.findElements((By.xpath(".//td")));
            try {
                for (WebElement td : tds) {
                    if (td.getText().equalsIgnoreCase(bundle)) {
                        WebElement image = td.findElement(By.tagName("img"));
                        String js = image.getAttribute("onmouseover");
                        Actions a = new Actions(getDriver());
                        a.moveToElement(image).build().perform();
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

        public void clickNextButton() {
            clickNextBtn();
        }

        public void selectBundlesByName(String[] names, String value) {
            for (String name : names) {
                WebElement tdCell = form.findElement(By.xpath(String.format(".//td[normalize-space(text())='%s']", name)));
                WebElement checkbox = tdCell.findElement(By.xpath(".//input[@type='checkbox']"));
                if (name.equalsIgnoreCase(value)) {
                    if (checkbox.getAttribute("checked") != "true") {
                        click(checkbox);
                        break;
                    }
                }
            }
        }

        public void selectBundlesByName(String value) {

            WebElement tdCell = form.findElement(By.xpath(String.format(".//td[normalize-space(text())='%s']", value)));
            WebElement checkbox = tdCell.findElement(By.xpath(".//input[@type='checkbox']"));
            if (checkbox.getAttribute("checked") != "true") {
                click(checkbox);
            }
        }


        public boolean isBonusBundle() {
            return isElementPresent(lblBonusBundle);
        }
    }
}
