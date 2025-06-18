package shopapp.strategy;

import java.sql.*;

public class WeeklySalesStrategy implements SalesStrategy {
    public ResultSet getStatistics(Connection conn) throws SQLException {
        String query = "SELECT DATEPART(week, CreatedAt) AS Week, YEAR(CreatedAt) AS Year, " +
                       "SUM(D.Quantity) AS TotalSold, SUM(D.Quantity * D.Price) AS TotalRevenue " +
                       "FROM Invoices I JOIN InvoiceDetails D ON I.InvoiceID = D.InvoiceID " +
                       "GROUP BY YEAR(CreatedAt), DATEPART(week, CreatedAt) " +
                       "ORDER BY Year DESC, Week DESC";
        return conn.createStatement().executeQuery(query);
    }
}
