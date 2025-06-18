package shopapp.view;

import shopapp.utils.DBUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class InvoiceHistoryView extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearchCustomer;
    private JFormattedTextField txtFromDate, txtToDate;
    private NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    public InvoiceHistoryView() {
        setTitle("Lịch sử hóa đơn bán hàng");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Lịch sử hóa đơn đã in");
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.NORTH);

        // ==== Tìm kiếm và lọc ====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearchCustomer = new JTextField(15);
        txtSearchCustomer.setToolTipText("Tìm theo tên khách hàng");

        txtFromDate = new JFormattedTextField(java.time.LocalDate.now().minusMonths(1).toString());
        txtToDate = new JFormattedTextField(java.time.LocalDate.now().toString());
        txtFromDate.setColumns(8);
        txtToDate.setColumns(8);
        JButton btnReload = new JButton("🔄 Tải lại");
        btnReload.addActionListener(e -> loadInvoiceData());

        searchPanel.add(new JLabel("Từ ngày:"));
        searchPanel.add(txtFromDate);
        searchPanel.add(new JLabel("Đến ngày:"));
        searchPanel.add(txtToDate);
        searchPanel.add(new JLabel("Khách hàng:"));
        searchPanel.add(txtSearchCustomer);
        searchPanel.add(btnReload);

        panel.add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);

        // ==== Bảng ====
        model = new DefaultTableModel(new String[]{
                "Mã HD", "Tên KH", "Email", "SĐT", "Địa chỉ", "Tổng tiền", "Ngày tạo"
        }, 0);
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(850, 350));
        panel.add(scrollPane, BorderLayout.CENTER);

        // ==== Sự kiện click ====
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int invoiceID = (int) model.getValueAt(row, 0);
                    showInvoiceDetails(invoiceID);
                }
            }
        });

        add(panel);
        loadInvoiceData();
    }

    private void loadInvoiceData() {
        model.setRowCount(0);
        try (Connection conn = DBUtil.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT i.InvoiceID, i.CustomerName, i.Email, i.Phone, i.Address, " +
                "SUM(d.Price * d.Quantity) AS TotalAmount, i.CreatedAt " +
                "FROM Invoices i " +
                "JOIN InvoiceDetails d ON i.InvoiceID = d.InvoiceID "
            );

            boolean hasCondition = false;
            if (!txtSearchCustomer.getText().trim().isEmpty()) {
                sql.append("WHERE i.CustomerName LIKE ? ");
                hasCondition = true;
            }

            if (!txtFromDate.getText().isEmpty() && !txtToDate.getText().isEmpty()) {
                sql.append(hasCondition ? "AND " : "WHERE ");
                sql.append("i.CreatedAt BETWEEN ? AND ? ");
            }

            sql.append("GROUP BY i.InvoiceID, i.CustomerName, i.Email, i.Phone, i.Address, i.CreatedAt ");
            sql.append("ORDER BY i.CreatedAt DESC");

            PreparedStatement stmt = conn.prepareStatement(sql.toString());

            int index = 1;
            if (!txtSearchCustomer.getText().trim().isEmpty()) {
                stmt.setString(index++, "%" + txtSearchCustomer.getText().trim() + "%");
            }

            if (!txtFromDate.getText().isEmpty() && !txtToDate.getText().isEmpty()) {
                stmt.setString(index++, txtFromDate.getText().trim() + " 00:00:00");
                stmt.setString(index, txtToDate.getText().trim() + " 23:59:59");
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("InvoiceID"),
                        rs.getString("CustomerName"),
                        rs.getString("Email"),
                        rs.getString("Phone"),
                        rs.getString("Address"),
                        numberFormat.format(rs.getDouble("TotalAmount")) + " đ",
                        rs.getTimestamp("CreatedAt")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử hóa đơn: " + e.getMessage());
        }
    }

    private void showInvoiceDetails(int invoiceID) {
        try (Connection conn = DBUtil.getConnection()) {
            StringBuilder details = new StringBuilder();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT p.Name, d.Quantity, d.Price " +
                "FROM InvoiceDetails d " +
                "JOIN Products p ON d.ProductID = p.ProductID " +
                "WHERE d.InvoiceID = ?"
            );
            stmt.setInt(1, invoiceID);
            ResultSet rs = stmt.executeQuery();

            details.append("Chi tiết hóa đơn #" + invoiceID + ":\n");
            details.append("---------------------------------------\n");
            while (rs.next()) {
                String productName = rs.getString("Name");
                int qty = rs.getInt("Quantity");
                double price = rs.getDouble("Price");
                double total = qty * price;
                details.append(String.format("- %s x%d = %s đ\n", productName, qty, numberFormat.format(total)));
            }

            JTextArea area = new JTextArea(details.toString());
            area.setEditable(false);
            area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Chi tiết hóa đơn", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi hiển thị chi tiết: " + e.getMessage());
        }
    }
}
