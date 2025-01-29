package Models;

import java.time.LocalDate;

public class Order {
    private int id;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private String deliveryAddress;
    private String status;

    public Order(int id, LocalDate orderDate, LocalDate deliveryDate, String deliveryAddress, String status) {
        this.id = id;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
    	String viewStatus = Integer.parseInt(status) == 0 ? "Not Delivered Yet" : "Delivered Successfully";
        return "Order ID: " + id + " | Status: " + viewStatus + " | Delivery Date: " + deliveryDate;
    }
}

