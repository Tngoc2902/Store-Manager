/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package shopapp.dao;

/**
 *
 * @author ASUS
 */
import shopapp.model.Product;
import java.sql.*;
import java.util.*;

public class ProductDAO {
    private Connection conn;

    public ProductDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM Products";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            products.add(new Product(
                rs.getInt("ProductID"),
                rs.getString("Name"),
                rs.getDouble("Price"),
                rs.getInt("Quantity")
            ));
        }
        return products;
    }

    public void addProduct(Product p) throws SQLException {
        String sql = "INSERT INTO Products (Name, Price, Quantity) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, p.getName());
        stmt.setDouble(2, p.getPrice());
        stmt.setInt(3, p.getQuantity());
        stmt.executeUpdate();
    }

    public void updateProduct(Product p) throws SQLException {
        String sql = "UPDATE Products SET Name=?, Price=?, Quantity=? WHERE ProductID=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, p.getName());
        stmt.setDouble(2, p.getPrice());
        stmt.setInt(3, p.getQuantity());
        stmt.setInt(4, p.getId());
        stmt.executeUpdate();
    }

    public void deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM Products WHERE ProductID=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, productId);
        stmt.executeUpdate();
    }
    public Product getProductById(int productId) throws SQLException {
    String sql = "SELECT * FROM Products WHERE ProductID = ?";
    PreparedStatement stmt = conn.prepareStatement(sql);
    stmt.setInt(1, productId);
    ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
        return new Product(
            rs.getInt("ProductID"),
            rs.getString("Name"),
            rs.getDouble("Price"),
            rs.getInt("Quantity")
        );
    }
    return null;
}

}
