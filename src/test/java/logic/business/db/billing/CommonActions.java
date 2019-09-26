package logic.business.db.billing;

import framework.utils.Log;
import logic.business.db.OracleDB;
import logic.business.entities.PaymentGatewayEnity;
import logic.business.entities.PaymentGatewayRespondEnity;
import logic.business.helper.FTPHelper;
import logic.business.helper.MiscHelper;
import logic.pages.care.find.PaymentPage;
import logic.utils.Parser;

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

    public static void updateCustomerAccessRoleToNone(){
        String sql = "delete from objectrole where roleid = 147 and clientobjectid  = 585";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static void updateCustomerAccessRoleToReadWrite(){
        String sql = "INSERT INTO objectrole (CLIENTOBJECTID, ROLEID,INCLUDEFLG,SELFLG,INSFLG,UPDFLG,DELFLG,ROWVERSION ) VALUES(585,147,'N','Y','Y','Y','Y', 0)";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static boolean checkCustomerAccessRole(){
        try {
            String sql = "select count(*) as quality from objectrole where roleid = 147 and clientobjectid = 585 ";
            return OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "quality").toString().equalsIgnoreCase("1");

        }catch (Exception ex){
            Log.info(ex.getMessage());
        }
        return false;
    }

    public static void updateChangeCustomerTypeAccessRoleToNone(){
        String sql = "delete from objectrole where roleid = 147 and clientobjectid  = 546";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static void updateChangeCustomerTypeAccessRoleToReadWrite(){
        String sql = "INSERT INTO objectrole (CLIENTOBJECTID, ROLEID,INCLUDEFLG,SELFLG,INSFLG,UPDFLG,DELFLG,ROWVERSION ) VALUES(546,147,'N','Y','Y','Y','Y', 0)";
        OracleDB.SetToNonOEDatabase().executeNonQuery(sql);
    }

    public static boolean checkChangeCustomerTypeAccessRole(){
        try {
            String sql = "select count(*) as quality from objectrole where roleid = 147 and clientobjectid = 546 ";
            return OracleDB.getValueOfResultSet(OracleDB.SetToNonOEDatabase().executeQuery(sql), "quality").toString().equalsIgnoreCase("1");

        }catch (Exception ex){
            Log.info(ex.getMessage());
        }
        return false;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println(checkCustomerAccessRole());
    }

}
