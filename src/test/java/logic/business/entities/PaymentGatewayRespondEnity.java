package logic.business.entities;

public class PaymentGatewayRespondEnity {
    public String Status;
    public String action;
    public String GatewayStatus;
    public String BankStatus;
    public String TDSAuthResponseType;
    public String FraudStatus;
    public String TokenStatus;
    public String GatewayRequestid;


    public void setGatewayRequestid(String GatewayRequestid) {
        this.GatewayRequestid = GatewayRequestid;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setGatewayStatus(String GatewayStatus) {
        this.GatewayStatus = GatewayStatus;
    }

    public void setTDSAuthResponseType(String TDSAuthResponseType) {
        this.TDSAuthResponseType = TDSAuthResponseType;
    }

    public void setFraudStatus(String FraudStatus) {
        this.FraudStatus = FraudStatus;
    }

    public void setTokenStatus(String TokenStatus) {
        this.TokenStatus = TokenStatus;
    }

    public void setBankStatus(String BankStatus) {
        this.BankStatus = BankStatus;
    }
    public void setStatus(String Status) {
        this.Status = Status;
    }

    public String getGatewayRequestid() {
        return GatewayRequestid;
    }

    public String getAction() { return action; }

    public String getBankStatus() {
        return BankStatus;
    }

    public String getTDSAuthResponseType() {
        return TDSAuthResponseType;
    }

    public String getFraudStatus() {
        return FraudStatus;
    }

    public String getTokenStatus() { return TokenStatus; }

    public String getGatewayStatus() {
        return GatewayStatus;
    }

    public String getStatus() {
        return Status;
    }


}
