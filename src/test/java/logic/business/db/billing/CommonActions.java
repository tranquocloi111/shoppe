package logic.business.db.billing;

import framework.config.Config;
import framework.utils.FTP;
import framework.utils.Log;
import logic.business.db.OracleDB;
import logic.business.helper.FTPHelper;
import logic.business.helper.MiscHelper;
import logic.utils.Parser;
import org.testng.Assert;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


}
