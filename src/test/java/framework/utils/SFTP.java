package framework.utils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.nio.file.Files;

/**
 * SFTP Connection
 * Upload and download file from server
 * Date : 04/12/2018
 *
 * @author : Tran Quoc Loi
 */

public class SFTP {

    protected String url;
    protected int port;
    protected String username;
    protected String password;
    private JSch mJschSession = null;
    private Session mSSHSession = null;

    //sftp channel
    private ChannelSftp mChannelSftp = null;


    public boolean connect(String strHostAddress, int iPort, String strUserName) {
        boolean blResult = false;

        try {
            //creating a new jsch session
            this.mJschSession = new JSch();
            String filePath = "src\\test\\resources\\privatekey\\privateKey.ppk";
            byte[] bFile = Files.readAllBytes(new File(filePath).toPath());
            final byte[] emptyPassPhrase = new byte[0];
            this.mJschSession.addIdentity(
                    strUserName,
                    bFile,
                    null,
                    emptyPassPhrase

            );

            //creating session with user, host port
            this.mSSHSession = mJschSession.getSession(strUserName, strHostAddress, iPort);
            this.mSSHSession.setConfig("PreferredAuthentications", "publickey");

            //set sftp server no check key when login
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            this.mJschSession.setConfig(config);

            //connect to host
            this.mSSHSession.connect();

            //open sftp channel
            this.mChannelSftp = (ChannelSftp) this.mSSHSession.openChannel("sftp");

            //connect to sftp session
            this.mChannelSftp.connect();
            if (this.mChannelSftp != null) {
                blResult = true;
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return blResult;
    }



    //download file
    public void downloadFile(String strSftpFile, String strLocalFile) {
        try {
            this.mChannelSftp.get(strSftpFile, strLocalFile);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    //upload file
    public void uploadFile(String strLocalFile, String strSftpFile) {
        try {
            this.mChannelSftp.put(strLocalFile, strSftpFile);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    //close session
    public void close() {
        try {
            this.mChannelSftp.disconnect();
        } catch (Exception exp) {

        }

        try {
            this.mSSHSession.disconnect();
        } catch (Exception exp) {

        }

        this.mChannelSftp = null;
        this.mSSHSession = null;
        this.mJschSession = null;
    }


}