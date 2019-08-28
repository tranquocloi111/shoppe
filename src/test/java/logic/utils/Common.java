package logic.utils;

import framework.utils.Log;
import framework.utils.Xml;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    public static String unzip(String zipFilePath, String destDir) {
        String zippedFile = "";
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                zippedFile = newFile.getAbsolutePath();
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  zippedFile;
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        String zipFilePath = "C:\\Users\\vuq\\Documents\\TM_HUB_DEAL_Onlines_20190816.XML.zip";

        String destDir = "/Users/pankaj/output";

        unzip(zipFilePath, getFolderLogFilePath());

        String file = getFolderLogFilePath() + "\\TM_HUB_DEAL_Onlines_20190816_232112.XML";
        Xml xml = new Xml(new File(file));

        System.out.println(xml.toString().contains("productCode=\"NC24-4500-3000-IP-S\""));

    }
}
