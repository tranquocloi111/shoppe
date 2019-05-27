package logic.business.helper;

import logic.utils.Common;
import framework.utils.FileDownloader;
import framework.utils.Log;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
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

}
