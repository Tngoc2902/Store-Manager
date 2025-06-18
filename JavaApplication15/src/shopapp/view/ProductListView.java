package shopapp.view;

import shopapp.dao.ProductDAO;
import shopapp.model.*;
import shopapp.observer.CartObserver;
import shopapp.utils.DBUtil;
import shopapp.utils.UndoStack;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

public class ProductListView extends JFrame implements CartObserver {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnAddToCart, btnViewCart, btnAdd, btnEdit, btnDelete, btnUndo;
    private JTextField searchField;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public ProductListView() {
        initComponents();
        initActions();
        Cart.getInstance().addObserver(this); // Đăng ký observer
        reloadProducts();
    }

    private void initComponents() {
        setTitle("🛍️Danh sách sản phẩm - Ứng dụng mua sắm");
        setSize(1500, 950);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(248, 249, 250));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("📦 Danh sách sản phẩm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(new Color(52, 58, 64));
        mainPanel.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Tên sản phẩm", "Giá (₫)", "Tồn kho"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(25, 135, 84));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(204, 255, 229));
        table.setGridColor(new Color(220, 220, 220));
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editProduct();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(new Color(248, 249, 250));
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton btnSearch = createButton("Tìm sản phẩm");
        btnSearch.setBackground(new Color(0, 184, 148));
        btnSearch.addActionListener(e -> searchProduct());

        topPanel.add(searchField, BorderLayout.CENTER);
        topPanel.add(btnSearch, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.BEFORE_FIRST_LINE);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        bottomPanel.setBackground(new Color(248, 249, 250));
        btnAddToCart = createButton("Thêm vào giỏ hàng");
        btnViewCart = createButton("Giỏ hàng");
        btnAdd = createButton("Thêm sản phẩm");
        btnEdit = createButton("Sửa sản phẩm");
        btnDelete = createButton("Xóa sản phẩm");
        btnUndo = createButton("️ Hoàn tác");

        bottomPanel.add(btnAddToCart);
        bottomPanel.add(btnViewCart);
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnUndo);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void initActions() {
        btnAddToCart.addActionListener(e -> addToCart());
        btnViewCart.addActionListener(e -> new CartView().setVisible(true));
        btnAdd.addActionListener(e -> addProduct());
        btnEdit.addActionListener(e -> editProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnUndo.addActionListener(e -> undo());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                searchProduct();
            }
        });
    }
    
//    private void loadCartItems() {
//        model.setRowCount(0);
//        for (CartItem item : Cart.getInstance().getItems()) {
//            model.addRow(new Object[]{
//                    item.getProduct().getName(),
//                    numberFormat.format(item.getProduct().getPrice()) + " đ",
//                    item.getQuantity(),
//                    numberFormat.format(item.getTotal()) + " đ"
//            });
//        }
//    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(13, 110, 253));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void reloadProducts() {
        try {
            model.setRowCount(0);
            Connection conn = DBUtil.getConnection();
            ProductDAO dao = new ProductDAO(conn);
            List<Product> products = dao.getAllProducts();
            for (Product p : products) {
                model.addRow(new Object[]{p.getId(), p.getName(), currencyFormat.format(p.getPrice()), p.getQuantity()});
            }
            UndoStack.push(products);
            highlightLowStock();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải sản phẩm: " + e.getMessage());
        }
    }

    private void highlightLowStock() {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                try {
                    int stock = Integer.parseInt(tbl.getModel().getValueAt(row, 3).toString());
                    if (stock < 5) {
                        c.setBackground(new Color(255, 153, 153));
                    } else if (stock < 10) {
                        c.setBackground(new Color(255, 255, 153));
                    } else {
                        c.setBackground(isSelected ? tbl.getSelectionBackground() : Color.WHITE);
                    }
                } catch (Exception ex) {
                    c.setBackground(isSelected ? tbl.getSelectionBackground() : Color.WHITE);
                }
                return c;
            }
        });
    }

    private void searchProduct() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        String keyword = searchField.getText();
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
    }

    private void addToCart() {
        int row = table.getSelectedRow();
        if (row != -1) {
            int modelRow = table.convertRowIndexToModel(row);
            int id = (int) model.getValueAt(modelRow, 0);
            String name = (String) model.getValueAt(modelRow, 1);
            try {
                Number price = currencyFormat.parse(model.getValueAt(modelRow, 2).toString());
                Cart.getInstance().addItem(new Product(id, name, price.doubleValue(), 0), 1);
                JOptionPane.showMessageDialog(this, "Đã thêm vào giỏ hàng!");
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Lỗi định dạng giá: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm.");
        }
    }

    private void addProduct() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField quantityField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Tên sản phẩm"));
        panel.add(nameField);
        panel.add(new JLabel("Giá:"));
        panel.add(priceField);
        panel.add(new JLabel("Tồn kho:"));
        panel.add(quantityField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Thêm sản phẩm", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().replaceAll("[^\\d.]", ""));
                int quantity = Integer.parseInt(quantityField.getText().trim());
                new ProductDAO(DBUtil.getConnection()).addProduct(new Product(0, name, price, quantity));
                reloadProducts();
            } catch (NumberFormatException | SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi thêm sản phẩm: " + e.getMessage());
            }
        }
    }

    private void editProduct() {
        int row = table.getSelectedRow();
        if (row != -1) {
            int modelRow = table.convertRowIndexToModel(row);
            JTextField nameField = new JTextField(model.getValueAt(modelRow, 1).toString());
            JTextField priceField = new JTextField(model.getValueAt(modelRow, 2).toString());
            JTextField quantityField = new JTextField(model.getValueAt(modelRow, 3).toString());

            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.add(new JLabel("Tên sản phẩm:"));
            panel.add(nameField);
            panel.add(new JLabel("Giá:"));
            panel.add(priceField);
            panel.add(new JLabel("Số lượng tồn kho:"));
            panel.add(quantityField);

            int id = (int) model.getValueAt(modelRow, 0);
            int result = JOptionPane.showConfirmDialog(this, panel, "Sửa sản phẩm", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText().trim();
                    Number priceNumber = currencyFormat.parse(priceField.getText());
                    double price = priceNumber.doubleValue();
                    int quantity = Integer.parseInt(quantityField.getText().trim());
                    new ProductDAO(DBUtil.getConnection()).updateProduct(new Product(id, name, price, quantity));
                    reloadProducts();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Lỗi sửa sản phẩm: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm.");
        }
    }

    private void deleteProduct() {
        int row = table.getSelectedRow();
        if (row != -1) {
            int modelRow = table.convertRowIndexToModel(row);
            int id = (int) model.getValueAt(modelRow, 0);
            String name = model.getValueAt(modelRow, 1).toString();
            int confirm = JOptionPane.showConfirmDialog(this, "Xóa sản phẩm \"" + name + "\"?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    new ProductDAO(DBUtil.getConnection()).deleteProduct(id);
                    reloadProducts();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Lỗi xóa: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm.");
        }
    }

    private void undo() {
        if (UndoStack.size() < 2) {
            JOptionPane.showMessageDialog(this, "Không có hành động nào để hoàn tác.");
            return;
        }
        UndoStack.pop();
        List<Product> previous = UndoStack.peek();
        model.setRowCount(0);
        for (Product p : previous) {
            model.addRow(new Object[]{p.getId(), p.getName(), currencyFormat.format(p.getPrice()), p.getQuantity()});
        }
        highlightLowStock();
    }

    @Override
    public void onCartUpdated() {
        System.out.println("[Observer] Giỏ hàng đã thay đổi!");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductListView().setVisible(true));
    }
}
