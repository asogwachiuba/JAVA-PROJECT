import java.awt.*;
import java.awt.event.*;
import java.util.Collections;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Database.AuthenticationDB;

public class Login extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private ButtonGroup roleGroup;

    public Login() {
        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create and add the title
        JLabel titleLabel = new JLabel(
                "<html>Please log in to your account to continue accessing your personalized dashboard, features, and services.<br>"
                        + "Enter your Email and Password to log in.<br>"
                        + "Select your role: Scheduler, Driver, or Consumer.</html>",
                SwingConstants.CENTER);
        titleLabel.setFont(new Font(Constants.font, Font.BOLD, Constants.regular));
        titleLabel.setBorder(new EmptyBorder(0, 100, 0, 100));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Create form panel for email, password, and checkboxes
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add email field
        formPanel.add(createFieldPanel("Email"));

        // Add password field
        formPanel.add(createFieldPanel("Password", true));

        // Add forgot password link
        JLabel forgotPasswordLabel = new JLabel("<html><a href='#'>Forgot Password?</a></html>", SwingConstants.CENTER);
        forgotPasswordLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        forgotPasswordLabel.setForeground(Color.BLUE);
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new ForgotPassword();
            }
        });
//        formPanel.add(forgotPasswordLabel);

        // Add row with checkboxes
        formPanel.add(createCheckboxRow());

        // Add form panel to the center
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Add bottom panel to the main panel
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        // Add main panel to the frame
        this.add(mainPanel);

        // Frame settings
        this.setTitle("LOGIN");
        this.setBounds(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private JPanel createBottomPanel() {
        JLabel unclickableText = new JLabel("You don't have an account?");
        unclickableText.setFont(new Font(Constants.font, Font.PLAIN, Constants.medium));

        JLabel clickableText = new JLabel("CLICK HERE");
        clickableText.setFont(new Font(Constants.font, Font.BOLD, Constants.medium));
        clickableText.setForeground(Color.BLUE);
        clickableText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Register();
            }
        });

        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font(Constants.font, Font.BOLD, Constants.medium));

        loginButton.addActionListener(e -> handleLogin());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        bottomPanel.add(unclickableText);
        bottomPanel.add(clickableText);
        bottomPanel.add(loginButton);
        return bottomPanel;
    }

    private JPanel createFieldPanel(String labelText) {
        return createFieldPanel(labelText, false);
    }

    private JPanel createFieldPanel(String labelText, boolean isPasswordField) {
        JPanel fieldPanel = new JPanel(new FlowLayout());
        fieldPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Constants.font, Font.PLAIN, Constants.regular));
        fieldPanel.add(label);

        if (isPasswordField) {
            passwordField = new JPasswordField(15);
            passwordField.setFont(new Font(Constants.font, Font.PLAIN, Constants.regular));
            fieldPanel.add(passwordField);
        } else {
            emailField = new JTextField(15);
            emailField.setFont(new Font(Constants.font, Font.PLAIN, Constants.regular));
            fieldPanel.add(emailField);
        }

        return fieldPanel;
    }

    private JPanel createCheckboxRow() {
        JPanel checkboxPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        checkboxPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JCheckBox schedulerCheckBox = new JCheckBox("Scheduler");
        JCheckBox driverCheckBox = new JCheckBox("Driver");
        JCheckBox consumerCheckBox = new JCheckBox("Customer");

        roleGroup = new ButtonGroup();
        roleGroup.add(schedulerCheckBox);
        roleGroup.add(driverCheckBox);
        roleGroup.add(consumerCheckBox);

        checkboxPanel.add(schedulerCheckBox);
        checkboxPanel.add(driverCheckBox);
        checkboxPanel.add(consumerCheckBox);

        return checkboxPanel;
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String role = getSelectedRole();

        if (role == null) {
            JOptionPane.showMessageDialog(this, "Please select a role.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean success = AuthenticationDB.login(email, password, role);

        if (success) {
            JOptionPane.showMessageDialog(this, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            if(role.equalsIgnoreCase("driver")) {
            	new DriverDashboard();
            } else if(role.equalsIgnoreCase("customer")) {
            	new CustomerDashboard();
            } else {
            	new SchedulerDashboard();
            }
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedRole() {
        for (AbstractButton button : Collections.list(roleGroup.getElements())) {
            if (button.isSelected()) {
                return button.getText().toLowerCase();
            }
        }
        return null;
    }
}
