package Models;

import java.time.LocalDate;

public class MissionOrder {
    private int id;
    private String customerName;
    private String deliveryAddress;
    private double deliveryWeight;
    private LocalDate deliveryDate;
    private int priority;

    public MissionOrder(int id, String customerName, String deliveryAddress, double deliveryWeight, 
    		LocalDate deliveryDate, int priority) {
        this.id = id;
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
        this.deliveryWeight = deliveryWeight;
        this.priority = priority;
        this.deliveryDate = deliveryDate;
    }

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public double getDeliveryWeight() {
        return deliveryWeight;
    }
    
    public LocalDate getDeliveryDate() {
    	return deliveryDate;
    }

    public int getPriority() {
        return priority;
    }
}

