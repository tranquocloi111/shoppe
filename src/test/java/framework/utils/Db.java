package framework.utils;

import java.sql.*;

public class Db {

    public static Connection createConnection(String url, String userName, String password) throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(url, userName, password);
    }

    public ResultSet executeQuery(Connection connection, String sql) {
        ResultSet rs = null;
        try {
            Connection conn = connection;
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            //conn.close();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return rs;
    }

    public int executeNonQuery(Connection connection, String sql) {
        int result = 0;
        Connection conn = null;
        try {
            conn = connection;
            PreparedStatement pstmt = conn.prepareStatement(sql);
            result = pstmt.executeUpdate();
            //conn.close();
        } catch (Exception ex) {
            Log.error(ex.getMessage());
        }
        return result;
    }

}
