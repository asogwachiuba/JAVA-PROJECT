package Models;


public class SessionTracker {
    private static Session activeSessions = null;

    /**
     * Creates a new session for the user.
     *
     * @param firstName             User's first name.
     * @param lastName              User's last name.
     * @param role                  User's role (e.g., customer, driver, scheduler).
     * @param email                 User's email address.
     * @param phoneNumber           User's phone number.
     * @param address               User's address.
     * @param zipCode               User's ZIP code.
     * @param truckCapacity         Truck capacity for drivers (default "0" for non-drivers).
     * @param truckRegistrationNumber Truck registration number for drivers (default "0" for non-drivers).
     */
    public static void createSession(String firstName, String lastName, String role, String email,
                                     String phoneNumber, String address, int zipCode,
                                     String truckCapacity, String truckRegistrationNumber, int id) {
        // Default "0" for non-driver fields if null
        if (!"driver".equalsIgnoreCase(role)) {
            truckCapacity = "0";
            truckRegistrationNumber = "0";
        }

        Session session = new Session(firstName, lastName, role, email, phoneNumber, address, zipCode, truckCapacity, truckRegistrationNumber, id);
        activeSessions = session;

        System.out.println("Session created for: " + email);
    }

    /**
     * Retrieves an active session by email.
     *
     * @param email Email of the user.
     * @return The Session object or null if no session exists.
     */
    public static Session getSession() {
        return activeSessions;
    }

    /**
     * Removes an active session by email.
     *
     * @param email Email of the user.
     */
    public static void removeSession() {
        if (activeSessions != null) {
            activeSessions = null;
            System.out.println("Session removed " );
        } else {
            System.out.println("No active session found ");
        }
    }

    /**
     * Checks if a session is active for the given email.
     *
     * @param email Email of the user.
     * @return True if a session is active, false otherwise.
     */
    public static boolean isLoggedIn(String email) {
        return activeSessions != null;
    }

    /**
     * Prints all active sessions.
     */
    public static void printActiveSessions() {
        if (activeSessions == null) {
            System.out.println("No active sessions.");
        } else {
            System.out.println("Active Sessions:");
            System.out.println(activeSessions);
        }
    }
}

