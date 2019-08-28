package logic.business.entities;

public class PaymentInfoEnity {
    public String paymentMethod;
    public String cardType;
    public String creditCardNumber;
    public String creditCardExpiryMonth;
    public String creditCardExpiryYear;
    public String bankSortCode;
    public String bankAccountNumber;
    public String dDIStatus;
    public String dDIReference;
    public String bankName;
    public String bankAccountHolderName;

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setcreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public void setcreditCardExpiryMonth(String creditCardExpiryMonth) {
        this.creditCardExpiryMonth = creditCardExpiryMonth;
    }

    public void setcreditCardExpiryYear(String creditCardExpiryYear) {
        this.creditCardExpiryYear = creditCardExpiryYear;
    }

    public void setbankSortCode(String bankSortCode) {
        this.bankSortCode = bankSortCode;
    }

    public void setbankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public void setdDIStatus(String dDIStatus) {
        this.dDIStatus = dDIStatus;
    }

    public void setdDIReference(String dDIStatus) {
        this.dDIReference = dDIReference;
    }

    public void setdbankAccountHolderName(String bankAccountHolderName) {
        this.bankAccountHolderName = bankAccountHolderName;
    }

    public String getCardType() {
        return this.cardType;
    }

    public String getpaymentMethod() {
        return this.paymentMethod;
    }

    public String getbankAccountHolderName() {
        return this.bankAccountHolderName;
    }

    public String getbankSortCode() {
        return this.bankSortCode;
    }

    public String getbankAccountNumber() {
        return this.bankAccountNumber;
    }

    public String getdDIReference() {
        return this.dDIReference;
    }

    public String getdDIStatus() {
        return this.dDIStatus;
    }


}
