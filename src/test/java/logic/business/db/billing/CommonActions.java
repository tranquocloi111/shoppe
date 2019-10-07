package logic.business.db.billing;

import framework.config.Config;
import framework.utils.Log;
import logic.business.db.OracleDB;
import logic.business.entities.PaymentGatewayEnity;
import logic.business.entities.PaymentGatewayRespondEnity;
import logic.business.helper.FTPHelper;
import logic.business.helper.MiscHelper;
import logic.pages.care.find.PaymentPage;
import logic.utils.Parser;
import logic.utils.TimeStamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CommonActions extends OracleDB {

    public static void updateCustomerStartDate(String cusId, Date newStartDate) {
        try {
            String sql = "{Call UPDATECUSTSTARTDATE(?,?)}";
            Connection connection = OracleDB.SetToNonOEDatabase().createConnection();
            CallableStatement cstmt = OracleDB.SetToNonOEDatabase().callableStatement();
            cstmt = connection.prepareCall(sql);
            cstmt.setString("icustid", cusId);
            cstmt.setString("istartdate_ddmmyyyy", Parser.parseDateFormate(newStartDate, "ddMMyyyy"));
            cstmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateCustomerEndDate(String cusId, Date newEndDate) {
        try {
            String sql = "{Call UPDATECUSTENDDATE(?,?)}";
            Connection connection = OracleDB.SetToNonOEDatabase().createConnection();
            CallableStatement cstmt = OracleDB.SetToNonOEDatabase().callableStatement();
            cstmt = connection.prepareCall(sql);
            cstmt.setString("icustid", cusId);
            cstmt.setString("ienddate", Parser.parseDateFormate(newEndDate, "ddMMyyyy"));
            cstmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getAgreementIsSigned(String orderId) {
        String sql = String.format("select count(*) as totalNumber FROM agreementdetail a where a.status = 'SIGNED' and a.agreementdetailid in (select s.agreementdetailid from selfcareorder s where s.orders_id =%s)", orderId);
        ResultSet resultSet = OracleDB.SetToNonOEDatabase().executeQuery(sql);
        return Integer.valueOf(String.valueOf(OracleDB.getValueOfResultSet(resultSet, "totalNumber")));
    }

    public static String getPinCode(String customerId) {
        String pinCode = "";
        String sql = String.format(("select ip.propvalchar from inventory i, invproperty ip, businessunit b where i.inventoryid=ip.inventoryid and b.buid = i.rootbuid and ip.propertykey='SNO' and i.datedeactive is null and b.buid = '%s'"), customerId);
        List mpn = OracleDB.SetToOEDatabase().executeQueryReturnList(sql);
        try {
            for (int y = 0; y < mpn.size(); y++) {
                sql = String.format("select PIN_CODE from PIN p where MPN = '%s' order by p.created desc", ((HashMap) mpn.get(y)).get("PROPVALCHAR"));
                for (int i = 0; i < 10; i++) {
                    pinCode = String.valueOf(OracleDB.getValueOfResultSet(OracleDB.SetToOEDatabase().executeQuery(sql), "PIN_CODE"));
                    Thread.sleep(2000);
                }
                if (!pinCode.equalsIgnoreCase("null")) {
                    return pinCode;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return pinCode;
    }

    public static void updateProvisionDateOfChangeBundleServiceOrder(String serviceOrderId) {
        Log.info("Service order id : " + serviceOrderId);
        String sql = String.format("update hitransactionproperty set propvaldate = trunc(sysdate) where hitransactionid = %s and propertykey in ('PDATE','BILLDATE')", serviceOrderId);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static List<String> getNumberSMSCreatedInHitransactionEventTable(String subNo1) {
        subNo1 = '%' + subNo1 + '%';
        String sql = String.format("select e.contextinfo from Hitransactionevent e  where e.hieventtype = 'SMS' and descr like '%s'", subNo1);
        List sms = new ArrayList<>();
        sms = OracleDB.SetToNonOEDatabase().executeQueryReturnList(sql);
        List<String> result = new ArrayList<>();
        if (!sms.isEmpty()) {
            for (int y = 0; y < sms.size(); y++) {
                result.add(sms.get(y).toString());
            }
        }
        return result;
    }

    public static boolean isBonusBundleExisting() {
        String sql = "SELECT bg.*\n" +
                "FROM   (SELECT dp.discountplanid\n" +
                "           ,dpp.propvalchar || ' - ' || dp.Descr descr\n" +
                "           ,dp.rowversion\n" +
                "     FROM   discountplan         dp\n" +
                "           ,discountplanproperty dpp\n" +
                "     WHERE dp.discountmethod IN ('BONUSBUNDGRP')\n" +
                "     AND    dp.discountplanid = dpp.discountplanid\n" +
                "     AND    dpp.propertykey = 'DBUNDGRPCODE') bg -- VW_BUNDLEGRP in dev\n" +
                "    -- WHERE bg.discountplanid = iCode";

        List list = OracleDB.SetToNonOEDatabase().executeQueryReturnList(sql);
        return list.size() > 0;
    }


    public static List getAllBundlesGroupByTariff(String tariff) {
        String sql = "select vwx.GroupCode,\n" +
                "     vwx.GroupDescr,\n" +
                "     vwx.GroupType,\n" +
                "     vwx.GroupPlanId, \n" +
                "    (select dpp.propvalnumber from discountplanproperty dpp where dpp.discountplanid = vwx.GroupPlanId and dpp.propertykey = 'QMIN') MinOccurs,\n" +
                "    (select dpp.propvalnumber from discountplanproperty dpp where dpp.discountplanid = vwx.GroupPlanId and dpp.propertykey = 'QMAX') MaxOccurs,\n" +
                "     vwx.ChildBundCode,\n" +
                "     vwx.ChildDescr,\n" +
                "     p.productcode\n" +
                " from   productmetaproperty pmp,\n" +
                "     vw_bundlegrpmap vwx,\n" +
                "     product p\n" +
                " where  pmp.productid = p.productid\n" +
                " and p.productcode = '" + tariff + "'\n" +
                " and    pmp.propertykey = 'BUNDLEGRP' \n" +
                " and    pmp.propvalnumber = vwx.GroupPlanId\n" +
                " and p.productcode = '" + tariff + "'";
        try {
            ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) OracleDB.SetToNonOEDatabase().executeQueryReturnList(sql);
            return list;
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return null;
    }

    public static List<String> getSMSIsSent(String serviceOrder, String description) {
        description = '%' + description + '%';
        String sql = String.format("select posttransactionid from hitransactionevent where hitransactionid=%s and DESCR like '%s'", serviceOrder, description);
        List sms = new ArrayList<>();
        sms = OracleDB.SetToNonOEDatabase().executeQueryReturnList(sql);
        List<String> result = new ArrayList<>();
        if (!sms.isEmpty()) {
            for (int y = 0; y < sms.size(); y++) {
                result.add(sms.get(y).toString());
            }
        }
        return result;
    }

    public static List<String> getContextInfoOfSMSServiceOrderIsCorrectInDb(String serviceOrder, String description) {
        description = '%' + description + '%';
        String sql = String.format("select contextinfo from hitransactionevent where hitransactionid=%s and DESCR like '%s'", serviceOrder, description);
        List sms = new ArrayList<>();
        sms = OracleDB.SetToNonOEDatabase().executeQueryReturnList(sql);
        List<String> result = new ArrayList<>();
        if (!sms.isEmpty()) {
            for (int y = 0; y < sms.size(); y++) {
                result.add(sms.get(y).toString());
            }
        }
        return result;
    }

    public static List getBundleByCustomerId(String customerId) {
        String sql = "SELECT * FROM VW_GETBUNDLE WHERE ROOTBUID = " + customerId + " and LEVL = 'Current'";
        return OracleDB.SetToNonOEDatabase().executeQueryReturnList(sql);
    }

    public static String getResponse(String query, String subNo) {
        try {
            String sql = String.format("{Call %s(?,?)}", query);
            Connection connection = OracleDB.SetToNonOEDatabase().createConnection();
            CallableStatement cstmt = OracleDB.SetToNonOEDatabase().callableStatement();
            cstmt = connection.prepareCall(sql);
            cstmt.setString("1", subNo);
            cstmt.registerOutParameter("2", Types.CLOB);
            cstmt.executeQuery();
            Clob text = cstmt.getClob("2");
            return clobToString(text);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String clobToString(final Clob clob) throws IOException, SQLException {
        final StringBuilder str = new StringBuilder();
        String string;

        if (clob != null) {
            try (BufferedReader bufferRead = new BufferedReader(clob.getCharacterStream())) {
                while ((string = bufferRead.readLine()) != null) {
                    str.append(string);
                }
            }
        }

        return str.toString();
    }

    public static void updateCustomerAccessRoleToNone() {
        String sql = "delete from objectrole where roleid = 147 and clientobjectid  =  " + Config.getProp("businessclientobjectid");
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static void updateCustomerAccessRoleToReadWrite() {
        String sql = "INSERT INTO objectrole (CLIENTOBJECTID, ROLEID,INCLUDEFLG,SELFLG,INSFLG,UPDFLG,DELFLG,ROWVERSION ) VALUES(" + Config.getProp("businessclientobjectid") + ",147,'N','Y','Y','Y','Y', 0)";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static boolean checkCustomerAccessRole() {
        try {
            String sql = "select count(*) as quality from objectrole where roleid = 147 and clientobjectid = " + Config.getProp("businessclientobjectid");
            return OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "quality").toString().equalsIgnoreCase("1");

        } catch (Exception ex) {
            Log.info(ex.getMessage());
        }
        return false;
    }

    public static void updateChangeCustomerTypeAccessRoleToNone() {
        String sql = "delete from objectrole where roleid = 147 and clientobjectid  =  " + Config.getProp("customertypeclientobjectid");
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static void updateChangeCustomerTypeAccessRoleToReadWrite() {
        String sql = "INSERT INTO objectrole (CLIENTOBJECTID, ROLEID,INCLUDEFLG,SELFLG,INSFLG,UPDFLG,DELFLG,ROWVERSION ) VALUES(" + Config.getProp("customertypeclientobjectid") + ",147,'N','Y','Y','Y','Y', 0) ";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static boolean checkChangeCustomerTypeAccessRole() {
        try {
            String sql = "select count(*) as quality from objectrole where roleid = 147 and clientobjectid =  " + Config.getProp("customertypeclientobjectid");
            return OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "quality").toString().equalsIgnoreCase("1");
        } catch (Exception ex) {
            Log.info(ex.getMessage());
        }
        return false;
    }

    public static void updateDueDateInvoice(String date, String invoiceId) {
        String sql = "UPDATE invoice SET datedue = '" + date + "' WHERE documentnbr = '" + invoiceId + "'  ";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static void updateCustomerDDIDetailsInDatabase(String dateStart, String hmbrid, String DDIReference) {
        String sql = String.format("update hmbrproperty set propvalchar='A' where propertykey='DDISTAT' and hmbrid=%s", hmbrid);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);

        sql = String.format("update hmbrproperty set propvalchar='%s', datestart= '" + dateStart + "' where propertykey='DDIREF' and hmbrid=%s", DDIReference, hmbrid);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static String getFileNameByInvoiceNumber(String invoiceNumber){
        String sql = "select invoiceid from invoice where documentnbr = '"+invoiceNumber+"'";
        String invoiceId = OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "invoiceid").toString() ;

        sql = "select ddbatchid from ddtrans  where invoiceid = '"+invoiceId+"'";
        String ddbatchid = OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "ddbatchid").toString() ;

        sql = "select filename from ddbatch  where ddbatchid=  " + ddbatchid;
        String fileName = OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "filename").toString() ;
        return fileName;
    }

    public static void isFileProcessInboundNonXmlProcessed(String fileName){
        String sql = "select count(*) as quality from remotejob where jobdescr like 'Process inbound non-xml file : "+fileName+"' ";
        boolean isFlg;
       try {
           do{
               isFlg = OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "quality").toString().equalsIgnoreCase("1");
               Thread.sleep(3000);
           }while (isFlg == false);
       }catch (Exception ex){

       }
    }

    public static void updateCreditCardDateToPast(String hmbrid) {
        String propvaldate = Parser.parseDateFormate(Date.valueOf(TimeStamp.Today().toLocalDate().minusYears(1)), "dd/MMM/yyyy");
        String propvalnumber[] = Parser.parseDateFormate(Date.valueOf(TimeStamp.Today().toLocalDate().minusYears(1)), "dd/MM/yyyy").split("/");
        String sql = "UPDATE  hmbrproperty  set propvaldate = '" + propvaldate + "' where propertykey like 'CCED' and hmbrid = '" + hmbrid + "' ";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
        sql = "UPDATE hmbrproperty  set propvalnumber = '" + propvalnumber[1] + "' where propertykey like 'CCEM' and hmbrid = '" + hmbrid + "' ";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
        sql = "UPDATE hmbrproperty  set propvalnumber = '" + propvalnumber[2] + "' where propertykey like 'CCEY' and hmbrid = '" + hmbrid + "' ";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }
    public static  String getReportUniqueId() {
        String id;
        String sql = " select ReportRunID.NEXTVAL from dual";
        try {
            id = OracleDB.SetToNonOEDatabase().executeQueryReturnListString(sql).get(0);
            id=id.substring(id.indexOf("=")+1,id.length()-1);
            for (int i=0;i<4;i++)
            {
                id="0"+id;
            }
            return id;
        } catch (Exception ex) {
            System.out.print(ex);
        }
        return null;
    }

    public static List getHiTransactionEvent(String serviceOrderId){
        String sql = "select HITRANSACTIONEVENTID, HITRANSACTIONID, HIEVENTTYPE, DESCR, CONTEXTINFO from hitransactionevent he where he.hitransactionid = " + serviceOrderId;
        List hitransactionEvent = OracleDB.SetToNonOEDatabase().executeQueryReturnList(sql);
        return hitransactionEvent;
    }

    public static void updateReceiptDate(String receiptid, String date){
        String sql = "UPDATE receipt set receiptdate = '"+date+"' where receiptid = '"+receiptid+"' ";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);

        sql = "UPDATE receiptallocation set alloctimestamp = '"+date+"' where receiptid = '"+receiptid+"' ";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println(checkCustomerAccessRole());
    }

    public static void updatePrpovaldate(String date, String customerNumber) {
        String sql = String.format("update hmbrproperty set propvaldate = to_date('%s','yyyy-mm-dd') where hmbrid in (select hmbrid from hierarchymbr hm, hierarchy h where h.rootbuid in (%s)  and h.hid = hm.hid and hm.hmbrtype = 'BP') and propertykey in ('TKEXPDATE')", date, customerNumber);
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static void updateProvalNumberValue(String customerNumber, String propertyKey, double value) {
        String sql = String.format("update hmbrproperty set PROPVALNUMBER = %s where hmbrid in (select hmbrid from hierarchymbr hm, hierarchy h where h.rootbuid in (%s)  and h.hid = hm.hid and hm.hmbrtype = 'BP') and propertykey in ('%s')", value, customerNumber, propertyKey);

        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }
}
