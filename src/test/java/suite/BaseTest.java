package suite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.KlovReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;
import framework.config.Config;
import framework.report.elasticsearch.ExecutionListener;
import framework.utils.RandomCharacter;
import framework.wdm.WDFactory;
import framework.wdm.WdManager;
import logic.business.db.OracleDB;
import logic.business.db.billing.BillingActions;
import logic.business.entities.DiscountBundleEntity;
import logic.business.helper.FTPHelper;
import logic.business.helper.RemoteJobHelper;
import logic.pages.care.MenuPage;
import logic.pages.care.find.CommonContentPage;
import logic.pages.care.find.InvoicesContentPage;
import logic.utils.TimeStamp;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Listeners(ExecutionListener.class)
public class BaseTest {

    //region Hooks
    @BeforeSuite
    public void beforeSuite() {
        Config.loadEnvInfoToQueue();
        setUpReport(false);
    }

    @AfterSuite
    public void afterSuite() {
        extent.flush();
    }

    @BeforeMethod
    public void beforeMethod(Method m) throws MalformedURLException {
        test.set(extent.createTest(m.getName()));
       //WdManager.set(WDFactory.remote(new URL("http://localhost:4444/wd/hub"), DesiredCapabilities.chrome()));
        WDFactory.getConfig().setDriverVersion("77");
        WdManager.set(WDFactory.initBrowser(Config.getProp("browser")));
        WdManager.get().get(Config.getProp("careUrl"));
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        if(result.getStatus() == ITestResult.FAILURE) {
            test.get().fail(MarkupHelper.createLabel("Test Case : " + result.getName().split("_")[0] + " FAILED ", ExtentColor.RED));
            test.get().fail(result.getThrowable());
        }
        else if(result.getStatus() == ITestResult.SUCCESS) {
            test.get().pass(MarkupHelper.createLabel("Test Case : " + result.getName().split("_")[0]+ " PASSED ", ExtentColor.GREEN));
        }
        else {
            test.get().skip(MarkupHelper.createLabel(result.getName()+" SKIPPED ", ExtentColor.ORANGE));
            test.get().skip(result.getThrowable());
        }

        Config.returnProp();
        WdManager.dismissWD();
    }

    //endregion

    //region Report
    protected static ExtentReports extent;
    protected static ThreadLocal<ExtentTest> test = new ThreadLocal();

    private void setUpReport(boolean useKlov) {
        //HTML
        ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter("test-output\\TM_Automation_Report.html");
        htmlReporter.config().setTestViewChartLocation(ChartLocation.BOTTOM);
        htmlReporter.config().setChartVisibilityOnOpen(true);
        htmlReporter.config().setTheme(Theme.STANDARD);
        htmlReporter.config().setDocumentTitle("TESCO MOBILE - HTML Test Report");
        htmlReporter.config().setEncoding("utf-8");
        htmlReporter.config().setReportName("TESCO MOBILE - HTML Test Report");

        extent = new ExtentReports();
        if (useKlov) {
            //Klov
            KlovReporter klovReporter = new KlovReporter();
            klovReporter.initMongoDbConnection("localhost", 27017);
            klovReporter.setProjectName("TescoMobile");
            klovReporter.setReportName("Test Report Name");
            klovReporter.setKlovUrl("http://localhost");
            extent.attachReporter(htmlReporter, klovReporter);
        } else {
            extent.attachReporter(htmlReporter);
        }

    }
    //endregion

    //Common Function
    protected static void createNewBillingGroup() {
        BillingActions.getInstance().createNewBillingGroup(0, true, -1);
    }
    protected static void createNewBillingGroupToMinus15days() {
        BillingActions.getInstance().createNewBillingGroup(-15, true, -1);
    }
    protected static void createNewBillingGroupToMinus20days() {
        BillingActions.getInstance().createNewBillingGroup(-20, true, -1);
    }

    public static void updateBillGroupPaymentCollectionDateTo10DaysLater() {
        Date paymentCollectionDate = Date.valueOf(LocalDate.now().plusDays(10));
        BillingActions.getInstance().updateBillGroupPaymentCollectionDate(paymentCollectionDate, BillingActions.getInstance().tempBillingGroupHeader.getKey());
    }

    protected static void setBillGroupForCustomer(String customerId) {
        BillingActions.getInstance().setBillGroupForCustomer(customerId, BillingActions.tempBillingGroupHeader.getKey());
    }

    protected static void updateThePDateAndBillDateForSO(String serviceOrderId){
        BillingActions.getInstance().updateThePdateForSo(serviceOrderId);
        BillingActions.getInstance().updateTheBillDateForSo(serviceOrderId);
    }

    protected static void verifyFCDiscountBundles(List<DiscountBundleEntity> allDiscountBundles, Date startDate, String partitionIdRef){
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "FC", startDate, TimeStamp.TodayMinus1Day(), partitionIdRef, "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "FC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), partitionIdRef, "ACTIVE"));
    }

    protected static void verifyNCDiscountBundles(List<DiscountBundleEntity> allDiscountBundles, Date startDate, String partitionIdRef){
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", startDate, TimeStamp.TodayPlus1MonthMinus1Day(), partitionIdRef, "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), partitionIdRef, "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(allDiscountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), partitionIdRef, "ACTIVE"));
    }

    protected static void updateThePDateAndBillDateForChangeBundleForSo(String serviceOrderId){
        BillingActions.getInstance().updateThePDateAndBillDateForChangeBundle(serviceOrderId);
    }

    protected static void verifyNewNCDiscountBundles(List<DiscountBundleEntity> allDiscountBundles, String partitionIdRef, String bundleCode){
        Assert.assertEquals(1, BillingActions.getInstance().findNewDiscountBundlesByCondition(allDiscountBundles, "NC", TimeStamp.Today(), TimeStamp.TodayPlus1MonthMinus1Day(), partitionIdRef, bundleCode, "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findNewDiscountBundlesByCondition(allDiscountBundles, "NC", TimeStamp.TodayPlus1Month(), TimeStamp.TodayPlus2MonthMinus1Day(), partitionIdRef, bundleCode, "ACTIVE"));
    }

    public static Date paymentCollectionDateEscapeNonWorkDay(int numberOfDate){
        return BillingActions.getInstance().getInvoiceDueDateByPaymentCollectionDate(numberOfDate);
    }

    public static void downloadInvoicePDFFile(String customerNumber){
        InvoicesContentPage.InvoiceDetailsContentPage.getInstance().saveFileFromWebRequest(customerNumber);
    }
    public static String getDownloadInvoicePDFFile(String customerNumber){
        return InvoicesContentPage.InvoiceDetailsContentPage.getInstance().getSaveFileFromWebRequest(customerNumber);
    }

    public String randomNumberAndString()
    {
        return RandomCharacter.getRandomAlphaNumericString(7);
    }

    protected static void submitDoRefillBCJob(){
        RemoteJobHelper.getInstance().submitDoRefillBcJob(TimeStamp.Today());
    }

    protected static void submitDoRefillNCJob(){
        RemoteJobHelper.getInstance().submitDoRefillNcJob(TimeStamp.Today());
    }

    protected static void submitDoBundleRenewJob(){
        RemoteJobHelper.getInstance().submitDoBundleRenewJob(TimeStamp.Today());
    }

    protected static void submitDraftBillRun(){
        RemoteJobHelper.getInstance().submitDraftBillRun();
    }

    protected static void submitConfirmBillRun(){
        RemoteJobHelper.getInstance().submitConfirmBillRun();
    }

    protected static String getSubscriptionNumberBySubscriptionNumber(String Subscription)
    {
        MenuPage.LeftMenuPage.getInstance().clickSubscriptionsLink();
        return CommonContentPage.SubscriptionsGridSectionPage.getInstance().getSubscriptionNumberValue(Subscription);
    }

    protected static void downLoadFile(String remotePath, String fileName, String localPath){
        FTPHelper.getInstance().downLoadFromDisk(remotePath, fileName, localPath);
    }
    public void updateCustomerDDIDetailsInDatabase(String DDIReference, int hrmid, String newStarDate)
    {
        String sql = String.format("update hmbrproperty set propvalchar='A' where propertykey='DDISTAT' and hmbrid=%d", hrmid);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);

        sql = String.format("update hmbrproperty set propvalchar='%s',datestart=to_date('%s','yyyy-mm-dd') where propertykey='DDIREF' and hmbrid=%d", DDIReference,newStarDate, hrmid);

        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }
    public void backDateThePaymentMethodStartDateToTodayMinus1Day(String customerNumber) {
        String disableTrigger = "ALTER TABLE hmbrproperty DISABLE ALL TRIGGERS";
        String enableTrigger = "ALTER TABLE hmbrproperty ENABLE ALL TRIGGERS";
        String sql = String.format("update hmbrproperty set datestart=sysdate-1 where hmbrid IN (SELECT hmbrid FROM hierarchymbr WHERE buid IN (SELECT buid FROM businessunit WHERE buid =%s OR rootbuid=%s)) AND datestart IS NOT NULL AND propertykey IN ('PAYMT', 'CLUBNUM')", customerNumber, customerNumber);
        OracleDB.SetToNonOEDatabase().executeNonQueryWithoutTrigger(disableTrigger, enableTrigger, sql);
    }

    protected void verifyFCDiscountBundlesFoBillingGroupMinus15days(List<DiscountBundleEntity> discountBundles, Date startDate, String partitionIdRef){
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "FC", startDate, TimeStamp.TodayMinus16DaysAdd1Month(), partitionIdRef, "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "FC", TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.TodayMinus16DaysAdd2Months(), partitionIdRef, "ACTIVE"));
    }
    protected void verifyNCDiscountBundlesFoBillingGroupMinus15days(List<DiscountBundleEntity> discountBundles, Date startDate, String partitionIdRef){
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", startDate, TimeStamp.TodayMinus16DaysAdd2Months(), partitionIdRef, "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayMinus15DaysAdd1Month(), TimeStamp.TodayMinus16DaysAdd2Months(), partitionIdRef, "ACTIVE"));
        Assert.assertEquals(1, BillingActions.getInstance().findDiscountBundlesByConditionByPartitionIdRef(discountBundles, "NC", TimeStamp.TodayMinus15DaysAdd2Months(), TimeStamp.TodayMinus16DaysAdd3Months(), partitionIdRef, "ACTIVE"));
    }

    protected static void waitLoadCDRJobComplete(){
        RemoteJobHelper.getInstance().waitLoadCDRJobComplete();
    }
    //end region

}
