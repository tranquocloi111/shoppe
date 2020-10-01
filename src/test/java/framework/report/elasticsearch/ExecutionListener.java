package framework.report.elasticsearch;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import framework.report.TestResult;
import framework.report.Status;

import java.util.concurrent.TimeUnit;

public class ExecutionListener implements ITestListener {

    public void onTestStart(final ITestResult iTestResult) {
        ResultManager.setResult(new TestResult());
    }

    public void onTestSuccess(final ITestResult iTestResult) {
        sendTestResult(iTestResult, Status.PASS);
    }

    public void onTestFailure(final ITestResult iTestResult) {
        sendTestResult(iTestResult, Status.FAIL);
    }

    public void onTestSkipped(final ITestResult iTestResult) {
        sendTestResult(iTestResult, Status.SKIPPED);
    }

    public void onTestFailedButWithinSuccessPercentage(final ITestResult iTestResult) {
    }

    @Override
    public void onStart(ITestContext iTestContext) {

    }


    public void onFinish(final ITestContext iTestContext) {
    }

    private void sendTestResult(final ITestResult itr, final Status stt) {
        result().setTestName(itr.getMethod().getDescription());
        result().setGroups(itr.getMethod().getGroups());
        result().setStatus(stt);
        result().setExecutedBy(System.getProperty("user.name"));
        result().setDuration(calDuration(itr));
        if (stt != Status.PASS) {
            result().setErrorMsg(itr.getThrowable().toString());
        }
        result().setExecutionDate(ISODateTimeFormat.dateTime().print(DateTime.now()));
        ElasticSender.sendTestResult(result());
    }


    private TestResult result() {
        return ResultManager.getResult();
    }

    private String calDuration(final ITestResult itr) {
        final long durInMillis = itr.getEndMillis() - itr.getStartMillis();
        return String.format("%d min %d sec",
                TimeUnit.MILLISECONDS.toMinutes(durInMillis),
                TimeUnit.MILLISECONDS.toSeconds(durInMillis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durInMillis)));
    }
}
