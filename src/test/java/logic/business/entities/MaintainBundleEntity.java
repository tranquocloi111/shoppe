package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

public class MaintainBundleEntity {

    public String typeAttribute;
    public String code;
    public String description;

    private static final String NEXTBILLDATE_CODE = "UBI_001";
    private static final String IMMEDIATE_CODE = "UBI_000";

    public String getTypeAttribute() {
        return typeAttribute;
    }

    public void setTypeAttribute(String typeAttribute) {
        this.typeAttribute = typeAttribute;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static MaintainBundleEntity getNormalMaintainBundle(){
        MaintainBundleEntity normalMaintainBundle = new MaintainBundleEntity();
        normalMaintainBundle.setTypeAttribute("INFO");
        normalMaintainBundle.setCode(NEXTBILLDATE_CODE);
        normalMaintainBundle.setDescription(String.format("Maintain Bundle request successful. Update scheduled for %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT3).toUpperCase()));

        return normalMaintainBundle;
    }

    public static MaintainBundleEntity getImmediateMaintainBundleData(){
        MaintainBundleEntity normalMaintainBundle = new MaintainBundleEntity();
        normalMaintainBundle.setTypeAttribute("INFO");
        normalMaintainBundle.setCode(IMMEDIATE_CODE);
        normalMaintainBundle.setDescription("Maintain Bundle request successful");

        return normalMaintainBundle;
    }

}
