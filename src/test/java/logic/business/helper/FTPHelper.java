package logic.business.helper;

import framework.config.Config;
import framework.utils.FTP;
import framework.utils.Log;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FTPHelper {
    FTP ftp;

    public static FTPHelper getInstance() {
        return new FTPHelper();
    }

    public FTPHelper() {
        ftp = new FTP(Config.getProp("unixServer"), 22, Config.getProp("unixUsername"), Config.getProp("unixPassword"));
        ftp.setUpConnection();
    }

    public void upLoadFromDisk(String localPathFile, String ftpFileName) {
        try {
            FileInputStream in = new FileInputStream(new File(localPathFile));
            ftp.uploadFile(Config.getProp("cdrFolder"), ftpFileName, in);
        } catch (FileNotFoundException ex) {
            Log.info(ex.getMessage());
        }
    }

    public void downLoadFromDisk(String remotePath, String fileName, String localPath) {
        try {
            ftp.downLoadFile(remotePath, fileName, localPath);
        } catch (Exception ex) {
            Log.info(ex.getMessage());
        }

    }

    public List<String> getAllFileName(String remoteServer) {
        return ftp.getAllFileName(remoteServer);
    }
}
