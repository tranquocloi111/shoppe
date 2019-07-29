package logic.business.helper;

import framework.utils.RandomCharacter;
import logic.business.db.OracleDB;
import logic.utils.Common;
import framework.utils.FileDownloader;
import framework.utils.Log;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.Callable;

public class MiscHelper {

    public static boolean executeFuncntion(int maxTryTimes, Callable<Boolean> func, int interval )  {
        try {
            while (maxTryTimes > 0) {
                if (func.call()) {
                    return true;
                }
                maxTryTimes--;
                //replaces thread sleep to  Wait_for_seconds
                //Thread.Sleep(interval * 1000);
                waitForSeconds(1500 * interval);
            }
        }catch (Exception ex){
            Log.error(ex.getMessage());
        }
        return false;
    }

    public static void waitForSeconds(int milliseconds){
        LocalDateTime now = LocalDateTime.now();
        int endtime = now.plusSeconds(milliseconds).getSecond();
        int current = 0;
        do{
            current = LocalDateTime.now().getSecond();
        }
        while (endtime > current);
    }

    public static void waitForAsyncProcessComplete(String orderId) {
        Boolean taskFinished = false;
        try {
            String sql = String.format("select count(*) as backOfficTaskCount from asynccommandreq areq where areq.processkey = '%s'", orderId);
            int backOfficTaskCount = Integer.parseInt(String.valueOf(OracleDB.getValueOfResultSet(OracleDB.SetToOEDatabase().executeQuery(sql), "backOfficTaskCount")));
            sql = String.format("select count(*) as successfullbackOfficTaskCount from asynccommandreq areq where areq.processkey = '%s' and areq.STATUS = 'COMPLETED_EXC_SUCCESS'", orderId);
            for (int i = 0; i < 300; i++) {
                int successfullbackOfficTaskCount = Integer.parseInt(String.valueOf(OracleDB.getValueOfResultSet(OracleDB.SetToOEDatabase().executeQuery(sql), "successfullbackOfficTaskCount")));
                Thread.sleep(2000);
                if (backOfficTaskCount == successfullbackOfficTaskCount) {
                    taskFinished = true;
                    break;
                } else {
                    taskFinished = false;
                }
                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        if (!taskFinished) {
            Log.error("The task can't finish in 5 minutes");
        }

    }

    public static void saveFileFromWebRequest(WebElement element, String url, String pdfFile){
        Common.createUserDir("QA_Project");
        String localDownloadPath = System.getProperty("user.home")+"\\Desktop\\QA_Project\\";
        FileDownloader fileDownloader = new FileDownloader(localDownloadPath);
        try {
            fileDownloader.downloadFile(element, url, pdfFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String RandomStringF9(){
        return RandomCharacter.getRandomNumericString(9);
    }
}
