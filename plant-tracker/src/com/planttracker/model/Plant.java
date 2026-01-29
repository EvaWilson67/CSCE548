package com.planttracker.model;

// Plant.java

import java.time.LocalDate;

public class Plant {
    private int plantId;
    private String name;
    private String type;
    private Double height; // nullable
    private LocalDate dateAcquired;
    private String locationName;

    public Plant() {}

    public Plant(int plantId, String name, String type, Double height, LocalDate dateAcquired, String locationName) {
        this.plantId = plantId;
        this.name = name;
        this.type = type;
        this.height = height;
        this.dateAcquired = dateAcquired;
        this.locationName = locationName;
    }

    // getters & setters
    public int getPlantId() { return plantId; }
    public void setPlantId(int plantId) { this.plantId = plantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public LocalDate getDateAcquired() { return dateAcquired; }
    public void setDateAcquired(LocalDate dateAcquired) { this.dateAcquired = dateAcquired; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    @Override
    public String toString() {
        return "Plant{" +
                "plantId=" + plantId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", height=" + height +
                ", dateAcquired=" + dateAcquired +
                ", locationName='" + locationName + '\'' +
                '}';
    }
}
