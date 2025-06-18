package shopapp.strategy;

import java.sql.*;

public class MonthlySalesStrategy implements SalesStrategy {
    public ResultSet getStatistics(Connection conn) throws SQLException {
        String query = "SELECT MONTH(CreatedAt) AS Month, " +
                       "YEAR(CreatedAt) AS Year, " +
                       "SUM(Quantity) AS TotalSold, " +
                       "SUM(Quantity * Price) AS TotalRevenue " +
                       "FROM Invoices I JOIN InvoiceDetails D ON I.InvoiceID = D.InvoiceID " +
                       "GROUP BY YEAR(CreatedAt), MONTH(CreatedAt) " +
                       "ORDER BY Year DESC, Month DESC";
        return conn.createStatement().executeQuery(query);
    }
}