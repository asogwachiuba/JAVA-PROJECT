import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ForgotPassword extends JFrame {

	String forgot_image_path = "/Users/user/eclipse-workspace/Logistics/src/images/forgot_password.jpg";
	JLabel forgot_label;
	// Fields list to store references to all text fields
    List<JTextField> textFields = new ArrayList<>();
    JButton retrieveButton;

	public ForgotPassword() {

		this.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// mouse UI
		ImageIcon forgot_icon = new ImageIcon(forgot_image_path);
		Image welcome_image = forgot_icon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
		forgot_icon = new ImageIcon(welcome_image);
		forgot_label = new JLabel(forgot_icon);
		forgot_label.setOpaque(true);
		// Add margin using EmptyBorder
		forgot_label.setBorder(new EmptyBorder(30, 125, 0, 125)); // Top, left, bottom, right margins

		this.add(forgot_label);
		
		this.add(createFieldPanel("Phone Number: "));
		
		this.add(createFieldPanel("First Name: "));
		
		retrieveButton = new JButton("Retrieve");
//		retrieveButton.setBorder(new EmptyBorder(30, 150, 0, 20));
		this.add(retrieveButton);

		// Create frame
		this.setTitle("FORGOT PASSWORD");
		this.setBounds(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	private JPanel createFieldPanel(String labelText) {
		JPanel fieldPanel = new JPanel(new FlowLayout());
		fieldPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		JLabel label = new JLabel(labelText);
		label.setFont(new Font(Constants.font, Font.PLAIN, Constants.regular));
		fieldPanel.add(label);

		JTextField textField = new JTextField(15);
		textField.setFont(new Font(Constants.font, Font.PLAIN, Constants.regular));
		textFields.add(textField);
		fieldPanel.add(textField);
		fieldPanel.setBorder(new EmptyBorder(0, 150, 0, 180));

		return fieldPanel;
	}

}
