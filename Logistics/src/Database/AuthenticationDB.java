package Database;

import java.sql.*;

import javax.swing.JOptionPane;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import Models.Session;
import Models.SessionTracker;

public class AuthenticationDB {
	private static Connection connect() throws SQLException {
		return DriverManager.getConnection(DatabaseConstants.url + DatabaseConstants.dbName, DatabaseConstants.username,
				DatabaseConstants.password);
	}

	public static boolean login(String email, String password, String role) {
		// Determine the table name based on the role
		String tableName;
		if ("scheduler".equalsIgnoreCase(role)) {
			tableName = "schedulers";
		} else if ("driver".equalsIgnoreCase(role)) {
			tableName = "drivers";
		} else if ("customer".equalsIgnoreCase(role)) {
			tableName = "customers";
		} else {
			System.out.println("Invalid role provided: " + role);
			return false;
		}

		// Build the query with the table name directly
		String query = "SELECT * FROM " + tableName + " WHERE email = ? AND password = ?";

		// Hash the provided password
		String hashedPassword = hashPassword(password);
		if (hashedPassword == null) {
			System.out.println("Password hashing failed.");
			return false;
		}

		try (Connection connection = connect(); PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, email);
			stmt.setString(2, hashedPassword); // Use the hashed password

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					// Login successful; extract user details
					String firstName = rs.getString("first_name");
					String lastName = rs.getString("last_name");
					String phoneNumber = rs.getString("phone_number");
					String address = rs.getString("address");
					int zipCode = rs.getInt("zip_code");
					int id = rs.getInt("id");

					// Default values for truck-related fields (set only for drivers)
					String truckCapacity = "0";
					String truckRegistrationNumber = "0";

					if ("driver".equalsIgnoreCase(role)) {
						truckCapacity = rs.getString("truck_capacity");
						truckRegistrationNumber = rs.getString("truck_registration_no");
					}

					// Create a session for the user
					SessionTracker.createSession(firstName, lastName, role, email, phoneNumber, address, zipCode,
							truckCapacity, truckRegistrationNumber, id);

					System.out.println("Login successful and session created for: " + email);
					return true;
				} else {
					System.out.println("Invalid email or password for role: " + role);
					return false;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean register(String email, String password, String role, String firstName, String lastName,
			String phoneNumber, String address, int zipCode, Integer truckCapacity, Integer truckRegistrationNo) {

		// Hash the password
		String hashedPassword = hashPassword(password);
		if (hashedPassword == null) {
			System.out.println("Password hashing failed.");
			return false;
		}

		// Determine the query based on the role
		String query = null;
		if ("Driver".equalsIgnoreCase(role)) {
			query = "INSERT INTO drivers (email, password, first_name, last_name, phone_number, address, zip_code, truck_capacity, truck_registration_no) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		} else if ("Scheduler".equalsIgnoreCase(role)) {
			query = "INSERT INTO schedulers (email, password, first_name, last_name, phone_number, address, zip_code) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		} else if ("Customer".equalsIgnoreCase(role)) {
			query = "INSERT INTO customers (email, password, first_name, last_name, phone_number, address, zip_code) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		} else {
			System.out.println("Invalid role provided: " + role);
			return false;
		}

		try (Connection connection = connect(); PreparedStatement stmt = connection.prepareStatement(query)) {

			// Set common fields
			stmt.setString(1, email);
			stmt.setString(2, hashedPassword); // Use the hashed password here
			stmt.setString(3, firstName);
			stmt.setString(4, lastName);
			stmt.setString(5, phoneNumber);
			stmt.setString(6, address);
			stmt.setInt(7, zipCode);

			// Set role-specific fields
			if ("Driver".equalsIgnoreCase(role)) {
				if (truckCapacity != null) {
					stmt.setInt(8, truckCapacity);
				} else {
					stmt.setNull(8, java.sql.Types.INTEGER);
				}

				if (truckRegistrationNo != null) {
					stmt.setInt(9, truckRegistrationNo);
				} else {
					stmt.setNull(9, java.sql.Types.INTEGER);
				}
			}

			stmt.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static String hashPassword(String password) {
		try {
			// Create a MessageDigest instance for SHA-256
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			// Hash the password bytes
			byte[] hashedBytes = md.digest(password.getBytes());

			// Convert the hashed bytes to a hexadecimal string
			StringBuilder sb = new StringBuilder();
			for (byte b : hashedBytes) {
				sb.append(String.format("%02x", b));
			}

			return sb.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null; // Indicate hashing failure
		}
	}


public static boolean editProfile(String firstName, String lastName, String email, String phoneNumber,
                                  String address, int zipCode, Integer truckCapacity, String truckRegNumber,
                                  String oldPassword, String newPassword) {
    // Retrieve the current session
    Session session = SessionTracker.getSession();
    if (session == null) {
        System.out.println("No active session found.");
        return false;
    }

    String role = session.getRole();
    String tableName;

    if ("scheduler".equalsIgnoreCase(role)) {
        tableName = "schedulers";
    } else if ("customer".equalsIgnoreCase(role)) {
        tableName = "customers";
    } else if ("driver".equalsIgnoreCase(role)) {
        tableName = "drivers";
    } else {
        System.out.println("Invalid role: " + role);
        return false;
    }

    try (Connection connection = connect()) {
        boolean updatePassword = oldPassword != null && !oldPassword.isEmpty() && newPassword != null && !newPassword.isEmpty();

        if (updatePassword) {
            // Step 1: Verify the old password
            String checkPasswordQuery = "SELECT password FROM " + tableName + " WHERE id = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkPasswordQuery)) {
                checkStmt.setInt(1, session.getId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("password");
                    if (!storedHashedPassword.equals(hashPassword(oldPassword))) {
                        JOptionPane.showMessageDialog(null, "Old password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                } else {
                    System.out.println("User not found.");
                    return false;
                }
            }
        }

        // Step 2: Construct the update query
        String query = "UPDATE " + tableName + " SET first_name = ?, last_name = ?, email = ?, phone_number = ?, address = ?, zip_code = ?"
                     + (updatePassword ? ", password = ?" : "")
                     + (tableName.equals("drivers") ? ", truck_capacity = ?, truck_registration_no = ?" : "")
                     + " WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, phoneNumber);
            stmt.setString(5, address);
            stmt.setInt(6, zipCode);

            int paramIndex = 7;

            if (updatePassword) {
                stmt.setString(paramIndex++, hashPassword(newPassword));
            }

            if (tableName.equals("drivers")) {
                if (truckCapacity != null) {
                    stmt.setInt(paramIndex++, truckCapacity);
                } else {
                    stmt.setNull(paramIndex++, Types.INTEGER);
                }

                if (truckRegNumber != null) {
                    stmt.setString(paramIndex++, truckRegNumber);
                } else {
                    stmt.setNull(paramIndex++, Types.VARCHAR);
                }
            }

            stmt.setInt(paramIndex, session.getId()); // ID parameter

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                SessionTracker.createSession(firstName, lastName, role, email, phoneNumber, address, zipCode,
                        truckCapacity != null ? truckCapacity.toString() : "N/A",
                        truckRegNumber != null ? truckRegNumber : "N/A",
                        session.getId());

                System.out.println("Profile updated successfully.");
                return true;
            } else {
                System.out.println("No matching record found to update.");
                return false;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

}
