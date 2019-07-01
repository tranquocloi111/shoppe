package framework.utils;

import com.testautomationguru.utility.CompareMode;
import logic.utils.Parser;
import logic.utils.TimeStamp;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * PDF Util
 * Read content of pdf file and get line number with the keyword
 * Date : 04/12/2018
 * @author Quyen Vu
 */

public class Pdf {


    private static Pdf instance = new Pdf();
    public static Pdf getInstance(){
        if (instance == null)
            return new Pdf();
        return instance;
    }

    private String imageDestinationPath;
    private boolean bTrimWhiteSpace = true;
    private boolean bHighlightPdfDifference = false;
    private Color imgColor;
    private boolean bCompareAllPages;
    private CompareMode compareMode;
    private String[] excludePattern;
    private int startPage = 1;
    private int endPage = -1;

    public Pdf() {
        this.imgColor = Color.MAGENTA;
        this.bCompareAllPages = false;
        this.compareMode = CompareMode.TEXT_MODE;
    }


    public void setCompareMode(CompareMode mode) {
        this.compareMode = mode;
    }

    public CompareMode getCompareMode() {
        return this.compareMode;
    }

    public void trimWhiteSpace(boolean flag) {
        this.bTrimWhiteSpace = flag;
    }

    public String getImageDestinationPath() {
        return this.imageDestinationPath;
    }

    public void setImageDestinationPath(String path) {
        this.imageDestinationPath = path;
    }

    public void highlightPdfDifference(boolean flag) {
        this.bHighlightPdfDifference = flag;
    }

    public void highlightPdfDifference(Color colorCode) {
        this.bHighlightPdfDifference = true;
        this.imgColor = colorCode;
    }

    public int getPageCount(String file) throws IOException {
        PDDocument doc = PDDocument.load(new File(file));
        int pageCount = doc.getNumberOfPages();
        doc.close();
        return pageCount;
    }

    public List<String>  getText(String file) {
        try {
            return this.getPDFText(file, -1, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String>  getText(String file, int startPage)  {
        try {
            return this.getPDFText(file, startPage, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String>  getText(String file, int startPage, int endPage)  {
        try {
            return this.getPDFText(file, startPage, endPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<String> getPDFText(String file, int startPage, int endPage) throws IOException {
        PDFTextStripper stripper;
        PDDocument doc = null;
        try {
            doc = PDDocument.load(new File(file));
            stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            this.updateStartAndEndPages(file, startPage, endPage);
            stripper.setStartPage(this.startPage);
            stripper.setEndPage(this.endPage);

            String x1 = stripper.getText(doc);
            List<String> ans = Arrays.asList(x1.split("\r\n"));
            return ans;
        } finally {
            if (doc != null)
                doc.close();
        }
    }

    public int getLineNumberWithTextMode(String text, String file){
        return this.getLineNumber(text, file, startPage, endPage);
    }

    public int getLineNumberWithTextMode(String text, String file, int startPage){
        return this.getLineNumber(text, file, startPage, endPage);
    }

    public int getLineNumberWithTextMode(String text, String file, int startPage, int endPage){
        return this.getLineNumber(text, file, startPage, endPage);
    }

    public boolean compare(String file1, String file2) throws IOException {
        return this.comparePdfFiles(file1, file2, -1, -1);
    }

    public boolean compare(String file1, String file2, int startPage, int endPage) throws IOException {
        return this.comparePdfFiles(file1, file2, startPage, endPage);
    }

    public boolean compare(String file1, String file2, int startPage) throws IOException {
        return this.comparePdfFiles(file1, file2, startPage, -1);
    }

    private boolean comparePdfFiles(String file1, String file2, int startPage, int endPage) throws IOException {
        return CompareMode.TEXT_MODE == this.compareMode ? this.comparepdfFilesWithTextMode(file1, file2, startPage, endPage) : this.comparePdfByImage(file1, file2, startPage, endPage);
    }

    private boolean comparePdfByImage(String file1, String file2, int startPage, int endPage) throws IOException {
        System.out.println("file1 : " + file1);
        System.out.println("file2 : " + file2);
        int pgCount1 = this.getPageCount(file1);
        int pgCount2 = this.getPageCount(file2);
        if (pgCount1 != pgCount2) {
            System.out.println("files page counts do not match - returning false");
            return false;
        } else {
            if (this.bHighlightPdfDifference) {
                this.createImageDestinationDirectory(file2);
            }

            this.updateStartAndEndPages(file1, startPage, endPage);
            return this.convertToImageAndCompare(file1, file2, this.startPage, this.endPage);
        }
    }

    private void createImageDestinationDirectory(String file) throws IOException {
        if (null == this.imageDestinationPath) {
            File sourceFile = new File(file);
            String destinationDir = sourceFile.getParent() + "/temp/";
            this.imageDestinationPath = destinationDir;
            this.createFolder(destinationDir);
        }

    }

    private boolean convertToImageAndCompare(String file1, String file2, int startPage, int endPage) throws IOException {
        boolean result = true;
        PDDocument doc1 = null;
        PDDocument doc2 = null;
        PDFRenderer pdfRenderer1 = null;
        PDFRenderer pdfRenderer2 = null;

        try {
            doc1 = PDDocument.load(new File(file1));
            doc2 = PDDocument.load(new File(file2));
            pdfRenderer1 = new PDFRenderer(doc1);
            pdfRenderer2 = new PDFRenderer(doc2);

            for(int iPage = startPage - 1; iPage < endPage; ++iPage) {
                String fileName = (new File(file1)).getName().replace(".pdf", "_") + (iPage + 1);
                fileName = this.getImageDestinationPath() + "/" + fileName + "_diff.png";
                System.out.println("Comparing Page No : " + (iPage + 1));
                BufferedImage image1 = pdfRenderer1.renderImageWithDPI(iPage, 300.0F, ImageType.RGB);
                BufferedImage image2 = pdfRenderer2.renderImageWithDPI(iPage, 300.0F, ImageType.RGB);
                result = compareAndHighlight(image1, image2, fileName, this.bHighlightPdfDifference, this.imgColor.getRGB()) && result;
                if (!this.bCompareAllPages && !result) {
                    break;
                }
            }
        } catch (Exception var17) {
            var17.printStackTrace();
        } finally {
            doc1.close();
            doc2.close();
        }

        return result;
    }

    private boolean comparepdfFilesWithTextMode(String file1, String file2, int startPage, int endPage) throws IOException {
        String file1Txt = this.getPDFText(file1, startPage, endPage).toString().trim();
        String file2Txt = this.getPDFText(file2, startPage, endPage).toString().trim();
        if (null != this.excludePattern && this.excludePattern.length > 0) {
            for(int i = 0; i < this.excludePattern.length; ++i) {
                file1Txt = file1Txt.replaceAll(this.excludePattern[i], "");
                file2Txt = file2Txt.replaceAll(this.excludePattern[i], "");
            }
        }

        System.out.println("File 1 Txt : " + file1Txt);
        System.out.println("File 2 Txt : " + file2Txt);
        boolean result = file1Txt.equalsIgnoreCase(file2Txt);
        if (!result) {
            System.out.println("PDF content does not match");
        }

        return result;
    }


    private  int getLineNumber(String text, String file, int startPage, int endPage) {
        int n = 0;
        PDFTextStripper stripper;
        PDDocument doc = null;
        try {
            doc = PDDocument.load(new File(file));
            stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setStartPage(startPage);
            stripper.setEndPage(endPage);

            String x1 = stripper.getText(doc);
            String [] lines = x1.split("\r\n");
            for(String line : lines) {
                if(line.contains(text)) {
                    return n;
                }
                n++;
            }
        } catch (InvalidPasswordException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (doc != null) {
                try {
                    doc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }


    private void updateStartAndEndPages(String file, int start, int end) throws IOException {
        PDDocument document = PDDocument.load(new File(file));
        int pagecount = document.getNumberOfPages();
        if (start > 0 && start <= pagecount) {
            this.startPage = start;
        } else {
            this.startPage = 1;
        }

        if (end > 0 && end >= start && end <= pagecount) {
            this.endPage = end;
        } else {
            this.endPage = pagecount;
        }

        document.close();
    }


    private boolean createFolder(String file) throws IOException {
        FileUtils.deleteDirectory(new File(file));
        return (new File(file)).mkdir();
    }

    private String getFileName(String file) {
        return (new File(file)).getName();
    }


    private boolean compareAndHighlight(BufferedImage img1, BufferedImage img2, String fileName, boolean highlight, int colorCode) throws IOException {
        int w = img1.getWidth();
        int h = img1.getHeight();
        int[] p1 = img1.getRGB(0, 0, w, h, (int[])null, 0, w);
        int[] p2 = img2.getRGB(0, 0, w, h, (int[])null, 0, w);
        if (Arrays.equals(p1, p2)) {
            return true;
        } else {
            System.out.println("Image compared - does not match");
            if (highlight) {
                for(int i = 0; i < p1.length; ++i) {
                    if (p1[i] != p2[i]) {
                        p1[i] = colorCode;
                    }
                }

                BufferedImage out = new BufferedImage(w, h, 2);
                out.setRGB(0, 0, w, h, p1, 0, w);
                saveImage(out, fileName);
            }

            return false;
        }
    }

    private void saveImage(BufferedImage image, String file) {
        try {
            File outputfile = new File(file);
            ImageIO.write(image, "png", outputfile);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public void saveToPDF( String saveTo, String value){
        byte[] decodedBytes = Base64.getDecoder().decode(value);
        File file = new File(saveTo);;
        FileOutputStream fop = null;
        try {
            fop = new FileOutputStream(file);
            fop.write(decodedBytes);
            fop.flush();
            fop.close();
        } catch (Throwable e) {

        }
    }

    public static void main(String[] args) throws IOException {
        Pdf pdf = new Pdf();

        List<String> data = pdf.getText("C:\\Users\\vuq\\Desktop\\QA_Project\\TC_14724_808930821.pdf",3);
        System.out.println(data.contains(String.format("%s %s Customer Care refund issued for %s", Parser.parseDateFormate(TimeStamp.TodayMinus2Days(), TimeStamp.DATE_FORMAT_IN_PDF),Parser.parseDateFormate(TimeStamp.TodayMinus2Days(), TimeStamp.DATE_FORMAT_IN_PDF),"07433946550")));


    }



}
