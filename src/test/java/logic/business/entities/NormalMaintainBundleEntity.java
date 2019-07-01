package logic.business.entities;

import logic.utils.Parser;
import logic.utils.TimeStamp;

public class NormalMaintainBundleEntity {

    public String typeAttribute;
    public String code;
    public String description;

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



    public static NormalMaintainBundleEntity getNormalMaintainBundle(){
        NormalMaintainBundleEntity normalMaintainBundle = new NormalMaintainBundleEntity();
        normalMaintainBundle.setTypeAttribute("INFO");
        normalMaintainBundle.setCode("UBI_001");
        normalMaintainBundle.setDescription(String.format("Maintain Bundle request successful. Update scheduled for %s", Parser.parseDateFormate(TimeStamp.TodayPlus1Month(), TimeStamp.DATE_FORMAT3).toUpperCase()));

        return normalMaintainBundle;
    }

}
