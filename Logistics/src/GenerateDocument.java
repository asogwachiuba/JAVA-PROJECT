import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import Database.SchedulerDB;
import Models.Mission;
import Models.MissionOrder;
import Utilities.MissionDocumentGenerator;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class GenerateDocument extends JFrame {

    private JTextField dateInputField;
    private JLabel selectedDateLabel;
    private JList<Mission> missionList;
    private DefaultListModel<Mission> missionListModel;
    private JButton viewAssignmentButton;
    private JButton downloadButton;
    private JPanel missionPanel;

    public GenerateDocument() {
        // Frame settings
        this.setTitle("Generate Daily Assignment Document");
        this.setBounds(100, 100, 800, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        // Top panel with date input and label
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel dateLabel = new JLabel("Assignment Date: ");
        dateInputField = new JTextField(10);
        viewAssignmentButton = new JButton("View Assignment");

        topPanel.add(dateLabel);
        topPanel.add(dateInputField);
        topPanel.add(viewAssignmentButton);

        this.add(topPanel, BorderLayout.NORTH);

        // Mission panel to display selected date and mission list
        missionPanel = new JPanel();
        missionPanel.setLayout(new BorderLayout());
        missionPanel.setVisible(false);

        selectedDateLabel = new JLabel();
        selectedDateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        missionListModel = new DefaultListModel<>();
        missionList = new JList<>(missionListModel);
        missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane missionScrollPane = new JScrollPane(missionList);
        missionPanel.add(selectedDateLabel, BorderLayout.NORTH);
        missionPanel.add(missionScrollPane, BorderLayout.CENTER);

        this.add(missionPanel, BorderLayout.CENTER);

        // Button panel with Download button
        JPanel buttonPanel = new JPanel();
        downloadButton = new JButton("DOWNLOAD");
        buttonPanel.add(downloadButton);

        this.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        viewAssignmentButton.addActionListener(this::handleViewAssignment);
        missionList.addListSelectionListener(this::handleMissionSelection);
        downloadButton.addActionListener(this::handleDownload);

        this.setVisible(true);
    }

    private void handleViewAssignment(ActionEvent e) {
        String inputDate = dateInputField.getText().trim();
        if (inputDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            LocalDate date = LocalDate.parse(inputDate);
            List<Mission> missions = SchedulerDB.getMissions(date);

            if (missions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No assignments found for the selected date.", "No Data", JOptionPane.INFORMATION_MESSAGE);
                missionPanel.setVisible(false);
            } else {
                selectedDateLabel.setText("Missions for Date: " + inputDate);
                missionListModel.clear();
                missions.forEach(missionListModel::addElement);
                missionPanel.setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching missions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMissionSelection(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && missionList.getSelectedValue() != null) {
            Mission selectedMission = missionList.getSelectedValue();

            StringBuilder missionDetails = new StringBuilder();
            missionDetails.append("Mission ID: ").append(selectedMission.getId()).append("\n");
            missionDetails.append("Scheduler: ").append(selectedMission.getSchedulerFirstName())
                    .append(" ").append(selectedMission.getSchedulerLastName()).append("\n");
            missionDetails.append("Driver: ").append(selectedMission.getDriverFirstName())
                    .append(" ").append(selectedMission.getDriverLastName()).append("\n");
            missionDetails.append("Date Assigned: ").append(selectedMission.getDateAssigned()).append("\n");
            missionDetails.append("Date Completed: ").append(selectedMission.getDateCompleted() != null
                    ? selectedMission.getDateCompleted()
                    : "Not Completed").append("\n");
            missionDetails.append("Is Completed: ").append(selectedMission.isCompleted() ? "Yes" : "No").append("\n");
            missionDetails.append("Total Weight: ").append(selectedMission.getTotalWeight()).append(" kg\n");
            missionDetails.append("Orders:\n");

            for (MissionOrder order : selectedMission.getOrders()) {
                missionDetails.append("  - Order ID: ").append(order.getId())
                        .append(", Customer: ").append(order.getCustomerName())
                        .append(", Address: ").append(order.getDeliveryAddress())
                        .append(", Weight: ").append(order.getDeliveryWeight())
                        .append(", Priority: ").append(order.getPriority()).append("\n");
            }

            JOptionPane.showMessageDialog(this, missionDetails.toString(), "Mission Details", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void handleDownload(ActionEvent e) {
        String inputDate = dateInputField.getText().trim();
        if (inputDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Mission> missions = Collections.list(missionListModel.elements());


        if (missions == null || missions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No missions available to generate a document.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String result = MissionDocumentGenerator.generateMissionDocument(inputDate, missions);
        JOptionPane.showMessageDialog(this, result, "Download Status", result.startsWith("Error") ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }
}
