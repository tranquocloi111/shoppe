package logic.business.helper;

import framework.utils.FileDownloader;
import framework.utils.Log;
import framework.utils.RandomCharacter;
import logic.business.db.OracleDB;
import logic.utils.Common;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public class MiscHelper {

    public static boolean executeFuncntion(int maxTryTimes, Callable<Boolean> func, int interval) {
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
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return false;
    }

    public static void waitForSeconds(int milliseconds) {
        LocalDateTime now = LocalDateTime.now();
        int endtime = now.plusSeconds(milliseconds).getSecond();
        int current = 0;
        do {
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

    public static void saveFileFromWebRequest(WebElement element, String url, String pdfFile) {
        String localDownloadPath = Common.getFolderLogFilePath();
        FileDownloader fileDownloader = new FileDownloader(localDownloadPath);
        try {
            fileDownloader.downloadFile(element, url, pdfFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void saveFileFromWebRequest(String url, String pdfFile) {
        String localDownloadPath = Common.getFolderLogFilePath();
        FileDownloader fileDownloader = new FileDownloader(localDownloadPath);
        try {
            Common.convertInputStreamToPdfFile(fileDownloader.downloadFile(url), localDownloadPath + pdfFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String RandomStringF9() {
        return RandomCharacter.getRandomNumericString(9);
    }

    public static void saveImage(String imageUrl, String destinationFile) {

        // Create a new trust manager that trust all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                         return new X509Certificate[0];
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

// Activate the new trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        try {
            destinationFile=Common.getFolderLogFilePath()+destinationFile;
            URL url = new URL(imageUrl);
            Authenticator.setDefault(new MyAuthenticator("un292108730@hsntech.com", "Password10"));
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            FileOutputStream fos = new FileOutputStream(new File(destinationFile));
            int length = -1;
            byte[] buffer = new byte[1024];// buffer for portion of data from connection
            while ((length = is.read(buffer)) > -1) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
        }

    }
    static class MyAuthenticator extends Authenticator {
        private String username, password;

        public MyAuthenticator(String user, String pass) {
            username = user;
            password = pass;
        }
    }

}


