import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import Database.SchedulerDB;
import Models.Driver;
import Models.MissionOrder;
import Models.SessionTracker;

public class AssignRoute extends JFrame {

    private JList<MissionOrder> deliveriesList;
    private DefaultListModel<MissionOrder> deliveriesListModel;

    public AssignRoute(List<MissionOrder> orders, Driver driver) {
        setTitle("Assign Route To " + driver.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // Top Label
        JLabel assignedDeliveriesLabel = new JLabel("Assigned Deliveries: " + (orders.isEmpty() ? "No Deliveries" : orders.get(0).getDeliveryDate()));
        add(assignedDeliveriesLabel, BorderLayout.NORTH);

        // Center Panel (List and Buttons)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // List of Deliveries
        deliveriesListModel = new DefaultListModel<>();
        for (MissionOrder order : orders) {
            deliveriesListModel.addElement(order);
        }
        deliveriesList = new JList<>(deliveriesListModel);
        deliveriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Renderer for Displaying Customer Name and Address
        deliveriesList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            MissionOrder order = (MissionOrder) value;
            String displayText = order.getCustomerName() + " - " + order.getDeliveryAddress();

            JLabel label = new JLabel(displayText);
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
                label.setOpaque(true);
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
                label.setOpaque(true);
            }
            return label;
        });

        JScrollPane listScrollPane = new JScrollPane(deliveriesList);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        centerPanel.add(listScrollPane, gbc);

        // Move Up and Move Down Buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JButton moveUpButton = new JButton("Move Up");
        JButton moveDownButton = new JButton("Move Down");

        moveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveItem(-1);
            }
        });

        moveDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveItem(1);
            }
        });

        buttonsPanel.add(moveUpButton);
        buttonsPanel.add(moveDownButton);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        centerPanel.add(buttonsPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Confirm Route Button
        JButton confirmRouteButton = new JButton("Confirm Route");
        confirmRouteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<MissionOrder> orderedList = new ArrayList<>();
                    for (int i = 0; i < deliveriesListModel.size(); i++) {
                        orderedList.add(deliveriesListModel.getElementAt(i));
                    }
                    int schedulerId = SessionTracker.getSession().getId();
                    if (SchedulerDB.confirmRoute( schedulerId, driver.getId(), orderedList)) {
                        JOptionPane.showMessageDialog(AssignRoute.this, "Route confirmed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        new SchedulerDashboard();
                    } else {
                        JOptionPane.showMessageDialog(AssignRoute.this, "Failed to confirm route.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(AssignRoute.this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AssignRoute.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        add(confirmRouteButton, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    private void moveItem(int direction) {
        int selectedIndex = deliveriesList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to move.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newIndex = selectedIndex + direction;
        if (newIndex < 0 || newIndex >= deliveriesListModel.size()) {
            return;
        }

        MissionOrder selectedOrder = deliveriesListModel.getElementAt(selectedIndex);
        deliveriesListModel.remove(selectedIndex);
        deliveriesListModel.add(newIndex, selectedOrder);
        deliveriesList.setSelectedIndex(newIndex);
    }
}
