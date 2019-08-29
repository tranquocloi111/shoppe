package logic.business.helper;

import framework.config.Config;
import framework.utils.Log;
import framework.utils.SFTP;

public class SFTPHelper extends SFTP {
    String userName = Config.getProp("unixUsername");
    String passWord = Config.getProp("unixPassword");
    String host = Config.getProp("unixServer");

    public static SFTPHelper getInstance() {
        return new SFTPHelper();
    }

    public void downloadFileFromRemoteServerToLocal(String localPath, String remotePath) {
        SFTP sftp = new SFTP();
        try {
            sftp.connect(host, 22, userName, passWord);
            sftp.downloadFile(remotePath, localPath);
            Log.info(localPath);
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
            sftp.connect(host, 22, userName, passWord);
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
