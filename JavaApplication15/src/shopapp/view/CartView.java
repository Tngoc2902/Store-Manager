package shopapp.view;

import shopapp.model.Cart;
import shopapp.model.CartItem;
import shopapp.utils.DBUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import shopapp.strategy.*;


public class CartView extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnPrintInvoice, btnStatistics, btnViewHistory;
    private JButton btnRemoveItem, btnEditQuantity, btnClearCart;
    private JTextField txtSearch;
    private NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public CartView() {
        setTitle("🛒 Giỏ hàng - Ứng dụng mua sắm");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("📦 Danh sách sản phẩm trong giỏ hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Sản phẩm", "Giá", "Số lượng", "Tổng"}, 0);
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setToolTipText("Tìm kiếm theo tên sản phẩm...");

        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterCartItems(txtSearch.getText().trim());
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(new JLabel("🔍 Tìm kiếm: "));
        topPanel.add(txtSearch);
        mainPanel.add(topPanel, BorderLayout.BEFORE_FIRST_LINE);

        btnPrintInvoice = createButton("In hóa đơn", new Color(0, 123, 255));
        btnPrintInvoice.addActionListener(e -> printInvoice());

        btnStatistics = createButton("Thống kê doanh số", new Color(40, 167, 69));
        btnStatistics.addActionListener(e -> showStatistics());

        btnViewHistory = createButton("Xem lịch sử hóa đơn", new Color(255, 193, 7));
        btnViewHistory.setForeground(Color.BLACK);
        btnViewHistory.addActionListener(e -> showInvoiceHistory());

        btnRemoveItem = createButton("Xóa sản phẩm", new Color(220, 53, 69));
        btnRemoveItem.addActionListener(e -> removeSelectedItem());

        btnEditQuantity = createButton("Sửa số lượng", new Color(108, 117, 125));
        btnEditQuantity.addActionListener(e -> editSelectedQuantity());

        btnClearCart = createButton(" Xóa toàn bộ", new Color(255, 87, 34));
        btnClearCart.addActionListener(e -> clearCart());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnRemoveItem);
        bottomPanel.add(btnEditQuantity);
        bottomPanel.add(btnClearCart);
        bottomPanel.add(btnPrintInvoice);
        bottomPanel.add(btnStatistics);
        bottomPanel.add(btnViewHistory);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);

        loadCartItems();
    }

    private void loadCartItems() {
        model.setRowCount(0);
        for (CartItem item : Cart.getInstance().getItems()) {
            model.addRow(new Object[]{
                item.getProduct().getName(),
                numberFormat.format(item.getProduct().getPrice()) + " đ",
                item.getQuantity(),
                numberFormat.format(item.getTotal()) + " đ"
            });
        }
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 40));
        return btn;
    }

    private void filterCartItems(String keyword) {
        model.setRowCount(0);
        for (CartItem item : Cart.getInstance().getItems()) {
            if (item.getProduct().getName().toLowerCase().contains(keyword.toLowerCase())) {
                model.addRow(new Object[]{
                    item.getProduct().getName(),
                    numberFormat.format(item.getProduct().getPrice()) + " đ",
                    item.getQuantity(),
                    numberFormat.format(item.getTotal()) + " đ"
                });
            }
        }
    }

    private void clearCart() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa toàn bộ giỏ hàng?",
                "Xác nhận xóa toàn bộ", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Cart.getInstance().clear();
            loadCartItems();
        }
    }

    private void editSelectedQuantity() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "️ Vui lòng chọn sản phẩm để sửa số lượng.");
            return;
        }

        String newQuantityStr = JOptionPane.showInputDialog(this, "Nhập số lượng mới:");
        if (newQuantityStr != null && !newQuantityStr.isEmpty()) {
            try {
                int newQuantity = Integer.parseInt(newQuantityStr);
                if (newQuantity <= 0) {
                    throw new NumberFormatException();
                }

                int modelRow = table.convertRowIndexToModel(viewRow); // ✅ thêm dòng này để khớp với danh sách thật
                Cart.getInstance().updateQuantity(modelRow, newQuantity);
                loadCartItems();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "️ Số lượng không hợp lệ.");
            }
        }
    }

    private void removeSelectedItem() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "️ Vui lòng chọn sản phẩm cần xóa.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            String productName = (String) model.getValueAt(modelRow, 0);
            CartItem item = Cart.getInstance().getItems().stream()
                    .filter(ci -> ci.getProduct().getName().equals(productName))
                    .findFirst().orElse(null);
            if (item != null) {
                Cart.getInstance().removeItem(item.getProduct());
                loadCartItems();
            }
        }
    }

    private void updateProductStock(int productId, int quantitySold) {
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement("UPDATE Products SET Quantity = Quantity - ? WHERE ProductID = ?")) {
            stmt.setInt(1, quantitySold);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật số lượng tồn: " + e.getMessage());
        }
    }

    private void printInvoice() {
        try {
            JTextField nameField = new JTextField();
            JTextField emailField = new JTextField();
            JTextField phoneField = new JTextField();
            JTextField addressField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.add(new JLabel("Tên khách hàng:"));
            panel.add(nameField);
            panel.add(new JLabel("Email:"));
            panel.add(emailField);
            panel.add(new JLabel("Số điện thoại:"));
            panel.add(phoneField);
            panel.add(new JLabel("Địa chỉ:"));
            panel.add(addressField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Nhập thông tin in hóa đơn",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String address = addressField.getText().trim();

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "️ Vui lòng nhập đầy đủ thông tin khách hàng.");
                    return;
                }

                Connection conn = DBUtil.getConnection();
                conn.setAutoCommit(false);

                String insertInvoiceSQL = "INSERT INTO Invoices (CustomerName, Email, Phone, Address, CreatedAt) VALUES (?, ?, ?, ?, GETDATE())";
                PreparedStatement invoiceStmt = conn.prepareStatement(insertInvoiceSQL, Statement.RETURN_GENERATED_KEYS);
                invoiceStmt.setString(1, name);
                invoiceStmt.setString(2, email);
                invoiceStmt.setString(3, phone);
                invoiceStmt.setString(4, address);
                invoiceStmt.executeUpdate();

                ResultSet generatedKeys = invoiceStmt.getGeneratedKeys();
                int invoiceID = generatedKeys.next() ? generatedKeys.getInt(1) : -1;

                if (invoiceID == -1) {
                    throw new Exception("Không thể tạo hóa đơn.");
                }

                String insertDetailSQL = "INSERT INTO InvoiceDetails (InvoiceID, ProductID, Quantity, Price) VALUES (?, ?, ?, ?)";
                PreparedStatement detailStmt = conn.prepareStatement(insertDetailSQL);

                String updateStockSQL = "UPDATE Products SET Quantity = Quantity - ? WHERE ProductID = ?";
                PreparedStatement stockStmt = conn.prepareStatement(updateStockSQL);

                for (CartItem item : Cart.getInstance().getItems()) {
                    detailStmt.setInt(1, invoiceID);
                    detailStmt.setInt(2, item.getProduct().getId());
                    detailStmt.setInt(3, item.getQuantity());
                    detailStmt.setDouble(4, item.getProduct().getPrice());
                    detailStmt.addBatch();

                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getProduct().getId());
                    stockStmt.addBatch();
                }

                detailStmt.executeBatch();
                stockStmt.executeBatch();
                conn.commit();

                StringBuilder invoice = new StringBuilder();
                invoice.append("HÓA ĐƠN MUA HÀNG\n");
                invoice.append("-----------------------------\n");
                invoice.append("Khách hàng: ").append(name).append("\n");
                invoice.append("Email: ").append(email).append("\n");
                invoice.append("SĐT: ").append(phone).append("\n");
                invoice.append("Địa chỉ: ").append(address).append("\n\n");
                invoice.append("Danh sách sản phẩm:\n");

                for (CartItem item : Cart.getInstance().getItems()) {
                    invoice.append("- ")
                            .append(item.getProduct().getName())
                            .append(" x").append(item.getQuantity())
                            .append(" = ").append(numberFormat.format(item.getTotal())).append(" đ\n");
                }

                invoice.append("\nTổng cộng: ").append(numberFormat.format(Cart.getInstance().getTotal())).append(" đ\n");
                invoice.append("-----------------------------\n");
                invoice.append("Cảm ơn bạn đã mua sắm!");

                JTextArea textArea = new JTextArea(invoice.toString());
                textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                textArea.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "️ Hóa đơn",
                        JOptionPane.INFORMATION_MESSAGE);

                Cart.getInstance().clear();
                loadCartItems();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi in hóa đơn: " + ex.getMessage());
        }
    }

    private void showStatistics() {
        String[] options = {"Theo ngày", "Theo tuần", "Theo tháng"};
        int choice = JOptionPane.showOptionDialog(this, "Chọn loại thống kê:", "📊 Chọn chiến lược",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        SalesStrategy strategy = switch (choice) {
            case 0 ->
                new shopapp.strategy.DailySalesStrategy();
            case 1 ->
                new shopapp.strategy.WeeklySalesStrategy();
            case 2 ->
                new shopapp.strategy.MonthlySalesStrategy();
            default ->
                null;
        };

        if (strategy == null) {
            return;
        }

        SalesContext context = new SalesContext();
        context.setStrategy(strategy);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        try (Connection conn = DBUtil.getConnection(); ResultSet rs = context.executeStrategy(conn)) {
            StringBuilder sb = new StringBuilder("📈 Kết quả thống kê:\n\n");
            while (rs.next()) {
                if (choice == 0) {
                    sb.append("Ngày: ").append(rs.getDate("Date"));
                } else if (choice == 1) {
                    sb.append("Tuần: ").append(rs.getInt("Week")).append(" - Năm: ").append(rs.getInt("Year"));
                } else {
                    sb.append("Tháng: ").append(rs.getInt("Month")).append(" - Năm: ").append(rs.getInt("Year"));
                }

                sb.append(" | Tổng bán: ").append(rs.getInt("TotalSold")).append(" sản phẩm");
                sb.append(" | Tổng tiền: ").append(currencyFormat.format(rs.getDouble("TotalRevenue")));
                sb.append("\n");
            }

            JOptionPane.showMessageDialog(this, sb.toString(), "📊 Kết quả thống kê", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi thống kê: " + e.getMessage());
        }
    }

    private void showInvoiceHistory() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = """
                SELECT i.InvoiceID, i.CustomerName, i.Email, i.Phone, i.Address,
                       SUM(d.Price * d.Quantity) AS Total, i.CreatedAt
                FROM Invoices i JOIN InvoiceDetails d ON i.InvoiceID = d.InvoiceID
                GROUP BY i.InvoiceID, i.CustomerName, i.Email, i.Phone, i.Address, i.CreatedAt
                ORDER BY i.CreatedAt DESC
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            StringBuilder history = new StringBuilder("LỊCH SỬ HÓA ĐƠN:\n\n");
            while (rs.next()) {
                history.append("Mã HD: ").append(rs.getInt("InvoiceID")).append("\n")
                        .append("Khách: ").append(rs.getString("CustomerName")).append("\n")
                        .append("Email: ").append(rs.getString("Email")).append("\n")
                        .append("SĐT: ").append(rs.getString("Phone")).append("\n")
                        .append("Địa chỉ: ").append(rs.getString("Address")).append("\n")
                        .append("Tổng tiền: ").append(numberFormat.format(rs.getDouble("Total"))).append(" đ\n")
                        .append("Ngày tạo: ").append(rs.getTimestamp("CreatedAt")).append("\n")
                        .append("-------------------------\n");
            }

            JTextArea textArea = new JTextArea(history.toString());
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textArea.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), " Lịch sử hóa đơn",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xem lịch sử hóa đơn: " + e.getMessage());
        }
    }

}
