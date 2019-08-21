package logic.business.entities;

public class PaymentGatewayEnity {
    public  String paymentGTWRequestID;
    public  String action;
    public  String paymentType;
    public  String saleChannel;

    public void setPaymentGTWRequestID(String paymentGTWRequestID) {
        this.paymentGTWRequestID = paymentGTWRequestID;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public void setSaleChannel(String saleChannel) {
        this.saleChannel = saleChannel;
    }

    public String getPaymentGTWRequestID() {
        return paymentGTWRequestID;
    }

    public String getAction() { return action; }

    public String getPaymentType() {
        return paymentType;
    }

    public String getSaleChannel() {
        return saleChannel;
    }


}
