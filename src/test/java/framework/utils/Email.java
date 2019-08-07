package framework.utils;

import com.sun.mail.imap.IMAPFolder;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import java.util.Arrays;
import java.util.Properties;

public class Email {

    protected String protocal;
    protected String host;
    protected String userName;
    protected String passWord;

    public Email(String protocal , String host, String userName, String passWord){
        this.protocal = protocal;
        this.host = host;
        this.userName = userName;
        this.passWord = passWord;
    }

    public Store setUpConnection() {
        Session session = Session.getDefaultInstance(setProperties(), null);
        Store store = null;
        try {
            store = session.getStore(protocal);
            store.connect(host, userName, passWord);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return  store;
    }

    public void searchEmail(String folderName, final String keyword) {
        try {
            // connects to the message store
            Store store = setUpConnection();
            // opens the inbox folder
            Folder folderInbox = store.getDefaultFolder().getFolder(folderName);
            folderInbox.open(Folder.READ_ONLY);
            // creates a search criterion
            SearchTerm searchCondition = new SearchTerm() {
                @Override
                public boolean match(Message message) {
                    try {
                        if (message.getSubject().contains(keyword)) {
                            return true;
                        }
                    } catch (MessagingException ex) {
                        ex.printStackTrace();
                    }
                    return false;
                }
            };

            // performs search through the folder
            Message[] foundMessages = folderInbox.search(searchCondition);
            for (int i = 0; i < foundMessages.length; i++) {
                Message message = foundMessages[i];
                String subject = message.getSubject();
                System.out.println("Found message #" + i + ": " + subject);
            }

            // disconnect
            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store.");
            ex.printStackTrace();
        }
    }

    public  void sendMail (String subject, String messageText, String to) throws AddressException, MessagingException{

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp-mail.outlook.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, passWord);
                    }
                });
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(userName));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));   // like inzi769@gmail.com
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public  void replyMail (String folderName, String subjectTittle) throws AddressException, MessagingException{

        String to = "test@gmail.ch";
        final String username = "user";
        final String password = "pass";

        IMAPFolder folder = null;
        Store store = null;
        try
        {
            Authenticator auth = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };
            Session session = Session.getInstance(setProperties(), auth);

            store = setUpConnection();
            folder = (IMAPFolder) store.getFolder(folderName);
            folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();

            for (int i = 0, n = messages.length; i < n; i++) {
                Message message = messages[i];
                String subject = message.getSubject();
                if (subject.contains(subjectTittle)) {
                    Message replyMessage = new MimeMessage(session);
                    replyMessage = (MimeMessage) message.reply(false);
                    replyMessage.setFrom(new InternetAddress(to));
                    replyMessage.setText("");
                    replyMessage.setReplyTo(message.getReplyTo());
                    Transport.send(replyMessage);
                }
            }
            System.out.println("-- Step: Replied Mail To Deactivate --");
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }


    public boolean findMailBySubject(String folderName, String subject) throws MessagingException {
        IMAPFolder folder = null;
        Store store = null;
        boolean istrue = false;
        try{
            store = setUpConnection();;
            folder = (IMAPFolder) store.getDefaultFolder().getFolder(folderName);
            if(!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();
            Arrays.sort(messages, (m1, m2) -> {
                try {
                    return m2.getSentDate().compareTo(m1.getSentDate());
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });

            for(int i = 0; i < messages.length; i++){
                if(messages[i].getSubject().toLowerCase().contains(subject.toLowerCase())){
                    System.out.println("-- Verify: Expected email found : " + messages[i].getSubject()+ " --");
                    messages[i].setFlag(Flags.Flag.SEEN,true);
                    return true;
                }
            }
            System.out.println("-- Verify: No email found with subject contains: " + subject + " --");
        }finally{
            if (folder != null && folder.isOpen()) { folder.close(true); }
            if (store != null) { store.close(); }
        }
        return istrue;
    }

    public  boolean findMailByIndex(String folderName, int index) throws MessagingException {
        IMAPFolder folder = null;
        Store store = null;
        boolean istrue = false;
        try{
            store = setUpConnection();
            folder = (IMAPFolder) store.getDefaultFolder().getFolder(folderName);
            if(!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();
            Arrays.sort(messages, (m1, m2) -> {
                try {
                    return m2.getSentDate().compareTo(m1.getSentDate());
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });

            if (messages.length > 0){
                if (index <= messages.length && index > 0){
                    if(!messages[index - 1].getSubject().isEmpty()){
                        System.out.println("-- Verify: Expected email found : " + messages[index - 1].getSubject()+ " --");
                        messages[index - 1].setFlag(Flags.Flag.SEEN,true);
                        return true;
                    }
                }else{
                    System.out.println("-- Verify: No email found at index : " + index + " --");
                }
            }else{
                System.out.println("-- Verify: No email found in this Folder" );
            }

        }finally{
            if (folder != null && folder.isOpen()) { folder.close(true); }
            if (store != null) { store.close(); }
        }
        return istrue;
    }

    public String getMailBody(String folderName, String subject) throws MessagingException {
        IMAPFolder folder = null;
        Store store = null;
        try{
            store = setUpConnection();;
            folder = (IMAPFolder) store.getDefaultFolder().getFolder(folderName);
            if(!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();
            Arrays.sort(messages, (m1, m2) -> {
                try {
                    return m2.getSentDate().compareTo(m1.getSentDate());
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            });

            for(int i = 0; i < messages.length; i++){
                if(messages[i].getSubject().toLowerCase().contains(subject.toLowerCase())){
                    messages[i].setFlag(Flags.Flag.SEEN,true);//false
                    return getTextFromMessage(messages[i]);
                }
            }
        }finally{
            if (folder != null && folder.isOpen()) { folder.close(true); }
            if (store != null) { store.close(); }
        }
        return null;
    }

    public  void deleteMailBySubject(String folderName, String subject, String username, String password) throws MessagingException {
        IMAPFolder folder = null;
        Store store = null;
        try{
            store = setUpConnection();
            folder = (IMAPFolder) store.getDefaultFolder().getFolder(folderName);
            if(!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();
            for(int i=0; i<messages.length; i++){
                if(messages[i].getSubject().toLowerCase().contains(subject.toLowerCase())) {
                    messages[i].setFlag(Flags.Flag.DELETED, true);
                    System.out.println("-- Step: Marked DELETE for message: " + messages[i].getSubject() + " --");
                }
            }
        }finally{
            if (folder != null && folder.isOpen()) { folder.close(true); }
            if (store != null) { store.close(); }
        }
    }

    public  void waitNewMailBySubject(String folderName, String subject, int timeout) throws MessagingException, InterruptedException {
        IMAPFolder folder = null;
        Store store = null;
        try{
            store = setUpConnection();
            folder = (IMAPFolder) store.getDefaultFolder().getFolder(folderName);
            if(!folder.isOpen())
                folder.open(Folder.READ_WRITE);
            Message[] messages = folder.search(
                    new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            for (int j = 0; j < timeout; j++) {
                if (messages.length > 0 && messages[j].getSubject().toLowerCase().contains(subject.toLowerCase()))
                    break;
                System.out.println("Waiting for new mail : " + j);
                Thread.sleep(1000);
            }
        }finally{
            if (folder != null && folder.isOpen()) { folder.close(true); }
            if (store != null) { store.close(); }
        }
    }

    private Properties setProperties(){
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", protocal);
        if (protocal.equalsIgnoreCase("imap")) {
            props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.imap.socketFactory.fallback", "false");
            props.setProperty("mail.imap.ssl.enable", "true");
            props.setProperty("mail.imap.socketFactory.port", "993");
        } else if (protocal.equalsIgnoreCase("pop3")) {
            props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.pop3.socketFactory.fallback", "false");
            props.setProperty("mail.pop3.ssl.enable", "true");
            props.setProperty("mail.pop3.socketFactory.port", "995");
        } else if (protocal.equalsIgnoreCase("imaps")) {
            props.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.imaps.socketFactory.fallback", "false");
            props.setProperty("mail.imaps.port", "993");
            props.setProperty("mail.imaps.socketFactory.port", "993");
            props.setProperty("mail.imaps.ssl.enable","true");
            props.setProperty("mail.imaps.connectiontimeout", "3000000");
            props.setProperty("mail.imaps.timeout", "90000000");
        }

        return  props;
    }

    private String getTextFromMessage(Message message) {
        String result = "";
        try {
            if (message.isMimeType("text/plain")) {
                result = message.getContent().toString();
            } else if (message.isMimeType("multipart/*")) {
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                result = getTextFromMimeMultipart(mimeMultipart);
            }
            return result;
        }catch (Exception ex){

        }
        return  null;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart)  {
        String result = "";
        try {
            int count = mimeMultipart.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    result = result + "\n" + bodyPart.getContent();
                    break; // without break same text appears twice in my tests
                } else if (bodyPart.isMimeType("text/html")) {
                    String html = (String) bodyPart.getContent();
                    result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
                } else if (bodyPart.getContent() instanceof MimeMultipart){
                    result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
                }
            }
            return result;
        }catch (Exception ex){

        }
        return  null;
    }


    public static void main(String[] args) throws MessagingException, InterruptedException {
        Email email = new Email("imaps","imap-mail.outlook.com","email","password");
        System.out.println(email.getMailBody("TescoMobilePayMonthly", "Password reset - Pay Monthly Price Plans - Tesco Mobile"));
    }





}