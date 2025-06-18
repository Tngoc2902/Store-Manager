// File: SalesContext.java
package shopapp.strategy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SalesContext {
    private SalesStrategy strategy;

    public void setStrategy(SalesStrategy strategy) {
        this.strategy = strategy;
    }

    public ResultSet executeStrategy(Connection conn) throws SQLException {
        return strategy.getStatistics(conn);
    }
}
