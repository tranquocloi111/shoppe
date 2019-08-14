package logic.business.helper;

import framework.config.Config;
import framework.utils.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

public class FTPHelper {
    FTP ftp;
    public static FTPHelper getInstance(){
        return new FTPHelper();
    }

    public FTPHelper(){
        ftp = new FTP(Config.getProp("unixServer"),22, Config.getProp("unixUsername"),Config.getProp("unixPassword"));
        ftp.setUpConnection();
    }

    public  void upLoadFromDisk(String localPathFile, String ftpFileName){
        try {
            FileInputStream in = new FileInputStream(new File(localPathFile));
            boolean flag = ftp.uploadFile(Config.getProp("cdrFolder"), ftpFileName, in);
            System.out.println(flag);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public  void downLoadFromDisk(String remotePath, String fileName, String localPath){
        boolean flag = ftp.downLoadFile( remotePath, fileName,localPath);
        System.out.println(flag);
    }

}
