package framework.utils;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

/**
 * FTP Connection
 * Upload and download file from server
 * Date : 04/12/2018
 * @author : Quyen Vu
 */

public class FTP {

    protected String url;
    protected int port;
    protected String username;
    protected String password;

    public FTP(String url, int port, String username, String password) {
        this.url = url;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public FTPClient setUpConnection() {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(url);//connect to FTP server
            ftp.login(username, password);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ftp ;
    }

    public  boolean uploadFile(String path, String fileName, InputStream input) {
        boolean success = false;
        FTPClient ftp = setUpConnection();
        try {
            int reply;
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            ftp.changeWorkingDirectory(path);
            ftp.storeFile(fileName, input);

            input.close();
            ftp.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }

    public  boolean downLoadFile(String remotePath, String fileName, String localPath) {
        boolean success = false;
        FTPClient ftp = setUpConnection();
        try {
            int reply;
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            ftp.changeWorkingDirectory(remotePath);
            FTPFile[] fs = ftp.listFiles();
            for(FTPFile ff:fs){
                if(ff.getName().equals(fileName)){
                    File localFile = new File(localPath+"/"+ff.getName());
                    OutputStream is = new FileOutputStream(localFile);
                    ftp.retrieveFile(ff.getName(), is);
                    is.close();
                }
            }

            ftp.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }
}