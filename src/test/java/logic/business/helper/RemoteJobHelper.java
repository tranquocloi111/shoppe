package logic.business.helper;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.SSHManager;
import logic.business.db.OracleDB;
import logic.business.db.billing.CommonActions;
import logic.utils.*;
import org.testng.Assert;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class RemoteJobHelper {

    public static int remoteJobId = 0;
    private static RemoteJobHelper instance = new RemoteJobHelper();
    private String envIndex = "9";
    private String unixUsernName;
    private String unixPassword;
    private String unixServer;
    private int currentMaxJobId;

    private RemoteJobHelper() {
        this.unixUsernName = Config.getProp("unixUsername");
        this.unixPassword = Config.getProp("unixPassword");
        this.unixServer = Config.getProp("unixServer");
        this.envIndex = Config.getProp("evnIndex");
    }

    public static RemoteJobHelper getInstance() {
        if (instance == null)
            return new RemoteJobHelper();
        return instance;
    }

    public int getMaxRemoteJobId() {
        return Integer.parseInt(String.valueOf(OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery("select max(JOBID) as MAXJOBID from REMOTEJOB"), "MAXJOBID")));
    }

    private void submitRemoteJobs(String command, int currentMaxJobId, String jobDescr) {
        String sql = String.format("select count(*) as numberJob from REMOTEJOB where jobid > %d ", currentMaxJobId);
        if (command.contains("-n 96 -j")) {
            delay(10);
        }

        MiscHelper.executeFuncntion(5, () ->{
            submitRemoteJob(command);
            //submitRemoteJob(command, jobDescr);
            return Parser.asInteger(OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "numberJob")) > 0;
        }, 5);
    }

    private void submitRemoteJob(String command) {
        SSHManager sshManager = new SSHManager(unixUsernName, unixPassword, unixServer, "");
        sshManager.connect();
        String[] commands = {envIndex, "cd $HUB_BIN", command};
        sshManager.sendCommandWithShell(commands);
    }

    private boolean delay(int delayTime) {
        int now = LocalDateTime.now().getMinute();
        int m;
        do {
            int timeSpan = LocalDateTime.now().getMinute() - now;
            m = timeSpan;
        }
        while (m < delayTime);
        return true;
    }

    public int runProvisionSevicesJob() {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("DoProvisionServices.sh -e $HUB_SID -JS $HUB_BIN/java/external-resources.zip -JS", currentMaxJobId, "Provision Waiting Services");
        return waitForRemoteJobComplete(currentMaxJobId, "Provision Waiting Services");
    }

    public int submitDoRefillBcJob(Date date) {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("DoRefillBC.sh -e $HUB_SID -d %s -S", Parser.parseDateFormate(date, "ddMMyyyy")), currentMaxJobId, "Refill Processing for Billing Cap");
        return waitForRemoteJobComplete(currentMaxJobId, "Refill Processing for Billing Cap");
    }

    public int submitDoRefillNcJob(Date date) {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("DoRefillNC.sh -e $HUB_SID -d %s -R Y -P Y -m CDR -S", Parser.parseDateFormate(date, "ddMMyyyy")), currentMaxJobId, "Refill Processing for Network Cap");
        return waitForRemoteJobComplete(currentMaxJobId, "Refill Processing for Network Cap");
    }

    public void submitDoBundleRenewJob(Date date) {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("DoBundleRenew.sh -e $HUB_SID -d %s -n 1 -S", Parser.parseDateFormate(date, "ddMMyyyy")), currentMaxJobId, "DoBundleRenew.sh");
        waitForBundleRenewJobComplete(currentMaxJobId);
    }

    private int waitForRemoteJobComplete(int currentMaxJobId, String jobDescr) {
        int remoteJobId = 0;
        String descrCondition = "=";
        if (jobDescr.contains("%")) {
            descrCondition = " like ";
        }
        String sql = String.format("select jobid from REMOTEJOB where jobdescr %s '%s' and jobid > %d", descrCondition, jobDescr, currentMaxJobId);
        try {
            for (int i = 0; i < 300; i++) {
                if (remoteJobId > currentMaxJobId)
                    break;
                try {
                    remoteJobId = Integer.parseInt(String.valueOf(OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "JOBID")));
                } catch (Exception ex) {
                }
                Thread.sleep(3000);
            }
            Log.info(jobDescr + " job id:" + remoteJobId);
            if (remoteJobId > currentMaxJobId) {
                sql = "select exitcode,cmdstatus from REMOTEJOB where jobid=" + remoteJobId;
                boolean jobComplete = false;
                for (int i = 0; i < 600; i++) {
                    jobComplete = true;
                    ResultSet rs = OracleDB.SetToNonOEDatabase().executeQuery(sql);
                    while (rs.next()) {
                        String exitcode = rs.getString("exitcode");
                        String cmdstatus = rs.getString("cmdstatus");
                        if (exitcode == null || cmdstatus == null) {
                            jobComplete = false;
                        } else if (!exitcode.equalsIgnoreCase("0") || !cmdstatus.equalsIgnoreCase("N")) {
                            jobComplete = false;
                        } else if (exitcode.equalsIgnoreCase("0") && cmdstatus.equalsIgnoreCase("N")) {
                            jobComplete = true;
                        }
                    }
                    if (jobComplete)
                        break;
                    Thread.sleep(3000);
                }
                if (!jobComplete) {
                    Log.info(String.format(jobDescr + " job {0} can't finish in 10 minutes", remoteJobId));
                }
            } else {
                Log.info(String.format("Can't find {0} job in 5 minutes", jobDescr));
            }
            waitAllNewRemoteJobsComplete(remoteJobId);
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }

        return remoteJobId;
    }

    private void waitForBundleRenewJobComplete(int currentMaxJobId) {
        String sql = "select cmdstatus,exitcode from REMOTEJOB where jobdescr like 'Discount Bundle Renewal%' and jobid > " + currentMaxJobId;
        try {
            boolean hasJob = false;
            boolean allComplete = true;
            for (int i = 0; i < 300; i++) {
                allComplete = true;
                ResultSet rs = OracleDB.SetToNonOEDatabase().executeQuery(sql);
                while (rs.next()) {
                    hasJob = true;
                    int exitCode = rs.getInt("exitcode");
                    String cmdStatus = rs.getString("cmdstatus");
                    if (exitCode != 0 || !cmdStatus.equalsIgnoreCase("N")) {
                        allComplete = false;
                        break;
                    } else if (exitCode == 0 && cmdStatus.equalsIgnoreCase("N")) {
                        allComplete = true;
                        break;
                    }
                }
                if (hasJob && allComplete)
                    break;
                Thread.sleep(1000);
            }

            if (!(hasJob && allComplete)) {
                Assert.fail("Discount bundle renewal job can't complete in 5 minutes or can't find jobs start from id " + currentMaxJobId);
            }
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
    }

    private void waitAllNewRemoteJobsComplete(int initJobId) {
        String sql = "select count(*) as ALLJOB  from REMOTEJOB where (exitcode is null or cmdstatus<>'N') and jobid > " + initJobId;
        MiscHelper.executeFuncntion(150, () -> Integer.parseInt(String.valueOf(OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "ALLJOB"))) == 0, 2);
    }

    public void submitDraftBillRun() {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("DoBillrun.sh -e $HUB_SID -a s -C -S", currentMaxJobId, "Bill Run");
        remoteJobId = waitForRemoteJobComplete(currentMaxJobId, "Bill Run");
    }

    public void submitConfirmBillRun() {
        ResultSet resultSet = OracleDB.SetToNonOEDatabase().executeQuery("select brinvocationid from billruninvocation where jobid=" + remoteJobId);
        try {
            for (int i = 0; i < 120; i++) {
                if (resultSet.isBeforeFirst()) {
                    break;
                } else {
                    resultSet = OracleDB.SetToNonOEDatabase().executeQuery("select brinvocationid from billruninvocation where jobid=" + remoteJobId);
                }
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int billRunInvocationId = Parser.asInteger(OracleDB.getValueOfResultSet(resultSet, "brinvocationid"));
        Log.info("InvocationId:" + billRunInvocationId);

        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("DoBillrun.sh -e $HUB_SID -a c -i %s -d %s -S", billRunInvocationId, Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT2)), currentMaxJobId, "Bill Run");
        waitForRemoteJobComplete(currentMaxJobId, "Bill Run");
    }

    public void submitConfirmBillRun(Date date) {
        ResultSet resultSet = OracleDB.SetToNonOEDatabase().executeQuery("select brinvocationid from billruninvocation where jobid=" + remoteJobId);
        try {
            for (int i = 0; i < 120; i++) {
                if (resultSet.isBeforeFirst()) {
                    break;
                } else {
                    resultSet = OracleDB.SetToNonOEDatabase().executeQuery("select brinvocationid from billruninvocation where jobid=" + remoteJobId);
                }
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int billRunInvocationId = Parser.asInteger(OracleDB.getValueOfResultSet(resultSet, "brinvocationid"));
        Log.info("InvocationId:" + billRunInvocationId);

        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("DoBillrun.sh -e $HUB_SID -a c -i %s -d %s -S", billRunInvocationId, Parser.parseDateFormate(date, TimeStamp.DATE_FORMAT2)), currentMaxJobId, "Bill Run");
        waitForRemoteJobComplete(currentMaxJobId, "Bill Run");
    }

    public void runSMSRequestJob() {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("DoSMSRequest.sh -e $HUB_SID -P -J", currentMaxJobId, "SMS Request");
        remoteJobId = waitForRemoteJobComplete(currentMaxJobId, "SMS Request");
    }

    public void runDoDealXMLExtractJob() {
        String reportrunId = CommonActions.getReportUniqueId();
        Log.info("reportrunId:" + reportrunId);
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJob(String.format("DoDealXMLExtract.sh -i %s -e $HUB_SID", reportrunId), "Deal XML Extract");
        waitForRemoteJobComplete(currentMaxJobId, "Deal XML Extract");
    }

    public void runDealCatalogueExtractJob() {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("Reports.sh -e $HUB_SID -J", currentMaxJobId, "Deal Catalogue Extract");
        remoteJobId = waitForRemoteJobComplete(currentMaxJobId, "Deal Catalogue Extract");
    }

    public void submitPaymentAllocationBatchJobRun() {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("DoAutoAlloc.sh -e $HUB_SID -S", currentMaxJobId, "Auto Allocation Processing");
        remoteJobId = waitForRemoteJobComplete(currentMaxJobId, "Auto Allocation Processing");
    }

    public void submitCreditCardBatchJobRun() {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("Subcreditcard.sh -e $HUB_SID -S", currentMaxJobId, "Process Credit Card");
        remoteJobId = waitForRemoteJobComplete(currentMaxJobId, "Process Credit Card");
    }

    public void submitSendDDIRequestJob() {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("Subdirectdebit1.sh -e $HUB_SID -S", currentMaxJobId, "Process Direct Debit - Send DDI");
        remoteJobId = waitForRemoteJobComplete(currentMaxJobId, "Process Direct Debit - Send DDI");
    }

    public void waitLoadCDRJobComplete() {
        currentMaxJobId = getMaxRemoteJobId();
        waitForRemoteJobComplete(currentMaxJobId, "Java LAR - Tesco Mobile Post Pay");
    }

    public static void submitRemoteJob(String command, String jobdescr) {
        String sql = "INSERT INTO remotejob (cmdtype,cmdstatus,execattimestamp,submitby, jobcmdline, jobtype, jobdescr) " +
                " SELECT 'R','W',pkg_calc.now,1 " +
                " ,'" + command + "' " +
                " ,'R' " +
                " ,'" + jobdescr + "' " +
                " FROM dual  ";

        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public void waitForTopUpFile(String fileName) {
        currentMaxJobId = getMaxRemoteJobId();
        waitForRemoteJobComplete(currentMaxJobId, "%" + fileName);
    }

    public void runDirectDebitBatchJobToCreatePayments() {
        int currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("Subdirectdebit2.sh -e $HUB_SID -S", currentMaxJobId, "Process Direct Debit - Create Payments");
        waitForRemoteJobComplete(currentMaxJobId, "Process Direct Debit - Create Payments");
    }

    public void  waitEncryptFileComplete(){
        currentMaxJobId = getMaxRemoteJobId();
        waitForRemoteJobComplete(currentMaxJobId, "Encrypt usage file");
    }

    public void  waitForLoadFile(String fileName){
        currentMaxJobId = getMaxRemoteJobId();
        waitForRemoteJobComplete(currentMaxJobId,  "Process inbound non-xml file : " + fileName);
    }

    public void submitDoUsageRemoteJob(){
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("DoUsage.sh -e $HUB_SID -L N -A N -R N -C Y -S", currentMaxJobId, "Usage Processing");
        waitForRemoteJobComplete(currentMaxJobId, "Usage Processing");
    }

    public void runCreditAgreementAnnualStatement() {
        int currentMaxJobId = getMaxRemoteJobId();
        String pathReport = Config.getProp("cdrFolder");
        String id = CommonActions.getReportUniqueId();
        pathReport = pathReport.substring(0, pathReport.indexOf("/Feed"));
        String command = String.format("Reports.sh -i %s -k CRP334R -f %s/Reports/%s.ps", id, pathReport, id);
        submitRemoteJob(command, "Credit Agreement Annual Statement");
        waitForRemoteJobComplete(currentMaxJobId, "Credit Agreement Annual Statement");
    }

    public void submitAnonymiseAccountJob(){
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJob("AnonymiseAccount.sh -e $HUB_SID","Anonymise Deactivated Accounts");
        waitForRemoteJobComplete(currentMaxJobId, "Anonymise Deactivated Accounts");
    }

    public void submitAnonymiseOrderJob(){
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJob("AnonymiseOrder.sh -e $HUB_SID -j","Anonymise Abandoned and Cancelled Orders");
        waitForRemoteJobComplete(currentMaxJobId, "Anonymise Abandoned and Cancelled Orders");
    }

    public  int submitDMExtractJob(String controlId, String jobDescr) {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("DMExtract.sh -e $HUB_SID -t S -n %s -j", controlId), currentMaxJobId, jobDescr);
        return waitForRemoteJobComplete(currentMaxJobId, jobDescr);
    }

    public  int submitDoDMExtractJob(){
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("DMExtract.sh -e $HUB_SID -t A -j", currentMaxJobId, "DataMart Data Extract");
        return waitForRemoteJobComplete(currentMaxJobId, "DataMart Data Extract");
    }

    public void submitTreatmentJob(Date date) {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("treatment.sh -a %s -R", Parser.parseDateFormate(date, "yyyyMMdd")), currentMaxJobId, "Treatment Batch run");
        remoteJobId = waitForRemoteJobComplete(currentMaxJobId, "Treatment Batch run");
        waitForRemoteJobComplete(remoteJobId, "Automatic Treatment Letters");
    }

    public void submitTreatmentJob(String date) {
        currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("treatment.sh -a %s -R", date), currentMaxJobId, "Treatment Batch run");
        remoteJobId = waitForRemoteJobComplete(currentMaxJobId, "Treatment Batch run");
        waitForRemoteJobComplete(remoteJobId, "Automatic Treatment Letters");
    }
}
