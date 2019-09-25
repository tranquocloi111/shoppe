package logic.business.helper;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.SFTP;
import org.testng.Assert;

public class SFTPHelper extends SFTP {
    String userName = Config.getProp("UnixSFTPUsername");
    String host = Config.getProp("UnixSFTPServer");

    public static SFTPHelper getInstance() {
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
}
