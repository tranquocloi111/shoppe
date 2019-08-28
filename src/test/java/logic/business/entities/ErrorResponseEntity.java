package logic.business.entities;

import java.util.List;

/**
 * User: Nhi Dinh
 * Date: 27/08/2019
 */
public class ErrorResponseEntity {
    private String faultCode;
    private String faultString;
    private String codeAttribute;
    private String typeAttribute;
    private String description;
    private String exceptionMsg;
    private String exceptionCauseMsg;
    private List<SelfCareServiceMultiExceptionEntity> SCSMultiExceptionMessages;

    public List<SelfCareServiceMultiExceptionEntity> getSCSMultiExceptionMessages() {
        return SCSMultiExceptionMessages;
    }
    public void setSCSMultiExceptionMessages(List<SelfCareServiceMultiExceptionEntity> SCSMultiExceptionMessages) {
        this.SCSMultiExceptionMessages = SCSMultiExceptionMessages;
    }

    public String getFaultCode() {
        return faultCode;
    }
    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    public String getFaultString() {
        return faultString;
    }
    public void setFaultString(String faultString) {
        this.faultString = faultString;
    }

    public String getCodeAttribute() {
        return codeAttribute;
    }
    public void setCodeAttribute(String codeAttribute) {
        this.codeAttribute = codeAttribute;
    }

    public String getTypeAttribute() {
        return typeAttribute;
    }
    public void setTypeAttribute(String typeAttribute) {
        this.typeAttribute = typeAttribute;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getExceptionMsg() {
        return exceptionMsg;
    }
    public void setExceptionMsg(String exceptionMsg) {
        this.exceptionMsg = exceptionMsg;
    }

    public String getExceptionCauseMsg() {
        return exceptionCauseMsg;
    }
    public void setExceptionCauseMsg(String exceptionCauseMsg) {
        this.exceptionCauseMsg = exceptionCauseMsg;
    }

    public ErrorResponseEntity() {
    }

    public ErrorResponseEntity(String faultCode, String faultString, String codeAttribute, String typeAttribute, String description, String exceptionMsg, String exceptionCauseMsg, List<SelfCareServiceMultiExceptionEntity> SCSMultiExceptionMessages) {
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.codeAttribute = codeAttribute;
        this.typeAttribute = typeAttribute;
        this.description = description;
        this.exceptionMsg = exceptionMsg;
        this.exceptionCauseMsg = exceptionCauseMsg;
        this.SCSMultiExceptionMessages = SCSMultiExceptionMessages;
    }

    public static class SelfCareServiceMultiExceptionEntity{
        private String typeAttribute;
        private String code;
        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTypeAttribute() {
            return typeAttribute;
        }

        public void setTypeAttribute(String typeAttribute) {
            this.typeAttribute = typeAttribute;
        }

        public SelfCareServiceMultiExceptionEntity(String code, String description, String typeAttribute){
            this.code = code;
            this.description = description;
            this.typeAttribute = typeAttribute;
        }

        public SelfCareServiceMultiExceptionEntity() {
        }
    }
}
