import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import Database.CustomerDB;
import Models.Session;
import Models.SessionTracker;
import Models.CartProduct;
import Models.Product;

public class CustomerDashboard extends JFrame {
    private Session sessionData;
    private DefaultListModel<Product> availableProductsModel;
    private DefaultListModel<CartProduct> cartProductsModel;
    private JList<Product> availableProductsList;
    private JList<CartProduct> cartProductsList;

    private JTextField quantityTextField;
    private JButton updateQuantityButton;

    public CustomerDashboard() {
        SessionTracker.printActiveSessions();
        sessionData = SessionTracker.getSession();

        availableProductsModel = new DefaultListModel<>();
        cartProductsModel = new DefaultListModel<>();

        // Fetch products from the database
        List<Product> availableProducts = getAvailableProductsFromDB();
        availableProducts.forEach(availableProductsModel::addElement);

        // List setup
        availableProductsList = new JList<>(availableProductsModel);
        cartProductsList = new JList<>(cartProductsModel);

        // Add the lists to scroll panes
        JScrollPane availableScrollPane = new JScrollPane(availableProductsList);
        JScrollPane cartScrollPane = new JScrollPane(cartProductsList);

        // Set list titles
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.add(new JLabel("Available Products", SwingConstants.CENTER), BorderLayout.NORTH);
        availablePanel.add(availableScrollPane, BorderLayout.CENTER);

        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.add(new JLabel("Products Cart", SwingConstants.CENTER), BorderLayout.NORTH);
        cartPanel.add(cartScrollPane, BorderLayout.CENTER);

        // Panel for lists
        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        listsPanel.add(availablePanel);
        listsPanel.add(cartPanel);

        // Panel for buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 3, 10, 10));

        JButton addButton = new JButton("Add ->");
        addButton.addActionListener(e -> addProductToCart());

        JButton removeButton = new JButton("Remove <-");
        removeButton.addActionListener(e -> removeProductFromCart());

        JButton productInfoButton = new JButton("Product Info");
        productInfoButton.addActionListener(e -> showProductInfo());

        buttonsPanel.add(addButton);
        buttonsPanel.add(removeButton);
        buttonsPanel.add(productInfoButton);

        // Update quantity panel
        JPanel updateQuantityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        quantityTextField = new JTextField(5);
        quantityTextField.setToolTipText("Enter new quantity");

        updateQuantityButton = new JButton("Update Quantity");
        updateQuantityButton.addActionListener(e -> updateQuantityInCart());

        updateQuantityPanel.add(new JLabel("New Quantity: "));
        updateQuantityPanel.add(quantityTextField);
        updateQuantityPanel.add(updateQuantityButton);

        // Add components to the frame
        add(listsPanel, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.CENTER);
        add(updateQuantityPanel, BorderLayout.EAST);

        // Panel for delivery details and actions
        JPanel deliveryPanel = new JPanel(new BorderLayout());
        JPanel deliveryInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Text fields
        JTextField deliveryDateTextField = new JTextField(10);
        deliveryDateTextField.setToolTipText("Enter delivery date (e.g., YYYY-MM-DD)");
        JTextField deliveryAddressTextField = new JTextField(15);
        deliveryAddressTextField.setToolTipText("Enter delivery address");

        // Buttons
        JButton previousOrdersButton = new JButton("Previous Orders");
        previousOrdersButton.addActionListener(e -> showPreviousOrders());
        
        JButton editProfileButton = new JButton("Edit Profile");
        editProfileButton.addActionListener(e -> {new EditProfile();});

        JButton createOrderButton = new JButton("Create New Order");
        createOrderButton.addActionListener(
                e -> createNewOrder(deliveryDateTextField.getText(), deliveryAddressTextField.getText()));

        deliveryInfoPanel.add(new JLabel("Delivery Date: "));
        deliveryInfoPanel.add(deliveryDateTextField);
        deliveryInfoPanel.add(new JLabel("Delivery Address: "));
        deliveryInfoPanel.add(deliveryAddressTextField);
        deliveryPanel.add(deliveryInfoPanel, BorderLayout.NORTH);
        deliveryPanel.add(previousOrdersButton, BorderLayout.WEST);
        deliveryPanel.add(editProfileButton, BorderLayout.CENTER);
        deliveryPanel.add(createOrderButton, BorderLayout.EAST);

        add(deliveryPanel, BorderLayout.SOUTH);

        // Frame settings
        this.setTitle("WELCOME " + sessionData.getFirstName() + " " + sessionData.getLastName());
        this.setBounds(0, 0, Constants.WINDOW_WIDTH+200, Constants.WINDOW_HEIGHT-100);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private List<Product> getAvailableProductsFromDB() {
        List<Product> products = new ArrayList<>();
        try {
             products = CustomerDB.getAllProducts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching products from the database: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return products;
    }

    private void createNewOrder(String deliveryDate, String deliveryAddress) {
        if (deliveryDate.isEmpty() || deliveryAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in both delivery date and address.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cartProductsModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty. Please add products to the cart before creating an order.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
        	List<CartProduct> cartProducts = new ArrayList<>();
        	int deliveryWeight = 0;
            for (int i = 0; i < cartProductsModel.getSize(); i++) {
            	CartProduct product = cartProductsModel.getElementAt(i);
                cartProducts.add(product); 
                deliveryWeight += (product.getQuantity() * product.getProduct().getWeightPerUnit());
            }
           boolean isSuccessful = CustomerDB.placeOrder(sessionData.getId(), cartProducts, deliveryDate, deliveryAddress, deliveryWeight);
           if(isSuccessful) {
               JOptionPane.showMessageDialog(this, "Your order was completed successfully.", "Order Successful", JOptionPane.INFORMATION_MESSAGE);
               new PreviousOrders();
           } else {
        	   JOptionPane.showMessageDialog(this, "Order not successful. Try again.", "Order Not Successful", JOptionPane.ERROR_MESSAGE);
           }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creating order: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProductToCart() {
        int selectedIndex = availableProductsList.getSelectedIndex();
        if (selectedIndex != -1) {
            Product selectedProduct = availableProductsModel.getElementAt(selectedIndex);
            cartProductsModel.addElement(new CartProduct(selectedProduct, 1));
            availableProductsModel.remove(selectedIndex);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to add to the cart.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeProductFromCart() {
        int selectedIndex = cartProductsList.getSelectedIndex();
        if (selectedIndex != -1) {
            CartProduct selectedCartProduct = cartProductsModel.getElementAt(selectedIndex);
            availableProductsModel.addElement(selectedCartProduct.getProduct());
            cartProductsModel.remove(selectedIndex);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to remove from the cart.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showProductInfo() {
        int selectedIndex = availableProductsList.getSelectedIndex();
        if (selectedIndex != -1) {
            Product selectedProduct = availableProductsModel.getElementAt(selectedIndex);
            JOptionPane.showMessageDialog(this,
                    "Product: " + selectedProduct.getName() + "\nID: " + selectedProduct.getId()
                            + "\nMeasuring Format: " + selectedProduct.getMeasuringFormat() + "\nQuantity Available: "
                            + selectedProduct.getQuantityAvailable() + "\nWeight per Unit: "
                            + selectedProduct.getWeightPerUnit() + " kg",
                    "Product Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product to view info.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateQuantityInCart() {
		int selectedIndex = cartProductsList.getSelectedIndex();
		if (selectedIndex != -1) {
			String newQuantityText = quantityTextField.getText();
			
			try {
				int newQuantity = Integer.parseInt(newQuantityText);
				CartProduct selectedCartProduct = cartProductsModel.getElementAt(selectedIndex);
				int availableQuantity = selectedCartProduct.getProduct().getQuantityAvailable();
				if (newQuantity <= 0) {
					JOptionPane.showMessageDialog(this, "Please enter a valid quantity greater than 0.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				} else if (newQuantity > availableQuantity) {
					JOptionPane.showMessageDialog(this, "Quantity available(" + availableQuantity + ") is less than selected quantity", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				selectedCartProduct.setQuantity(newQuantity);
				cartProductsModel.setElementAt(selectedCartProduct, selectedIndex);
				quantityTextField.setText(""); // Clear the text field after updating
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Please enter a valid number for the quantity.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Please select a product to update.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}


    private void showPreviousOrders() {
       new PreviousOrders();
    }
}
