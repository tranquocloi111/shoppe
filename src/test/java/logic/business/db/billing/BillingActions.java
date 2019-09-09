package logic.business.db.billing;

import framework.utils.Log;
import framework.utils.RandomCharacter;
import javafx.util.Pair;
import logic.business.db.OracleDB;
import logic.business.entities.DiscountBundleEntity;
import logic.business.entities.PaymentGatewayEnity;
import logic.business.entities.PaymentGatewayRespondEnity;
import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BillingActions extends OracleDB {

    public static Pair<Integer, String> tempBillingGroupHeader;
    private static BillingActions instance;

    public static BillingActions getInstance() {
        if (instance == null)
            instance = new BillingActions();
        return instance;
    }

    public static void updateBillGroupPaymentCollectionDate(Date collectionDate, int billingGroupId) {
        LocalDate localDate = collectionDate.toLocalDate();
        if (localDate.getDayOfMonth() > 28) {
            localDate = localDate.plusDays(28 - localDate.getDayOfMonth());
        }
        String sql = String.format("update BGPROPERTY set propvalnumber= %d where propertykey='BGPCDAY' and billinggroupid= %d ", localDate.getDayOfMonth(), billingGroupId);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static void setBillGroupForCustomer(String customerId, int billGroupId) {
        int hmbrid = getHmbrid(customerId);
        String sql = String.format("update hmbrproperty set propvalnumber=%d where hmbrid = %d and propertykey='BGRP'", billGroupId, hmbrid);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static int getHmbrid(String customerId) {
        String sql = String.format("select hmbrid from hierarchymbr hm,hierarchy h where h.rootbuid = %s and h.hid = hm.hid and hm.hmbrtype = 'BP'", customerId);
        ResultSet resultSet = OracleDB.SetToNonOEDatabase().executeQuery(sql);
        int hmbrid = Integer.parseInt(String.valueOf(OracleDB.getValueOfResultSet(resultSet, 1)));
        return hmbrid;
    }

    public static void updateThePdateForSo(String serviceOrderId) {
        String sql = String.format("update hitransactionproperty set propvaldate=trunc(sysdate) where hitransactionid=%s and propertykey='PDATE'", serviceOrderId);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static void updateTheBillDateForSo(String serviceOrderId) {
        String sql = String.format("update hitransactionproperty set propvaldate=trunc(sysdate-1) where hitransactionid=%s and propertykey='BILLDATE'", serviceOrderId);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static Date getInvoiceDueDateByPaymentCollectionDate(int numberOfDate) {
        LocalDate today;
        LocalDate date = LocalDate.now().plusDays(numberOfDate);
        if (date.getDayOfMonth() > 28) {
            for (int i = 0; i < numberOfDate; i++) {
                today = LocalDate.now().plusDays(i);
                if ((today.getDayOfWeek() == DayOfWeek.SATURDAY) || (today.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                    date = date.minusDays(1);
                }
            }
        } else {
            while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                date = date.plusDays(1);
            }
        }
        return Date.valueOf(date);
    }

    public static Date getInvoiceDueDateByPaymentCollectionDate(Date paymentCollectionDate) {
        LocalDate today = LocalDate.now();
        LocalDate date = Parser.convertToLocalDateViaMilisecond(paymentCollectionDate);
        long days = ChronoUnit.DAYS.between(date, today);
        if (date.getDayOfMonth() > 28) {
            for (int i = 0; i < Math.abs(days); i++) {
                today = LocalDate.now().plusDays(i);
                if ((today.getDayOfWeek() == DayOfWeek.SATURDAY) || (today.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                    date = date.minusDays(1);
                }
            }
        } else {
            while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                date = date.plusDays(1);
            }
        }
        return Date.valueOf(date);
    }

    private void changeExistingBillRunCalendar(int daysAgo, int asAtDateOffset) {
        try {
            OracleDB.SetToNonOEDatabase().executeNonQuery(String.format("update billruncalendar set asatdate=trunc(SYSDATE - %d) where asatdate=trunc(SYSDATE - %d)", daysAgo + 2, daysAgo + Math.abs(asAtDateOffset)));
            OracleDB.SetToNonOEDatabase().executeNonQuery(String.format("update billruncalendar set rundate=trunc(SYSDATE - %d) where rundate=trunc(SYSDATE - %d)", daysAgo + 2, daysAgo));
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    private Pair<Integer, String> createNewBillGroupHeader() throws SQLException {
        String sql = "select periodid from PERIOD where descr='Monthly'";
        BigDecimal periodId = (BigDecimal) OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), 1);
        sql = String.format("insert into billinggroup (billinggroupid,descr,bgstatus,periodid,bgtype,psid) values (BILLINGGROUPID.nextval,?,'A',%d,'BILL',200)", Integer.parseInt(String.valueOf(periodId)));

        //Execute Insert Query
        String billingGroupName = "TC - " + RandomCharacter.getRandomNumericString(9);
        HashMap<Integer, Object> formParams = new HashMap<Integer, Object>();
        formParams.put(1, billingGroupName);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql, formParams);

        //Get BillingId
        sql = "Select billinggroupid,descr from billinggroup where descr = '" + billingGroupName + "' ";
        int billingGroupId = Integer.parseInt(String.valueOf(OracleDB.getValueOfResultSet(SetToNonOEDatabase().executeQuery(sql), 1)));

        //Update bill group to PUBLIC
        sql = "update bgproperty set propvalchar = 'PUBLIC' where  propertykey = 'BGACCESS' and billinggroupid = " + billingGroupId;
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);

        sql = String.format("update bgproperty set propvalchar = '%s' where  propertykey = 'EXTERNDESCR' and billinggroupid = %d", billingGroupName, billingGroupId);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);

        return new Pair<Integer, String>(billingGroupId, billingGroupName);
    }

    private void createBillrunCalendar(int billGroupId, Date runDate, Date asAtDat) {
        String sql = String.format("insert into BILLRUNCALENDAR (BILLRUNCALENDARID,BILLINGGROUPID,RUNDATE,ASATDATE,BRCALTYPE) values (BILLRUNCALENDARID.nextval,%d,?,?,'R')", billGroupId);
        HashMap<Integer, Object> formParams = new HashMap<Integer, Object>();
        formParams.put(1, runDate);
        formParams.put(2, asAtDat);
        OracleDB.SetToNonOEDatabase().executeNonQueryForDate(sql, formParams);
    }

    public Pair<Integer, String> createNewBillingGroup(int daysAgo, Boolean changeExistingBillingGroups, int asAtDateOffset) {
        try {
            if (changeExistingBillingGroups)
                changeExistingBillRunCalendar(daysAgo, asAtDateOffset);

            Pair<Integer, String> billGroupHeader = createNewBillGroupHeader();
            tempBillingGroupHeader = billGroupHeader;
            LocalDate futureDate = LocalDate.now().plusDays(daysAgo);
            for (int i = 0; i < 4; i++) {
                createBillrunCalendar(billGroupHeader.getKey(), Date.valueOf(futureDate.plusMonths(i)), Date.valueOf(futureDate.plusMonths(i).plusDays(asAtDateOffset)));
            }

        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return tempBillingGroupHeader;
    }

    public List<DiscountBundleEntity> getDiscountBundlesByDiscountGroupCode(String code) {
        List<DiscountBundleEntity> listBundles = new ArrayList<>();
        DiscountBundleEntity discountBundle;
        try {
            String sql = String.format("select captype,startdate,enddate,partitionidref,bundlecode,status,deletehitransactionid,deleteflg,deletedate,hitransactionid,bucket1,bucket2,bucket3,bucket4,deletehitransactioneventid,inventoryid,discountbundleid,offeridref,bucket1hiteventid,bucket2hiteventid,bucket3hiteventid,bucket4hiteventid from DISCOUNTBUNDLE where discgrpcode='%s' order by inventoryid desc", code);
            ResultSet rs = OracleDB.SetToNonOEDatabase().executeQuery(sql);
            while (rs.next()) {
                discountBundle = new DiscountBundleEntity();
                discountBundle.setCapType(rs.getString(1));
                discountBundle.setStartDate(rs.getDate(2));
                discountBundle.setEndDate(rs.getDate(3));
                discountBundle.setPartitionIdRef(rs.getString(4));
                discountBundle.setBundleCode(rs.getString(5));
                discountBundle.setStatus(rs.getString(6));
                discountBundle.setDeleteHitransactionID(rs.getInt(7));
                discountBundle.setDeleteFLG(rs.getString(8));
                discountBundle.setDeleteDate(rs.getDate(9));
                discountBundle.setHitransactionID(rs.getInt(10));
                discountBundle.setBucket1(rs.getInt(11));
                discountBundle.setBucket2(rs.getInt(12));
                discountBundle.setBucket3(rs.getInt(13));
                discountBundle.setBucket4(rs.getInt(14));
                discountBundle.setDeleteHitransactionEventID(rs.getInt(15));
                discountBundle.setInventoryId(rs.getInt(16));
                discountBundle.setDiscountBundleId(rs.getInt(17));
                discountBundle.setOfferIdRef(rs.getString(18));
                discountBundle.setBucket1HitEventId(rs.getInt(19));
                discountBundle.setBucket2HitEventId(rs.getInt(20));
                discountBundle.setBucket3HitEventId(rs.getInt(21));
                discountBundle.setBucket4HitEventId(rs.getInt(22));

                listBundles.add(discountBundle);
            }
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }

        return listBundles;
    }

    public int countStatusOfDiscountBundles(List<DiscountBundleEntity> discountBundles, String bundleStatus) {
        return Integer.parseInt(String.valueOf(discountBundles.stream().filter(status -> status.getStatus().equalsIgnoreCase(bundleStatus)).count()));
    }

    public String getInvoiceTypeByInvoiceNumber(String invoiceNumber) {
        try {
            String sql = String.format("select invoicetype from invoice where documentnbr='%s'", invoiceNumber);
            ResultSet rs = OracleDB.SetToNonOEDatabase().executeQuery(sql);
            return String.valueOf(OracleDB.getValueOfResultSet(rs, "invoicetype"));
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return null;
    }

    public int findDiscountBundlesByConditionByPartitionIdRef(List<DiscountBundleEntity> allDiscountBundles, String capType, Date startDate, Date endDate, String partitionIdRef, String status) {
        return Integer.parseInt(String.valueOf(allDiscountBundles.stream().filter(x -> x.capType.equalsIgnoreCase(capType) && x.startDate.equals(startDate) && x.endDate.equals(endDate) && x.partitionIdRef.equalsIgnoreCase(partitionIdRef) && x.status.equalsIgnoreCase(status)).count()));
    }

    public int findDiscountBundlesByConditionByBundleCode(List<DiscountBundleEntity> allDiscountBundles, String capType, Date startDate, Date endDate, String bundleCode, String status) {
        return Integer.parseInt(String.valueOf(allDiscountBundles.stream().filter(x -> x.capType.equalsIgnoreCase(capType) && x.startDate.equals(startDate) && x.endDate.equals(endDate) && x.bundleCode.equalsIgnoreCase(bundleCode) && x.status.equalsIgnoreCase(status)).count()));
    }

    public int findNewDiscountBundlesByCondition(List<DiscountBundleEntity> allDiscountBundles, String capType, Date startDate, Date endDate, String partitionIdRef, String bundleCode, String status) {
        return Integer.parseInt(String.valueOf(allDiscountBundles.stream().filter(x -> x.capType.equalsIgnoreCase(capType) && x.startDate.equals(startDate) && x.endDate.equals(endDate) && x.partitionIdRef.equalsIgnoreCase(partitionIdRef) && x.bundleCode.equalsIgnoreCase(bundleCode) && x.status.equalsIgnoreCase(status)).count()));
    }

    public int findDeletedDiscountBundlesByCondition(List<DiscountBundleEntity> allDiscountBundles, Date startDate, Date endDate, int deleteHitransactionID, Date deleteDate, String capType, String partitionIdRef, String bundleCode) {
        return Integer.parseInt(String.valueOf(allDiscountBundles.stream().filter(x -> x.capType.equalsIgnoreCase(capType) && x.startDate.equals(startDate) && x.endDate.equals(endDate) && x.partitionIdRef.equalsIgnoreCase(partitionIdRef) && x.bundleCode.equalsIgnoreCase(bundleCode) && x.status.equalsIgnoreCase("DELETED") && x.deleteHitransactionID == deleteHitransactionID && x.deleteDate.equals(deleteDate) && x.deleteFLG.equalsIgnoreCase("Y")).count()));
    }

    public void updateThePDateAndBillDateForChangeBundle(String serviceOrderId) {
        String sql = String.format("update hitransactionproperty set propvaldate=trunc(sysdate) where hitransactionid=%s and propertykey='PDATE'", serviceOrderId);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);

        sql = String.format("update hitransactionproperty set propvaldate=trunc(sysdate) where hitransactionid=%s and propertykey='BILLDATE'", serviceOrderId);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public void updateProvisionDateOfChangeBundleServiceOrder(String serviceOrderId) {
        Log.info("Service order id:" + serviceOrderId);
        String sql = String.format("update hitransactionproperty set propvaldate = trunc(sysdate) where hitransactionid = %s and propertykey in ('PDATE','BILLDATE')", serviceOrderId);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static int findPayemtGateWay(List<PaymentGatewayEnity> allPaymentGateEnity, String action, String paymentType, String saleChannel) {
        return Integer.parseInt(String.valueOf(allPaymentGateEnity.stream().filter(x -> x.getAction().equalsIgnoreCase(action) && x.getPaymentType().equalsIgnoreCase(paymentType) && x.getSaleChannel().equalsIgnoreCase(saleChannel)).count()));
    }

    public static List<PaymentGatewayEnity> getPaymentGatewayRequestForOrderInOEDb(String serviceOrder) {

        List<PaymentGatewayEnity> paymentGateWayList = new ArrayList<>();

        String sql = String.format("select paymentgtwrequestid,action,paymenttype,saleschannel from paymentgtwrequest where externalid='%s'", serviceOrder);
        try {
            ResultSet res = OracleDB.SetToOEDatabase().executeQuery(sql);
            while (res.next()) {
                PaymentGatewayEnity payment = new PaymentGatewayEnity();
                payment.setPaymentGTWRequestID(res.getString(1));
                payment.setAction(res.getString(2));
                payment.setPaymentType(res.getString(3));
                payment.setSaleChannel(res.getString(4));
                System.out.println(payment.toString());
                paymentGateWayList.add(payment);
            }
        } catch (Exception ex) {

        }
        return paymentGateWayList;
    }

    public static List<PaymentGatewayRespondEnity> getPaymentGatewayRespondForOrderInOEDb(List<String> requestIDList) {

        List<PaymentGatewayRespondEnity> paymentGateWayList = new ArrayList<>();
        String requestID = String.join(",", requestIDList);
        String sql = String.format("select action,status,gatewaystatus,bankstatus,tdsauthresponsetype,fraudstatus,tokenstatus,gatewayrequestid from paymentgtwresponse where paymentgtwrequestid in (%s)", requestID);
        try {
            ResultSet res = OracleDB.SetToOEDatabase().executeQuery(sql);
            while (res.next()) {
                PaymentGatewayRespondEnity payment = new PaymentGatewayRespondEnity();
                payment.setAction(res.getString(1));
                payment.setStatus(res.getString(2));
                payment.setGatewayStatus(res.getString(3));
                payment.setBankStatus(res.getString(4));
                payment.setTDSAuthResponseType(res.getString(5));
                payment.setFraudStatus(res.getString(6));
                payment.setTokenStatus(res.getString(7));
                payment.setGatewayRequestid(res.getString(8));
                paymentGateWayList.add(payment);
            }
        } catch (Exception ex) {

        }
        return paymentGateWayList;
    }

    public static int findPaymentGateWayRespond(List<PaymentGatewayRespondEnity> allPaymentGateEnity, String action, String status, String gateWayStatus, String bankStatus, String tDSAuthResponseType) {
        return Integer.parseInt(String.valueOf(allPaymentGateEnity.stream().filter(x -> x.getAction().equalsIgnoreCase(action)
                && x.getStatus().equalsIgnoreCase(status) && x.getGatewayStatus().equalsIgnoreCase(gateWayStatus)
                && x.getBankStatus().equalsIgnoreCase(bankStatus)
                && x.getTDSAuthResponseType().equalsIgnoreCase(tDSAuthResponseType)).count()));
    }

    public static int findPayemtGateWayRespondByFraudStatus(List<PaymentGatewayRespondEnity> allPaymentGateEnity, String action, String status, String gateWayStatus, String bankStatus, String FraudStatus) {
        return Integer.parseInt(String.valueOf(allPaymentGateEnity.stream().filter(x -> x.getAction().equalsIgnoreCase(action)
                && x.getStatus().equalsIgnoreCase(status) && x.getGatewayStatus().equalsIgnoreCase(gateWayStatus)
                && x.getBankStatus().equalsIgnoreCase(bankStatus)
                && x.getFraudStatus().equalsIgnoreCase(FraudStatus)).count()));
    }

    public static int findPaymentGateWayRespondByTokenStatus(List<PaymentGatewayRespondEnity> allPaymentGateEnity, String action, String status, String gateWayStatus, String bankStatus, String TokenStatus) {
        return Integer.parseInt(String.valueOf(allPaymentGateEnity.stream().filter(x -> x.getAction().equalsIgnoreCase(action)
                && x.getStatus().equalsIgnoreCase(status) && x.getGatewayStatus().equalsIgnoreCase(gateWayStatus)
                && x.getBankStatus().equalsIgnoreCase(bankStatus)
                && x.getTokenStatus().equalsIgnoreCase(TokenStatus)).count()));
    }

    public static int findPaymentGateWayRespondByBankStatus(List<PaymentGatewayRespondEnity> allPaymentGateEnity, String action, String status, String gateWayStatus, String bankStatus) {
        return Integer.parseInt(String.valueOf(allPaymentGateEnity.stream().filter(x -> x.getAction().equalsIgnoreCase(action)
                && x.getStatus().equalsIgnoreCase(status) && x.getGatewayStatus().equalsIgnoreCase(gateWayStatus)
                && x.getBankStatus().equalsIgnoreCase(bankStatus)).count()));
    }

    public static void updateRunAsAtDateOfCurrentDateMinus1MonthAnd1Day() {
        try {
            OracleDB.SetToNonOEDatabase().executeNonQuery(String.format("update billruncalendar set asatdate=trunc(SYSDATE - %d) where asatdate=trunc(SYSDATE-1) and billinggroupid=%d", TimeStamp.TodayMinusTodayMinus1MonthMinus1Day(), tempBillingGroupHeader.getKey()));
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void updateInvoiceDueDate(String customerNumber) {
        try {
            OracleDB.SetToNonOEDatabase().executeNonQuery(String.format("update invoice set DateDue=trunc(to_date('%s','yyyy-mm-dd')) where hmbrid=%s", TimeStamp.Today(), getHmbrid(customerNumber)));
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public int findDiscountBundlesByCondition(List<DiscountBundleEntity> allDiscountBundles, String capType, Date startDate, Date endDate, String partitionIdRef, String bundleCode, String status) {
        return Integer.parseInt(String.valueOf(allDiscountBundles.stream().filter(x -> x.capType.equalsIgnoreCase(capType) && x.startDate.equals(startDate) && x.endDate.equals(endDate) && x.partitionIdRef.equalsIgnoreCase(partitionIdRef) && x.bundleCode.equalsIgnoreCase(bundleCode) && x.status.equalsIgnoreCase(status)).count()));
    }
}

