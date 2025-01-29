import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Database.AuthenticationDB;
import Models.Session;
import Models.SessionTracker;

public class EditProfile extends JFrame {
    private JTextField firstNameField, lastNameField, emailField, phoneField, addressField, zipCodeField, truckRegField;
    private JPasswordField oldPasswordField, newPasswordField;
    private JSpinner truckCapacitySpinner;
    private JPanel truckFieldsPanel;
    private JButton saveChangesButton;
    private String userRole;
    private Session session;

    public EditProfile() {
        session = SessionTracker.getSession();
        this.userRole = session.getRole();

        // Main panel setup
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 20, 20));

        // Populate fields
        firstNameField = createField("First Name", session.getFirstName(), formPanel);
        lastNameField = createField("Last Name", session.getLastName(), formPanel);
        emailField = createField("Email", session.getEmail(), formPanel);
        phoneField = createField("Phone Number", session.getPhoneNumber(), formPanel);
        addressField = createField("Address", session.getAddress(), formPanel);
        zipCodeField = createField("Zip Code", String.valueOf(session.getZipCode()), formPanel);

        // Password fields
        oldPasswordField = createPasswordField("Old Password", formPanel);
        newPasswordField = createPasswordField("New Password", formPanel);

        // Save changes button
        saveChangesButton = new JButton("Save Changes");
        saveChangesButton.addActionListener(e -> handleSaveChanges(session));

        if ("Driver".equalsIgnoreCase(userRole)) {
            truckFieldsPanel = new JPanel(new GridLayout(0, 2, 20, 20));
            truckFieldsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

            truckCapacitySpinner = new JSpinner(new SpinnerNumberModel(
                Integer.parseInt(session.getTruckCapacity()), 1, 1000, 1
            ));
            truckRegField = new JTextField(session.getTruckRegistrationNumber());
            truckFieldsPanel.add(createLabeledComponent("Truck Capacity", truckCapacitySpinner));
            truckFieldsPanel.add(createLabeledComponent("Truck Reg Number", truckRegField));
        }

        // Add components to the main panel
        mainPanel.add(formPanel, BorderLayout.NORTH);
        if ("Driver".equalsIgnoreCase(userRole)) {
            mainPanel.add(truckFieldsPanel, BorderLayout.CENTER);
        }
        mainPanel.add(saveChangesButton, BorderLayout.SOUTH);

        // Frame setup
        this.add(mainPanel);
        this.setTitle("Edit Profile");
        this.setSize(600, 500);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    private JTextField createField(String label, String value, JPanel parentPanel) {
        JTextField field = new JTextField(value, 15);
        parentPanel.add(createLabeledComponent(label, field));
        return field;
    }

    private JPasswordField createPasswordField(String label, JPanel parentPanel) {
        JPasswordField field = new JPasswordField(15);
        parentPanel.add(createLabeledComponent(label, field));
        return field;
    }

    private JPanel createLabeledComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private void handleSaveChanges(Session session) {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            int zipCode = Integer.parseInt(zipCodeField.getText().trim());
            String oldPassword = new String(oldPasswordField.getPassword()).trim();
            String newPassword = new String(newPasswordField.getPassword()).trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Integer truckCapacity = null;
            String truckRegNumber = null;

            if ("Driver".equalsIgnoreCase(userRole)) {
                truckCapacity = (int) truckCapacitySpinner.getValue();
                truckRegNumber = truckRegField.getText().trim();

                if (truckRegNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Truck Registration Number cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            boolean success = AuthenticationDB.editProfile(
                firstName, lastName, email, phone, address, zipCode, truckCapacity, truckRegNumber, oldPassword, newPassword
            );

            if (success) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input in numeric fields!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}