/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package shopapp.utils;

/**
 *
 * @author ASUS
 */
import java.sql.*;

public class DBUtil {
    private static Connection conn;

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=ShopDB;encrypt=true;trustServerCertificate=true";
            String user = "ngoc";
            String password = "292004";
            conn = DriverManager.getConnection(url, user, password);
            
        }
        return conn;
    }
}
