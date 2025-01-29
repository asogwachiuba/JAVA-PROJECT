package Models;

public class Product {
    private String name;
    private int id;
    private String measuringFormat;
    private int quantityAvailable;
    private double weightPerUnit;

    public Product(int id, String name, String measuringFormat, int quantityAvailable, double weightPerUnit) {
        this.id = id;
        this.name = name;
        this.measuringFormat = measuringFormat;
        this.quantityAvailable = quantityAvailable;
        this.weightPerUnit = weightPerUnit;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getMeasuringFormat() {
        return measuringFormat;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public double getWeightPerUnit() {
        return weightPerUnit;
    }

    @Override
    public String toString() {
        return name; // Display only name in the list
    }
}