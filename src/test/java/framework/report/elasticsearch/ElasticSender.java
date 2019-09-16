package framework.report.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import framework.report.TestResult;

public final class ElasticSender {
    //region CONFIG
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ELASTICSEARCH_URL = "http://localhost:9200";
    private static final String RESULT_URL = ELASTICSEARCH_URL + "/result";
    private static final String META_URL = ELASTICSEARCH_URL + "/tm_auto/settings";
    //endregion

    //region INFO
    private static String version = "unknown";
    private static String cycle = "unknown";
    private static boolean isEnable = false;
    private static ObjectMapper mapper = new ObjectMapper();
    private static String curReleaseUrl;
    //endregion


    public static void setRunInfo() {
        try {
            final String resObj = Unirest.get(META_URL + "/_search?q=*").asJson().getBody().getObject().toString();
            version = JsonPath.read(resObj, "$.hits.hits[0]._source.version");
            cycle = JsonPath.read(resObj, "$.hits.hits[0]._source.cycle");
            isEnable = JsonPath.read(resObj, "$.hits.hits[0]._source.isEnable");
        } catch (Exception e) {
        }
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new ISO8601DateFormat());
        curReleaseUrl = RESULT_URL + "/" + version + "_" + cycle;
    }


    public static void sendTestResult(final TestResult testResult) {
        try {
            if (isEnable) {
                testResult.setVersion(version);
                testResult.setCycle(cycle);

                final String testId = isExistingTest(testResult.getTestName());
                if ("".equals(testId)) {
                    createNewTestResult(testResult);
                } else {
                    updateExistingTestResult(testId, testResult);
                }
            }
        } catch (UnirestException e) {
        }
    }


    private static String isExistingTest(final String testName) throws UnirestException {
        String testId = "";
        final String resStr = Unirest.get(curReleaseUrl + "/_search?q=testName:" + testName).asJson()
                .getBody().getObject().toString();
        try {
            if (!"0".equals(JsonPath.read(resStr, "$.hits.total"))) {
                testId = JsonPath.read(resStr, "$.hits.hits[0]._id");
            }
        } catch (PathNotFoundException e) {
        }
        return testId;
    }

    private static void createNewTestResult(final TestResult testStatus) throws UnirestException {
        Unirest.post(curReleaseUrl)
                .header(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .body(serialize(testStatus)).asJson();
    }

    private static void updateExistingTestResult(final String testId, final TestResult testStatus) throws UnirestException {
        Unirest.post(curReleaseUrl + "/" + testId + "/_update")
                .header(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .body("{\"doc\" :" + serialize(testStatus) + "}").asJson();
    }

    private static String serialize(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
