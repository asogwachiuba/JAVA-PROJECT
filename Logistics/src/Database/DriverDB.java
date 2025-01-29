package Database;

import Models.Mission;
import Models.MissionOrder;
import Models.Scheduler;
import Models.SessionTracker;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DriverDB {

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DatabaseConstants.url + DatabaseConstants.dbName, 
                DatabaseConstants.username, DatabaseConstants.password);
    }

    public static List<Mission> getMissionsForDriver(int driverId) throws SQLException {
        List<Mission> missions = new ArrayList<>();
        String missionQuery = """
            SELECT m.id, m.is_completed, m.date_assigned, m.date_completed, 
                   s.first_name AS scheduler_first_name, s.last_name AS scheduler_last_name
            FROM missions m
            JOIN schedulers s ON m.scheduler_id = s.id
            WHERE m.driver_id = ?
        """;

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(missionQuery)) {
            stmt.setInt(1, driverId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int missionId = rs.getInt("id");
                List<MissionOrder> missionOrders = getMissionOrdersForMission(missionId);

                missions.add(new Mission(
                    missionId,
                    rs.getString("scheduler_first_name"),
                    rs.getString("scheduler_last_name"),
                    missionOrders,
                    rs.getBoolean("is_completed"),
                    rs.getDate("date_assigned").toLocalDate(),
                    rs.getDate("date_completed") != null ? rs.getDate("date_completed").toLocalDate() : null,
                    SessionTracker.getSession().getFirstName(),
                    SessionTracker.getSession().getLastName()
                ));
            }
        }
        return missions;
    }


    private static List<MissionOrder> getMissionOrdersForMission(int missionId) throws SQLException {
        List<MissionOrder> missionOrders = new ArrayList<>();
        String missionOrdersQuery = """
            SELECT o.id, o.delivery_date, o.delivery_address, o.delivery_weight, 
                   c.first_name AS customer_first_name, c.last_name AS customer_last_name,
                   r.order_priority
            FROM routes r
            JOIN orders o ON r.order_id = o.id
            JOIN customers c ON o.customer_id = c.id
            WHERE r.mission_id = ?
            ORDER BY r.order_priority
        """;

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(missionOrdersQuery)) {
            stmt.setInt(1, missionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                missionOrders.add(new MissionOrder(
                    rs.getInt("id"), // id
                    rs.getString("customer_first_name") + " " + rs.getString("customer_last_name"), // customerName
                    rs.getString("delivery_address"), // deliveryAddress
                    rs.getDouble("delivery_weight"), // deliveryWeight
                    rs.getDate("delivery_date").toLocalDate(),
                    rs.getInt("order_priority")
                     // priority
                ));
            }
        }
        return missionOrders;
    }
    
    public static boolean updateMissionStatus(boolean isCompleted, Mission mission) throws SQLException {
        Connection conn = null;
        PreparedStatement updateMissionStmt = null;
        PreparedStatement updateOrdersStmt = null;

        String updateMissionQuery = """
            UPDATE missions
            SET is_completed = ?, date_completed = ?
            WHERE id = ?
        """;

        String updateOrdersQuery = """
            UPDATE orders
            SET delivery_status = ?
            WHERE id = ?
        """;

        try {
            conn = connect();
            conn.setAutoCommit(false); // Begin transaction

            // Update is_completed and date_completed in missions table
            updateMissionStmt = conn.prepareStatement(updateMissionQuery);
            updateMissionStmt.setBoolean(1, isCompleted);
            updateMissionStmt.setDate(2, isCompleted ? java.sql.Date.valueOf(LocalDate.now()) : null); // Date if completed, null otherwise
            updateMissionStmt.setInt(3, mission.getId());
            updateMissionStmt.executeUpdate();

            // Update delivery_status for all orders associated with the mission
            updateOrdersStmt = conn.prepareStatement(updateOrdersQuery);
            int deliveryStatus = isCompleted ? 1 : 0; // 1 if completed, 0 otherwise

            // Assuming mission contains a list of related orders
            for (MissionOrder order : mission.getOrders()) { // Retrieve associated orders
                updateOrdersStmt.setInt(1, deliveryStatus);
                updateOrdersStmt.setInt(2, order.getId());
                updateOrdersStmt.addBatch();
            }

            updateOrdersStmt.executeBatch(); // Execute all order updates in one batch

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback transaction on error
            }
            throw e; // Re-throw exception for the caller to handle
        } finally {
            if (updateMissionStmt != null) updateMissionStmt.close();
            if (updateOrdersStmt != null) updateOrdersStmt.close();
            if (conn != null) conn.close();
        }
    }

}
