/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package shopapp.dao;

/**
 *
 * @author ASUS
 */
import java.sql.*;
import shopapp.model.Cart;
import shopapp.model.CartItem;
public class OrderDAO {
    private Connection conn;

    public OrderDAO(Connection conn) {
        this.conn = conn;
    }

    public void placeOrder(String customerName,String customerEmail, String customerPhone, String customerAddress) throws SQLException {
        conn.setAutoCommit(false);
        try {
            String insertCustomer = "INSERT INTO Customers (Name ,Email ,Phone, Address) OUTPUT INSERTED.CustomerID VALUES (?,?,?, ?)";
            PreparedStatement cs = conn.prepareStatement(insertCustomer);
            cs.setString(1, customerName);
            cs.setString(2,customerEmail);
            cs.setString(3, customerPhone);
            cs.setString(4, customerAddress);
            ResultSet crs = cs.executeQuery();
            int customerId = -1;
            if (crs.next()) customerId = crs.getInt(1);

            String orderSql = "INSERT INTO Orders (CustomerId, OrderDate, TotalAmount) OUTPUT INSERTED.OrderID VALUES (?, GETDATE(), ?)";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql);
            orderStmt.setInt(1, customerId);
            orderStmt.setDouble(2, Cart.getInstance().getTotal());
            ResultSet rs = orderStmt.executeQuery();
            int orderId = -1;
            if (rs.next()) orderId = rs.getInt(1);

            String detailSql = "INSERT INTO OrderDetails (OrderID, ProductID, Quantity, Price) VALUES (?, ?, ?, ?)";
            PreparedStatement detailStmt = conn.prepareStatement(detailSql);
            for (CartItem item : Cart.getInstance().getItems()) {
                detailStmt.setInt(1, orderId);
                detailStmt.setInt(2, item.getProduct().getId());
                detailStmt.setInt(3, item.getQuantity());
                detailStmt.setDouble(4, item.getProduct().getPrice());
                detailStmt.addBatch();
            }
            detailStmt.executeBatch();
            conn.commit();
            Cart.getInstance().clear();

            System.out.println("[Thông báo] Đơn hàng mới đã được tạo: Mã đơn hàng = " + orderId);
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}