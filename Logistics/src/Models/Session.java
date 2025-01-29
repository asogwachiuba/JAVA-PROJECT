package Models;

public class Session {
    private String firstName;
    private String lastName;
    private int id;
    private String role;
    private String email;
    private String phoneNumber;
    private String address;
    private int zipCode;
    private String truckCapacity;
    private String truckRegistrationNumber;

    public Session(String firstName, String lastName, String role, String email, 
                   String phoneNumber, String address, int zipCode, 
                   String truckCapacity, String truckRegistrationNumber, int id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.id = id;
        this.zipCode = zipCode;
        this.truckCapacity = truckCapacity != null ? truckCapacity : "0"; // Default to "0"
        this.truckRegistrationNumber = truckRegistrationNumber != null ? truckRegistrationNumber : "0"; // Default to "0"
    }

    // Getters
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public int getZipCode() {
        return zipCode;
    }

    public String getTruckCapacity() {
        return truckCapacity;
    }

    public String getTruckRegistrationNumber() {
        return truckRegistrationNumber;
    }
    
    public int getId() {
    	return id;
    }

    @Override
    public String toString() {
        return "Session{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", zipCode=" + zipCode +
                ", truckCapacity='" + truckCapacity + '\'' +
                ", truckRegistrationNumber='" + truckRegistrationNumber + '\'' +
                '}';
    }
}

