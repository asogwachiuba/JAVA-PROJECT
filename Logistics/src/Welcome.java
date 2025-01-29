import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Welcome extends JFrame implements ActionListener {

	JLabel welcome_text;
	JLabel welcome_label;
	String welcome_image_path = "/Users/user/eclipse-workspace/Logistics/src/images/welcome-image.jpg";
	JPanel buttonPanel;
	JButton registerButton;
	JButton loginButton;

	public static void main(String[] args) {
		new Welcome();
	}

	public Welcome() {
		this.setLayout(new FlowLayout());
		
		// mouse UI
		ImageIcon welcome_icon = new ImageIcon(welcome_image_path);
		Image welcome_image = welcome_icon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
		welcome_icon = new ImageIcon(welcome_image);
		welcome_label = new JLabel(welcome_icon);
		welcome_label.setOpaque(true);
		// Add margin using EmptyBorder
        welcome_label.setBorder(new EmptyBorder(30, 125, 0, 125)); // Top, left, bottom, right margins

		this.add(welcome_label);
		
		// Welcome text
         welcome_text = new JLabel("<html>"
                 + "<div style='text-align: center; font-size: 12px;'>"
                 + "<span style='font-size: 15px; font-weight: bold; color: #003366;'>Welcome to Logistics Management!</span><br>"
                 + "<span style='font-size: 10px; color: #444444;'>We ensure seamless operations with reliability and efficiency.</span><br>"
                 + "<span style='font-size: 8px; color: #666666;'>Navigate through features designed for your convenience.</span>"
                 + "</div>"
                 + "</html>");
        welcome_text.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font
        welcome_text.setHorizontalAlignment(SwingConstants.CENTER);

        // Add margin to the text
        welcome_text.setBorder(new EmptyBorder(20, 30, 0, 30)); // Top, left, bottom, right margins
        this.add(welcome_text);
        
        // Register and Login Buttons
         buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); // Buttons in a single line with spacing
         registerButton = new JButton("Register");
         loginButton = new JButton("Login");

        // Add margin to the panel
        buttonPanel.setBorder(new EmptyBorder(50, 0, 0, 0)); // Margin above the buttons
        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);

        // Add the button panel to the frame
        this.add(buttonPanel);
        registerButton.addActionListener(this);
        loginButton.addActionListener(this);
        

		// Create frame
		this.setTitle("Welcome"); 
		this.setBounds(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Register navigation
		if(e.getSource() == registerButton) { 
			new Register();
			this.dispose();
		} else {
			new Login();
			this.dispose();
		}
		
	}

}
