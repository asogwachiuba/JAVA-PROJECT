package Models;

 public class ProductOrdered {
    private int id;
    private String name;
    private int quantity;
    private String measuringFormat;
    private double weightPerUnit;

    public ProductOrdered(int id, String name, int quantity, String measuringFormat, double weightPerUnit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.measuringFormat = measuringFormat;
        this.weightPerUnit = weightPerUnit;
    }

    @Override
    public String toString() {
        return name + " | Qty: " + quantity + " | " + measuringFormat + " | " + weightPerUnit + " kg/unit";
    }
}

