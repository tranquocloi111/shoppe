package framework.utils;

import framework.wdm.WdManager;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Set;
import java.io.IOException;
import java.io.InputStream;

public class FileDownloader {

    final static int size = 1024;
    private String localDownloadPath;
    private boolean followRedirects = true;
    private boolean mimicWebDriverCookieState = true;
    private int httpStatusOfLastDownloadAttempt = 0;

    public FileDownloader(String localDownloadPath) {
        this.localDownloadPath = localDownloadPath;
    }

    /**
     * Specify if the FileDownloader class should follow redirects when trying to download a file
     *
     * @param value
     */
    public void followRedirectsWhenDownloading(boolean value) {
        this.followRedirects = value;
    }

    /**
     * Get the current location that files will be downloaded to.
     *
     * @return The filepath that the file will be downloaded to.
     */
    public String localDownloadPath() {
        return this.localDownloadPath;
    }

//    /**
//     * Download the image specified in the src attribute of a WebElement
//     *
//     * @param element
//     * @return
//     * @throws Exception
//     */
//    public String downloadImage(WebElement element, String url, String pdfFile) throws Exception {
//        return downloader(element, "src", url, pdfFile);
//    }

    /**
     * Set the path that files will be downloaded to.
     *
     * @param filePath The filepath that the file will be downloaded to.
     */
    public void localDownloadPath(String filePath) {
        this.localDownloadPath = filePath;
    }

    /**
     * Download the file specified in the href attribute of a WebElement
     *
     * @param element
     * @return
     * @throws Exception
     */
    public String downloadFile(WebElement element, String url, String pdfFile) throws Exception {
        return downloader(element, url, pdfFile);
    }

    /**
     * Download the file specified in the href attribute of a WebElement
     *
     * @param
     * @return
     * @throws Exception
     */
    public InputStream downloadFile(String url) throws Exception {
        return downloader(url);
    }

    /**
     * Gets the HTTP status code of the last download file attempt
     *
     * @return
     */
    public int getHTTPStatusOfLastDownloadAttempt() {
        return this.httpStatusOfLastDownloadAttempt;
    }

    /**
     * Mimic the cookie state of WebDriver (Defaults to true)
     * This will enable you to access files that are only available when logged in.
     * If set to false the connection will be made as an anonymouse user
     *
     * @param value
     */
    public void mimicWebDriverCookieState(boolean value) {
        this.mimicWebDriverCookieState = value;
    }

    /**
     * Load in all the cookies WebDriver currently knows about so that we can mimic the browser cookie state
     *
     * @param seleniumCookieSet
     * @return
     */
    private BasicCookieStore mimicCookieState(Set<Cookie> seleniumCookieSet) {
        BasicCookieStore mimicWebDriverCookieStore = new BasicCookieStore();

        for (Cookie seleniumCookie : seleniumCookieSet) {
            BasicClientCookie duplicateCookie = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
            duplicateCookie.setDomain(seleniumCookie.getDomain());
            duplicateCookie.setSecure(seleniumCookie.isSecure());
            duplicateCookie.setExpiryDate(seleniumCookie.getExpiry());
            duplicateCookie.setPath(seleniumCookie.getPath());
            mimicWebDriverCookieStore.addCookie(duplicateCookie);
        }

        return mimicWebDriverCookieStore;
    }

    /**
     * Perform the file/image download.
     *
     * @param element
     * @return
     * @throws IOException
     * @throws NullPointerException
     */
    private String downloader(WebElement element, String fileToDownloadLocation, String pdfFile) throws IOException, NullPointerException, URISyntaxException {
        URL fileToDownload = new URL(fileToDownloadLocation);
        File downloadedFile = new File(this.localDownloadPath + pdfFile);
        if (downloadedFile.canWrite() == false) downloadedFile.setWritable(true);

        HttpClient client = HttpClientBuilder.create().build();
        BasicHttpContext localContext = new BasicHttpContext();

        Log.info("Mimic WebDriver cookie state: " + this.mimicWebDriverCookieState);
        if (this.mimicWebDriverCookieState) {
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, mimicCookieState(WdManager.get().manage().getCookies()));
        }

        HttpGet httpget = new HttpGet(fileToDownload.toURI());

        Log.info("Sending GET request for: " + httpget.getURI());
        HttpResponse response = client.execute(httpget, localContext);
        this.httpStatusOfLastDownloadAttempt = response.getStatusLine().getStatusCode();
        Log.info("HTTP GET request status: " + this.httpStatusOfLastDownloadAttempt);
        Log.info("Downloading file: " + downloadedFile.getName());
        FileUtils.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
        response.getEntity().getContent().close();

        String downloadedFileAbsolutePath = downloadedFile.getAbsolutePath();
        Log.info("File downloaded to '" + downloadedFileAbsolutePath + "'");
        return downloadedFileAbsolutePath;
    }

    private InputStream downloader(String url) {
        String script = "var url = arguments[0];" +
                "var callback = arguments[arguments.length - 1];" +
                "var xhr = new XMLHttpRequest();" +
                "xhr.open('GET', url, true);" +
                "xhr.responseType = \"arraybuffer\";" + //force the HTTP response, response-type header to be array buffer
                "xhr.onload = function() {" +
                "  var arrayBuffer = xhr.response;" +
                "  var byteArray = new Uint8Array(arrayBuffer);" +
                "  callback(byteArray);" +
                "};" +
                "xhr.send();";
        Object response = ((JavascriptExecutor) WdManager.get()).executeAsyncScript(script, url);
        // Selenium returns an Array of Long, we need byte[]
        ArrayList<Long> byteList = (ArrayList<Long>) response;
        byte[] bytes = new byte[byteList.size()];
        for(int i = 0; i < byteList.size(); i++) {
            bytes[i] = (byte)(long)byteList.get(i);
        }
        return new ByteArrayInputStream(bytes);
    }

}