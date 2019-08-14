package logic.business.helper;

import com.sun.mail.imap.IMAPFolder;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import framework.config.Config;
import framework.utils.Email;
import framework.utils.Log;
import logic.utils.Common;
import net.bytebuddy.dynamic.scaffold.TypeWriter;

public class EmailHelper {

    private static EmailHelper instance = new EmailHelper();
    protected static String protocal;
    protected static String host;
    protected static String userName;
    protected static String passWord;
    Email email;

    public EmailHelper() {
        protocal = Config.getProp("protocal");
        host = Config.getProp("host");
        userName = Config.getProp("emailUsername");
        passWord = Config.getProp("emailPassword");
        email =new Email(protocal,host,userName,passWord);
    }


    public static EmailHelper getInstance() {
        if (instance == null)
            return new EmailHelper();
        return instance;
    }



    public String getPasswordFromEmail(String body) {
        String input = "Your password is:";
        int index = body.indexOf(input);
        return body.substring(index + 18, 97);
    }

    public String geLinkFromChangePasswordEmail(String body) {
        String input = "please visit ";
        int index = body.indexOf(input);
        return body.substring(index + 13, 325);
    }

    public String extractPasswordEmailByFolderNameAndEmailSubject(String folderName, String subjectEmail) {
        try {
            String body = email.getMailBody(folderName, subjectEmail);
            return getPasswordFromEmail(body);
        } catch (Exception ex) {
            return null;
        }
    }

    public String extractLinkEmailByFolderNameAndEmailSubject(String folderName, String subjectEmail) {
        try {
            String body = email.getMailBody(folderName, subjectEmail);
            return geLinkFromChangePasswordEmail(body);
        } catch (Exception ex) {
            return null;
        }
    }

    public void deleteAllEmailByFolderNameAndEmailSubject(String folderName, String subjectEmail) {
        try {
            email.deleteMailBySubject(folderName, subjectEmail);
        } catch (Exception ex) {

        }
    }

    public void waitEmailByFolderNameAndEmailSubject(String folderName, String subjectEmail, int timeOut) {
        try {
            email.waitNewMailBySubject(folderName, subjectEmail, timeOut);
        } catch (Exception ex) {

        }
    }

    public void convertEmailToFile(String folderName, String subjectEmail, String nameFile) {
        try {
            String body = email.getMailBody(folderName, subjectEmail);
            Common.writeFile(body, nameFile);

        } catch (Exception ex) {

        }

    }
    public boolean findStringInEmail(String folderName, String subjectEmail, String expectedString) {
        try {
            String body = email.getMailBody(folderName, subjectEmail);
            return body.contains(expectedString);

        } catch (Exception ex) {

        }
        return false;
    }


}