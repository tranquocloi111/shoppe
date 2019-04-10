package logic.utils;

import framework.config.Config;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DB {

    static String dbUrl = "";
    static String userName = "";
    static String passWord = "";

    private static DB db;

    private DB(String url, String userName, String passWord){
        this.dbUrl = url;
        this.userName = userName;
        this.passWord = passWord;
    }

    public static DB SetToNonOEDatabase(){
        dbUrl = "jdbc:oracle:thin:@10.50.168.242:1521:TESVM";//Config.getProp("oeUrl");
        userName = "TST03";//Config.getProp("oeUserName");
        passWord = "TST03";//Config.getProp("oePassWord");
        if (db != null)
            return db;
        return new DB(dbUrl, userName, passWord);
    }

    public static DB SetToOEDatabase(){
        dbUrl = "jdbc:oracle:thin:@10.50.168.242:1521:TESVM";//Config.getProp("oeUrl");
        userName = "OETST03";//Config.getProp("oeUserName");
        passWord = "OETST03";//Config.getProp("oePassWord");
        if (db != null)
            return db;
        return new DB(dbUrl, userName, passWord);
    }

    public ResultSet executeQuery(String sql)  {
        ResultSet rs = null;
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            //conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rs;
    }

    public int  executeNonQuery(String sql, HashMap<Integer, Object> formParams)  {
        int result = 0;
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeQuery("select pkg_audit.SetInfo('pererae',2) from dual");

            PreparedStatement pStmt = conn.prepareStatement(sql);
            for (Map.Entry mapElement : formParams.entrySet()) {
                int key = (Integer) mapElement.getKey();
                String value = (String) mapElement.getValue();
                pStmt.setString(key, value);
            }
            result = pStmt.executeUpdate();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public int  executeNonQueryDate(String sql, HashMap<Integer, Object> formParams)  {
        int result = 0;
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeQuery("select pkg_audit.SetInfo('pererae',2) from dual");

            PreparedStatement pStmt = conn.prepareStatement(sql);
            for (Map.Entry mapElement : formParams.entrySet()) {
                int key = (Integer) mapElement.getKey();
                Date value = (Date) mapElement.getValue();
                pStmt.setDate(key, value);
            }
            result = pStmt.executeUpdate();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public int executeNonQuery(String sql)  {
        int result = 0;
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.executeQuery("select pkg_audit.SetInfo('pererae',2) from dual");

            PreparedStatement pstmt = conn.prepareStatement(sql);
            result = pstmt.executeUpdate();
            //conn.close();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return result;
    }

    public CallableStatement  callableStatement(){
        CallableStatement stmt = null;
        return  stmt;
    }

    protected void executeSetInfo(){
        try {
           executeQuery("select pkg_audit.SetInfo('pererae',2) from dual");
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
    }

    public static Object retriveDataInResultSet(ResultSet resultSet, int index){
        Object object = null;
        try {
            if (resultSet.next()) {
                object = resultSet.getObject(index);
                return object;
            }
        }catch(Exception ex){
            Log.error(ex.getMessage());
        }
        return  null;
    }

    public static Object retriveDataInResultSet(ResultSet resultSet, String columnName){
        Object object = null;
        try {
            if (resultSet.next()) {
                object = resultSet.getObject(columnName);
                return object;
            }
        }catch(Exception ex){
            Log.error(ex.getMessage());
        }
        return  null;
    }

    public Connection getConnection(){
        Connection conn = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(dbUrl, userName, passWord);
            Log.info("connect successfully!");
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return conn;
    }
}
