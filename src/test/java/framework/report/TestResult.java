package framework.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

public class TestResult {
    @JsonProperty("version")
    private String version;

    @JsonProperty("cycle")
    private String cycle;

    @JsonProperty("testId")
    private String testId;

    @JsonProperty("testName")
    private String testName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("groups")
    private String[] groups;

    @JsonProperty("executedBy")
    private String executedBy;

    @JsonProperty("executionDate")
    private String executionDate;

    @JsonProperty("duration")
    private String duration;

    @JsonProperty("status")
    private Status status;

    @JsonProperty("steps")
    private List<String> steps;

    @JsonProperty("errorMsg")
    private String errorMsg;

    @JsonProperty("imgPath")
    private String imgPath;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName.replaceAll("\\s+", "").trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getGroups() {
        return Arrays.copyOf(groups, groups.length);
    }

    public void setGroups(String... groups) {
        this.groups = Arrays.copyOf(groups, groups.length);
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(String executionDate) {
        this.executionDate = executionDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
}
