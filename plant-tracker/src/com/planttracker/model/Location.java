package com.planttracker.model;

// Location.java
public class Location {
    private int plantId;
    private String locationName;
    private String lightLevel;

    public Location() {}

    public Location(int plantId, String locationName, String lightLevel) {
        this.plantId = plantId;
        this.locationName = locationName;
        this.lightLevel = lightLevel;
    }

    public int getPlantId() { return plantId; }
    public void setPlantId(int plantId) { this.plantId = plantId; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getLightLevel() { return lightLevel; }
    public void setLightLevel(String lightLevel) { this.lightLevel = lightLevel; }

    @Override
    public String toString() {
        return "Location{" +
                "plantId=" + plantId +
                ", locationName='" + locationName + '\'' +
                ", lightLevel='" + lightLevel + '\'' +
                '}';
    }
}
