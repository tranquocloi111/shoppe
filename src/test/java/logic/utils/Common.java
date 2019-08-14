package logic.utils;

import com.sun.org.apache.xml.internal.security.c14n.CanonicalizationException;
import com.sun.org.apache.xml.internal.security.c14n.Canonicalizer;
import com.sun.org.apache.xml.internal.security.c14n.InvalidCanonicalizerException;
import framework.config.Config;
import framework.utils.Log;
import logic.business.db.OracleDB;
import logic.business.helper.FTPHelper;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.testng.Assert.assertEquals;

public class Common {
    public static String splitSignatureCode(String imgUrl) {
        return imgUrl.split("uniqueCode=")[1];
    }

    public static String stripNonDigits(final CharSequence input) {
        final StringBuilder sb = new StringBuilder(
                input.length() /* also inspired by seh's comment */);
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c > 47 && c < 58) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void createUserDir(final String dirName) {
        final File homeDir = new File(System.getProperty("user.home") + "//Desktop");
        final File dir = new File(homeDir, dirName);
        if (!dir.exists() && !dir.mkdirs()) {
            try {
                throw new IOException("Unable to create " + dir.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean steamAnyMatchEndsWith(List<String> list, String value) {
        return list.stream().anyMatch(x -> x.endsWith(value));
    }

    public static int steamFilterCondition(List<Integer> list, int value) {
        return Integer.parseInt(String.valueOf(list.stream().filter(x -> x == value).count()));
    }

    public static String findValueOfStream(List<String> list, String value) {
        return list.stream().filter(x -> x.contains(value)).findAny().get();
    }

    public static LinkedList<DiffMatchPatch.Diff> compareFile(String file1, String file2) {
        LinkedList<DiffMatchPatch.Diff> d = new LinkedList<DiffMatchPatch.Diff>();
        try {
            DiffMatchPatch dmp = new DiffMatchPatch();
            dmp.Diff_Timeout = 0;

            long start_time = System.nanoTime();
            d = dmp.diff_main(readFile(file1), readFile(file2), false);
            long end_time = System.nanoTime();
            System.out.printf("Elapsed time: %f\n", ((end_time - start_time) / 1000000000.0));

            dmp.diff_cleanupSemantic(d);
            dmp.diff_prettyHtml(d);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return d;
    }
    public static   List<String>  compareFiles(String fileExpected, String fileActual, String removeString) {
        BufferedReader br1 = null;
        BufferedReader br2 = null;
        String sCurrentLine;
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<String>();
        try {
            br1 = new BufferedReader(new FileReader(fileExpected));
            br2 = new BufferedReader(new FileReader(fileActual));
            while ((sCurrentLine = br1.readLine()) != null) {
               if(!sCurrentLine.contains(removeString)){
                list1.add(sCurrentLine);}
            }
            while ((sCurrentLine = br2.readLine()) != null) {
                if(!sCurrentLine.contains(removeString)){
                list2.add(sCurrentLine);}
            }
        }catch (Exception ex)
        {}
        List<String> tmpList = new ArrayList<String>(list1);
        tmpList.removeAll(list2);
        return tmpList;
    }

    public static String readFile(String filename) {
        try {
            // Read a file from disk and return the text contents.
            StringBuilder sb = new StringBuilder();
            FileReader input = new FileReader(filename);
            BufferedReader bufRead = new BufferedReader(input);
            try {
                String line = bufRead.readLine();
                while (line != null) {
                    sb.append(line).append('\n');
                    line = bufRead.readLine();
                }
            } finally {
                bufRead.close();
                input.close();
            }
            return sb.toString();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return null;
    }

    public static void writeFile(String value, String filename) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filename));
            writer.write(value);

        } catch (IOException e) {
            Log.error(e.getMessage());
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                Log.error(e.getMessage());
            }
        }
    }


    public static String saveXmlFile(String fileName, String xmlValue) {
        String path = System.getProperty("user.home") + "\\Desktop\\QA_Project\\";
        if (!new File(path).exists())
            Common.createUserDir(path);
        try {
            File newTextFile = new File(path + fileName);
            FileWriter fw = new FileWriter(newTextFile);
            fw.write(xmlValue);
            fw.close();

        } catch (Exception iox) {
            Log.error(iox.getMessage());
        }

        return (path + fileName);
    }

    //Function to get random number
    private static Random getrandom = new Random();

    public static int getRandomNumber(int min, int max) {
        return getrandom.nextInt(max - min) + min;
    }

    public static String getFolderLogFilePath() {
        String path = System.getProperty("user.home") + "\\Desktop\\QA_Project\\";
        if (!new File(path).exists())
            Common.createUserDir(path);

        return path + "\\";
    }

    public static void deleteFile(String fileName) {
        File newTextFile = new File(fileName);
        try {
            if (newTextFile.exists()) {
                newTextFile.delete();
            }

        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
    }

    public static void waitForFileExist(int timeOut, String fileName) {
        try {
            Thread.sleep(5000);
            File file = new File(fileName);
            for (int i = 0; i <= timeOut; i++) {
                if (file.exists()) {
                    break;
                } else {
                    System.out.println("Waiting for file : " + i);
                    Thread.sleep(1000);
                }
            }
        } catch (Exception ex) {
        }
    }
    public static void waitForFileDelete(int timeOut, String fileName) {
        try {
             File file = new File(fileName);
            for (int i = 0; i <= timeOut; i++) {
                if (!file.exists()) {
                    break;
                } else {
                    System.out.println("Waiting for delete file : " + i);
                    Thread.sleep(1000);
                }
            }
        } catch (Exception ex) {
        }

    }



    public static void main(String[] args) throws InterruptedException, IOException {

    }
}
