package logic.business.helper;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.SSHManager;
import logic.business.db.OracleDB;
import logic.utils.*;
import org.testng.Assert;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class RemoteJobHelper {

    static int remoteJobId = 0;
    private static RemoteJobHelper instance = new RemoteJobHelper();
    private String envIndex = "9";
    private String unixUsernName;
    private String unixPassword;
    private String unixServer;

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

    private void submitRemoteJobs(String command, int currentMaxJobId) {
        String sql = String.format("select count(*) as numberJob from REMOTEJOB where jobid > %d ", currentMaxJobId);
        if (command.contains("-n 96 -j")) {
            delay(10);
        }

        MiscHelper.executeFuncntion(5, () ->
        {
            submitRemoteJob(command);
            return Integer.parseInt(String.valueOf(OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "numberJob"))) > 0;
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
        int currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("DoProvisionServices.sh -e $HUB_SID -JS $HUB_BIN/java/external-resources.zip -JS", currentMaxJobId);
        return waitForRemoteJobComplete(currentMaxJobId, "Provision Waiting Services");
    }

    public int submitDoRefillBcJob(Date date) {
        int currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("DoRefillBC.sh -e $HUB_SID -d %s -S", Parser.parseDateFormate(date, "ddMMyyyy")), currentMaxJobId);
        return waitForRemoteJobComplete(currentMaxJobId, "Refill Processing for Billing Cap");
    }

    public int submitDoRefillNcJob(Date date) {
        int currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("DoRefillNC.sh -e $HUB_SID -d %s -R Y -P Y -m CDR -S", Parser.parseDateFormate(date, "ddMMyyyy")), currentMaxJobId);
        return waitForRemoteJobComplete(currentMaxJobId, "Refill Processing for Network Cap");
    }

    public void submitDoBundleRenewJob(Date date) {
        int currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("DoBundleRenew.sh -e $HUB_SID -d %s -n 1 -S", Parser.parseDateFormate(date, "ddMMyyyy")), currentMaxJobId);
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
                remoteJobId = Integer.parseInt(String.valueOf(OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "JOBID")));
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
                        if (exitcode == null) {
                            jobComplete = false;
                        }else{
                            if (!cmdstatus.equalsIgnoreCase("N")) {
                                jobComplete = false;
                            }
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
                    int exitcode = rs.getInt("exitcode");
                    String cmdstatus = rs.getString("cmdstatus");
                    if (exitcode != 0) {
                        allComplete = false;
                        break;
                    }
                    if (!cmdstatus.equalsIgnoreCase("N")) {
                        allComplete = false;
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
        int currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs("DoBillrun.sh -e $HUB_SID -a s -C -S", currentMaxJobId);
        remoteJobId = waitForRemoteJobComplete(currentMaxJobId, "Bill Run");
    }

    public void submitConfirmBillRun() {
        ResultSet resultSet = OracleDB.SetToNonOEDatabase().executeQuery("select brinvocationid from billruninvocation where jobid=" + remoteJobId);
        try {
            for (int i = 0; i < 120; i++) {
                if (resultSet.isBeforeFirst()){
                    break;
                }else{
                    resultSet = OracleDB.SetToNonOEDatabase().executeQuery("select brinvocationid from billruninvocation where jobid=" + remoteJobId);
                }
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int billRunInvocationId = Integer.parseInt(String.valueOf(OracleDB.getValueOfResultSet(resultSet, "brinvocationid")));
        Log.info("InvocationId:" + billRunInvocationId);

        int currentMaxJobId = getMaxRemoteJobId();
        submitRemoteJobs(String.format("DoBillrun.sh -e $HUB_SID -a c -i %s -d %s -S", billRunInvocationId, Parser.parseDateFormate(TimeStamp.Today(), TimeStamp.DATE_FORMAT2)), currentMaxJobId);

        waitForRemoteJobComplete(currentMaxJobId, "Bill Run");
    }
}
