package logic.business.db;

import framework.config.Config;
import framework.utils.Db;
import framework.utils.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OracleDB extends Db {
    private static Connection connection;
    private static String url;
    private static String username;
    private static String password;
    private static OracleDB instance;

    public static OracleDB SetToNonOEDatabase() {
        url = Config.getProp("dbUrl");
        username = Config.getProp("dbUserName");
        password = Config.getProp("dbPassWord");
        try {
            if (connection != null) {
                if (connection.getMetaData().getUserName().equalsIgnoreCase(Config.getProp("oeUserName")))
                    getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (instance == null)
            instance = new OracleDB();
        return instance;
    }

    public static OracleDB SetToOEDatabase() {
        url = Config.getProp("oeUrl");
        username = Config.getProp("oeUserName");
        password = Config.getProp("oePassWord");
        try {
            if (connection != null) {
                if (connection.getMetaData().getUserName().equalsIgnoreCase(Config.getProp("dbUserName")))
                    getConnection();
            }
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        if (instance == null)
            instance = new OracleDB();
        return instance;
    }

    public static Object getValueOfResultSet(ResultSet resultSet, int index) {
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

    public static Object getValueOfResultSet(ResultSet resultSet, String columnName) {
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
            executeQuery(createConnection(), "select pkg_audit.SetInfo('pererae',2) from dual");
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
    }

    public int executeNonQuery(String sql, HashMap<Integer, Object> formParams) {
        int result = 0;
        Connection conn = null;
        try {
            conn = createConnection();
            allowUpdating();
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
                Log.error(ex.getMessage());
            }
        }
        return result;
    }

    public int executeNonQueryForDate(String sql, HashMap<Integer, Object> formParams) {
        int result = 0;
        Connection conn = null;
        try {
            allowUpdating();
            conn = createConnection();
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
                Log.error(ex.getMessage());
            }
        }
        return result;
    }

    public ResultSet executeQuery(String sql) {
        return executeQuery(createConnection(), sql);
    }

    public int executeNonQuery(String sql) {
        allowUpdating();
        return executeNonQuery(createConnection(), sql);
    }


    public CallableStatement callableStatement() {
        CallableStatement stmt = null;
        return stmt;
    }

    public List executeQueryReturnList(String query) {
        try {
            return Db.executeQuery(createConnection(), query, 100);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Connection getConnection() {
        try {
            connection = Db.createConnection(url, username, password);
            Log.info("Connect to " + username + " database successfully!");
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return connection;
    }

    public Connection createConnection() {
        if (connection != null) {
            return connection;
        } else {
            return getConnection();
        }
    }

    public List<String> executeQueryReturnListString(String query) {
        List sms = new ArrayList<>();
        sms = OracleDB.SetToNonOEDatabase().executeQueryReturnList(query);
        List<String> result = new ArrayList<>();
        if (!sms.isEmpty()) {
            for (int y = 0; y < sms.size(); y++) {
                result.add(sms.get(y).toString());
            }
        }
        return result;
    }
    public int executeNonQueryWithoutTrigger(String disable, String enable,String sql) {
        allowUpdating();
        return executeNonQueryWithOutTrigger(createConnection(),disable,enable, sql);
    }
    public int executeNonQueryWithOutTrigger(Connection connection, String disable, String enable, String sql) {
        int result = 0;
        Connection conn  = connection;;
        try {
            executeNonQuery(connection,disable);
            executeNonQuery(connection,sql);
            //conn.close();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        } finally {
            executeNonQuery(connection,enable);
        }
        return result;
    }

}
