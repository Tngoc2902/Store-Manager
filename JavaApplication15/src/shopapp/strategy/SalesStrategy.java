// File: SalesStrategy.java
package shopapp.strategy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface SalesStrategy {
    ResultSet getStatistics(Connection conn) throws SQLException;
}
