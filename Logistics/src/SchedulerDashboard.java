import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

import Database.SchedulerDB;
import Models.Driver;
import Models.MissionOrder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SchedulerDashboard extends JFrame {
	private DefaultListModel<MissionOrder> deliveriesModel;
	private DefaultListModel<Driver> driversModel;
	private JList<MissionOrder> deliveriesList;
	private JList<Driver> driversList;
	private JTextField deliverySearchField;
	private JTextField driverSearchField;
	private List<MissionOrder> allDeliveries;
	private List<Driver> allDrivers;

	public SchedulerDashboard() {
		setTitle("Scheduler Dashboard");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000, 600);
		setLayout(new BorderLayout());

		// Initialize data
		allDeliveries = fetchDeliveriesFromDB();
		allDrivers = fetchDriversFromDB();

		// Delivery list
		deliveriesModel = new DefaultListModel<>();
		deliveriesList = new JList<>(deliveriesModel);
		deliveriesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		deliveriesList.setCellRenderer(new DeliveryRenderer());
		deliveriesList.addListSelectionListener(this::validateDeliverySelection);

		// Populate delivery list
		allDeliveries.forEach(deliveriesModel::addElement);

		// Delivery search field
		String deliverySearchText = "Search deliveries by name..";
		deliverySearchField = new JTextField(deliverySearchText);
		deliverySearchField.setForeground(Color.GRAY);

		// Add FocusListener to manage hint text
		deliverySearchField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (deliverySearchField.getText().equals(deliverySearchText)) {
					deliverySearchField.setText("");
					deliverySearchField.setForeground(Color.BLACK);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (deliverySearchField.getText().isEmpty()) {
					deliverySearchField.setText(deliverySearchText);
					deliverySearchField.setForeground(Color.GRAY);
				}
			}
		});

		// Add KeyListener for filtering
		deliverySearchField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (!deliverySearchField.getText().equals(deliverySearchText)) {
					filterDeliveries(deliverySearchField.getText());
				}
			}
		});

		JPanel deliveryPanel = new JPanel(new BorderLayout());
		deliveryPanel.setBorder(BorderFactory.createTitledBorder("Available Deliveries"));
		deliveryPanel.add(new JScrollPane(deliveriesList), BorderLayout.CENTER);
		deliveryPanel.add(deliverySearchField, BorderLayout.SOUTH);

		// Driver list
		driversModel = new DefaultListModel<>();
		driversList = new JList<>(driversModel);
		driversList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		driversList.setCellRenderer(new DriverRenderer());
		driversList.addListSelectionListener(this::validateDriverSelection);

		// Populate driver list
		allDrivers.forEach(driversModel::addElement);

		// Driver search field with placeholder
		String driverSearchText = "Search drivers by name...";
		driverSearchField = new JTextField(driverSearchText);
		driverSearchField.setForeground(Color.GRAY);

		// Add FocusListener to manage hint text
		driverSearchField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (driverSearchField.getText().equals(driverSearchText)) {
					driverSearchField.setText("");
					driverSearchField.setForeground(Color.BLACK);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (driverSearchField.getText().isEmpty()) {
					driverSearchField.setText(driverSearchText);
					driverSearchField.setForeground(Color.GRAY);
				}
			}
		});

		// Add KeyListener for filtering
		driverSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (!driverSearchField.getText().equals(driverSearchText)) {
					filterDrivers(driverSearchField.getText());
				}
			}
		});

		JPanel driverPanel = new JPanel(new BorderLayout());
		driverPanel.setBorder(BorderFactory.createTitledBorder("Available Drivers"));
		driverPanel.add(new JScrollPane(driversList), BorderLayout.CENTER);
		driverPanel.add(driverSearchField, BorderLayout.SOUTH);

		// Buttons
		JButton generateDocumentButton = new JButton("Generate Document");
		generateDocumentButton.addActionListener(e -> {
			this.dispose();
			new GenerateDocument();
		});
		
		JButton editProfileButton = new JButton("Edit Profile");
		editProfileButton.addActionListener(e -> {
			new EditProfile();
		});

		JButton assignRouteButton = new JButton("Assign Route");
		assignRouteButton.addActionListener(e -> {
			// Get selected driver and deliveries
			Driver selectedDriver = driversList.getSelectedValue();
			List<MissionOrder> selectedDeliveries = deliveriesList.getSelectedValuesList();

			if (selectedDriver == null || selectedDeliveries.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please select both a driver and at least one delivery.",
						"Selection Error", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Calculate total weight of selected deliveries for the same date
			LocalDate deliveryDate = selectedDeliveries.get(0).getDeliveryDate();
			double totalWeight = selectedDeliveries.stream()
					.filter(delivery -> delivery.getDeliveryDate().equals(deliveryDate))
					.mapToDouble(MissionOrder::getDeliveryWeight).sum();

			// Check if the total weight exceeds driver's capacity for that day
			if (isDriverAvailable(selectedDriver, totalWeight, deliveryDate)) {
				this.dispose();
				new AssignRoute(selectedDeliveries, selectedDriver);
			} else {
				JOptionPane.showMessageDialog(this,
						"The driver does not have enough remaining capacity for this delivery on " + deliveryDate,
						"Capacity Exceeded", JOptionPane.WARNING_MESSAGE);
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(generateDocumentButton);
		buttonPanel.add(assignRouteButton);
		buttonPanel.add(editProfileButton);

		// Add components to the main frame
		JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		centerPanel.add(deliveryPanel);
		centerPanel.add(driverPanel);

		add(centerPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		setVisible(true);
	}

	private void validateDeliverySelection(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) { // Ensure it only processes once when the selection stabilizes
			List<MissionOrder> selectedDeliveries = deliveriesList.getSelectedValuesList();
			if (selectedDeliveries.size() > 1) {
				LocalDate firstDate = selectedDeliveries.get(0).getDeliveryDate();
				boolean allSameDate = selectedDeliveries.stream()
						.allMatch(delivery -> delivery.getDeliveryDate().equals(firstDate));
				if (!allSameDate) {
					JOptionPane.showMessageDialog(this, "Only deliveries with the same delivery date can be selected.",
							"Invalid Selection", JOptionPane.WARNING_MESSAGE);

					// Clear only invalid selections (deselect the last added item)
					deliveriesList.setSelectedIndices(
							selectedDeliveries.stream().filter(delivery -> delivery.getDeliveryDate().equals(firstDate))
									.mapToInt(allDeliveries::indexOf).toArray());
				}
			}
		}
	}

	private void validateDriverSelection(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			List<MissionOrder> selectedDeliveries = deliveriesList.getSelectedValuesList();
			Driver selectedDriver = driversList.getSelectedValue();

			if (selectedDeliveries.isEmpty() || selectedDriver == null)
				return;

			double totalWeight = selectedDeliveries.stream().mapToDouble(MissionOrder::getDeliveryWeight).sum();

			if (totalWeight > selectedDriver.getTruckCapacity()) {
				JOptionPane.showMessageDialog(this, "Total delivery weight exceeds the driver's truck capacity.",
						"Capacity Exceeded", JOptionPane.WARNING_MESSAGE);
				driversList.clearSelection();
			}
		}
	}

	private void filterDeliveries(String query) {
		deliveriesModel.clear();
		allDeliveries.stream()
				.filter(delivery -> delivery.getCustomerName().toLowerCase().contains(query.toLowerCase()))
				.forEach(deliveriesModel::addElement);
	}

	private void filterDrivers(String query) {
		driversModel.clear();
		allDrivers.stream().filter(driver -> driver.getName().toLowerCase().contains(query.toLowerCase()))
				.forEach(driversModel::addElement);
	}

	private void navigateToPage(String pageName) {
		JOptionPane.showMessageDialog(this, "Navigating to " + pageName, "Navigation", JOptionPane.INFORMATION_MESSAGE);
		// Replace this with actual page navigation logic
	}

	private static class DeliveryRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			MissionOrder delivery = (MissionOrder) value;
			String displayText = String.format("%s, %s, %.2f kg, %s", delivery.getCustomerName(),
					delivery.getDeliveryDate(), delivery.getDeliveryWeight(), delivery.getDeliveryAddress());
			return super.getListCellRendererComponent(list, displayText, index, isSelected, cellHasFocus);
		}
	}

	private static class DriverRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Driver driver = (Driver) value;
			String displayText = String.format("%s, %d Kg-Truck capacity", driver.getName(), driver.getTruckCapacity());
			return super.getListCellRendererComponent(list, displayText, index, isSelected, cellHasFocus);
		}
	}

	private List<MissionOrder> fetchDeliveriesFromDB() {
		try {
			return SchedulerDB.getUnassignedMissionOrders();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Failed to fetch deliveries from the database: " + e.getMessage(),
					"Database Error", JOptionPane.ERROR_MESSAGE);
			return new ArrayList<>();
		}
	}

	private List<Driver> fetchDriversFromDB() {
		try {
			return SchedulerDB.getAllDrivers();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Failed to fetch drivers from the database: " + e.getMessage(),
					"Database Error", JOptionPane.ERROR_MESSAGE);
			return new ArrayList<>();
		}
	}

	// Method to check if the driver is available
	private boolean isDriverAvailable(Driver driver, double totalWeight, LocalDate deliveryDate) {
		List<MissionOrder> deliveriesForDriver = new ArrayList<>();

		try {
			// Fetch all deliveries for the selected date and driver
			deliveriesForDriver = SchedulerDB.getMissionOrdersByDriverAndDate(driver.getId(), deliveryDate);
		} catch (SQLException e) {
			e.printStackTrace();
			return false; // Return false in case of a database error
		}

		// Calculate the total weight of deliveries already assigned to the driver on
		// that day
		double assignedWeight = deliveriesForDriver.stream().mapToDouble(MissionOrder::getDeliveryWeight).sum();

		// Check if the driver has enough capacity for the new deliveries
		return driver.getTruckCapacity() >= (assignedWeight + totalWeight);
	}

}
