package logic.business.entities;

public class TariffSearchCriteriaEnity {
    public String staffTariff;
    public String billingType;
    public String tariffType;
    public String contractPeriod;
    public String monthlyRental;
    public String earlyTerminationCharge;

    public String getTariffCode() {
        return tariffCode;
    }

    public void setTariffCode(String tariffCode) {
        this.tariffCode = tariffCode;
    }

    public String tariffCode;

    public String getSpecialTariff() {
        return specialTariff;
    }

    public void setSpecialTariff(String specialTariff) {
        this.specialTariff = specialTariff;
    }

    public String specialTariff;

    public String getLastSaleDateExpired() {
        return lastSaleDateExpired;
    }

    public void setLastSaleDateExpired(String lastSaleDateExpired) {
        this.lastSaleDateExpired = lastSaleDateExpired;
    }

    public String lastSaleDateExpired;

    public String getStaffTariff() {
        return staffTariff;
    }

    public void setStaffTariff(String staffTariff) {
        this.staffTariff = staffTariff;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public String getTariffType() {
        return tariffType;
    }

    public void setTariffType(String tariffType) {
        this.tariffType = tariffType;
    }

    public String getContractPeriod() {
        return contractPeriod;
    }

    public void setContractPeriod(String contractPeriod) {
        this.contractPeriod = contractPeriod;
    }

    public String getMonthlyRental() {
        return monthlyRental;
    }

    public void setMonthlyRental(String monthlyRental) {
        this.monthlyRental = monthlyRental;
    }

    public String getEarlyTerminationCharge() {
        return earlyTerminationCharge;
    }

    public void setEarlyTerminationCharge(String earlyTerminationCharge) {
        this.earlyTerminationCharge = earlyTerminationCharge;
    }



}
