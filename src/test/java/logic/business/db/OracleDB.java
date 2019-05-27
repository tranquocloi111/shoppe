package logic.business.db;

import framework.config.Config;
import framework.utils.Db;
import framework.utils.Log;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class OracleDB extends Db {
    private static Connection connection;
    private static String url;
    private static String username;
    private static String password;
    private static OracleDB oracleDB = new OracleDB();

    public static OracleDB SetToNonOEDatabase() {
        url = Config.getProp("dbUrl");
        username = Config.getProp("dbUserName");
        password = Config.getProp("dbPassWord");
        try {
            if (connection != null) {
                if (connection.getMetaData().getUserName().equalsIgnoreCase(Config.getProp("oeUserName")))
                    connection = Db.createConnection(url, username, password);
            } else {
                connection = Db.createConnection(url, username, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (oracleDB != null)
            return oracleDB;
        return new OracleDB();
    }

    public static OracleDB SetToOEDatabase() {
        url = Config.getProp("oeUrl");
        username =Config.getProp("oeUserName");
        password = Config.getProp("oePassWord");
        try {
            if (connection != null) {
                if (connection.getMetaData().getUserName().equalsIgnoreCase(Config.getProp("dbUserName")))
                    connection = Db.createConnection(url, username, password);
            } else {
                connection = Db.createConnection(url, username, password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (oracleDB != null)
            return oracleDB;
        return new OracleDB();
    }

    public static Object retriveDataInResultSet(ResultSet resultSet, int index) {
        Object object = null;
        try {
            if (resultSet.next()) {
                object = resultSet.getObject(index);
                return object;
            }
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return null;
    }

    public static Object retriveDataInResultSet(ResultSet resultSet, String columnName) {
        Object object = null;
        try {
            if (resultSet.next()) {
                object = resultSet.getObject(columnName);
                return object;
            }
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return null;
    }

    protected void allowUpdating() {
        try {
            executeQuery(connection, "select pkg_audit.SetInfo('pererae',2) from dual");
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
    }

    public int executeNonQuery(String sql, HashMap<Integer, Object> formParams) {
        int result = 0;
        try {
            allowUpdating();
            PreparedStatement pStmt = connection.prepareStatement(sql);
            for (Map.Entry mapElement : formParams.entrySet()) {
                int key = (Integer) mapElement.getKey();
                String value = (String) mapElement.getValue();
                pStmt.setString(key, value);
            }
            result = pStmt.executeUpdate();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
            try {
                connection.close();
            } catch (SQLException e) {
                Log.error(ex.getMessage());
            }
        }
        return result;
    }

    public int executeNonQueryForDate(String sql, HashMap<Integer, Object> formParams) {
        int result = 0;
        try {
            allowUpdating();
            PreparedStatement pStmt = connection.prepareStatement(sql);
            for (Map.Entry mapElement : formParams.entrySet()) {
                int key = (Integer) mapElement.getKey();
                Date value = (Date) mapElement.getValue();
                pStmt.setDate(key, value);
            }
            result = pStmt.executeUpdate();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
            try {
                connection.close();
            } catch (SQLException e) {
                Log.error(ex.getMessage());
            }
        }
        return result;
    }

    public ResultSet executeQuery(String sql) {
        return executeQuery(connection, sql);
    }

    public int executeNonQuery(String sql) {
        allowUpdating();
        return executeNonQuery(connection, sql);
    }

    public CallableStatement callableStatement() {
        CallableStatement stmt = null;
        return stmt;
    }

    public Connection getConnection() {
        return connection;
    }

}
