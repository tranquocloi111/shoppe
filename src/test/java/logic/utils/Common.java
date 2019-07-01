package logic.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

    public static int steamFilterCondition(List<Integer>  list, int value) {
        return Integer.parseInt(String.valueOf(list.stream().filter(x -> x == value).count()));
    }

    public static String findValueOfStream(List<String> list, String value){
        return list.stream().filter(x -> x.contains(value)).findAny().get();
    }

    public static void main(String[] args) throws InterruptedException {

    }
}
