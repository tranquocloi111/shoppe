package logic.entities;

public class Address {

    private String BuildingIdentifier;
    private String AddressLine1 ;
    private String AddressLine2 ;
    private String Town ;
    private String Country ;
    private String Postcode ;
    private String ptcAbsCode ;

    public String getBuildingIdentifier() {
        return BuildingIdentifier;
    }

    public void setBuildingIdentifier(String buildingIdentifier) {
        BuildingIdentifier = buildingIdentifier;
    }

    public String getAddressLine1() {
        return AddressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        AddressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return AddressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        AddressLine2 = addressLine2;
    }

    public String getTown() {
        return Town;
    }

    public void setTown(String town) {
        Town = town;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getPostcode() {
        return Postcode;
    }

    public void setPostcode(String postcode) {
        Postcode = postcode;
    }

    public String getPtcAbsCode() {
        return ptcAbsCode;
    }

    public void setPtcAbsCode(String ptcAbsCode) {
        this.ptcAbsCode = ptcAbsCode;
    }


}
