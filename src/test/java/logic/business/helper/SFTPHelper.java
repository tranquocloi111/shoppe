package logic.business.helper;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.SFTP;
import org.testng.Assert;

public class SFTPHelper extends SFTP {
    private static String userName = Config.getProp("UnixSFTPUsername");
    private static String host = Config.getProp("UnixSFTPServer");
    private static String passWord;

    public static SFTPHelper getInstance() {
        return new SFTPHelper();
    }

    public static SFTPHelper getGlassFishInstance() {
        host = Config.getProp("GlassFishServer");
        userName = Config.getProp("GFSUsername");
        passWord= Config.getProp("GFSPassword");
        return new SFTPHelper();
    }

    public void downloadFileFromRemoteServerToLocal(String localPath, String remotePath) {
        SFTP sftp = new SFTP();
        try {
            Assert.assertTrue(sftp.connect(host, 22, userName));
            sftp.downloadFile(remotePath, localPath);
            Log.info(localPath+remotePath);
        } catch (Exception ex) {
            System.out.print(ex);
        }
        finally {
            sftp.close();
        }

    }

    public void upFileFromLocalToRemoteServer(String localPath, String remotePath) {
        SFTP sftp = new SFTP();
        try {
            Assert.assertTrue(sftp.connect(host, 22, userName));
            sftp.uploadFile( localPath,remotePath);
            Log.info(localPath);
        } catch (Exception ex) {
            System.out.print(ex);
        }
        finally {
            sftp.close();
        }

    }

    public String generateDownLoadFile(String customerNumber) {
        int temp = 9 - customerNumber.length();
        String value = "";
        for (int i = 0; i < temp; i++) {
            value = value + "0";
        }

        return value + customerNumber;
    }

    public void downloadGlassFishFile(String localPath, String remotePath) {
        SFTP sftp = new SFTP();
        try {
            Assert.assertTrue(sftp.connect(host, 22, userName, passWord));
            sftp.downloadFile(remotePath, localPath);
        } catch (Exception ex) {
            System.out.print(ex);
        }
        finally {
            sftp.close();
        }

    }

}
