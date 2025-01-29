package Database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import Models.Driver;
import Models.Mission;
import Models.MissionOrder;

public class SchedulerDB {
	
	  private static Connection connect() throws SQLException {
	        return DriverManager.getConnection(DatabaseConstants.url + DatabaseConstants.dbName, 
	                DatabaseConstants.username, DatabaseConstants.password);
	    }
	  
	 public static List<MissionOrder> getUnassignedMissionOrders() throws SQLException {
		    List<MissionOrder> unassignedMissionOrders = new ArrayList<>();
		    String unassignedOrdersQuery = """
		        SELECT o.id, o.delivery_date, o.delivery_address, o.delivery_weight,
		               c.first_name AS customer_first_name, c.last_name AS customer_last_name
		        FROM orders o
		        JOIN customers c ON o.customer_id = c.id
		        WHERE o.driver_id IS NULL
		        ORDER BY o.delivery_date
		    """;

		    try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(unassignedOrdersQuery)) {
		        ResultSet rs = stmt.executeQuery();

		        while (rs.next()) {
		            unassignedMissionOrders.add(new MissionOrder(
		                rs.getInt("id"), // id
		                rs.getString("customer_first_name") + " " + rs.getString("customer_last_name"), // customerName
		                rs.getString("delivery_address"), // deliveryAddress
		                rs.getDouble("delivery_weight"), // deliveryWeight
		                rs.getDate("delivery_date").toLocalDate(), // deliveryDate
		                0 // priority (defaulted to zero)
		            ));
		        }
		    }
		    return unassignedMissionOrders;
		}
	 
	public static List<Driver> getAllDrivers() throws SQLException {
		    List<Driver> drivers = new ArrayList<>();
		    String driversQuery = """
		        SELECT id, first_name, last_name, zip_code, email, phone_number, 
		               truck_capacity, truck_registration_no
		        FROM drivers
		    """;

		    try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(driversQuery)) {
		        ResultSet rs = stmt.executeQuery();

		        while (rs.next()) {
		            drivers.add(new Driver(
		                rs.getString("first_name"),                  // firstName
		                rs.getString("last_name"),                   // lastName
		                rs.getInt("zip_code"),                       // zipCode
		                rs.getString("email"),                       // email
		                rs.getString("phone_number"),                // phoneNumber
		                rs.getInt("truck_capacity"),                 // truckCapacity
		                rs.getString("truck_registration_no"),        // truckRegistrationNumber
		                rs.getInt("id")
		            ));
		        }
		    }
		    return drivers;
		}
	
	public static List<MissionOrder> getMissionOrdersByDriverAndDate(int driverId, LocalDate deliveryDate) throws SQLException {
	    List<MissionOrder> missionOrders = new ArrayList<>();
	    String missionOrdersQuery = """
	        SELECT o.id, o.delivery_date, o.delivery_address, o.delivery_weight,
	               c.first_name AS customer_first_name, c.last_name AS customer_last_name
	        FROM orders o
	        JOIN customers c ON o.customer_id = c.id
	        WHERE o.driver_id = ? AND o.delivery_date = ?
	        ORDER BY o.delivery_date
	    """;

	    try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(missionOrdersQuery)) {
	        stmt.setInt(1, driverId);
	        stmt.setDate(2, java.sql.Date.valueOf(deliveryDate));

	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            missionOrders.add(new MissionOrder(
	                rs.getInt("id"), // id
	                rs.getString("customer_first_name") + " " + rs.getString("customer_last_name"), // customerName
	                rs.getString("delivery_address"), // deliveryAddress
	                rs.getDouble("delivery_weight"), // deliveryWeight
	                rs.getDate("delivery_date").toLocalDate(), // deliveryDate
	                0 // priority (defaulted to zero)
	            ));
	        }
	    }
	    return missionOrders;
	}
	
	public static boolean confirmRoute(int schedulerId, int driverId, List<MissionOrder> missionOrders) throws SQLException {
	    Connection conn = null;
	    PreparedStatement missionStmt = null;
	    PreparedStatement routeStmt = null;
	    PreparedStatement updateOrderStmt = null;

	    String insertMissionQuery = """
	        INSERT INTO missions (is_completed, date_assigned, date_completed, scheduler_id, driver_id)
	        VALUES (?, ?, ?, ?, ?)
	    """;

	    String insertRouteQuery = """
	        INSERT INTO routes (order_id, mission_id, order_priority)
	        VALUES (?, ?, ?)
	    """;

	    String updateOrderQuery = """
	        UPDATE orders SET driver_id = ? WHERE id = ?
	    """;

	    try {
	        conn = connect();
	        conn.setAutoCommit(false); // Begin transaction

	        // Insert into missions table and retrieve generated missionId
	        missionStmt = conn.prepareStatement(insertMissionQuery, PreparedStatement.RETURN_GENERATED_KEYS);
	        missionStmt.setBoolean(1, false); // is_completed = false initially
	        missionStmt.setDate(2, java.sql.Date.valueOf(LocalDate.now())); // date_assigned = today
	        missionStmt.setDate(3, null); // date_completed = null initially
	        missionStmt.setInt(4, schedulerId);
	        missionStmt.setInt(5, driverId);
	        missionStmt.executeUpdate();

	        int missionId;
	        try (ResultSet generatedKeys = missionStmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                missionId = generatedKeys.getInt(1); // Get the auto-generated missionId
	            } else {
	                throw new SQLException("Failed to retrieve mission ID.");
	            }
	        }

	        // Insert each order into the routes table
	        routeStmt = conn.prepareStatement(insertRouteQuery);
	        updateOrderStmt = conn.prepareStatement(updateOrderQuery);

	        int priority = 1; // Start priority at 1
	        for (MissionOrder order : missionOrders) {
	            // Add route insert batch
	            routeStmt.setInt(1, order.getId());
	            routeStmt.setInt(2, missionId); // Use the generated missionId
	            routeStmt.setInt(3, priority); // Assign priority starting from 1
	            routeStmt.addBatch();

	            // Add orders update batch
	            updateOrderStmt.setInt(1, driverId);
	            updateOrderStmt.setInt(2, order.getId());
	            updateOrderStmt.addBatch();

	            priority++; // Increment priority for the next order
	        }

	        routeStmt.executeBatch(); // Execute all route insertions in one batch
	        updateOrderStmt.executeBatch(); // Execute all order updates in one batch
	        conn.commit(); // Commit the transaction
	        return true;

	    } catch (SQLException e) {
	        if (conn != null) {
	            conn.rollback(); // Rollback the transaction on error
	        }
	        throw e; // Re-throw the exception for the caller to handle
	    } finally {
	        if (missionStmt != null) missionStmt.close();
	        if (routeStmt != null) routeStmt.close();
	        if (updateOrderStmt != null) updateOrderStmt.close();
	        if (conn != null) conn.close();
	    }
	}
	
	
	public static List<Mission> getMissions(LocalDate date) {
	    List<Mission> missions = new ArrayList<>();

	    String missionQuery = "SELECT m.id, m.is_completed, m.date_assigned, m.date_completed, " +
	            "s.first_name AS scheduler_first_name, s.last_name AS scheduler_last_name, " +
	            "d.first_name AS driver_first_name, d.last_name AS driver_last_name " +
	            "FROM missions m " +
	            "JOIN schedulers s ON m.scheduler_id = s.id " +
	            "JOIN drivers d ON m.driver_id = d.id " +
	            "WHERE m.date_assigned = ?";

	    String ordersQuery = "SELECT o.id, o.delivery_date, o.delivery_address, o.delivery_weight, " +
	            "COALESCE(c.first_name, '') || ' ' || COALESCE(c.last_name, '') AS customer_name, " +
	            "r.order_priority " +
	            "FROM orders o " +
	            "JOIN customers c ON o.customer_id = c.id " +
	            "JOIN routes r ON o.id = r.order_id " +
	            "WHERE r.mission_id = ?";

	    try (Connection conn = connect();
	         PreparedStatement missionStmt = conn.prepareStatement(missionQuery);
	         PreparedStatement ordersStmt = conn.prepareStatement(ordersQuery)) {

	        // Set the date parameter for the mission query
	        missionStmt.setDate(1, Date.valueOf(date));
	        try (ResultSet missionResult = missionStmt.executeQuery()) {

	            while (missionResult.next()) {
	                int missionId = missionResult.getInt("id");
	                boolean isCompleted = missionResult.getBoolean("is_completed");
	                LocalDate dateAssigned = missionResult.getDate("date_assigned").toLocalDate();
	                LocalDate dateCompleted = missionResult.getDate("date_completed") != null
	                        ? missionResult.getDate("date_completed").toLocalDate()
	                        : null;
	                String schedulerFirstName = missionResult.getString("scheduler_first_name");
	                String schedulerLastName = missionResult.getString("scheduler_last_name");
	                String driverFirstName = missionResult.getString("driver_first_name");
	                String driverLastName = missionResult.getString("driver_last_name");

	                // Fetch orders for the mission
	                ordersStmt.setInt(1, missionId);
	                try (ResultSet ordersResult = ordersStmt.executeQuery()) {
	                    List<MissionOrder> orders = new ArrayList<>();
	                    while (ordersResult.next()) {
	                        int orderId = ordersResult.getInt("id");
	                        LocalDate deliveryDate = ordersResult.getDate("delivery_date").toLocalDate();
	                        String deliveryAddress = ordersResult.getString("delivery_address");
	                        double deliveryWeight = ordersResult.getDouble("delivery_weight");
	                        String customerName = ordersResult.getString("customer_name");
	                        int priority = ordersResult.getInt("order_priority");

	                        orders.add(new MissionOrder(orderId, customerName, deliveryAddress, deliveryWeight, deliveryDate, priority));
	                    }

	                    // Create Mission object
	                    Mission mission = new Mission(
	                            missionId, schedulerFirstName, schedulerLastName,
	                            orders, isCompleted, dateAssigned, dateCompleted,
	                            driverFirstName, driverLastName
	                    );
	                    missions.add(mission);
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        throw new RuntimeException("Error fetching missions from the database", e);
	    }

	    return missions;
	}



}
