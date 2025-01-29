import javax.swing.*;
import Database.DriverDB;
import Models.Mission;
import Models.MissionOrder;
import Models.Session;
import Models.SessionTracker;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DriverDashboard extends JFrame {
    private DefaultListModel<String> assignedMissionsModel;
    private DefaultListModel<String> completedMissionsModel;
    private JList<String> assignedMissionsList;
    private JList<String> completedMissionsList;
    private List<Mission> missions;
    private Session sessionData;

    public DriverDashboard() {
        SessionTracker.printActiveSessions();
        sessionData = SessionTracker.getSession();

        // Frame settings
        this.setTitle("WELCOME " + sessionData.getFirstName() + " " + sessionData.getLastName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Initialize mission list models
        assignedMissionsModel = new DefaultListModel<>();
        completedMissionsModel = new DefaultListModel<>();
        assignedMissionsList = new JList<>(assignedMissionsModel);
        completedMissionsList = new JList<>(completedMissionsModel);

        // Load missions from database
        try {
            missions = DriverDB.getMissionsForDriver(sessionData.getId());
            missions.forEach(mission -> {
                if (mission.isCompleted()) {
                    completedMissionsModel.addElement(mission.getMissionName());
                } else {
                    assignedMissionsModel.addElement(mission.getMissionName());
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading missions: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            missions = new ArrayList<>();
        }

        // Create labeled panels for mission lists
        JPanel assignedPanel = new JPanel(new BorderLayout());
        assignedPanel.add(new JLabel("Assigned Mission", JLabel.CENTER), BorderLayout.NORTH);
        assignedPanel.add(new JScrollPane(assignedMissionsList), BorderLayout.CENTER);

        JPanel completedPanel = new JPanel(new BorderLayout());
        completedPanel.add(new JLabel("Completed Mission", JLabel.CENTER), BorderLayout.NORTH);
        completedPanel.add(new JScrollPane(completedMissionsList), BorderLayout.CENTER);

        JPanel missionListsPanel = new JPanel(new GridLayout(1, 2));
        missionListsPanel.add(assignedPanel);
        missionListsPanel.add(completedPanel);

        // Buttons for managing missions
        JButton completeButton = new JButton("Completed ->");
        completeButton.addActionListener(this::markAsCompleted);

        JButton uncompleteButton = new JButton("<- Uncompleted");
        uncompleteButton.addActionListener(this::markAsUncompleted);

        JButton viewDetailsButton = new JButton("View Selected Mission");
        viewDetailsButton.addActionListener(e -> viewMissionDetails());
        
        JButton editProfileButton = new JButton("Edit Profile");
        editProfileButton.addActionListener(e -> {new EditProfile();});

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(completeButton);
        buttonPanel.add(uncompleteButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(editProfileButton);

        // Add components to the frame
        add(missionListsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void markAsCompleted(ActionEvent e) {
        String selectedMissionName = assignedMissionsList.getSelectedValue();
        if (selectedMissionName == null) {
            JOptionPane.showMessageDialog(this, "Please select a mission to mark as completed.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Mission selectedMission = getMissionByName(selectedMissionName);
        if (selectedMission != null) {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to mark this mission as completed?", "Confirm Action", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                try {
                    if (DriverDB.updateMissionStatus(true, selectedMission)) {
                        assignedMissionsModel.removeElement(selectedMissionName);
                        completedMissionsModel.addElement(selectedMissionName);
                        JOptionPane.showMessageDialog(this, "Mission marked as completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update the mission.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error updating mission: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void markAsUncompleted(ActionEvent e) {
        String selectedMissionName = completedMissionsList.getSelectedValue();
        if (selectedMissionName == null) {
            JOptionPane.showMessageDialog(this, "Please select a mission to mark as uncompleted.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Mission selectedMission = getMissionByName(selectedMissionName);
        if (selectedMission != null) {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to mark this mission as uncompleted?", "Confirm Action", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                try {
                    if (DriverDB.updateMissionStatus(false, selectedMission)) {
                        completedMissionsModel.removeElement(selectedMissionName);
                        assignedMissionsModel.addElement(selectedMissionName);
                        JOptionPane.showMessageDialog(this, "Mission marked as uncompleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update the mission.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error updating mission: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void viewMissionDetails() {
        String selectedName = assignedMissionsList.getSelectedValue();
        if (selectedName == null) {
            selectedName = completedMissionsList.getSelectedValue();
        }

        if (selectedName != null) {
            Mission mission = getMissionByName(selectedName);
            if (mission != null) {
                JOptionPane.showMessageDialog(this, mission.toString(), "Mission Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a mission to view details.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Mission getMissionByName(String missionName) {
        return missions.stream().filter(m -> m.getMissionName().equals(missionName)).findFirst().orElse(null);
    }
}
