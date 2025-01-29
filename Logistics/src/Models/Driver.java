package Models;

public class Driver {
	private int id;
    private String firstName;
    private String lastName;
    private int zipCode;
    private String email;
    private String phoneNumber;
    private int truckCapacity;
    private String truckRegistrationNumber;

    // Constructor
    public Driver(String firstName, String lastName, int zipCode, String email, String phoneNumber, 
                  int truckCapacity, String truckRegistrationNumber, int id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.zipCode = zipCode;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.truckCapacity = truckCapacity;
        this.truckRegistrationNumber = truckRegistrationNumber;
        this.id = id;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }
    
    public int getId() {
    	return id;
    }
    
    public String getName() {
        return firstName + " " + lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getZipCode() {
        return zipCode;
    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getTruckCapacity() {
        return truckCapacity;
    }

    public void setTruckCapacity(int truckCapacity) {
        this.truckCapacity = truckCapacity;
    }

    public String getTruckRegistrationNumber() {
        return truckRegistrationNumber;
    }

    public void setTruckRegistrationNumber(String truckRegistrationNumber) {
        this.truckRegistrationNumber = truckRegistrationNumber;
    }

    

    // toString method
    @Override
    public String toString() {
        return "Driver{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", zipCode=" + zipCode +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", truckCapacity='" + truckCapacity + '\'' +
                ", truckRegistrationNumber='" + truckRegistrationNumber + '\'' +
                '}';
    }
}
