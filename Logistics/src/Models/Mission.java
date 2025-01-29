package Models;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import Database.DatabaseConstants;

public class Mission {
	private int id;
    private String schedulerFirstName;
    private String schedulerLastName;
    private LocalDate dateAssigned;
    private LocalDate dateCompleted;
    private List<MissionOrder> orders;
    private boolean isCompleted;
    private String driverFirstName;
    private String driverLastName;

    public Mission(int id, String schedulerFirstName, String schedulerLastName, List<MissionOrder> orders, boolean isCompleted, 
    		LocalDate dateAssigned, LocalDate dateCompleted, String driverFirstName, String driverLastName) {
        this.schedulerFirstName = schedulerFirstName;
        this.schedulerLastName = schedulerLastName;
        this.orders = orders;
        this.isCompleted = isCompleted;
        this.dateAssigned = dateAssigned;
        this.dateCompleted = dateCompleted;
        this.id  = id;
        this.driverFirstName = driverFirstName;
        this.driverLastName = driverLastName;
    }

    public String getSchedulerFirstName() {
        return schedulerFirstName;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }

    public String getSchedulerLastName() {
        return schedulerLastName;
    }
    
    public String getDriverLastName() {
        return driverLastName;
    }
    
    public String getDriverFirstName() {
        return driverFirstName;
    }
    
    public LocalDate getDateAssigned() {
    	return dateAssigned;
    }
    
    public LocalDate getDateCompleted() {
    	return dateCompleted;
    }

    public List<MissionOrder> getOrders() {
        return orders;
    }

    public double getTotalWeight() {
        return orders.stream().mapToDouble(MissionOrder::getDeliveryWeight).sum();
    }

    @Override
    public String toString() {
        // Generate the route
        String route = orders.stream()
                .sorted((o1, o2) -> Integer.compare(o1.getPriority(), o2.getPriority()))
                .map(MissionOrder::getDeliveryAddress)
                .collect(Collectors.joining(" -> ", DatabaseConstants.warehouseAddress + " -> ", " -> " + DatabaseConstants.warehouseAddress));

        // Mission Details
        StringBuilder details = new StringBuilder();
        details.append("Scheduler: ").append(schedulerFirstName).append(" ").append(schedulerLastName).append("\n");
        details.append("Route: ").append(route).append("\n");
        details.append("Orders:\n");
        for (MissionOrder order : orders) {
            details.append("  - ID: ").append(order.getId())
                    .append(", Customer: ").append(order.getCustomerName())
                    .append(", Address: ").append(order.getDeliveryAddress())
                    .append(", Weight: ").append(order.getDeliveryWeight())
                    .append(", Priority: ").append(order.getPriority()).append("\n");
        }
        details.append("Total Weight: ").append(getTotalWeight()).append(" kg\n");

        return details.toString();
    }

    public String getMissionName() {
        // Generate mission name: delivery date (dummy for now), total weight, and first two customer names
        String firstTwoCustomers = orders.stream()
                .limit(2)
                .map(MissionOrder::getCustomerName)
                .collect(Collectors.joining(", "));
        return String.format("Mission [Date: %s, Total Weight: %.2f kg, Customers: %s]", 
                             "2025-01-22", // Placeholder for delivery date
                             getTotalWeight(), 
                             firstTwoCustomers.isEmpty() ? "No Customers" : firstTwoCustomers);
    }
}

