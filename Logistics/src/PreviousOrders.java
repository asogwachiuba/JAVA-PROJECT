import javax.swing.JFrame;

import Database.CustomerDB;
import Models.Order;
import Models.ProductOrdered;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PreviousOrders extends JFrame {
    private final DefaultListModel<Order> ordersModel;
    private final DefaultListModel<ProductOrdered> productsModel;

    public PreviousOrders() {
        // Set frame properties
        setTitle("Previous Orders");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize models
        ordersModel = new DefaultListModel<>();
        productsModel = new DefaultListModel<>();

        // Create UI components
        JList<Order> ordersList = new JList<>(ordersModel);
        JList<ProductOrdered> productsList = new JList<>(productsModel);

        // Fetch and populate orders
        populateOrders();

        // Configure orders panel
        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.add(new JLabel("Orders", SwingConstants.CENTER), BorderLayout.NORTH);
        ordersPanel.add(new JScrollPane(ordersList), BorderLayout.CENTER);

        // Configure products panel
        JPanel productsPanel = new JPanel(new BorderLayout());
        productsPanel.add(new JLabel("Products in Order", SwingConstants.CENTER), BorderLayout.NORTH);
        productsPanel.add(new JScrollPane(productsList), BorderLayout.CENTER);

        // Add listener to update products based on selected order
        ordersList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && ordersList.getSelectedIndex() != -1) {
                Order selectedOrder = ordersList.getSelectedValue();
                populateProducts(selectedOrder.getId());
            }
        });

        // Split panel for orders and products
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ordersPanel, productsPanel);
        splitPane.setDividerLocation(400);

        // Add components to the frame
        add(splitPane, BorderLayout.CENTER);

        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void populateOrders() {
        try {
            List<Order> orders = CustomerDB.getCustomerOrders();
            ordersModel.clear();
            orders.forEach(ordersModel::addElement);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching orders: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateProducts(int orderId) {
        try {
            List<ProductOrdered> products = CustomerDB.getProductsInOrder(orderId);
            productsModel.clear();
            products.forEach(productsModel::addElement);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching products: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

