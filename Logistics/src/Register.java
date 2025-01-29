import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Database.AuthenticationDB;

public class Register extends JFrame {

    public Register() {
        // Textfield panel panel (Scrollable content)
        JPanel textFieldPanel = new JPanel();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        textFieldPanel.setLayout(new GridLayout(0, 2, 20, 20)); // 2 columns, horizontal and vertical spacing
        textFieldPanel.setBorder(new EmptyBorder(20, 20, 10, 20)); // Padding for the panel

        // Fields list to store references to all text fields
        List<JTextField> textFields = new ArrayList<>();

        // Adding fields
        textFieldPanel.add(createFieldPanel("First Name", textFields));
        textFieldPanel.add(createFieldPanel("Last Name", textFields));

        textFieldPanel.add(createFieldPanel("Email", textFields));
        textFieldPanel.add(createFieldPanel("Phone Number", textFields));

        textFieldPanel.add(createFieldPanel("Address", textFields));
        textFieldPanel.add(createFieldPanel("Zip Code", textFields));

        textFieldPanel.add(createFieldPanel("Password", textFields, true));
        textFieldPanel.add(createFieldPanel("Confirm Password", textFields, true));

        // Adding the text fields to the main panel
        mainPanel.add(textFieldPanel, BorderLayout.NORTH);

        // Adding checkboxes
        JPanel checkboxPanel = createCheckboxRow();
        JCheckBox driverCheckBox = (JCheckBox) checkboxPanel.getComponent(0);
        JCheckBox customerCheckBox = (JCheckBox) checkboxPanel.getComponent(1);
        JCheckBox schedulerCheckBox = (JCheckBox) checkboxPanel.getComponent(2);
        mainPanel.add(checkboxPanel, BorderLayout.CENTER);

        // Adding truck fields and register button
        JPanel truckRowPanel = createTruckRow(textFields, driverCheckBox, customerCheckBox, schedulerCheckBox);
        mainPanel.add(truckRowPanel, BorderLayout.SOUTH);

        // Wrap the main panel in a scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Add scroll pane to the frame
        this.add(scrollPane);

        // Create frame
        this.setTitle("Register");
        this.setBounds(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    private JPanel createFieldPanel(String labelText, List<JTextField> textFields) {
        return createFieldPanel(labelText, textFields, false);
    }

    private JPanel createFieldPanel(String labelText, List<JTextField> textFields, boolean isPasswordField) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BorderLayout(20, 20)); // Vertical spacing between label and field
        fieldPanel.setBorder(new EmptyBorder(4, 0, 0, 5)); // Padding around the panel

        // Label setup
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        label.setHorizontalAlignment(SwingConstants.LEFT);

        // Text field setup
        JTextField textField = isPasswordField ? new JPasswordField(15) : new JTextField(15);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textFields.add(textField); // Add to the list for validation

        // Add label and text field to the panel
        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(textField, BorderLayout.CENTER);

        return fieldPanel;
    }

    private JPanel createCheckboxRow() {
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridLayout(1, 3, 20, 0)); // 3 checkboxes in one row

        // Create checkboxes
        JCheckBox driverCheckBox = new JCheckBox("Driver");
        JCheckBox customerCheckBox = new JCheckBox("Customer");
        JCheckBox schedulerCheckBox = new JCheckBox("Scheduler");

        // Add checkboxes to the panel
        checkboxPanel.add(driverCheckBox);
        checkboxPanel.add(customerCheckBox);
        checkboxPanel.add(schedulerCheckBox);
        checkboxPanel.setBorder(new EmptyBorder(20, 20, 0, 40));

        return checkboxPanel;
    }

    private JPanel createTruckRow(List<JTextField> textFields, JCheckBox driverCheckBox, JCheckBox customerCheckBox, JCheckBox schedulerCheckBox) {
        JPanel truckRowPanel = new JPanel();
        truckRowPanel.setLayout(new BorderLayout());

        // Truck Registration Number
        JTextField truckRegTextField = new JTextField(15);
        textFields.add(truckRegTextField);

        // Truck Capacity
        JSpinner truckCapacitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        // Create a panel for truck fields
        JPanel fieldsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        fieldsPanel.add(createLabeledComponent("Truck Capacity", truckCapacitySpinner));
        fieldsPanel.add(createLabeledComponent("Truck Reg Number", truckRegTextField));
        fieldsPanel.setBorder(new EmptyBorder(20, 20, 30, 20));

        // Register button
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Ensure one checkbox is selected
                    String role = null;
                    if (driverCheckBox.isSelected()) {
                        role = "Driver";
                    } else if (customerCheckBox.isSelected()) {
                        role = "Customer";
                    } else if (schedulerCheckBox.isSelected()) {
                        role = "Scheduler";
                    } else {
                        JOptionPane.showMessageDialog(Register.this, "Select at least one role (Driver, Customer, or Scheduler)!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    if(driverCheckBox.isSelected()) {
                    	// Validate all text fields
                        for (JTextField field : textFields) {
                            if (field.getText().trim().isEmpty()) {
                                JOptionPane.showMessageDialog(Register.this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        // Additional validation for Truck Capacity and Truck Registration Number for Drivers
                        try {
                            // Fetching and validating truck capacity
                            int truckCapacity = (int) truckCapacitySpinner.getValue();
                            if (truckCapacity <= 0) {
                                JOptionPane.showMessageDialog(Register.this, "Truck Capacity must be greater than 0!", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            // Fetching and validating truck registration number
                            String truckRegistrationText = textFields.get(8).getText().trim();
                            if (truckRegistrationText.isEmpty()) {
                                JOptionPane.showMessageDialog(Register.this, "Truck Registration Number cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            
                            // Attempt to parse truck registration number
                            int truckRegistration;
                            try {
                                truckRegistration = Integer.parseInt(truckRegistrationText);
                            } catch (NumberFormatException numericError) {
                                JOptionPane.showMessageDialog(Register.this, "Truck Registration Number must be numeric!", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                        } catch (Exception error) {
                            JOptionPane.showMessageDialog(Register.this, "Unexpected error: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }

                    } else {
                    	 // Validate only the general fields (excluding Truck Capacity and Registration Number) for other roles
                        for (JTextField field : textFields) {
                            if ( !field.equals(textFields.get(8))) { // Skip validation for truck-specific fields
                                if (field.getText().trim().isEmpty()) {
                                    JOptionPane.showMessageDialog(Register.this, "All required fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                        }
                    }

                    // Collect data from text fields
                    String firstName = textFields.get(0).getText();
                    String lastName = textFields.get(1).getText();
                    String email = textFields.get(2).getText();
                    String phoneNumber = textFields.get(3).getText();
                    String address = textFields.get(4).getText();
                    int zipCode = Integer.parseInt(textFields.get(5).getText());
                    String password = textFields.get(6).getText();
                    String confirmPassword = textFields.get(7).getText();

                    if (!password.equals(confirmPassword)) {
                        JOptionPane.showMessageDialog(Register.this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Driver-specific fields
                    Integer truckCapacity = null;
                    Integer truckRegistrationNo = null;
                    if ("Driver".equals(role)) {
                        truckCapacity = (Integer) truckCapacitySpinner.getValue();
                        truckRegistrationNo = textFields.get(8).getText().isEmpty() ? null : Integer.parseInt(textFields.get(8).getText());
                    }

                    // Call the register method
                    boolean success = new AuthenticationDB().register(
                        email, password, role, firstName, lastName, phoneNumber, address, zipCode, truckCapacity, truckRegistrationNo
                    );

                    if (success) {
                        JOptionPane.showMessageDialog(Register.this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // Close the registration form
                        new Login();
                    } else {
                        JOptionPane.showMessageDialog(Register.this, "Registration failed! Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Register.this, "Invalid input! Please enter valid data.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(Register.this, "An unexpected error occurred!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(registerButton);

        // Combine panels
        truckRowPanel.add(fieldsPanel, BorderLayout.NORTH);
        truckRowPanel.add(buttonPanel, BorderLayout.SOUTH);
        return truckRowPanel;
    }

    private JPanel createLabeledComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

}
