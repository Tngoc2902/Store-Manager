package shopapp.strategy;

import java.sql.*;

public class DailySalesStrategy implements SalesStrategy {
    public ResultSet getStatistics(Connection conn) throws SQLException {
        String query = "SELECT CONVERT(date, CreatedAt) AS Date, " +
                       "SUM(Quantity) AS TotalSold, " +
                       "SUM(Quantity * Price) AS TotalRevenue " +
                       "FROM Invoices I JOIN InvoiceDetails D ON I.InvoiceID = D.InvoiceID " +
                       "GROUP BY CONVERT(date, CreatedAt) " +
                       "ORDER BY Date DESC";
        return conn.createStatement().executeQuery(query);
    }
}